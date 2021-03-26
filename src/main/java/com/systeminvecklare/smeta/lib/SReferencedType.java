package com.systeminvecklare.smeta.lib;

import java.util.List;

import com.mattiasselin.linewriter.ILineWriter;

/**
 * Represents a type we know will exist, but we don't have a java class. 
 * We only know it by its fully qualified name. 
 * @author Mattias Selin
 *
 */
public class SReferencedType implements ISType, ISInterface, ISClass {
	private final String qname;
	
	public SReferencedType(String qname) {
		this.qname = qname;
	}

	@Override
	public void collectImports(ISTypeImportCollector collector) {
		collector.add(this);
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
		return this;
	}

	@Override
	public ISClass asSClass(boolean nullForbidden) {
		return this;
	}

	@Override
	public List<ISInterface> getImplementedInterfaces() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ISInterface> getExtendedInterfaces() {
		throw new UnsupportedOperationException();
	}

	public String getSimpleName() {
		return qname.substring(qname.lastIndexOf(".")+1);
	}

	public String getPackageName() {
		int lastDot = qname.lastIndexOf(".");
		if(lastDot == -1) {
			return null;
		} else {
			return qname.substring(0, lastDot);
		}
	}

	public String getQName() {
		return qname;
	}
	
	@Override
	public int hashCode() {
		return qname.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof SReferencedType && equalsOwn((SReferencedType) obj);
	}
	
	private boolean equalsOwn(SReferencedType other) {
		return this.qname.equals(other.qname);
	}
}
