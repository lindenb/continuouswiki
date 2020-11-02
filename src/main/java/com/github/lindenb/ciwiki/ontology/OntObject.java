package com.github.lindenb.ciwiki.ontology;

public interface OntObject {
	public String getName();
	public String getLabel();
	public default boolean hasName(final String s) {
		return getName().equalsIgnoreCase(s);
	}
}
