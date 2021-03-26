package com.systeminvecklare.smeta.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mattiasselin.linewriter.ILineWriter;

public abstract class SInterface implements ISInterface {
	private SInterface() {
	}
	
	public static ISInterface JavaInterface(Class type) {
		if(type.isInterface()) {
			return new JavaSInterface(type);
		} else {
			throw new IllegalArgumentException(type.getName()+" is not an interface");
		}
	}
	
	private static class JavaSInterface extends JavaSType implements ISInterface {
		public JavaSInterface(Class javaType) {
			super(javaType);
		}

		@Override
		public List<ISInterface> getExtendedInterfaces() {
			List<ISInterface> extendedInterfaces = new ArrayList<>();
			for(Class extended : getJavaType().getInterfaces()) {
				extendedInterfaces.add(JavaInterface(extended));
			}
			return extendedInterfaces;
		}
	}
	
	public abstract void compile(ISInterfaceDefinition definition);

	public static SInterface Declared() {
		return new DeclaredInterface();
	}
	
	private static class DeclaredInterface extends SInterface {
		private final List<SGenericParameter> genericParameters = new ArrayList<SGenericParameter>();
		private final List<ISInterface> extendedInterfaces = new ArrayList<ISInterface>();
		private final List<SMethodHead> methods = new ArrayList<SMethodHead>();
		private final List<SConstant> constants = new ArrayList<SConstant>();
		private boolean packagePrivate = false;
		private boolean compiled = false;
		
		@Override
		public void compile(ISInterfaceDefinition definition) {
			final List<SMethodHeadConstruction> methodConstructions = new ArrayList<>();
			definition.build(new ISInterfaceConstruction() {

				@Override
				public void makePackagePrivate() {
					packagePrivate = true;
				}
				@Override
				public void extend(ISInterface sInterface) {
					extendedInterfaces.add(sInterface);
				}

				@Override
				public void extend(Class<?> javaInterface) {
					extend(SInterface.JavaInterface(javaInterface));
				}
				
				@Override
				public IMethodHeadConstruction addMethodHead(Class<?> javaType, String name) {
					return addMethodHead(JavaSInterface.ofJava(javaType), name);
				}
				
				@Override
				public IMethodHeadConstruction addMethodHead(ISType returnType, String name) {
					SMethodHeadConstruction methodHead = new SMethodHeadConstruction(returnType, name);
					methodConstructions.add(methodHead);
					return methodHead;
				}
				
				@Override
				public void addConstant(Class<?> javaType, String name, SCode value) {
					addConstant(JavaSType.ofJava(javaType), name, value);
				}
				
				@Override
				public void addConstant(ISType type, String name, SCode value) {
					constants.add(new SConstant(type, name, value));
				}
				
				@Override
				public ISInterfaceConstruction addGenericParameter(String name) {
					genericParameters.add(new SGenericParameter(name, null));
					return this;
				}
				
				@Override
				public ISInterfaceConstruction addGenericParameter(String name, Class<?> javaExtendedType) {
					return addGenericParameter(name, JavaSType.ofJava(javaExtendedType));
				}
				
				@Override
				public ISInterfaceConstruction addGenericParameter(String name, ISType extendedType) {
					genericParameters.add(new SGenericParameter(name, extendedType));
					return this;
				}
			});
			for(SMethodHeadConstruction construction : methodConstructions) {
				methods.add(construction.compile(DeclaredInterface.this));
			}
			compiled = true;
		}

		@Override
		public List<ISInterface> getExtendedInterfaces() {
			verifyCompiled();
			return Collections.unmodifiableList(extendedInterfaces);
		}
		
		@Override
		public List<SMethodHead> getMethodHeads() {
			verifyCompiled();
			return Collections.unmodifiableList(methods);
		}

		private void verifyCompiled() {
			if(!compiled) {
				throw new IllegalStateException("Interface not yet compiled");
			}
		}
		
		@Override
		public void collectImports(ISTypeImportCollector collector) {
			verifyCompiled();
			InternalUtil.collectFromList(genericParameters, collector);
			InternalUtil.addAll(extendedInterfaces, collector);
			InternalUtil.collectFromList(methods, collector);
			InternalUtil.collectFromList(constants, collector);
		}
		

		@Override
		public void renderDefinition(String simpleTypeName, IAliasResolver aliasResolver, ILineWriter lineWriter) {
			verifyCompiled();
			RenderUtil.renderAccessLevel(lineWriter, packagePrivate ? 2 : 3);
			lineWriter.print("interface ").print(simpleTypeName);
			if(!genericParameters.isEmpty()) {
				RenderUtil.renderGenericParameters(genericParameters, aliasResolver, lineWriter);
				lineWriter.print(" ");
			}
			if(!extendedInterfaces.isEmpty()) {
				lineWriter.print(" extends ");
				boolean first = true;
				for(ISInterface sInterface : extendedInterfaces) {
					if(first) {
						first = false;
					} else {
						lineWriter.print(", ");
					}
					lineWriter.print(aliasResolver.getAlias(sInterface));
				}
			}
			lineWriter.println(" {");
				lineWriter.indent();
				if(!constants.isEmpty()) {
					for(SConstant constant : constants) {
						lineWriter.print("public static final ").print(aliasResolver.getAlias(constant.type)).print(" ").print(constant.name).print(" = ");
						constant.initialValue.render(lineWriter, aliasResolver);
						lineWriter.println(";");
					}
					lineWriter.println();
				}
				for(SMethodHead method : methods) {
					RenderUtil.printJavaDoc(lineWriter, aliasResolver, method.getJavaDoc());
					if(method.isOverriding()) {
						lineWriter.print("@Override ");
					}
					if(!method.getGenericParameters().isEmpty()) {
						RenderUtil.renderGenericParameters(method.getGenericParameters(), aliasResolver, lineWriter);
						lineWriter.print(" ");
					}
					lineWriter.print(aliasResolver.getAlias(method.getReturnType())).print(" ");
					lineWriter.print(method.getName()).print("(");
					RenderUtil.renderParameters(lineWriter, aliasResolver, method.getParameters());
					lineWriter.println(");");
				}
				
				lineWriter.unindent();
			lineWriter.print("}");
		}
	}
	
	private static class SMethodHeadConstruction implements IMethodHeadConstruction, ISTypeImportContributor {
		private SCode javaDoc = null;
		private boolean isOverriding = false;
		private final ISType returnType;
		private final String name;
		private final List<SParameter> parameters = new ArrayList<>();
		private final List<SGenericParameter> genericParameters = new ArrayList<>();
		
		public SMethodHeadConstruction(ISType returnType, String name) {
			this.returnType = returnType;
			this.name = name;
		}

		@Override
		public IMethodHeadConstruction addParameter(ISType type, String name) {
			parameters.add(new SParameter(type, name));
			return this;
		}

		public SMethodHead compile(ISType owner) {
			return new SMethodHead(owner, genericParameters, returnType, name, parameters, isOverriding, javaDoc);
		}

		@Override
		public IMethodHeadConstruction addParameter(Class<?> javaType, String name) {
			return addParameter(JavaSType.ofJava(javaType), name);
		}

		@Override
		public void collectImports(ISTypeImportCollector collector) {
			collector.add(returnType);
			InternalUtil.collectFromList(parameters, collector);
			InternalUtil.collectFromList(genericParameters, collector);
		}
		
		@Override
		public IMethodHeadConstruction makeOverride() {
			this.isOverriding = true;
			return this;
		}
		
		@Override
		public IMethodHeadConstruction setJavaDoc(SCode javaDoc) {
			this.javaDoc = javaDoc;
			return this;
		}

		@Override
		public IMethodHeadConstruction addGenericParameter(String name) {
			genericParameters.add(new SGenericParameter(name, null));
			return this;
		}

		@Override
		public IMethodHeadConstruction addGenericParameter(String name, ISType extendedType) {
			genericParameters.add(new SGenericParameter(name, extendedType));
			return this;
		}

		@Override
		public IMethodHeadConstruction addGenericParameter(String name, Class<?> javaExtendedType) {
			return addGenericParameter(name, JavaSType.ofJava(javaExtendedType));
		}
	}
	
	private static class SConstant implements ISTypeImportContributor {
		private final ISType type;
		private final String name;
		private final SCode initialValue;

		public SConstant(ISType type, String name, SCode initialValue) {
			this.type = type;
			this.name = name;
			this.initialValue = initialValue;
		}

		@Override
		public void collectImports(ISTypeImportCollector collector) {
			collector.add(type);
			initialValue.collectImports(collector);
		}
	}
	
	@Override
	public ISClass asSClass(boolean nullForbidden) {
		return InternalUtil.asType(ISClass.class, this, nullForbidden);
	}
	
	@Override
	public ISInterface asSInterface(boolean nullForbidden) {
		return this;
	}
	
	@Override
	public boolean isReferenceType() {
		return true;
	}
}
