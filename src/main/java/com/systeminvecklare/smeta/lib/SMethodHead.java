package com.systeminvecklare.smeta.lib;

import java.util.List;
import java.lang.String;
import java.lang.NullPointerException;

public class SMethodHead implements ISTypeImportContributor {
	private final ISType owner;
	private final List<SGenericParameter> genericParameters;
	private final ISType returnType;
	private final String name;
	private final List<SParameter> parameters;
	private final boolean isOverriding;
	private final SCode javaDoc;

	public SMethodHead(ISType owner, List<SGenericParameter> genericParameters, ISType returnType, String name, List<SParameter> parameters) {
		if(owner == null) {
			throw new NullPointerException("owner was null");
		}
		if(genericParameters == null) {
			throw new NullPointerException("genericParameters was null");
		}
		if(returnType == null) {
			throw new NullPointerException("returnType was null");
		}
		if(name == null) {
			throw new NullPointerException("name was null");
		}
		if(parameters == null) {
			throw new NullPointerException("parameters was null");
		}
		this.owner = owner;
		this.genericParameters = genericParameters;
		this.returnType = returnType;
		this.name = name;
		this.parameters = parameters;
		this.isOverriding = false;
		this.javaDoc = null;
	}

	public SMethodHead(ISType owner, List<SGenericParameter> genericParameters, ISType returnType, String name, List<SParameter> parameters, boolean isOverriding) {
		if(owner == null) {
			throw new NullPointerException("owner was null");
		}
		if(genericParameters == null) {
			throw new NullPointerException("genericParameters was null");
		}
		if(returnType == null) {
			throw new NullPointerException("returnType was null");
		}
		if(name == null) {
			throw new NullPointerException("name was null");
		}
		if(parameters == null) {
			throw new NullPointerException("parameters was null");
		}
		this.owner = owner;
		this.genericParameters = genericParameters;
		this.returnType = returnType;
		this.name = name;
		this.parameters = parameters;
		this.isOverriding = isOverriding;
		this.javaDoc = null;
	}

	public SMethodHead(ISType owner, List<SGenericParameter> genericParameters, ISType returnType, String name, List<SParameter> parameters, SCode javaDoc) {
		if(owner == null) {
			throw new NullPointerException("owner was null");
		}
		if(genericParameters == null) {
			throw new NullPointerException("genericParameters was null");
		}
		if(returnType == null) {
			throw new NullPointerException("returnType was null");
		}
		if(name == null) {
			throw new NullPointerException("name was null");
		}
		if(parameters == null) {
			throw new NullPointerException("parameters was null");
		}
		this.owner = owner;
		this.genericParameters = genericParameters;
		this.returnType = returnType;
		this.name = name;
		this.parameters = parameters;
		this.isOverriding = false;
		this.javaDoc = javaDoc;
	}

	public SMethodHead(ISType owner, List<SGenericParameter> genericParameters, ISType returnType, String name, List<SParameter> parameters, boolean isOverriding, SCode javaDoc) {
		if(owner == null) {
			throw new NullPointerException("owner was null");
		}
		if(genericParameters == null) {
			throw new NullPointerException("genericParameters was null");
		}
		if(returnType == null) {
			throw new NullPointerException("returnType was null");
		}
		if(name == null) {
			throw new NullPointerException("name was null");
		}
		if(parameters == null) {
			throw new NullPointerException("parameters was null");
		}
		this.owner = owner;
		this.genericParameters = genericParameters;
		this.returnType = returnType;
		this.name = name;
		this.parameters = parameters;
		this.isOverriding = isOverriding;
		this.javaDoc = javaDoc;
	}


	public ISType getOwner() {
		return this.owner;
	}

	public List<SGenericParameter> getGenericParameters() {
		return this.genericParameters;
	}

	public ISType getReturnType() {
		return this.returnType;
	}

	public String getName() {
		return this.name;
	}

	public List<SParameter> getParameters() {
		return this.parameters;
	}

	public boolean isOverriding() {
		return this.isOverriding;
	}

	public SCode getJavaDoc() {
		return this.javaDoc;
	}

	@Override
	public void collectImports(ISTypeImportCollector collector) {
		collector.add(returnType);
		for(SParameter parameter : parameters) {
			collector.add(parameter.getType());
		}
		if(javaDoc != null) {
			javaDoc.collectImports(collector);
		}
	}

}
