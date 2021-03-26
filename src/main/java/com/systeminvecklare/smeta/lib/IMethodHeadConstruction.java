package com.systeminvecklare.smeta.lib;

import java.lang.String;
import java.lang.Class;

public interface IMethodHeadConstruction {
	IMethodHeadConstruction addParameter(ISType type, String name);
	IMethodHeadConstruction addParameter(Class<?> javaType, String name);
	IMethodHeadConstruction addGenericParameter(String name);
	IMethodHeadConstruction addGenericParameter(String name, ISType extendedType);
	IMethodHeadConstruction addGenericParameter(String name, Class<?> javaExtendedType);
	IMethodHeadConstruction makeOverride();
	IMethodHeadConstruction setJavaDoc(SCode javaDoc);
}
