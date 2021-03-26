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
					.print("return ").type(String.class).println(".valueOf(value);")
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
					System.out.println();
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
outputs
```
-----------------------------------
-----------------------------------
my/great/packagename/IStringConverter.java:

package my.great.packagename;

import java.lang.String;

public interface IStringConverter {
    String intToString(int value);
    String shortToString(short value);
    String byteToString(byte value);
    String floatToString(float value);
    String doubleToString(double value);
    String longToString(long value);
    String charToString(char value);
    String booleanToString(boolean value);
}
-----------------------------------
-----------------------------------
my/great/packagename/StringConverter.java:

package my.great.packagename;

import java.lang.String;

public class StringConverter implements IStringConverter {


    @Override
    public String intToString(int value) {
        return String.valueOf(value);
    }

    @Override
    public String shortToString(short value) {
        return String.valueOf(value);
    }

    @Override
    public String byteToString(byte value) {
        return String.valueOf(value);
    }

    @Override
    public String floatToString(float value) {
        return String.valueOf(value);
    }

    @Override
    public String doubleToString(double value) {
        return String.valueOf(value);
    }

    @Override
    public String longToString(long value) {
        return String.valueOf(value);
    }

    @Override
    public String charToString(char value) {
        return String.valueOf(value);
    }

    @Override
    public String booleanToString(boolean value) {
        return String.valueOf(value);
    }

}
-----------------------------------
```
