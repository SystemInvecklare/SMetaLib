package com.systeminvecklare.smeta.lib;

import java.lang.Class;

public interface ISGenericParametersConstruction {
	void add(ISType type);
	void add(Class<?> javaType);
	void addWildcardExtends(ISType type);
	void addWildcardExtends(Class<?> javaType);
	void addWildcardSuper(ISType type);
	void addWildcardSuper(Class<?> javaType);
	void addWildcard();
}
