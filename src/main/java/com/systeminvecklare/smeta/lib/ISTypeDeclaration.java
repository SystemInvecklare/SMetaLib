package com.systeminvecklare.smeta.lib;

import java.lang.String;

public interface ISTypeDeclaration {
	ISType getDeclared();
	boolean isCompiled();
	void compile(ICompilationCycle compilationCycle);
	String getDesc();
	SPackage getPackage();
	String getSimpleTypeName();
	void visitImportContributors(ISTypeImportCollector visitor);
}
