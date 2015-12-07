package ch.fhnw.sna.examples.dbpedia;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.fhnw.sna.examples.dbpedia.model.Airline;
import ch.fhnw.sna.examples.dbpedia.model.AirlineAirportGraph;
import ch.fhnw.sna.examples.dbpedia.model.Airport;
import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.Attribute;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;

public class AirportHubGephiExport {
	private static final Logger LOG = LoggerFactory.getLogger(AirportHubGephiExport.class);
	private static final String OUTPUT_FOLDER = "output/AirportHub/";
	
	private final String OUTPUT_FILE;
	
	// Node attributes
	AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
	Attribute attType = attrList.createAttribute(AttributeType.STRING,"airlineOrHub");
	
	// constructor
	public AirportHubGephiExport(String outputFile) {
		this.OUTPUT_FILE = outputFile;
	}
	
	public void export(AirlineAirportGraph airlineGraph) {
		Gexf gexf = initializeGexf();
		Graph graph = gexf.getGraph();
		LOG.info("Creating gephi Graph");
		createGraph(graph, airlineGraph);
		writeGraphToFile(gexf);
		LOG.info("Finished Gephi export");
	}
	
	private void writeGraphToFile(Gexf gexf) {
		StaxGraphWriter graphWriter = new StaxGraphWriter();
		File f = new File(OUTPUT_FOLDER + OUTPUT_FILE);
		Writer out;
		try {
			out = new FileWriter(f, false);
			graphWriter.writeToStream(gexf, out, "UTF-8");
			LOG.info("Stored graph in file: " +f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void createGraph(Graph graph, AirlineAirportGraph aNaGraph) {
		LOG.info("Creating nodes");
		Map<String, Node> nodeMap = createNodes(aNaGraph, graph);
		LOG.info("Creating edges");
		createEdges(aNaGraph, graph, nodeMap);
	}
	
	private Map<String, Node> createNodes(AirlineAirportGraph aNa, Graph graph) {
		Map<String, Node> nodeMap = new HashMap<>((aNa.getAirlines().size() + aNa.getAirports().size()) * 2);

		for (Airline airline : aNa.getAirlines()) {
			if (!nodeMap.containsKey(airline.getId())) {
				nodeMap.put(airline.getId(), createAirlineNode(graph, airline));
			}
		}
		for (Airport airport : aNa.getAirports()) {
			if (!nodeMap.containsKey(airport.getId())) {
				nodeMap.put(airport.getId(), createAirportNode(graph, airport));
			}
		}
		return nodeMap;
	}
	
	private Node createAirlineNode(Graph graph, Airline airline) {
		Node node = graph.createNode(airline.getId()).setLabel(airline.getName());
		node.getAttributeValues().addValue(attType, "Airline");
		return node;
	}
	
	private Node createAirportNode(Graph graph, Airport airport) {
		Node node = graph.createNode(airport.getId()).setLabel(airport.getName());
		node.getAttributeValues().addValue(attType, "Airport");
		return node;
	}
	
	private void createEdges(AirlineAirportGraph aNaGraph, Graph graph, Map<String, Node> nodeMap) {
		for (Map.Entry<String, Set<String>> airlines : aNaGraph.getHubs().entrySet()) {
			for (String airports : airlines.getValue()) {
				createOrUpdateEdge(graph, nodeMap, airlines.getKey(), airports);
			}
		}
	}
	
	private void createOrUpdateEdge(Graph graph, Map<String, Node> nodeMap, String airline, String airport) {
		Node source = nodeMap.get(airport);
		Node target = nodeMap.get(airline);
		if (source == null || target == null)
			throw new IllegalStateException(
					"Source and Target must not be null. Every node must be added to network as a node before using it as a edge.");
		if (source.hasEdgeTo(airline)) {
			Edge edge = getEdgeBetween(source, target);
			edge.setWeight(edge.getWeight() + 1f);

		} else if (target.hasEdgeTo(airport)) {
			Edge edge = getEdgeBetween(target, source);
			edge.setWeight(edge.getWeight() + 1f);

		} else {
			source.connectTo(target).setWeight(1f);
		}
	}
	
	private Edge getEdgeBetween(Node source, Node target) {
		for (Edge edge : source.getEdges()) {
			if (edge.getTarget().equals(target)) {
				return edge;
			}
		}
		return null;
	}
	
	private Gexf initializeGexf() {
		Gexf gexf = new GexfImpl();
		gexf.getMetadata().setLastModified(Calendar.getInstance().getTime())
				.setCreator("SNA Dbpedia Graph");
		gexf.setVisualization(true);

		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.DIRECTED);
		graph.setMode(Mode.STATIC);
		graph.getAttributeLists().add(attrList);
		return gexf;
	}
}
