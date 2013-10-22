package com.aqua.artifacto.pom.dependency;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public interface Aggregator
{
    public final static String CLASS_ENTRY = ".class";
    public final static String POM_ENTRY = "pom.xml";

    void storeInformation( JarEntry jarEntry, File jarIOFile );

    void print();

    class DependentJarsAggregator implements Aggregator
    {
        private Map<String, Collection<String>> keywordSet = new LinkedHashMap<String, Collection<String>>();

        private DependentJarsAggregator( String... keywords ) {
            for( String each : keywords ) {
                String key = "<artifactId>" + each + "</artifactId>";
                keywordSet.put( key, new ArrayList<String>() );
            }
        }

        public static Aggregator forKeywords( String... keywords ) {
            return new DependentJarsAggregator( keywords );
        }

        @Override
        public void storeInformation( JarEntry jarEntry, File jarIOFile ) {
            String jarEntryName = jarEntry.getName();
            if( !isValidEntry( jarEntryName ) ) {
                return;
            }

            try {
                JarFile jarFile = new JarFile( jarIOFile );
                InputStream pomAsStream = jarFile.getInputStream( jarEntry );
                // Element xmlDocument = translateToXMLDocument( pomAsStream );

                if( pomAsStream != null ) {
                    String pomString = collateInputStreamAsSting( pomAsStream );
                    Set<String> keySet = keywordSet.keySet();
                    for( String keyword : keySet ) {
                        if( pomString.contains( keyword ) ) {
                            Collection<String> collection = keywordSet.get( keyword );
                            int index = collection.size() + 1;
                            collection.add( printFormat( index + ".", jarIOFile.getName(), jarIOFile.getParent() ) );
                        }
                    }
                }

                try {
                    pomAsStream.close();
                } catch( Exception e ) {}
            } catch( Exception e ) {
                e.printStackTrace();
            }

        }

        private boolean isValidEntry( String jarEntryName ) {
            return jarEntryName.endsWith( POM_ENTRY );
        }

        @Override
        public void print() {
            StringBuffer buf = new StringBuffer();
            for( Map.Entry<String, Collection<String>> each : keywordSet.entrySet() ) {
                Collection<String> value = each.getValue();
                if( !value.isEmpty() ) {
                    buf.append( "\n\n Dependent artifacts of [" + each.getKey() + "]==>" );
                    buf.append( "\n\t" + printFormat( "S.No.", "Artifact-name", "Location" ) );
                    for( String eachV : value ) {
                        buf.append( "\n\t" + eachV );
                    }
                }
            }
            System.out.println( buf );
        }

        String printFormat( Object index, String name, String path ) {
            return String.format( "%-7s", index ) + String.format( "%-60s", name ) + "\t" + path;
        }

        public static String collateInputStreamAsSting( InputStream sourceInputStream ) throws IOException {
            DataCollater stringDataCollater = new DataCollater.StringDataCollater();
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( sourceInputStream ) );
            String s = null;
            while( (s = bufferedReader.readLine()) != null ) {
                stringDataCollater.collectData( s );
            }
            
            String fileData = stringDataCollater.data();
            return fileData;
        }
        
    }
    
    public interface DataCollater
    {
        void collectData( String s );
        String data();
        
        public static class StringDataCollater implements DataCollater
        {
            private final StringBuilder data = new StringBuilder();

            public void collectData( String s ) {
                if( s != null ) {
                    data.append( "\n" + s );
                }
            }

            public String data() {
                return data.toString();
            }
        }

    }
}
