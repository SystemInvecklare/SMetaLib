package com.systeminvecklare.smeta.lib;

/*package-private*/ class SLocalClassDeclaration extends SClassDeclaration implements ISTypeDeclaration {
	public SLocalClassDeclaration(String className) {
		super(null, className, SClass.DeclaredLocal(className));
	}

	@Override
	public String getDesc() {
		return "local class "+getSimpleTypeName();
	}
	
	@Override
	public void compile(ICompilationCycle compilationCycle) {
		if(!isCompiled()) {
			super.compile(compilationCycle);
		}
	}

	@Override
	public SPackage getPackage() {
		throw new UnsupportedOperationException();
	}
}
