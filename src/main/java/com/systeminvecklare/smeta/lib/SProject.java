package com.systeminvecklare.smeta.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mattiasselin.linewriter.ILineWriter;
import com.systeminvecklare.smeta.lib.SClass.JavaPrimitive;

public class SProject {
	private final SPackage src = new SPackage();
	private final SPackage rootPackage;
	private final Map<ISType,DefinitionDependency> definitionDependencies = new HashMap<>();
	
	public SProject(String rootPackage) {
		this.rootPackage = src.in(rootPackage);
		
	}
	
	public SPackage getRootPackage() {
		return rootPackage;
	}
	
	private <T extends Collection<ISTypeDeclaration>> T collectDeclarations(final T collector) {
		src.visitTypeDeclarations(new ISTypeDeclarationVisitor() {
			@Override
			public void visit(ISTypeDeclaration typeDeclaration) {
				collector.add(typeDeclaration);
			}
		});
		return collector;
	}
	
	public void compile() {
		//Step 1. Visit all packages and collect all STypes and their dependencies
		final List<ISTypeDeclaration> declarations = collectDeclarations(new ArrayList<ISTypeDeclaration>());
		
		class DependencyOrder {
			private final ISTypeDeclaration declaration;
			private final List<ISType> dependenciesLeft;
			
			public DependencyOrder(ISTypeDeclaration declaration) {
				this.declaration = declaration;
				this.dependenciesLeft = new ArrayList<>(getDefinitionDependency(declaration.getDeclared()).dependencies);
			}
		}
		
		//Step 2. Order by dependency
		//TODO Need to make library for this dependency ordering shit!
		List<DependencyOrder> dependencyOrders = new ArrayList<>();
		boolean needsReordering = false;
		for(ISTypeDeclaration declaration : declarations) {
			DependencyOrder dependencyOrder = new DependencyOrder(declaration);
			dependencyOrders.add(dependencyOrder);
			if(!dependencyOrder.dependenciesLeft.isEmpty()) {
				needsReordering = true;
			}
		}
		Comparator<DependencyOrder> leastLeft = new Comparator<DependencyOrder>() {
			@Override
			public int compare(DependencyOrder o1, DependencyOrder o2) {
				return Integer.compare(o1.dependenciesLeft.size(), o2.dependenciesLeft.size());
			}
		};
		
		if(needsReordering) {
			while(needsReordering) {
				needsReordering = false;
				Collections.sort(dependencyOrders, leastLeft);
				boolean dependencyRemoved = false;
				for(int i = 0; i < dependencyOrders.size(); ++i) {
					DependencyOrder dependencyOrder = dependencyOrders.get(i);
					if(!dependencyOrder.dependenciesLeft.isEmpty()) {
						for(int j = i-1; j >= 0; --j) {
							dependencyRemoved = true;
							dependencyOrder.dependenciesLeft.remove(dependencyOrders.get(j).declaration.getDeclared());
						}
						if(!dependencyOrder.dependenciesLeft.isEmpty()) {
							needsReordering = true;
						}
					}
				}
				if(!dependencyRemoved) {
					throw new RuntimeException("Circular dependency!");
				}
			}
			//Apply new order
			declarations.clear();
			for(DependencyOrder dependencyOrder : dependencyOrders) {
				declarations.add(dependencyOrder.declaration);
			}
		}
		
		//Step 3. Compile
		CompilationCycle compilationCycle = new CompilationCycle();
		for(ISTypeDeclaration declaration : declarations) {
			declaration.compile(compilationCycle);
			compilationCycle.throwFatalIfAny();
		}
	}
	
	private static String getQName(ISTypeDeclaration declaration) {
		SPackage sPackage = declaration.getPackage();
		if(sPackage.isDefault()) {
			return declaration.getSimpleTypeName();
		} else {
			return sPackage.getFullName()+"."+declaration.getSimpleTypeName();
		}
	}
	
	public void buildFiles(final ISourceFileWriter sourceFileWriter) {
		final List<ISTypeDeclaration> declarations = collectDeclarations(new ArrayList<ISTypeDeclaration>());
		DeclarationFinder declarationFinder = new DeclarationFinder(declarations);
		
		for(ISTypeDeclaration typeDeclaration : declarations) {
			if(!typeDeclaration.isCompiled()) {
				throw new IllegalStateException(typeDeclaration.getDesc()+" was not compiled!");
			}
			SPackage sPackage = typeDeclaration.getPackage();
			StringBuilder fileName = new StringBuilder();
			if(!sPackage.isDefault()) {
				fileName.append(sPackage.getFullName().replace(".", "/"));
				fileName.append("/");
			}
			fileName.append(typeDeclaration.getSimpleTypeName());
			fileName.append(".java"); //File ending
			ISourceFile sourceFile = sourceFileWriter.createFile(fileName.toString());
			ILineWriter sourceWriter = sourceFile.getSourceWriter();
			
			
			SPackage pack = typeDeclaration.getPackage();
			if(!pack.isDefault()) {
				sourceWriter.print("package ").print(pack.getFullName()).println(";");
				sourceWriter.println();
			}
			
			ImportHandler importHandler = new ImportHandler(typeDeclaration, declarationFinder);
			typeDeclaration.visitImportContributors(importHandler);
			
			for(String importedType : importHandler.imports) {
				sourceWriter.print("import ").print(importedType).println(";");
			}
			sourceWriter.println();
			
			typeDeclaration.getDeclared().renderDefinition(typeDeclaration.getSimpleTypeName(), importHandler, sourceWriter);
			sourceFile.complete();
		}
	}
	
	private static class ImportHandler implements ISTypeImportCollector, IAliasResolver {
		private final ISTypeDeclaration forDeclaration;
		private final DeclarationFinder declarationFinder;
		private final String forPackage;
		private final Map<ISType, String> aliases = new LinkedHashMap<ISType, String>();
		private final List<String> imports = new ArrayList<>();

		public ImportHandler(ISTypeDeclaration forDeclaration, DeclarationFinder declarationFinder) {
			this.forDeclaration = forDeclaration;
			this.declarationFinder = declarationFinder;
			this.forPackage = toString(forDeclaration.getPackage());
		}
		
		private static String toString(SPackage sPackage) {
			if(sPackage.isDefault()) {
				return "";
			} else {
				return sPackage.getFullName();
			}
		}
		
		private boolean isInSamePackage(String packageName) {
			return packageName.equals(forPackage);
		}
		
		private void addImportMaybe(String packageName, String typeName, ISType type) {
			if(typeName.equals(forDeclaration.getSimpleTypeName())) {
				return;
			}
			if(!aliases.values().contains(typeName)) {
				aliases.put(type, typeName);
				if(!isInSamePackage(packageName) && !"".equals(packageName)) {
					imports.add(packageName+"."+typeName);
				}
			} 
		}

		@Override
		public void add(ISType typeImport) {
			if(forDeclaration.getDeclared().equals(typeImport)) {
				return;
			}
			ISTypeDeclaration declaration = declarationFinder.findDeclaration(typeImport, true);
			if(declaration != null) {
				addImportMaybe(toString(declaration.getPackage()), declaration.getSimpleTypeName(), typeImport);
			}
			if(typeImport instanceof JavaSType) {
				Class javaClass = ((JavaSType) typeImport).getJavaType();
				String name = javaClass.getSimpleName();
				Package javaPackage = javaClass.getPackage();
				if(javaPackage != null) {
					addImportMaybe(javaPackage.getName(), name, typeImport);
				}
			}
			if(typeImport instanceof SReferencedType) {
				String name = ((SReferencedType) typeImport).getSimpleName();
				String packageName = ((SReferencedType) typeImport).getPackageName();
				if(packageName != null) {
					addImportMaybe(packageName, name, typeImport);
				}
			}
			if(typeImport instanceof ISelfAliasing) {
				typeImport.collectImports(this);
			}
		}

		@Override
		public String getAlias(ISType type) {
			if(forDeclaration.getDeclared().equals(type)) {
				return forDeclaration.getSimpleTypeName();
			}
			if(type instanceof JavaPrimitive) {
				return ((JavaPrimitive) type).getPrimitiveName();
			} 
			String alias = aliases.get(type);
			if(alias != null) {
				return alias;
			}
			ISTypeDeclaration declaration = declarationFinder.findDeclaration(type, true);
			if(declaration != null) {
				return getQName(declaration);
			}
			if(type instanceof JavaSType) {
				return ((JavaSType) type).getJavaType().getName();
			}
			if(type instanceof ISelfAliasing) {
				return ((ISelfAliasing) type).getAlias(this);
			}
			if(type instanceof SReferencedType) {
				return ((SReferencedType) type).getQName();
			}
			throw new RuntimeException("Unknown SType "+type);
		}
	}
	
	private static class DeclarationFinder {
		private final Map<ISType, ISTypeDeclaration> knownDeclarations = new HashMap<ISType, ISTypeDeclaration>();
		public DeclarationFinder(List<ISTypeDeclaration> declarations) {
			for(ISTypeDeclaration declaration : declarations) {
				knownDeclarations.put(declaration.getDeclared(), declaration);
			}
		}
		public ISTypeDeclaration findDeclaration(ISType type, boolean allowNull) {
			ISTypeDeclaration declaration = knownDeclarations.get(type);
			if(declaration == null && !allowNull) {
				throw new IllegalArgumentException("Could not find type declaration!");
			}
			return declaration;
		}
	}
	
	private DefinitionDependency getDefinitionDependency(ISType type) {
		DefinitionDependency definitionDependency = definitionDependencies.get(type);
		if(definitionDependency == null) {
			definitionDependency = new DefinitionDependency(type);
			definitionDependencies.put(type, definitionDependency);
		}
		return definitionDependency;
	}

	public void defineAfter(ISType type, ISType dependency, ISType ... moreDependencies) {
		getDefinitionDependency(type).addDependency(dependency);
		for(ISType extra : moreDependencies) {
			defineAfter(type, extra);
		}
	}
	
	private static class DefinitionDependency {
		private final ISType forType;
		private final List<ISType> dependencies = new ArrayList<>();
		
		public DefinitionDependency(ISType forType) {
			this.forType = forType;
		}

		public void addDependency(ISType type) {
			if(!dependencies.contains(type) && !forType.equals(type)) {
				dependencies.add(type);
			}
		}
	}
}
