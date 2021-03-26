package com.systeminvecklare.smeta.lib;

import java.util.Collection;
import java.lang.Class;
import java.lang.Object;
import java.lang.ClassCastException;
import java.lang.String;

/*package-private*/ class InternalUtil {



	public static void collectFromList(Collection<? extends ISTypeImportContributor> contributors, ISTypeImportCollector collector) {
		for(ISTypeImportContributor contributor : contributors) {
			contributor.collectImports(collector);
		}
	}

	public static void addAll(Collection<? extends ISType> types, ISTypeImportCollector collector) {
		for(ISType type : types) {
			collector.add(type);
		}
	}

	public static <T> T asType(Class<T> resultType, Object object, boolean nullForbidden) {
		if(resultType.isInstance(object)) {
			return resultType.cast(object);
		} else {
			return nullIfAllowed(resultType, object != null ? object.getClass().getName() : "null" , nullForbidden);
		}
	}

	public static <T> T nullIfAllowed(Class<T> resultType, String misscastType, boolean nullForbidden) {
		if(nullForbidden) {
			throw new ClassCastException(misscastType+" is not a "+resultType.getName());
		} else {
			return null;
		}
	}

}
