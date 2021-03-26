package com.systeminvecklare.smeta.lib;

import com.mattiasselin.linewriter.ILineWriter;

public interface ISourceFile {
	ILineWriter getSourceWriter();
	void complete();
}
