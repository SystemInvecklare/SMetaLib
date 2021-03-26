package com.systeminvecklare.smeta.lib;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mattiasselin.linewriter.BufferingLineWriter;
import com.mattiasselin.linewriter.ILineWriter;
import com.mattiasselin.linewriter.WriterLineWriter;

public class SCode implements ISTypeImportContributor {
	private final List<ICodePart> parts = new ArrayList<ICodePart>();
	private final Map<String, ICodePart> replacements = new HashMap<String, ICodePart>();
	private final ITokenReplacer tokenReplacer = new MapTokenReplacer(replacements);
	
	public SCode print(String text) {
		getEndLineWriter().print(text);
		return this;
	}
	
	public SCode println(String text) {
		getEndLineWriter().println(text);
		return this;
	}
	
	public SCode println() {
		getEndLineWriter().println();
		return this;
	}
	
	public SCode print(SCode code) {
		parts.add(new SCodeCodePart(code));
		return this;
	}
	
	public SCode println(SCode code) {
		print(code);
		return println();
	}
	
	public SCode indent() {
		getEndLineWriter().indent();
		return this;
	}
	
	public SCode unindent() {
		getEndLineWriter().unindent();
		return this;
	}
	

	public SCode type(Class<?> javaType) {
		return type(JavaSType.ofJava(javaType));
	}
	
	public SCode type(ISType sType) {
		parts.add(new STypeCodePart(sType));
		return this;
	}
	
	public SCode typeln(Class<?> javaType) {
		return typeln(JavaSType.ofJava(javaType));
	}
	
	public SCode typeln(ISType sType) {
		type(sType);
		return println();
	}
	
	private ILineWriter getEndLineWriter() {
		return getEndLineWriter(parts);
	}
	
	private static ILineWriter getEndLineWriter(List<ICodePart> parts) {
		if(parts.isEmpty() || !(parts.get(parts.size()-1) instanceof StringCodePart)) {
			StringCodePart codePart = new StringCodePart();
			parts.add(codePart);
			return codePart.lineWriter;
		} else {
			return ((StringCodePart) parts.get(parts.size()-1)).lineWriter;
		}
	}
	
	@Override
	public void collectImports(ISTypeImportCollector collector) {
		for(ICodePart part : parts) {
			part.collectImports(collector, tokenReplacer);
		}
	}


	public void render(ILineWriter lineWriter, IAliasResolver aliasResolver) {
		for(ICodePart codePart : parts) {
			codePart.render(lineWriter, aliasResolver, tokenReplacer);
		}
	}
	

	public SCode define(String name, Class<?> type) {
		return define(name, JavaSType.ofJava(type));
	}

	public SCode define(String name, ISType type) {
		setReplacement(name, new STypeCodePart(type));
		return this;
	}
	

	public SCode define(String name, String replacement) {
		StringCodePart stringCodePart = new StringCodePart();
		stringCodePart.lineWriter.print(replacement);
		setReplacement(name, stringCodePart);
		return this;
	}
	
	private void setReplacement(String name, ICodePart codePart) {
		if(replacements.put(name, codePart) != null) {
			throw new IllegalArgumentException("Duplicate token definition for "+name);
		}
	}

	private interface ICodePart {
		void collectImports(ISTypeImportCollector collector, ITokenReplacer tokenReplacer);
		void render(ILineWriter lineWriter, IAliasResolver aliasResolver, ITokenReplacer tokenReplacer);
	}
	
	private static class StringCodePart implements ICodePart {
		private static final Pattern TOKEN_PATTERN = Pattern.compile("\\$\\{(?<token>.*?)\\}");
		private final BufferingLineWriter lineWriter = new BufferingLineWriter();

		@Override
		public void render(ILineWriter target, IAliasResolver aliasResolver, ITokenReplacer tokenReplacer) {
			class ReplacingLineWriter extends BufferingLineWriter {
				public ReplacingLineWriter() {
				}
				
				private ReplacingLineWriter(IIdGenerator idGenerator, List<ICommand> bufferedCommands) {
					super(idGenerator, bufferedCommands);
				}
				
				private String replaceTokens(String text) {
					Matcher matcher = TOKEN_PATTERN.matcher(text);
					StringBuffer stringBuffer = new StringBuffer();
					while(matcher.find()) {
						ICodePart codePart = tokenReplacer.getTokenReplacement(matcher.group("token"));
						StringWriter stringWriter = new StringWriter();
						codePart.render(new WriterLineWriter(stringWriter, true), aliasResolver, tokenReplacer);
						matcher.appendReplacement(stringBuffer, stringWriter.getBuffer().toString());
					}
					matcher.appendTail(stringBuffer);
					return stringBuffer.toString();
				}
				
				@Override
				public ILineWriter print(String text) {
					return super.print(replaceTokens(text));
				}
				
				@Override
				public void println(String text) {
					super.println(replaceTokens(text));
				}
				
				@Override
				protected BufferingLineWriter newBufferingLineWriter(IIdGenerator idGenerator, List<ICommand> bufferedCommands) {
					return new ReplacingLineWriter(idGenerator, bufferedCommands);
				}
			}
			
			BufferingLineWriter writer = new ReplacingLineWriter();
			lineWriter.writeTo(writer);
			writer.writeTo(target);
		}

		@Override
		public void collectImports(ISTypeImportCollector collector, ITokenReplacer tokenReplacer) {
			class ImportingLineWriter extends BufferingLineWriter {
				public ImportingLineWriter() {
				}
				
				private ImportingLineWriter(IIdGenerator idGenerator, List<ICommand> bufferedCommands) {
					super(idGenerator, bufferedCommands);
				}
				
				private String importFromTokens(String text) {
					Matcher matcher = TOKEN_PATTERN.matcher(text);
					while(matcher.find()) {
						ICodePart codePart = tokenReplacer.getTokenReplacement(matcher.group("token"));
						codePart.collectImports(collector, tokenReplacer);
					}
					return text;
				}
				
				@Override
				public ILineWriter print(String text) {
					return super.print(importFromTokens(text));
				}
				
				@Override
				public void println(String text) {
					super.println(importFromTokens(text));
				}
				
				@Override
				protected BufferingLineWriter newBufferingLineWriter(IIdGenerator idGenerator, List<ICommand> bufferedCommands) {
					return new ImportingLineWriter(idGenerator, bufferedCommands);
				}
			}
			lineWriter.writeTo(new ImportingLineWriter());
		}
	}
	
	private static class STypeCodePart implements ICodePart {
		private final ISType sType;

		public STypeCodePart(ISType sType) {
			this.sType = sType;
		}

		@Override
		public void render(ILineWriter lineWriter, IAliasResolver aliasResolver, ITokenReplacer tokenReplacer) {
			lineWriter.print(aliasResolver.getAlias(sType));
		}

		@Override
		public void collectImports(ISTypeImportCollector collector, ITokenReplacer tokenReplacer) {
			collector.add(sType);
		}
	}
	
	private static class SCodeCodePart implements ICodePart {
		private final SCode code;

		public SCodeCodePart(SCode code) {
			this.code = code;
		}
		
		@Override
		public void collectImports(ISTypeImportCollector collector, ITokenReplacer tokenReplacer) {
			//TODO add support for multiple token replacers. We need to figure out how to resolve multiple layers
			code.collectImports(collector);
		}
		
		@Override
		public void render(ILineWriter lineWriter, IAliasResolver aliasResolver, ITokenReplacer tokenReplacer) {
			//TODO add support for multiple token replacers. We need to figure out how to resolve multiple layers
			code.render(lineWriter, aliasResolver);
		}
	}
	
	private static class LocalClassCodePart implements ICodePart {
		private final SLocalClassDeclaration localClass;
		
		public LocalClassCodePart(SLocalClassDeclaration localClass) {
			this.localClass = localClass;
		}

		@Override
		public void collectImports(ISTypeImportCollector collector, ITokenReplacer tokenReplacer) {
			makeSureCompiled();
			localClass.visitImportContributors(collector);
		}

		@Override
		public void render(ILineWriter lineWriter, IAliasResolver aliasResolver, ITokenReplacer tokenReplacer) {
			makeSureCompiled();
			localClass.getDeclared().renderDefinition(localClass.getSimpleTypeName(), aliasResolver, lineWriter);
			lineWriter.println();
		}

		private void makeSureCompiled() {
			if(!localClass.isCompiled()) {
				CompilationCycle adhocCompilationCycle = new CompilationCycle();
				localClass.compile(adhocCompilationCycle);
				adhocCompilationCycle.throwFatalIfAny();
			}
		}
	}
	
	public static SCode code() {
		return new SCode();
	}
	
	private interface ITokenReplacer {
		ICodePart getTokenReplacement(String tokenName);
		ICodePart getTokenReplacementOpt(String tokenName);
	}
	
	private static class MapTokenReplacer implements ITokenReplacer {
		private final Map<String, ICodePart> map;
		
		public MapTokenReplacer(Map<String, ICodePart> map) {
			this.map = map;
		}

		@Override
		public ICodePart getTokenReplacement(String tokenName) {
			ICodePart codePart = getTokenReplacementOpt(tokenName);
			if(codePart == null) {
				throw new IllegalArgumentException("No definition of token "+tokenName+" found");
			}
			return codePart;
		}

		@Override
		public ICodePart getTokenReplacementOpt(String tokenName) {
			return map.get(tokenName);
		}
	}

	public SClassDeclaration declareLocalClass(String className) {
		SLocalClassDeclaration localClass = new SLocalClassDeclaration(className);
		parts.add(new LocalClassCodePart(localClass));
		return localClass;
	}
}
