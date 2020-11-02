package com.github.lindenb.ciwiki.ontology;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.lindenb.ciwiki.record.Record;
import com.github.lindenb.ciwiki.reporter.Reporter;

public interface OntClass extends OntObject {
	public List<OntProperty> getProperties();
	
	public default Optional<? extends OntProperty> getPropertyByName(final String key) {
		return getProperties().stream().filter(P->P.hasName(key)).findFirst();
	}
	
	public default Stream<? extends OntDataProperty> getDataProperties() {
		return getProperties().
				stream().
				filter(P->P.isDataProperty()).
				map(P->P.asDataProperty());
	}
	
	public default Stream<? extends OntObjectProperty> getObjectProperties() {
		return getProperties().
				stream().
				filter(P->P.isObjectProperty()).
				map(P->P.asObjectProperty());
	}
	
	public default void validate(Reporter reporter,Record rec) {
		if(reporter.isDone()) return;
		rec.getButFirst().forEach(L->{
			if(!this.getPropertyByName(L.getKey()).isPresent()) {
				reporter.warning(L, "undefined property");
				}
			});
		
		}
}
