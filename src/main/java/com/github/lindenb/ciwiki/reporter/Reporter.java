package com.github.lindenb.ciwiki.reporter;

import com.github.lindenb.ciwiki.record.SourceLine;

public interface Reporter {
	public boolean isDone();
	public void warning(SourceLine src,String msg);
	public void error(SourceLine src,String msg);
}
