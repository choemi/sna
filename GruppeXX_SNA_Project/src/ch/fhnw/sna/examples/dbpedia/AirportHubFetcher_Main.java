package ch.fhnw.sna.examples.dbpedia;

import ch.fhnw.sna.examples.dbpedia.model.AirlineAirportGraph;
// TEST
/**
 * 
 * Main class for Music artist fetcher
 *
 */
public class AirportHubFetcher_Main {

	public static void main(String[] args) {
		String FILE = "AirportHub-associations.gexf";
		
		AirlineAirportGraph graph = new AirportHubFetcher().fetch();
		//new AirportHubGephiExport(FILE).export(graph);
	}
}
