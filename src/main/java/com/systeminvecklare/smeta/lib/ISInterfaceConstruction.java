package com.systeminvecklare.smeta.lib;

import java.lang.Class;
import java.lang.String;

public interface ISInterfaceConstruction {
	void makePackagePrivate();
	void extend(ISInterface sInterface);
	void extend(Class<?> javaInterface);
	IMethodHeadConstruction addMethodHead(ISType returnType, String name);
	IMethodHeadConstruction addMethodHead(Class<?> javaReturnType, String name);
	void addConstant(ISType type, String name, SCode value);
	void addConstant(Class<?> javaType, String name, SCode value);
	ISInterfaceConstruction addGenericParameter(String name);
	ISInterfaceConstruction addGenericParameter(String name, ISType extendedType);
	ISInterfaceConstruction addGenericParameter(String name, Class<?> javaExtendedType);
}
