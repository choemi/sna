package ch.fhnw.sna.examples.dbpedia.model;

public class Airline {
	private final String id;
	private String label;
	
	public Airline(String id) {
		this.id = id;
	}

	public Airline(String id, String label) {
		this(id);
		this.label = label;
	}
}
