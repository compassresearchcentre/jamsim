package org.jamsim.ascape.stats;

import org.apache.commons.lang.mutable.MutableDouble;

public class NamedMutableDouble extends MutableDouble {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8244010035448235738L;
	private final String name;
	
	public NamedMutableDouble() {
		super();
		this.name = "Unnamed";
	}

	public NamedMutableDouble(double value) {
		super(value);
		this.name = "Unnamed";
	}

	public NamedMutableDouble(Number value) {
		super(value);
		this.name = "Unnamed";
	}

	public NamedMutableDouble(String name) {
		super();
		this.name = name;
	}

	public NamedMutableDouble(double value, String name) {
		super(value);
		this.name = name;
	}

	public NamedMutableDouble(Number value, String name) {
		super(value);
		this.name = name;
	}

	
}
