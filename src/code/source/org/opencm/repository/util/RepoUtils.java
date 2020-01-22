package org.opencm.repository.util;

import java.io.File;

import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.GraphDatabaseService;

public class RepoUtils {
	
	
	public static void start() {
		GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( new File("var/graphDb") );
		registerShutdownHook( graphDb );
	}
	
	private static void registerShutdownHook( final GraphDatabaseService graphDb ) {
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//			​@Override
//			public void run() ​{ graphDb.shutdown(); }
//		});
	}
}