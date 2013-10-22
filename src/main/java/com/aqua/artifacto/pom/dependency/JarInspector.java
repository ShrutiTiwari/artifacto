package com.aqua.artifacto.pom.dependency;

import java.io.File;
import java.io.FileInputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarInspector
{
    private final JarEntriesProcessor jarEntriesProcessor;
    
    
    private JarInspector( Aggregator... aggregators ) {
        this.jarEntriesProcessor = new JarEntriesProcessor( aggregators );
    }

    public static void main( String[] args ) {
        String MAVEN_3_LIB_LOCATION = "C:\\software\\installation\\build-tools\\apache-maven-3.0.4\\lib\\";
        String artifact1 = "aether";
        String artifact2 = "junit";
        JarInspector.searchDependentArtifacts( MAVEN_3_LIB_LOCATION, artifact1, artifact2 );
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
