package com.systeminvecklare.smeta.lib;

public class SClassDeclaration implements ISTypeDeclaration {
	private final SClass sClass;
	private final SPackage sPackage;
	private final String className;
	private boolean compiled = false;
	private ISClassDefinition definition = null;

	public SClassDeclaration(SPackage sPackage, String className, SClass sClass) {
		this.sPackage = sPackage;
		this.className = className;
		this.sClass = sClass;
	}

	@Override
	public SClass getDeclared() {
		return sClass;
	}
	
	@Override
	public String getSimpleTypeName() {
		return className;
	}
	
	@Override
	public String getDesc() {
		return "class "+sPackage.getFullName()+"."+className;
	}
	
	//TODO what we should really do:
	//     collectImports on a type (class or interface) should just collect what is needed to print that type. For generics this might be multiple.
	//     collectImports on the declaration on the other hand should loop through all the fields and everything, because they
	//     are needed for rendering the declaration/definition.
	//     Actually, all the logic of building the class from the definition, as well as fields and methods,
	//     should probably belong to the sclassdeclaraion instead of the sclass. The declared sclass should instead
	//     have a way to get methods FROM the declaration when asked later. Makes so much more sense!
	
	@Override
	public void visitImportContributors(ISTypeImportCollector visitor) {
		sClass.TEMP_collectDeclarationImports(visitor);
	}
	
	public void define(ISClassDefinition definition) {
		if(this.definition != null) {
			throw new IllegalStateException(className+" already defined!");
		}
		
		this.definition = definition;
	}
	
	public boolean isDefined() {
		return definition != null;
	}
	
	@Override
	public void compile(ICompilationCycle compilationCycle) {
		try {
			if(isCompiled()) {
				throw new IllegalStateException(className+" already compiled!");
			}
			if(!isDefined()) {
				throw new IllegalStateException(className+" was not defined!");
			}
			sClass.compile(definition);
			compiled = true;
		} catch (Exception e) {
			compilationCycle.reportFatal("Failed to compile "+className, e);
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
