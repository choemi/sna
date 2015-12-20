package ch.fhnw.sna.examples.dbpedia;

import java.io.Console;
import java.time.format.DateTimeFormatter;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.fhnw.sna.examples.dbpedia.model.Airline;
import ch.fhnw.sna.examples.dbpedia.model.AirlineAirportGraph;

public class ANAFetcher {
	private static final String DBPEDIA_SPARQL_ENDPOINT = "http://dbpedia.org/sparql";
	private static final DateTimeFormatter ACTIVE_YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	private static final Logger LOG = LoggerFactory.getLogger(ANAFetcher.class);
	
	public AirlineAirportGraph fetch() {
		AirlineAirportGraph graph = new AirlineAirportGraph();
		LOG.info("Start fetching Airline and Airport (AnA) Network");
		fetchHubs(graph);
		LOG.info("Fiinished fetching  Airline and Airport (AnA) Network");
		LOG.info("Start fetching node attributs");
		//enrichNodeInformation(graph);
		LOG.info("Finished fetching node attributes");
		return graph;
	}
	
	private void fetchHubs(AirlineAirportGraph graph) {
		final int LIMIT = Integer.MAX_VALUE; // Means no limit
		boolean hasMoreResults = true;
		int currentOffset = 0;
		int fetchedTotal = 0;
		while (hasMoreResults && fetchedTotal < LIMIT) {
			String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n "
					+ "PREFIX dbo: <http://dbpedia.org/ontology/subsidiary> \n"
					+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
					+ "SELECT ?AirlineUri ?HubAirportUri ?AirlineName ?HubAirportName "
					+ "WHERE { ?AirlineUri <http://dbpedia.org/ontology/hubAirport> ?HubAirportUri. "
					+ "?AirlineUri rdfs:label ?AirlineName . ?HubAirportUri rdfs:label ?HubAirportName. "
					+ "FILTER langMatches( lang(?AirlineName ), \"en\" ) . "
					+ "FILTER langMatches( lang(?HubAirportName ), \"en\" ) .}  "
					+ "LIMIT 5000 OFFSET " + currentOffset;
						      
			
			LOG.debug("Querying: {}", queryString);

			Query query = QueryFactory.create(queryString);
			int resultCounter = 0;
			try (QueryExecution qexec = QueryExecutionFactory.sparqlService(DBPEDIA_SPARQL_ENDPOINT, query)) {
				ResultSet results = qexec.execSelect();

				while (results.hasNext()) {
					++resultCounter;
					QuerySolution sol = results.next();
					String airlineUri = sol.getResource("AirlineUri").getURI();
					String airportUri = sol.getResource("HubAirportUri").getURI();
					String airline = sol.getLiteral("AirlineName").getLexicalForm();
					String airport = sol.getLiteral("HubAirportName").getLexicalForm();
					graph.addAirlineIfNotExists(airlineUri, airline);
					graph.addAirportIfNotExists(airportUri, airport);
					graph.addHub(airlineUri, airportUri);
					LOG.info("Enrich Hub {}", airport);
				}
			}
			LOG.debug("Fetches {} new results.", resultCounter);
			fetchedTotal += resultCounter;
			currentOffset += 1000;
			if (resultCounter < 1000) {
				hasMoreResults = false;
			}
		}
	}
}
