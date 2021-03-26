package com.systeminvecklare.smeta.lib;

public interface ISClassConstruction {
	void makePackagePrivate();
	void makeFinal();
	void makeAbstract();
	void makePrivate();
	void extend(ISClass superclass);
	void extend(Class<?> javaSuperclass);
	void implement(ISInterface sInterface);
	void implement(Class<?> javaInterface);
	ISFieldConstruction addField(ISType type, String name);
	ISFieldConstruction addField(Class<?> javaType, String name);
	ISFieldConstruction addStaticField(ISType type, String name);
	ISFieldConstruction addStaticField(Class<?> javaType, String name);
	IMethodConstruction addConstructor();
	IMethodConstruction addMethod(ISType returnType, String name);
	IMethodConstruction addMethod(Class<?> javaReturnType, String name);
	IMethodConstruction addStaticMethod(ISType returnType, String name);
	IMethodConstruction addStaticMethod(Class<?> javaReturnType, String name);
	ISClassConstruction addGenericParameter(String name);
	ISClassConstruction addGenericParameter(String name, ISType extendedType);
	ISClassConstruction addGenericParameter(String name, Class<?> javaExtendedType);
	SInterfaceDeclaration addNestedInterface(String interfaceName);
	SClassDeclaration addNestedClass(String className);
	SClassDeclaration addStaticNestedClass(String className);
}
