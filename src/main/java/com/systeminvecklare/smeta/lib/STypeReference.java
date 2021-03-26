package com.systeminvecklare.smeta.lib;

import java.lang.String;
import java.lang.UnsupportedOperationException;
import com.mattiasselin.linewriter.ILineWriter;
import java.util.List;

public final class STypeReference implements ISType, ISelfAliasing {
	private final String name;

	public STypeReference(String name) {
		this.name = name;
	}


	@Override
	public String getAlias(IAliasResolver aliasResolver) {
		return name;
	}

	@Override
	public void collectImports(ISTypeImportCollector collector) {
	}

	@Override
	public boolean isReferenceType() {
		return true;
	}

	@Override
	public void renderDefinition(String simpleTypeName, IAliasResolver aliasResolver, ILineWriter lineWriter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<SMethodHead> getMethodHeads() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ISInterface asSInterface(boolean nullForbidden) {
		return InternalUtil.nullIfAllowed(ISInterface.class, "type reference", nullForbidden);
	}

	@Override
	public ISClass asSClass(boolean nullForbidden) {
		return InternalUtil.nullIfAllowed(ISClass.class, "type reference", nullForbidden);
	}

}
