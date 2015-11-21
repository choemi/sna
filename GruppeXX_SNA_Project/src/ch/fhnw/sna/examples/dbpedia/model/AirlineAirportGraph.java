package ch.fhnw.sna.examples.dbpedia.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class AirlineAirportGraph {
	private Map<String, Airline> URIToAirline = Maps.newHashMap();
	private Map<String, Airport> URIToAirport = Maps.newHashMap();
	private Map<String, Set<String>> Hubs = Maps.newHashMap();
	
	public void addAirline(Airline airline) {
		URIToAirline.put(airline.getId(), airline);
	}
	
	public boolean containsAirline(String uri) {
		return URIToAirline.containsKey(uri);
	}
	
	public Collection<Airline> getAirlines() {
		return URIToAirline.values();
	}
	
	public void addAirlineIfNotExists(String uri, String name) {
		if (!URIToAirline.containsKey(uri)){
			URIToAirline.put(uri, new Airline(uri, name));
		}
	}
	
	public void addAirport(Airport airport) {
		URIToAirport.put(airport.getId(), airport);
	}
	
	public boolean containsAirport(String uri) {
		return URIToAirport.containsKey(uri);
	}
	
	public Collection<Airport> getAirports() {
		return URIToAirport.values();
	}
	
	public void addAirportIfNotExists(String uri, String name) {
		if (!URIToAirport.containsKey(uri)){
			URIToAirport.put(uri, new Airport(uri, name));
		}
	}
	
	public void addHub(String airlineURI, String airportURI) {
		Set<String> to = Hubs.get(airlineURI);
		if (to == null) {
			to = Sets.newHashSet();
			Hubs.put(airlineURI, to);
		}
		to.add(airportURI);
	}

	public Map<String, Set<String>> getHubs() {
		return Hubs;
	}
}
