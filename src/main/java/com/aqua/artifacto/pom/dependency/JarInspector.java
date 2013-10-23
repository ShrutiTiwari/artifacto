package com.aqua.artifacto.pom.dependency;

import java.io.File;
import java.io.FileInputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarInspector
{
    private final JarEntriesProcessor jarEntriesProcessor;
    private final static String  sep= File.separator ;
    private final static String DEFAULT_DISTRIBUTION = System.getenv( "M2_HOME" )+ sep + "lib" + sep;
    private final static String[] DEFAULT_ARTIFACTS = new String[] { "aether", "junit" };

    private JarInspector( Aggregator... aggregators ) {
        this.jarEntriesProcessor = new JarEntriesProcessor( aggregators );
    }

    public static void main( String[] args ) {
        if( args == null || args.length < 2 ) {
            System.out.println("executing for M2_HOME distribution");
            JarInspector.searchDependentArtifacts( DEFAULT_DISTRIBUTION, DEFAULT_ARTIFACTS );
        }else{
            System.out.println("executing for distribution at "+ args[0] );
            String[] searchList= new String[args.length-1];
            for(int i=1; i<args.length; i++){
                searchList[i-1]=args[i];
            }
            JarInspector.searchDependentArtifacts( args[0], searchList );
        }
    }

    private static void searchDependentArtifacts( String path, String... artifactNames ) {
        Aggregator dependentJarsAggregator = Aggregator.DependentJarsAggregator.forKeywords( artifactNames );
        new JarInspector( dependentJarsAggregator ).proessJarsUnder( new File( path ) );
        dependentJarsAggregator.print();
    }

    private boolean isArtifactJar( String name ) {
        return name.endsWith( ".jar" ) || name.endsWith( ".war" );
    }

    private void proessJarsUnder( File candiateRootFile ) {
        if( !candiateRootFile.isDirectory() ) {
            return;
        }
        for( File candiateFile : candiateRootFile.listFiles() ) {
            if( isArtifactJar( candiateFile.getName() ) ) {
                jarEntriesProcessor.analyseJarEntriesOf( candiateFile );
            } else {
                proessJarsUnder( candiateFile );
            }
        }
    }

    private static class JarEntriesProcessor
    {
        private Aggregator[] aggregators;

        public JarEntriesProcessor( Aggregator[] aggregators ) {
            this.aggregators = aggregators;
        }

        static JarEntriesProcessor withAggregator( Aggregator... aggregators ) {
            return new JarEntriesProcessor( aggregators );
        }

        public void analyseJarEntriesOf( File jarIOFile ) {
            JarInputStream jarFile = null;
            try {
                jarFile = new JarInputStream( new FileInputStream( jarIOFile ) );
                while( true ) {
                    JarEntry jarEntry = jarFile.getNextJarEntry();
                    if( jarEntry == null ) {
                        break;
                    }
                    for( Aggregator each : aggregators ) {
                        each.storeInformation( jarEntry, jarIOFile );
                    }
                }
            } catch( Exception e ) {
                e.printStackTrace();
            } finally {
                try {
                    jarFile.close();
                } catch( Exception e ) {

                }
            }
        }
    }
}
