package com.systeminvecklare.smeta.lib;

import java.util.Collections;
import java.util.List;

import com.mattiasselin.linewriter.ILineWriter;

public class SArrayType implements ISClass, ISelfAliasing {
	private final ISType elementType;
	
	public SArrayType(ISType elementType) {
		this.elementType = elementType;
	}

	@Override
	public void renderDefinition(String simpleTypeName, IAliasResolver aliasResolver, ILineWriter lineWriter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<SMethodHead> getMethodHeads() {
		throw new UnsupportedOperationException();
	}
	
	public ISType getElementType() {
		return elementType;
	}

	@Override
	public void collectImports(ISTypeImportCollector collector) {
		elementType.collectImports(collector);
	}

	@Override
	public List<ISInterface> getImplementedInterfaces() {
		return Collections.emptyList();
	}
	
	@Override
	public int hashCode() {
		return elementType.hashCode()*91;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof SArrayType && equalsOwn((SArrayType) obj);
	}

	private boolean equalsOwn(SArrayType obj) {
		return this.elementType.equals(obj.elementType);
	}

	@Override
	public String getAlias(IAliasResolver aliasResolver) {
		return aliasResolver.getAlias(elementType)+"[]";
	}
	
	@Override
	public ISClass asSClass(boolean nullForbidden) {
		return this;
	}
	
	@Override
	public ISInterface asSInterface(boolean nullForbidden) {
		return InternalUtil.nullIfAllowed(ISInterface.class, "array", nullForbidden);
	}
	
	@Override
	public boolean isReferenceType() {
		return true;
	}
} 
