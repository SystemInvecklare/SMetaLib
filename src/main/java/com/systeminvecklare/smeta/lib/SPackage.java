package com.systeminvecklare.smeta.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SPackage {
	private final Map<String, SubPackage> subPackages = new LinkedHashMap<>();
	private final Collection<ISTypeDeclaration> declaredTypes = new ArrayList<ISTypeDeclaration>();
	
	public SPackage() {
	}

	public String getFullName() {
		return null;
	}
	
	public final boolean isDefault() {
		return getParent() == null;
	}
	
	public SPackage getParent() {
		return null;
	}
	
	public SPackage in(String subPath) {
		if("".equals(subPath)) {
			return this;
		}
		if(subPath.contains(".")) {
			SPackage pack = this;
			for(String path : subPath.split("\\.")) {
				pack = pack.in(path);
			}
			return pack;
		} else {
			SubPackage subPackage = subPackages.get(subPath);
			if(subPackage == null) {
				subPackage = new SubPackage(this, subPath);
				subPackages.put(subPath, subPackage);
			}
			return subPackage;
		}
	}
	
	private static class SubPackage extends SPackage {
		private final SPackage parent;
		private final String packageName;

		public SubPackage(SPackage parent, String packageName) {
			if(parent == null) {
				throw new IllegalArgumentException("parent is null");
			}
			this.parent = parent;
			this.packageName = packageName;
		}
		
		@Override
		public String getFullName() {
			if(parent.isDefault()) {
				return packageName;
			} else {
				return parent.getFullName()+"."+packageName;
			}
		}
		
		@Override
		public SPackage getParent() {
			return parent;
		}
	}

	public SClassDeclaration declareClass(String className) {
		SClassDeclaration classDeclaration = new SClassDeclaration(this, className, SClass.Declared());
		declaredTypes.add(classDeclaration);
		return classDeclaration;
	}
	

	public SInterfaceDeclaration declareInterface(String interfaceName) {
		SInterfaceDeclaration interfaceDeclaration = new SInterfaceDeclaration(this, interfaceName, SInterface.Declared());
		declaredTypes.add(interfaceDeclaration);
		return interfaceDeclaration;
	}

	/*package-protected*/ void visitTypeDeclarations(ISTypeDeclarationVisitor visitor) {
		for(ISTypeDeclaration declaration : declaredTypes) {
			visitor.visit(declaration);
		}
		for(Entry<String, SubPackage> entry : subPackages.entrySet()) {
			entry.getValue().visitTypeDeclarations(visitor);
		}
	}
}
