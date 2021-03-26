package com.systeminvecklare.smeta.lib;

/*package-private*/ class SNestedClassDeclaration extends SClassDeclaration implements ISTypeDeclaration {
	public SNestedClassDeclaration(ISType parentType, String className, boolean isStatic) {
		super(null, className, SClass.DeclaredNested(parentType, className, isStatic));
	}

	@Override
	public String getDesc() {
		return "nested class "+getSimpleTypeName();
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
