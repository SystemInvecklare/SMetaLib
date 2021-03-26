package com.systeminvecklare.smeta.lib;

import java.lang.String;
import com.mattiasselin.linewriter.ILineWriter;
import java.util.List;

public interface ISType extends ISTypeImportContributor {
	public static final ISType VOID = SClass.JavaPrimitive.VOID;

	boolean isReferenceType();
	void renderDefinition(String simpleTypeName, IAliasResolver aliasResolver, ILineWriter lineWriter);
	/**
	* Get ALL public method heads of this type. This includes those defined in superinterface/interfaces and superclass
	*/
	List<SMethodHead> getMethodHeads();
	ISInterface asSInterface(boolean nullForbidden);
	ISClass asSClass(boolean nullForbidden);
}
