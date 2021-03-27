package com.systeminvecklare.smeta.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mattiasselin.linewriter.ILineWriter;

public abstract class SClass implements ISClass {
	
	private SClass() {
	}
	
	/**
	 * Runs the builder constructions and initializes the class. This must be done before generating code. 
	 * @param compilationCycle 
	 */
	public void compile(ISClassDefinition definition) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void renderDefinition(String simpleTypeName, IAliasResolver aliasResolver, ILineWriter lineWriter) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void collectImports(ISTypeImportCollector collector) {
	}
	
	public void TEMP_collectDeclarationImports(ISTypeImportCollector collector) {
	}
	
	public abstract List<ISInterface> getImplementedInterfaces();
	
	private static class DeclaredClass extends SClass {
		private final List<SGenericParameter> genericParameters = new ArrayList<SGenericParameter>();
		private ISClass superClass = null;
		private final List<ISInterface> implementedInterfaces = new ArrayList<ISInterface>();
		private final List<SField> fields = new ArrayList<SField>();
		private final List<SMethod> methods = new ArrayList<SMethod>();
		private final List<SConstructor> constructors = new ArrayList<SConstructor>();
		private boolean compiled = false;
		private int accessLevel = 3; //0 == private, 1 == protected, 2 == packagePrivate, 3 == public
		private boolean isFinal = false;
		private boolean isAbstract = false;
		private final List<ISTypeDeclaration> nestedTypes = new ArrayList<>();
		
		@Override
		public void compile(ISClassDefinition definition) {
			definition.build(new ISClassConstruction() {
				@Override
				public void makeFinal() {
					isFinal = true;
				}
				
				@Override
				public void makePackagePrivate() {
					accessLevel = 2;
				}
				
				@Override
				public void makeAbstract() {
					isAbstract = true;
				}
				
				@Override
				public void makePrivate() {
					accessLevel = 0;
				}
				
				@Override
				public void extend(Class<?> javaClass) {
					extend(JavaClass(javaClass));
				}
				
				@Override
				public void extend(ISClass sClass) {
					if(superClass != null && !superClass.equals(sClass)) {
						throw new IllegalArgumentException("Can only extend one class!");
					}
					superClass = sClass;
				}
				
				@Override
				public void implement(Class<?> javaInterface) {
					implement(SInterface.JavaInterface(javaInterface));
				}

				@Override
				public void implement(ISInterface sInterface) {
					implementedInterfaces.add(sInterface);
				}
				
				@Override
				public ISFieldConstruction addField(ISType type, String name) {
					return addField(type, name, false);
				}
				
				@Override
				public ISFieldConstruction addField(Class<?> javaType, String name) {
					return addField(JavaSType.ofJava(javaType), name);
				}
				
				@Override
				public ISFieldConstruction addStaticField(ISType type, String name) {
					return addField(type, name, true);
				}
				
				@Override
				public ISFieldConstruction addStaticField(Class<?> javaType, String name) {
					return addStaticField(JavaSType.ofJava(javaType), name);
				}
				
				@Override
				public SInterfaceDeclaration addNestedInterface(String interfaceName) {
					throw new RuntimeException("TODO! Use abstract class for now");
//					SInterfaceDeclaration declaration = new SInterfaceDeclaration(null, interfaceName, SInterface);
//					nestedTypes
//					return null;
				}
				
				@Override
				public SClassDeclaration addNestedClass(String className) {
					SNestedClassDeclaration declaration = new SNestedClassDeclaration(DeclaredClass.this, className, false);
					nestedTypes.add(declaration);
					return declaration;
				}
				
				@Override
				public SClassDeclaration addStaticNestedClass(String className) {
					SNestedClassDeclaration declaration = new SNestedClassDeclaration(DeclaredClass.this, className, true);
					nestedTypes.add(declaration);
					return declaration;
				}
				
				private ISFieldConstruction addField(ISType type, String name, boolean isStatic) {
					final SField field = new SField(type, name);
					field.isStatic = isStatic;
					field.accessLevel = isStatic ? 3 : 0;
					fields.add(field);
					return new ISFieldConstruction() {
						@Override
						public void initialValue(SCode expression) {
							field.initialValue = expression;
						}

						@Override
						public ISFieldConstruction makeFinal() {
							field.isFinal = true;
							return this;
						}
						
						@Override
						public ISFieldConstruction makePrivate() {
							field.accessLevel = 0;
							return this;
						}
						
						@Override
						public ISFieldConstruction makePublic() {
							field.accessLevel = 3;
							return this;
						}
					};
				}

				private IMethodConstruction constructionFor(final SMethodConstructorBase base) {
					return new IMethodConstruction() {
						@Override
						public IMethodConstruction addParameter(ISType type, String name) {
							base.addParameter(type, name);
							return this;
						}
						
						@Override
						public IMethodConstruction addParameter(Class<?> javaType, String name) {
							return addParameter(JavaSType.ofJava(javaType), name);
						}
						
						@Override
						public IMethodConstruction addGenericParameter(String name) {
							return addGenericParameterImpl(name, null);
						}
						
						@Override
						public IMethodConstruction addGenericParameter(String name, Class<?> javaExtendedType) {
							return addGenericParameter(name, JavaSType.ofJava(javaExtendedType));
						}
						
						@Override
						public IMethodConstruction addGenericParameter(String name, ISType extendedType) {
							return addGenericParameterImpl(name, extendedType);
						}
						
						private IMethodConstruction addGenericParameterImpl(String name, ISType extendedType) {
							base.genericParameters.add(new SGenericParameter(name, extendedType));
							return this;
						}

						@Override
						public void setBody(SCode code) {
							base.body = code;
						}

						@Override
						public IMethodConstruction makeOverride() {
							base.isOverriding = true;
							return this;
						}
						
						@Override
						public IMethodConstruction setJavaDoc(SCode javaDoc) {
							base.javaDoc = javaDoc;
							return this;
						}
						
						@Override
						public IMethodConstruction makePrivate() {
							base.accessLevel = 0;
							return this;
						}
						
						@Override
						public IMethodConstruction makeProtected() {
							base.accessLevel = 1;
							return this;
						}
						
						@Override
						public IMethodConstruction makeAbstract() {
							base.isAbstract = true;
							return this;
						}
					};
				}
				
				@Override
				public IMethodConstruction addConstructor() {
					SConstructor constructor = new SConstructor();
					constructors.add(constructor);
					return constructionFor(constructor);
				}
				
				@Override
				public IMethodConstruction addMethod(ISType returnType, String name) {
					SMethod method = new SMethod(returnType, name);
					methods.add(method);
					return constructionFor(method);
				}
				
				@Override
				public IMethodConstruction addMethod(Class<?> javaType, String name) {
					return addMethod(JavaSType.ofJava(javaType), name);
				}
				
				@Override
				public IMethodConstruction addStaticMethod(Class<?> javaType, String name) {
					return addStaticMethod(JavaSType.ofJava(javaType), name);
				}
				
				@Override
				public IMethodConstruction addStaticMethod(ISType returnType, String name) {
					SMethod method = new SMethod(returnType, name);
					method.isStatic = true;
					methods.add(method);
					return constructionFor(method);
				}
				
				@Override
				public ISClassConstruction addGenericParameter(String name) {
					genericParameters.add(new SGenericParameter(name, null));
					return this;
				}
				
				@Override
				public ISClassConstruction addGenericParameter(String name, Class<?> javaExtendedType) {
					return addGenericParameter(name, JavaSType.ofJava(javaExtendedType));
				}
				
				@Override
				public ISClassConstruction addGenericParameter(String name, ISType extendedType) {
					genericParameters.add(new SGenericParameter(name, extendedType));
					return this;
				}
			});
			for(ISTypeDeclaration nested : nestedTypes) {
				nested.compile(null); //TODO send from parent?
			}
			compiled = true;
		}

		@Override
		public List<ISInterface> getImplementedInterfaces() {
			verifyCompiled();
			return Collections.unmodifiableList(implementedInterfaces);
		}
		
		@Override
		public List<SMethodHead> getMethodHeads() {
			verifyCompiled();
			List<SMethodHead> methodHeads = new ArrayList<>();
			for(SMethod method : methods) {
				methodHeads.add(method.getMethodHead(DeclaredClass.this));
			}
			return Collections.unmodifiableList(methodHeads);
		}

		private void verifyCompiled() {
			if(!compiled) {
				throw new IllegalStateException("Class not yet compiled");
			}
		}
		
		@Override
		public void TEMP_collectDeclarationImports(ISTypeImportCollector collector) {
			super.TEMP_collectDeclarationImports(collector);
			verifyCompiled();
			InternalUtil.collectFromList(genericParameters, collector);
			if(superClass != null) {
				collector.add(superClass);
			}
			InternalUtil.addAll(implementedInterfaces, collector);
			InternalUtil.collectFromList(fields, collector);
			InternalUtil.collectFromList(methods, collector);
			InternalUtil.collectFromList(constructors, collector);
			for(ISTypeDeclaration typeDeclaration : nestedTypes) {
				typeDeclaration.visitImportContributors(collector);
			}
		}
		
		private void renderMethod(SMethod method, IAliasResolver aliasResolver, ILineWriter lineWriter) {
			RenderUtil.printJavaDoc(lineWriter, aliasResolver, method.javaDoc);
			if(method.isOverriding) {
				lineWriter.println("@Override");
			}
			RenderUtil.renderAccessLevel(lineWriter, method.accessLevel);
			if(method.isStatic) {
				lineWriter.print("static ");
			}
			if(method.isAbstract) {
				lineWriter.print("abstract ");
			}
			if(!method.genericParameters.isEmpty()) {
				RenderUtil.renderGenericParameters(method.genericParameters, aliasResolver, lineWriter);
				lineWriter.print(" ");
			}
			lineWriter.print(aliasResolver.getAlias(method.returnType)).print(" ");
			lineWriter.print(method.name).print("(");
			RenderUtil.renderParameters(lineWriter, aliasResolver, method.parameters);
			if(method.isAbstract) {
				lineWriter.println(");");
			} else {
				lineWriter.println(") {");
				lineWriter.indent();
				if(method.body != null) {
					method.body.render(lineWriter, aliasResolver);
				}
				lineWriter.unindent();
				lineWriter.println("}");
			}
			lineWriter.println();
		}
		
		protected int getAccessLevel() {
			return accessLevel;
		}
		
		protected void renderKeywordStaticMaybe(ILineWriter lineWriter) {
		}
		
		@Override
		public void renderDefinition(String simpleTypeName, IAliasResolver aliasResolver, ILineWriter lineWriter) {
			verifyCompiled();
			RenderUtil.renderAccessLevel(lineWriter, getAccessLevel());
			renderKeywordStaticMaybe(lineWriter);
			if(isFinal) {
				lineWriter.print("final ");
			}
			if(isAbstract) {
				lineWriter.print("abstract ");
			}
			lineWriter.print("class ").print(simpleTypeName);
			if(!genericParameters.isEmpty()) {
				RenderUtil.renderGenericParameters(genericParameters, aliasResolver, lineWriter);
			}
			if(superClass != null) {
				lineWriter.print(" extends ").print(aliasResolver.getAlias(superClass));
			}
			if(!implementedInterfaces.isEmpty()) {
				lineWriter.print(" implements ");
				boolean first = true;
				for(ISInterface sInterface : implementedInterfaces) {
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
				for(SField field : fields) {
					RenderUtil.renderAccessLevel(lineWriter, field.accessLevel);
					if(field.isStatic) {
						lineWriter.print("static ");
					}
					if(field.isFinal) {
						lineWriter.print("final ");
					}
					lineWriter.print(aliasResolver.getAlias(field.type)).print(" ").print(field.name);
					if(field.initialValue != null) {
						lineWriter.print(" = ");
						field.initialValue.render(lineWriter, aliasResolver);
					}
					lineWriter.println(";");						
				}
				lineWriter.println();
				for(SConstructor constructor : constructors) {
					RenderUtil.printJavaDoc(lineWriter, aliasResolver, constructor.javaDoc);
					RenderUtil.renderAccessLevel(lineWriter, constructor.accessLevel);
					lineWriter.print(simpleTypeName).print("(");
					RenderUtil.renderParameters(lineWriter, aliasResolver, constructor.parameters);
					lineWriter.println(") {");
						if(constructor.body != null) {
							lineWriter.indent();
							constructor.body.render(lineWriter, aliasResolver);
							lineWriter.unindent();
						}
					lineWriter.println("}");
					lineWriter.println();
				}
				lineWriter.println();
				List<SMethod> staticMethods = new ArrayList<>();
				for(SMethod method : methods) {
					if(method.isStatic) {
						staticMethods.add(method);
						continue;
					}
					renderMethod(method, aliasResolver, lineWriter);
				}
				if(!staticMethods.isEmpty()) {
					lineWriter.println();
					for(SMethod method : staticMethods) {
						renderMethod(method, aliasResolver, lineWriter);
					}
				}
				
				if(!nestedTypes.isEmpty()) {
					lineWriter.println();
					for(ISTypeDeclaration nestedDeclaration : nestedTypes) {
						ISType nested = nestedDeclaration.getDeclared();
						nested.renderDefinition(nestedDeclaration.getSimpleTypeName(), aliasResolver, lineWriter);
						lineWriter.println();
					}
				}
				
				lineWriter.unindent();
			lineWriter.print("}");
		}
		
		@Override
		public boolean isReferenceType() {
			return true;
		}
	}
	
	public static SClass Declared() {
		return new DeclaredClass();
	}
	
	public static SClass DeclaredLocal(String name) {
		return new DeclaredLocalClass(name);
	}
	
	private static class DeclaredLocalClass extends DeclaredClass implements ISelfAliasing {
		private final String name;
		
		public DeclaredLocalClass(String name) {
			this.name = name;
		}

		@Override
		public String getAlias(IAliasResolver aliasResolver) {
			return name;
		}
		
		@Override
		protected int getAccessLevel() {
			return 2;
		}
	}
	
	public static SClass DeclaredNested(ISType parentType, String name, boolean isStatic) {
		return new DeclaredNestedClass(parentType, name, isStatic);
	}
	
	private static class DeclaredNestedClass extends DeclaredClass implements ISelfAliasing {
		private final ISType parentType;
		private final String name;
		private final boolean isStatic;
		
		public DeclaredNestedClass(ISType parentType, String name, boolean isStatic) {
			this.parentType = parentType;
			this.name = name;
			this.isStatic = isStatic;
		}
		
		@Override
		protected void renderKeywordStaticMaybe(ILineWriter lineWriter) {
			if(isStatic) {
				lineWriter.print("static ");
			}
		}

		@Override
		public String getAlias(IAliasResolver aliasResolver) {
			return aliasResolver.getAlias(parentType)+"."+name;
		}
	}
	
	public static ISClass JavaClass(Class<?> type) {
		if(type.isInterface()) {
			throw new IllegalArgumentException(type.getName()+" is an interface");
		} else {
			return new JavaSClass(type);
		}
	}
	
	public static class JavaSClass extends JavaSType implements ISClass {
		private JavaSClass(Class<?> javaClass) {
			super(javaClass);
		}
		
		@Override
		public List<ISInterface> getImplementedInterfaces() {
			List<ISInterface> implementedInterfaces = new ArrayList<>();
			for(Class<?> face : getJavaType().getInterfaces()) {
				implementedInterfaces.add(SInterface.JavaInterface(face));
			}
			return implementedInterfaces;
		}
	}
	
	public static class JavaPrimitive extends SClass {
		public static final JavaPrimitive INT = new JavaPrimitive("int");
		public static final JavaPrimitive SHORT = new JavaPrimitive("short");
		public static final JavaPrimitive BYTE = new JavaPrimitive("byte");
		public static final JavaPrimitive FLOAT = new JavaPrimitive("float");
		public static final JavaPrimitive DOUBLE = new JavaPrimitive("double");
		public static final JavaPrimitive LONG = new JavaPrimitive("long");
		public static final JavaPrimitive CHAR = new JavaPrimitive("char");
		public static final JavaPrimitive BOOLEAN  = new JavaPrimitive("boolean");
		public static final JavaPrimitive VOID  = new JavaPrimitive("void");
		
		private String name;
		
		private JavaPrimitive(String name) {
			this.name = name;
		}
		
		@Override
		public List<ISInterface> getImplementedInterfaces() {
			return Collections.emptyList();
		}
		
		public String getPrimitiveName() {
			return name;
		}
		
		public static JavaPrimitive getPrimitive(Class<?> javaType) {
			if(!javaType.isPrimitive()) {
				throw new IllegalArgumentException(javaType.getName()+" is not primitive.");
			}
			String canonicalName = javaType.getCanonicalName();
			if("void".equals(canonicalName)) {
				return VOID;
			} else if("int".equals(canonicalName)) {
				return INT;
			} else if("short".equals(canonicalName)) {
				return SHORT;
			} else if("byte".equals(canonicalName)) {
				return BYTE;
			} else if("float".equals(canonicalName)) {
				return FLOAT;
			} else if("double".equals(canonicalName)) {
				return DOUBLE;
			} else if("long".equals(canonicalName)) {
				return LONG;
			} else if("char".equals(canonicalName)) {
				return CHAR;
			} else if("boolean".equals(canonicalName)) {
				return BOOLEAN;
			} else {
				throw new RuntimeException("Unknown java primitive "+canonicalName);
			}
		}

		@Override
		public List<SMethodHead> getMethodHeads() {
			return Collections.emptyList();
		}

		public static <R extends Collection<? super JavaPrimitive>> R allPrimitives(R result) {
			result.add(INT);
			result.add(SHORT);
			result.add(BYTE);
			result.add(FLOAT);
			result.add(DOUBLE);
			result.add(LONG);
			result.add(CHAR);
			result.add(BOOLEAN);
			return result;
		}

		public ISClass getBoxed() {
			if(this == JavaPrimitive.VOID) {
				throw new UnsupportedOperationException();
			}
			if(this == JavaPrimitive.INT) {
				return JavaClass(Integer.class);
			} else if(this == JavaPrimitive.SHORT) {
				return JavaClass(Short.class);
			} else if(this == JavaPrimitive.BYTE) {
				return JavaClass(Byte.class);
			} else if(this == JavaPrimitive.FLOAT) {
				return JavaClass(Float.class);
			} else if(this == JavaPrimitive.DOUBLE) {
				return JavaClass(Double.class);
			} else if(this == JavaPrimitive.LONG) {
				return JavaClass(Long.class);
			} else if(this == JavaPrimitive.CHAR) {
				return JavaClass(Character.class);
			} else if(this == JavaPrimitive.BOOLEAN) {
				return JavaClass(Boolean.class);
			}
			return null;
		}
		
		@Override
		public ISClass asSClass(boolean nullForbidden) {
			return this;
		}
		
		@Override
		public ISInterface asSInterface(boolean nullForbidden) {
			return InternalUtil.asType(ISInterface.class, this, nullForbidden);
		}
		
		@Override
		public boolean isReferenceType() {
			return false;
		}
	}
	
	private static class SField implements ISTypeImportContributor {
		private boolean isStatic = false;
		private boolean isFinal = false;
		private int accessLevel = 0;
		private final ISType type;
		private final String name;
		private SCode initialValue = null;

		public SField(ISType type, String name) {
			this.type = type;
			this.name = name;
		}

		@Override
		public void collectImports(ISTypeImportCollector collector) {
			collector.add(type);
			if(initialValue != null) {
				initialValue.collectImports(collector);
			}
		}
	}
	
	private static class SMethodConstructorBase implements ISTypeImportContributor {
		protected SCode javaDoc = null;
		protected int accessLevel = 3;
		protected boolean isOverriding = false;
		protected SCode body = null;
		protected List<SParameter> parameters = new ArrayList<>();
		protected List<SGenericParameter> genericParameters = new ArrayList<>();
		protected boolean isAbstract = false;
		
		public void addParameter(ISType type, String parameterName) {
			parameters.add(new SParameter(type, parameterName));
		}

		@Override
		public void collectImports(ISTypeImportCollector collector) {
			if(body != null) {
				body.collectImports(collector);
			}
			InternalUtil.collectFromList(parameters, collector);
			InternalUtil.collectFromList(genericParameters, collector);
		}
		
	}
	
	private static class SMethod extends SMethodConstructorBase {
		private boolean isStatic = false;
		private final ISType returnType;
		private final String name;
		
		public SMethod(ISType returnType, String name) {
			this.returnType = returnType;
			this.name = name;
		}


		public SMethodHead getMethodHead(ISType owner) {
			return new SMethodHead(owner, genericParameters, returnType, name, parameters, isOverriding, javaDoc);
		}
		
		public void collectImports(ISTypeImportCollector collector) {
			super.collectImports(collector);
			collector.add(returnType);
		}
	}
	
	private static class SConstructor extends SMethodConstructorBase {
	}

	public static ISClass array(ISType type) {
		return new SArrayType(type);
	}
	
	public static ISClass array(Class<?> javaType) {
		return array(JavaSType.ofJava(javaType));
	}
	
	@Override
	public ISClass asSClass(boolean nullForbidden) {
		return this;
	}
	
	@Override
	public ISInterface asSInterface(boolean nullForbidden) {
		return InternalUtil.asType(ISInterface.class, getClass().getName(), nullForbidden);
	}
}
