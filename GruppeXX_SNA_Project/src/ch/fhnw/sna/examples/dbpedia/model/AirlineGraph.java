package ch.fhnw.sna.examples.dbpedia.model;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

public class AirlineGraph {
	private Map<String, MusicArtist> IdToAirline = Maps.newHashMap();
	private Map<String, Set<String>> Hubs = Maps.newHashMap();
}
