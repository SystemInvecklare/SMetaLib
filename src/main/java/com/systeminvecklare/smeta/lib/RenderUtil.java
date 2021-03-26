package com.systeminvecklare.smeta.lib;

import com.mattiasselin.linewriter.ILineWriter;
import java.util.List;
import java.lang.Iterable;
import java.lang.IllegalArgumentException;
import com.mattiasselin.linewriter.AbstractLineWriter;
import java.lang.String;

/*package-private*/ class RenderUtil {



	public static void renderParameters(ILineWriter lineWriter, IAliasResolver aliasResolver, List<SParameter> parameters) {
		boolean firstParam = true;
		for(SParameter parameter : parameters) {
			if(firstParam) {
				firstParam = false;
			} else {
				lineWriter.print(", ");
			}
			lineWriter.print(aliasResolver.getAlias(parameter.getType())).print(" ").print(parameter.getName());
		}
	}

	public static void renderGenericParameters(Iterable<? extends SGenericParameter> genericParams, IAliasResolver aliasResolver, ILineWriter lineWriter) {
		lineWriter.print("<");
		boolean first = true;
		for(SGenericParameter genericParameter : genericParams) {
			if(first) {
				first = false;
			} else {
				lineWriter.print(",");
			}
			genericParameter.render(aliasResolver, lineWriter);
		}
		lineWriter.print(">");
	}

	public static void renderAccessLevel(ILineWriter lineWriter, int accessLevel) {
		if(accessLevel == 0) {
			lineWriter.print("private ");
		} else if(accessLevel == 1) {
			lineWriter.print("protected ");
		} else if(accessLevel == 2) {
			lineWriter.print("/*package-private*/ ");
		} else if(accessLevel == 3) {
			lineWriter.print("public ");
		} else {
			throw new IllegalArgumentException("Unknown accesslevel "+accessLevel);
		}
	}

	public static void printJavaDoc(ILineWriter lineWriter, IAliasResolver aliasResolver, SCode javaDoc) {
		if(javaDoc != null) {
			lineWriter.println("/**");
			/*package-private*/ class JavaDocLineWriter extends AbstractLineWriter {
				private final ILineWriter wrapped;

				public JavaDocLineWriter(ILineWriter wrapped) {
					this(0, wrapped);
				}

				protected JavaDocLineWriter(int indent, ILineWriter wrapped) {
					super(indent);
					this.wrapped = wrapped;
				}


				@Override
				protected ILineWriter newIndented(int indent) {
					return new JavaDocLineWriter(indent, wrapped);
				}

				@Override
				protected void doPrintIndent(int indent) {
					wrapped.print("* ");
				}

				@Override
				protected void doPrint(String text) {
					wrapped.print(text);
				}

				@Override
				protected void doPrintln() {
					wrapped.println();
				}

			}
			javaDoc.render(new JavaDocLineWriter(lineWriter), aliasResolver);
			lineWriter.println("*/");
		}
	}

}
