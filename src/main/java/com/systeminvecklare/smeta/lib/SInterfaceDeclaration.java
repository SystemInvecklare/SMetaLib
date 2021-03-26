package com.systeminvecklare.smeta.lib;

public class SInterfaceDeclaration implements ISTypeDeclaration {
	private final SInterface sInterface;
	private final SPackage sPackage;
	private final String interfaceName;
	private boolean compiled = false;
	private ISInterfaceDefinition definition = null;

	public SInterfaceDeclaration(SPackage sPackage, String interfaceName, SInterface sInterface) {
		this.sPackage = sPackage;
		this.interfaceName = interfaceName;
		this.sInterface = sInterface;
	}

	@Override
	public SInterface getDeclared() {
		return sInterface;
	}
	
	@Override
	public String getSimpleTypeName() {
		return interfaceName;
	}
	
	@Override
	public String getDesc() {
		return "interface "+sPackage.getFullName()+"."+interfaceName;
	}
	
	@Override
	public void visitImportContributors(ISTypeImportCollector visitor) {
		sInterface.collectImports(visitor);
	}
	
	public void define(ISInterfaceDefinition definition) {
		this.definition = definition;
	}
	
	public boolean isDefined() {
		return definition != null;
	}
	
	@Override
	public void compile(ICompilationCycle compilationCycle) {
		try {
			if(isCompiled()) {
				throw new IllegalStateException(interfaceName+" already compiled!");
			}
			if(!isDefined()) {
				throw new IllegalStateException(interfaceName+" was not defined!");
			}
			sInterface.compile(definition);
			compiled = true;
		} catch (Exception e) {
			compilationCycle.reportFatal("Failed to compile "+interfaceName, e);
		}
	}
	
	@Override
	public boolean isCompiled() {
		return compiled;
	}

	@Override
	public SPackage getPackage() {
		return sPackage;
	}
}
