package com.github.lindenb.ciwiki.record;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecordIterator implements Iterator<Record>,AutoCloseable {
	private BufferedReader br;
	private int index = 0;
	private String path;
	private List<SourceLine> lines = new ArrayList<>();
	private Record curr=null;
	
	private RecordIterator() {		
	}
	
	private String readLine() throws IOException {
		String line = null;
		for(;;) {
			String s = this.br.readLine();
			if(s==null) break;
			index++;
			boolean slash = s.endsWith("\\");
			if(slash) {
				s=s.substring(0,s.length()-1);
			}
			if(line==null) {
				line=s;
			} else {
				line += s;
			}
			if(!slash) break;
		}
		return line;
	}
	
	private Record advance() {
		lines.clear();
		try {
			for(;;) {
				final String line = this.readLine();
				if(line==null || line.trim().isEmpty()) {
					if(!lines.isEmpty()) {
						return new Record(lines);
						}
					if(line==null) break;
					}
				lines.add(new SourceLine(path, index, line));
				}
			return null;
		} catch(IOException err) {
			throw new RuntimeException(err);
			}
		}
	
	@Override
	public boolean hasNext() {
		if(curr!=null) return true;
		curr=advance();
		return curr!=null;
		}
	@Override
	public Record next() {
		if(!hasNext()) throw new IllegalStateException();
		Record r = this.curr;
		this.curr = null;
		return r;
		}
	@Override
	public void close()  {
		try {
			this.br.close();
			}
		catch(IOException err) {
			
			}
		}
	
	public static RecordIterator open(final Path path) throws IOException {
		RecordIterator rc=new RecordIterator();
		rc.br = Files.newBufferedReader(path);
		rc.path = path.toString();
		return rc;
		}
	}
