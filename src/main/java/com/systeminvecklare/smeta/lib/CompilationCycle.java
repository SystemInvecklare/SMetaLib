package com.systeminvecklare.smeta.lib;

import java.lang.RuntimeException;
import java.lang.String;
import java.lang.Exception;

/*package-private*/ class CompilationCycle implements ICompilationCycle {
	private RuntimeException fatal = null;


	@Override
	public void reportFatal(String message, Exception e) {
		RuntimeException exception = new RuntimeException(message, e);
		if(fatal == null) {
			fatal = exception;
		} else {
			fatal.addSuppressed(exception);
		}
	}

	public RuntimeException getFatalIfAny() {
		return fatal;
	}

	public void throwFatalIfAny() {
		RuntimeException fatalOrNull = getFatalIfAny();
		if(fatalOrNull != null) {
			throw fatalOrNull;
		}
	}

}
