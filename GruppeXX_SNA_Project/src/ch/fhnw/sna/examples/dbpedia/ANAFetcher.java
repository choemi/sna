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
	
	private static final Logger LOG = LoggerFactory.getLogger(AirportHubFetcher.class);
	
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
			String queryString = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" 
							 + "PREFIX dbo: <http://dbpedia.org/ontology/subsidiary> \n"
							 + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
						+ "SELECT ?Company ?HubAirport ?Alliance ?ParentCompany \n"
						     + " WHERE { { { ?Company <http://dbpedia.org/ontology/hubAirport> ?HubAirport } "
						     + " UNION  { ?Company <http://dbpedia.org/property/hubs> ?HubAirport } "
						     + "}.FILTER regex(?HubAirport, \"http\") "
						     + ". OPTIONAL { ?Company  <http://dbpedia.org/ontology/alliance> ?Alliance}"
						     + ". OPTIONAL { ?Company  <http://dbpedia.org/ontology/parentCompany> ?ParentCompany} }  LIMIT 5000 OFFSET 1"; 
			
			LOG.debug("Querying: {}", queryString);

			Query query = QueryFactory.create(queryString);
			int resultCounter = 0;
			try (QueryExecution qexec = QueryExecutionFactory.sparqlService(DBPEDIA_SPARQL_ENDPOINT, query)) {
				ResultSet results = qexec.execSelect();

				while (results.hasNext()) {
					++resultCounter;
					QuerySolution sol = results.next();
					String airlineUri = sol.getResource("sourceuri").getURI();
					String airportUri = sol.getResource("targeturi").getURI();
					String airline = sol.getLiteral("sourcename").getLexicalForm();
					String airport = sol.getLiteral("targetname").getLexicalForm();
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
