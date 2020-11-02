package com.github.lindenb.ciwiki.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Record implements Iterable<SourceLine> {
	public enum Find {
		REQUIRED,
		OPTIONAL,
		FIRST,
		AT_LEAST_ONE
		};
	private final List<SourceLine> lines ;
	public Record() {
		this(Collections.emptyList());
	}
	public Record(final List<SourceLine> lines) {
		this.lines = new ArrayList<>(lines);
	}
	public SourceLine getFirst() {
		return this.lines.get(0);
	}
	
	public List<SourceLine> getButFirst() {
		if(this.lines.size()<2) return Collections.emptyList();
		return this.lines.subList(1, this.lines.size());
		}
	
	public boolean hasProperty(final String key) {
		return this.getButFirst().stream().
				anyMatch(L->L.getKey().equalsIgnoreCase(key));
		}
	
	public List<SourceLine> getProperties(final String key) {
		return this.getButFirst().stream().
				filter(L->L.getKey().equalsIgnoreCase(key)).
				collect(Collectors.toList());
		}

	
	public Optional<SourceLine> getProperty(final String key,Find find) {
		switch(find) {
			case AT_LEAST_ONE: {
				Optional<SourceLine> opt= this.lines.stream().
						skip(1L).
						filter(L->L.getKey().equalsIgnoreCase(key)).
						findFirst();
				if(opt.isPresent()) return opt;
				throw new IllegalStateException("Expected at least one "+key+" but got  none in "+this);
				}
			case FIRST:
				return this.lines.stream().
						skip(1L).
						filter(L->L.getKey().equalsIgnoreCase(key)).
						findFirst();
			case OPTIONAL: {
				final List<SourceLine> L = getProperties(key);
				if(L.isEmpty()) return Optional.empty();
				if(L.size()==1) return Optional.of(L.get(0));
				throw new IllegalStateException("Expected one or zero "+key+" but got  "+L.size()+" in "+this);
				}
			case REQUIRED: {
				final List<SourceLine> L = getProperties(key);
				if(L.size()==1) return Optional.of(L.get(0));
				throw new IllegalStateException("Expected one and only one "+key+" but got  "+L.size()+" in "+this);
				}
			default: throw new IllegalStateException();
			}
		}
	
	
	@Override
	public Iterator<SourceLine> iterator() {
		return this.lines.iterator();
		}
	boolean isEmpty() {
		return this.lines.isEmpty();
	}
	
	@Override
	public String toString() {
		if(isEmpty()) return "(empty)";
		return getFirst().getPath()+"#"+getFirst().getIndex()+":\n"+
				this.lines.stream()
				.map(L->"\t"+L.getLine())
				.collect(Collectors.joining("\n"));
		}
}
