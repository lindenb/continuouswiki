package com.github.lindenb.ciwiki.record;

import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.Map;

public class SourceLine {
	private final String path;
	private final int index;
	private final String line;
	
	public SourceLine(final Path path,int index,final String line) {
		this(path.toString(),index,line);
	}
	
	public SourceLine(final String path,int index,final String line) {
		this.path = path;
		this.index = index;
		this.line = line;
	}
	public String getPath() {
		return path;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public String getLine() {
		return line;
	}
	
	public Map.Entry<String, String> getEntry() {
		final String L = getLine();
		int i= L.indexOf(':');
		if(i==-1) i= L.indexOf('=');
		if(i==-1) throw new IllegalArgumentException("no delimiter ':' or '=' in " + this);
		final String key = L.substring(0,i).trim().toLowerCase();
		if(key.isEmpty())  throw new IllegalArgumentException("Empty key in " + this);
		final String value = L.substring(i+1).trim();
		if( value.isEmpty()) throw new IllegalArgumentException("Empty value in " + this);
		return new AbstractMap.SimpleEntry<String, String>(key,value);
		}
	
	public String getKey() {
		return getEntry().getKey();
		}
	
	public String getValue() {
		return getEntry().getValue();
		}
	
	public boolean isBlank() {
		return getValue().trim().isEmpty();
		}
	
	@Override
	public String toString() {
		return path+"["+index+"]"+line;
		}
}
