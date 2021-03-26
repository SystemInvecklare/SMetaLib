package com.systeminvecklare.smeta.lib;

import java.lang.String;
import java.lang.Exception;

/*package-private*/ interface ICompilationCycle {
	void reportFatal(String message, Exception e);
}
