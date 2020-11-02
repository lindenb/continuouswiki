package com.github.lindenb.ciwiki.ontology;

import com.github.lindenb.ciwiki.record.SourceLine;

public class SchemaException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SchemaException() {
		// TODO Auto-generated constructor stub
	}

	
	public SchemaException(final SourceLine src) {
		this(src.toString());
	}
	
	public SchemaException(final SourceLine src,final String msg) {
		this(src.toString()+":"+msg);
	}

	
	public SchemaException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public SchemaException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public SchemaException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public SchemaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
