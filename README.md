# SMetaLib
Java code generation library

## Usage

```java
SProject project = new SProject("my.great.packagename");

SPackage pack = project.getRootPackage();

final SInterfaceDeclaration IStringConverter = pack.declareInterface("IStringConverter");
IStringConverter.define(new ISInterfaceDefinition() {
	@Override
	public void build(ISInterfaceConstruction construction) {
		for(JavaPrimitive primitive : SClass.JavaPrimitive.allPrimitives(new ArrayList<SClass.JavaPrimitive>())) {
			construction.addMethodHead(String.class, primitive.getPrimitiveName()+"ToString")
				.addParameter(primitive, "value");
		}
	}
});

SClassDeclaration StringConverterImpl = pack.declareClass("StringConverter");
StringConverterImpl.define(new ISClassDefinition() {
	@Override
	public void build(ISClassConstruction construction) {
		construction.implement(IStringConverter.getDeclared());
		for(JavaPrimitive primitive : SClass.JavaPrimitive.allPrimitives(new ArrayList<SClass.JavaPrimitive>())) {
			construction.addMethod(String.class, primitive.getPrimitiveName()+"ToString")
				.makeOverride()
				.addParameter(primitive, "value")
				.setBody(SCode.code()
					.println("return ").type(String.class).println(".valueOf(value);")
				);
		}
	}
});

project.compile();

if(dryRun) {
	project.buildFiles(new ISourceFileWriter() {
		@Override
		public ISourceFile createFile(String fullPath) {
			return new ISourceFile() {
				private LineWriter lineWriter = new LineWriter();
				@Override
				public ILineWriter getSourceWriter() {
					return lineWriter;
				}
				
				@Override
				public void complete() {
					System.out.println("-----------------------------------");
					System.out.println(fullPath+":");
					System.out.println();
					lineWriter.writeTo(new SystemOutLineWriter());
					System.out.println("-----------------------------------");
				}
			};
		}
	});
} else {
	project.buildFiles(new SourceFileWriter(outputRootDir) {
		
		@Override
		protected boolean shouldCreate(String fullPath) {
			return true;
		}
		
		@Override
		protected boolean overwriteAllowed(String fullPath) {
			return fullPath.startsWith("my.great.packagename");
		}
	});
}
```
