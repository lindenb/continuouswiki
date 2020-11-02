package com.github.lindenb.ciwiki.ontology;

public interface OntProperty  extends OntObject  {
	boolean isDataProperty();
	/** 0 for optional */
	int getMinCardinality();
	/** -1 for infinite */
	int getMaxCardinality();
	public default boolean isObjectProperty() {
		return !isDataProperty();
	}
	
	public default OntDataProperty asDataProperty() {
		if(!isDataProperty()) throw new IllegalStateException("Not a data property");
		return OntDataProperty.class.cast(this);
	}
	
	public default OntObjectProperty asObjectProperty() {
		if(!isObjectProperty()) throw new IllegalStateException("Not a data property");
		return OntObjectProperty.class.cast(this);
	}
}
