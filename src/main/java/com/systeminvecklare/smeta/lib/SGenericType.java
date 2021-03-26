package com.systeminvecklare.smeta.lib;

import java.util.ArrayList;
import java.util.List;

import com.mattiasselin.linewriter.ILineWriter;

public class SGenericType implements ISType, ISelfAliasing {
	public static final ISGenericParameterBuilder WILDCARD = new ISGenericParameterBuilder() {
		@Override
		public void build(ISGenericParametersConstruction construction) {
			construction.addWildcard();
		}
	};
	
	private final ISType base;
	private final List<ITypeParameter> typeParameters;
	
	public SGenericType(Class<?> javaType, ISType ... types) {
		this(JavaSType.ofJava(javaType), new SimpleParameterBuilder(types));
	}
	
	public SGenericType(Class<?> javaType, Class ... types) {
		this(JavaSType.ofJava(javaType), new SimpleParameterBuilder(types));
	}
	
	public SGenericType(ISType base, ISType ... types) {
		this(base, new SimpleParameterBuilder(types));
	}
	
	public SGenericType(ISType base, Class ... types) {
		this(base, new SimpleParameterBuilder(types));
	}
	
	public SGenericType(Class<?> javaType, ISGenericParameterBuilder parameterBuilder) {
		this(JavaSType.ofJava(javaType), parameterBuilder);
	}
	
	public SGenericType(ISType base, ISGenericParameterBuilder parameterBuilder) {
		this.base = base;
		this.typeParameters = new ArrayList<>();
		parameterBuilder.build(new ISGenericParametersConstruction() {
			@Override
			public void add(ISType type) {
				if(!type.isReferenceType()) {
					throw new IllegalArgumentException("Only reference types allowed");
				}
				typeParameters.add(new Type(type));
			}

			@Override
			public void add(Class<?> javaType) {
				add(JavaSType.ofJava(javaType));
			}

			@Override
			public void addWildcardExtends(ISType type) {
				if(!type.isReferenceType()) {
					throw new IllegalArgumentException("Only reference types allowed");
				}
				typeParameters.add(new WildcardExtends(type));
			}

			@Override
			public void addWildcardExtends(Class<?> javaType) {
				addWildcardExtends(JavaSType.ofJava(javaType));
			}

			@Override
			public void addWildcardSuper(ISType type) {
				if(!type.isReferenceType()) {
					throw new IllegalArgumentException("Only reference types allowed");
				}
				typeParameters.add(new WildcardSuper(type));
			}

			@Override
			public void addWildcardSuper(Class<?> javaType) {
				addWildcardSuper(JavaSType.ofJava(javaType));
			}
			
			@Override
			public void addWildcard() {
				typeParameters.add(new Wildcard());
			}
		});
	}
	
	private SGenericType(ISType base, List<ITypeParameter> typeParameters) {
		this.base = base;
		this.typeParameters = typeParameters;
	}

	@Override
	public void collectImports(ISTypeImportCollector collector) {
		collector.add(base);
		for(ITypeParameter typeParameter : typeParameters) {
			typeParameter.collectImports(collector);
		}
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
	public String getAlias(IAliasResolver aliasResolver) {
		StringBuilder builder = new StringBuilder();
		builder.append(aliasResolver.getAlias(base)).append("<");
		boolean first = true;
		for(ITypeParameter typeParameter : typeParameters) {
			if(first) {
				first = false;
			} else {
				builder.append(", ");
			}
			builder.append(typeParameter.getAlias(aliasResolver));
		}
		builder.append(">");
		return builder.toString();
	}
	
	private interface ITypeParameter extends ISTypeImportContributor, ISelfAliasing {
		
	}
	
	private static class Type implements ITypeParameter {
		private final ISType type;

		public Type(ISType type) {
			this.type = type;
		}
		
		@Override
		public void collectImports(ISTypeImportCollector collector) {
			collector.add(type);
		}

		@Override
		public String getAlias(IAliasResolver aliasResolver) {
			return aliasResolver.getAlias(type);
		}
	}

	private static class Wildcard implements ITypeParameter {
		@Override
		public void collectImports(ISTypeImportCollector collector) {
		}

		@Override
		public String getAlias(IAliasResolver aliasResolver) {
			return "?";
		}
	}

	private static class WildcardExtends extends Type {
		public WildcardExtends(ISType type) {
			super(type);
		}
		
		@Override
		public String getAlias(IAliasResolver aliasResolver) {
			return "? extends "+super.getAlias(aliasResolver);
		}
	}
	
	private static class WildcardSuper extends Type {
		public WildcardSuper(ISType type) {
			super(type);
		}
		
		@Override
		public String getAlias(IAliasResolver aliasResolver) {
			return "? super "+super.getAlias(aliasResolver);
		}
	}
	
	private static class SGenericInterfaceType extends SGenericType implements ISInterface {
		private final ISInterface baseInterface;

		public SGenericInterfaceType(ISInterface base, List<ITypeParameter> typeParameters) {
			super(base, typeParameters);
			this.baseInterface = base;
		}

		@Override
		public List<ISInterface> getExtendedInterfaces() {
			return baseInterface.getExtendedInterfaces();
		}
	}
	
	private static class SGenericClassType extends SGenericType implements ISClass {
		private final ISClass ungenericClass;

		public SGenericClassType(ISClass ungeneric, List<ITypeParameter> typeParameters) {
			super(ungeneric, typeParameters);
			this.ungenericClass = ungeneric;
		}

		@Override
		public List<ISInterface> getImplementedInterfaces() {
			return ungenericClass.getImplementedInterfaces();
		}
	}

	@Override
	public ISInterface asSInterface(boolean nullForbidden) {
		ISInterface baseInterface = base.asSInterface(nullForbidden);
		if(baseInterface != null) {
			return new SGenericInterfaceType(baseInterface, typeParameters);
		} else {
			return InternalUtil.nullIfAllowed(ISInterface.class, "generic class", nullForbidden);
		}
	}

	@Override
	public ISClass asSClass(boolean nullForbidden) {
		ISClass ungeneric = base.asSClass(nullForbidden);
		if(ungeneric != null) {
			return new SGenericClassType(ungeneric, typeParameters);
		} else {
			return InternalUtil.nullIfAllowed(ISClass.class, "generic interface", nullForbidden);
		}
	}
	
	@Override
	public boolean isReferenceType() {
		return true;
	}
	
	private static class SimpleParameterBuilder implements ISGenericParameterBuilder {
		private final ISType[] types;

		public SimpleParameterBuilder(ISType[] types) {
			this.types = types;
		}
		
		public SimpleParameterBuilder(Class[] types) {
			this.types = new ISType[types.length];
			for(int i = 0; i < types.length; ++i) {
				this.types[i] = JavaSType.ofJava(types[i]);
			}
		}

		@Override
		public void build(ISGenericParametersConstruction construction) {
			for(ISType type : types) {
				construction.add(type);
			}
		}
	}
}
