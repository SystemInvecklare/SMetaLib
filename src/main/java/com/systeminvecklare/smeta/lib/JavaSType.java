package com.systeminvecklare.smeta.lib;

import java.lang.Class;
import java.lang.Object;
import java.lang.RuntimeException;
import java.lang.String;
import com.mattiasselin.linewriter.ILineWriter;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.lang.NullPointerException;

public class JavaSType implements ISType {
	private final Class javaType;

	public JavaSType(Class javaType) {
		if(javaType == null) {
			throw new NullPointerException("javaType was null");
		}
		this.javaType = javaType;
	}


	@Override
	public int hashCode() {
		int hash = this.javaType.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof JavaSType && equalsOwn((JavaSType) obj);
	}

	private boolean equalsOwn(JavaSType obj) {
		return this.javaType.equals(obj.javaType);
	}

	public Class getJavaType() {
		return this.javaType;
	}

	@Override
	public void collectImports(ISTypeImportCollector collector) {
	}

	@Override
	public boolean isReferenceType() {
		return !javaType.isPrimitive();
	}

	@Override
	public void renderDefinition(String simpleTypeName, IAliasResolver aliasResolver, ILineWriter lineWriter) {
		throw new RuntimeException("Not yet implemented!");
	}

	@Override
	public List<SMethodHead> getMethodHeads() {
		List<SMethodHead> methodHeads = new ArrayList<SMethodHead>();
		for(Method method : javaType.getMethods()) {
			List<SParameter> parameters = new ArrayList<SParameter>();
			for(Parameter parameter : method.getParameters()) {
				parameters.add(new SParameter(ofJava(parameter.getParameterizedType()), parameter.getName()));
			}
			//TODO rename genericparameter to TypeVariable or something maybe
			List<SGenericParameter> genericParameters = new ArrayList<SGenericParameter>();
			for(TypeVariable<Method> typeVariable : method.getTypeParameters()) {
				SGenericParameter genericParameter = ofJavaTypeVar(typeVariable);
				if(typeVariable != null) {
					genericParameters.add(genericParameter);
				} else {
					//Log?
				}
			}
			methodHeads.add(new SMethodHead(ofJava(method.getDeclaringClass()), genericParameters, ofJava(method.getReturnType()), method.getName(), parameters));
		}
		return methodHeads;
	}

	@Override
	public ISInterface asSInterface(boolean nullForbidden) {
		if(javaType.isInterface()) {
			return SInterface.JavaInterface(javaType);
		} else {
			return InternalUtil.nullIfAllowed(ISInterface.class, javaType.getName(), nullForbidden);
		}
	}

	@Override
	public ISClass asSClass(boolean nullForbidden) {
		if(!javaType.isInterface()) {
			return SClass.JavaClass(javaType);
		} else {
			return InternalUtil.nullIfAllowed(ISClass.class, javaType.getName(), nullForbidden);
		}
	}


	private static ISType ofJava(Type type) {
		if(type instanceof Class) {
			return ofJava((Class) type);
		} else if(type instanceof ParameterizedType) {
			return ofJava((ParameterizedType) type);
		} else if(type instanceof TypeVariable) {
			SGenericParameter genParam = ofJavaTypeVar((TypeVariable) type);
			return new STypeReference(genParam.getName());
		} else if(type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			return SClass.array(ofJava(componentType));
		} else if(type instanceof WildcardType) {
			return new STypeReference("?");
		} else {
			throw new RuntimeException("TODO: handle "+type.getClass().getName());
		}
	}

	private static ISType ofJava(ParameterizedType type) {
		return new SGenericType(ofJava(type.getRawType()), new ISGenericParameterBuilder() {
			@Override
			public void build(ISGenericParametersConstruction construction) {
				for(Type arg : type.getActualTypeArguments()) {
					construction.add(ofJava(arg));
				}
			}
		});
	}

	private static SGenericParameter ofJavaTypeVar(TypeVariable<?> typeVariable) {
		String name = typeVariable.getName();
		Type[] bounds = typeVariable.getBounds();
		if(bounds.length == 1 && Object.class.equals(bounds[0])) {
			return new SGenericParameter(name, null);
		} else if(bounds.length > 1) {
			//TODO we need to implement a '&-type' that can take {0 or 1} ISClass and multiple ISInterfaces
			//     This is to handle like:   <T extends ILineWriter&ILineSource>  or  <T extends SClass&ILineWriter&ILineSource>
			throw new RuntimeException("Not yet implemented!");
		} else if(bounds[0] instanceof Class) {
			return new SGenericParameter(name, ofJava((Class) bounds[0]));
		} else {
			return null;
		}
	}

	public static ISType ofJava(Class javaType) {
		if(javaType.isPrimitive()) {
			return SClass.JavaPrimitive.getPrimitive(javaType);
		}
		return new JavaSType(javaType);
	}

}
