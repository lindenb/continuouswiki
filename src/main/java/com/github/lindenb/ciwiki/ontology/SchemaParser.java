package com.github.lindenb.ciwiki.ontology;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.lindenb.ciwiki.record.Record;
import com.github.lindenb.ciwiki.record.RecordIterator;
import com.github.lindenb.ciwiki.record.SourceLine;

public class SchemaParser {
	private SchemaImpl schema = null;
	private List<Map.Entry<AbstractOntPropertyImpl,SourceLine>> joinDomainProperties = new ArrayList<>();
	
	private abstract class AbstractOntType implements OntType {
		private final Class<?> clazz;
		protected AbstractOntType(final Class<?> clazz) {
			this.clazz=clazz;
			}
		@Override
		public String getName() {
			return this.clazz.getSimpleName().toLowerCase();
			}
		@Override
		public int hashCode() 
			{
			return getName().hashCode();
			}
		public String toString() 
			{
			return getName();
			}
		public Optional<Object> parse(final String s) {
			try {
				final Constructor<?> ctor = this.clazz.getConstructor(String.class);
				return Optional.of(ctor.newInstance(s));
				} catch(final Throwable err) {
				return Optional.empty();
				}
			}
		public boolean canParse(final String s) {
			return parse(s).isPresent();
			}
		}
	
	private final OntType T_STRING = new AbstractOntType(String.class) {
		};

	private final OntType T_INTEGER = new AbstractOntType(Integer.class) {
		};
	private final OntType T_LONG = new AbstractOntType(Long.class) {
		};
		
		
	private final List<OntType> all_ont_types = Arrays.asList(
			T_STRING,T_INTEGER,T_LONG);
			
		
	private abstract class AbstractOntNodeImpl implements OntObject {
		protected final SourceLine placeDefined;
		String name;
		String label;
		AbstractOntNodeImpl(SourceLine placeDefined) {
			this.placeDefined = placeDefined;
		}
		@Override
		public String getName() {
			if(this.name==null) throw new SchemaException(this.placeDefined,"no @name defined");
			return this.name;
			}
		@Override
		public String getLabel() {
			return label;
			}
		@Override
		public int hashCode() {
			return getName().hashCode();
			}
		}
	
	
	private class OntClassImpl extends AbstractOntNodeImpl implements OntClass {
		private final List<OntProperty> properties = new ArrayList<>();
		OntClassImpl(final SourceLine src) {
			super(src);
		}
		@Override
		public List<OntProperty> getProperties() {
			return this.properties;
			}
		private void addProperty(final AbstractOntPropertyImpl prop) {
			final Optional<AbstractOntPropertyImpl> preexist = this.properties.stream().
					filter(P->P.hasName(prop.getName())).
					map(P->(AbstractOntPropertyImpl)P).
					findFirst();
			if(preexist.isPresent()) {
				throw new RuntimeException("property "+prop.getName()+" defined twice for class "+toString()+" "+
							preexist.get().placeDefined +" and "+
							prop.placeDefined);
				}
			this.properties.add(prop);
			}
		}
	
	private abstract class AbstractOntPropertyImpl extends AbstractOntNodeImpl implements OntProperty {
		int minCardinality = 0;
		int maxCardinality = -1;
		
		AbstractOntPropertyImpl(final SourceLine src) {
			super(src);
		}
		
		@Override
		public int getMinCardinality() {
			return minCardinality;
			}
		@Override
		public int getMaxCardinality() {
			return this.maxCardinality;
			}
		@Override
		public OntDataProperty asDataProperty() {
			// TODO Auto-generated method stub
			return OntProperty.super.asDataProperty();
		}
		}
	
	private class OntDataPropertyImpl extends AbstractOntPropertyImpl implements OntDataProperty {
		private OntType range = T_STRING;
		OntDataPropertyImpl(final SourceLine src) {
			super(src);
		}
		
		@Override
		public OntType getRange() {
			return range;
			}
		@Override
		public boolean isObjectProperty() {
			return false;
			}
		@Override
		public boolean isDataProperty() {
			return true;
			}
		}
	
	private class OntObjectPropertyImpl extends AbstractOntPropertyImpl implements OntObjectProperty {
		private SourceLine rangeOntClassName;
		private final Schema schema;
		private OntClass range;

		OntObjectPropertyImpl(final SourceLine src,final Schema schema) {
			super(src);
			this.schema=schema;
			}
		@Override
		public OntClass getRange() {
			if(this.range!=null) return range;
			this.range = this.schema.getOntClassById(this.rangeOntClassName.getValue()).orElseThrow(()->new RuntimeException());
			return this.range;
			}
		@Override
		public boolean isDataProperty() {
			return false;
			}
		}
	
	private class SchemaImpl implements Schema {
		private List<OntClassImpl> ontClasses = new ArrayList<>();
		public SchemaImpl() {
		}
		@Override
		public List<? extends OntClass> getOntClasses() {
			return this.ontClasses;
			}
		@Override
		public Optional<? extends OntClass> getOntClassById(String id) {
			return getOntClasses().stream().filter(C->C.getName().equalsIgnoreCase(id)).findFirst();
			}
		}
	
	private OntType findOntTypeByName(final SourceLine sl) {
		final String v=sl.getValue();
		return this.all_ont_types.stream().
			filter(K->K.getName().equalsIgnoreCase(v)).findFirst().
			orElseThrow(()->new RuntimeException("unknown dataType in "+sl+" available are" + all_ont_types.stream().map(T->T.getName()).collect(Collectors.joining(" "))));
		}
	
	private void fillOntObject(final AbstractOntNodeImpl base,final Record rec) throws IOException {
		base.name = rec.getProperty("name", Record.Find.REQUIRED).get().getValue().toLowerCase();
		if(base.name.isEmpty()) throw new IllegalArgumentException("empty name  in "+rec);
		SourceLine sL = rec.getProperty("label", Record.Find.OPTIONAL).orElse(null);
		base.label=base.name;
		if(sL!=null && !sL.isBlank()) base.label= sL.getValue();
	}
	
	private void fillOntProperty(final AbstractOntPropertyImpl base,final Record rec) throws IOException {
		fillOntObject(base, rec);
		rec.getProperty("domain",Record.Find.AT_LEAST_ONE);
		}
	
	private void parseClass(final Record rec) throws IOException {
		OntClassImpl clazz = new OntClassImpl(rec.getFirst());
		fillOntObject(clazz, rec);
		
	}
	
	private OntDataPropertyImpl parseDataProperty(final Record rec) throws IOException {
		OntDataPropertyImpl prop = new OntDataPropertyImpl(rec.getFirst());
		fillOntProperty(prop, rec);
		SourceLine sL = rec.getProperty("range",Record.Find.OPTIONAL).orElse(null);
		if(sL!=null) {
			prop.range = findOntTypeByName(sL);
			}
		return prop;
		
	}
	private OntObjectPropertyImpl parseObjectProperty(final Record rec) throws IOException {
		final OntObjectPropertyImpl prop = new OntObjectPropertyImpl(rec.getFirst(),this.schema);
		fillOntProperty(prop, rec);
		prop.rangeOntClassName = rec.getProperty("range",Record.Find.OPTIONAL).orElseThrow(()->new RuntimeException("'range' missing in "+rec.getFirst()));
		return prop;
		}

	
	Schema parse(final Path paths[]) throws IOException {
			try {
			this.joinDomainProperties.clear();
			this.schema = new SchemaImpl();
			for(final Path p:paths) {
				try(RecordIterator iter = RecordIterator.open(p)) {
						while(iter.hasNext()) {
							Record rec = iter.next();
							SourceLine first = rec.getFirst();
							String type = first.getLine().toLowerCase().trim();
							if(type.equals("class")) {
								parseClass(rec);
							} else if(type.equals("dataproperty") || type.equals("data-property")) {
								parseDataProperty(rec);
							} else if(type.equals("objectproperty") || type.equals("object-property")) {
								parseObjectProperty(rec);
							}
							else {
								throw new IOException("unknown type "+type+ "in "+rec);
							}
						}
					}
				}
			while(!joinDomainProperties.isEmpty()) {
				final Map.Entry<AbstractOntPropertyImpl,SourceLine> first = this.joinDomainProperties.remove(0);
				final OntClassImpl ontClass = (OntClassImpl)this.schema.getOntClassById(first.getValue().getValue()).orElseThrow(()->new RuntimeException("Cannot find class in "+first.getValue()));
				final Optional<OntProperty> preexist =ontClass.getProperties().stream().filter(P->P.hasName(first.getValue().getValue())).findFirst();
				if(preexist.isPresent()) {
					throw new RuntimeException("property defined twice for class "+ontClass+" "+preexist.get()+" and "+first.getValue());
					}
				ontClass.properties.add(first.getKey());
				}
			//check all objectProperties have corresponding class
			this.schema.ontClasses.stream().
				flatMap(C->C.getObjectProperties()).
				forEach(P->P.getRange());
				
			final Schema sc = this.schema;
			this.schema = null;
			return sc;
			}
		catch(final SchemaException err ) {
			throw new IOException(err);
			}
		finally {
			this.schema = null;
			}
		}
	}

