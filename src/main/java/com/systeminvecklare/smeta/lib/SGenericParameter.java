package com.systeminvecklare.smeta.lib;

import java.lang.String;
import com.mattiasselin.linewriter.ILineWriter;
import java.lang.NullPointerException;

public final class SGenericParameter implements ISTypeImportContributor {
	private final String name;
	private final ISType extendedTypeOrNull;

	public SGenericParameter(String name, ISType extendedTypeOrNull) {
		if(name == null) {
			throw new NullPointerException("name was null");
		}
		this.name = name;
		this.extendedTypeOrNull = extendedTypeOrNull;
	}


	public String getName() {
		return this.name;
	}

	public ISType getExtendedTypeOrNull() {
		return this.extendedTypeOrNull;
	}

	public void render(IAliasResolver aliasResolver, ILineWriter lineWriter) {
		lineWriter.print(name);
		if(extendedTypeOrNull != null) {
			lineWriter.print(" extends ");
			lineWriter.print(aliasResolver.getAlias(extendedTypeOrNull));
		}
	}

	@Override
	public void collectImports(ISTypeImportCollector collector) {
		if(extendedTypeOrNull != null) {
			collector.add(extendedTypeOrNull);
		}
	}

}
