package com.systeminvecklare.smeta.lib;

public interface IMethodConstruction extends IMethodHeadConstruction {
	@Override IMethodConstruction addParameter(ISType type, String name);
	@Override IMethodConstruction addParameter(Class<?> javaType, String name);
	@Override IMethodConstruction addGenericParameter(String name);
	@Override IMethodConstruction addGenericParameter(String name, ISType extendedType);
	@Override IMethodConstruction addGenericParameter(String name, Class<?> javaExtendedType);
	@Override IMethodConstruction makeOverride();
	@Override IMethodConstruction setJavaDoc(SCode javaDoc);
	IMethodConstruction makePrivate();
	IMethodConstruction makeProtected();
	IMethodConstruction makeAbstract();
	void setBody(SCode code);
}
