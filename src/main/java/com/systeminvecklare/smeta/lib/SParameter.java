package com.systeminvecklare.smeta.lib;

import java.lang.String;
import java.lang.Object;
import java.lang.NullPointerException;

public final class SParameter implements ISTypeImportContributor {
	private final ISType type;
	private final String name;

	public SParameter(ISType type, String name) {
		if(type == null) {
			throw new NullPointerException("type was null");
		}
		if(name == null) {
			throw new NullPointerException("name was null");
		}
		this.type = type;
		this.name = name;
	}


	public ISType getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		int hash = this.type.hashCode();
		hash = (hash*37) ^ this.name.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SParameter && equalsOwn((SParameter) obj);
	}

	private boolean equalsOwn(SParameter obj) {
		return this.type.equals(obj.type)
			&& this.name.equals(obj.name);
	}

	@Override
	public void collectImports(ISTypeImportCollector collector) {
		collector.add(type);
	}

}
