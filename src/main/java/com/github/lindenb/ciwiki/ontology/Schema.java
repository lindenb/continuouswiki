package com.github.lindenb.ciwiki.ontology;

import java.util.List;
import java.util.Optional;

public interface Schema {
	public List<? extends OntClass> getOntClasses();
	public Optional<? extends OntClass> getOntClassById(final String id);
}
