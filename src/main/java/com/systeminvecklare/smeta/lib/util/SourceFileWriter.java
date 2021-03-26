package com.systeminvecklare.smeta.lib.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import com.mattiasselin.linewriter.ILineWriter;
import com.mattiasselin.linewriter.LineWriter;
import com.mattiasselin.linewriter.SystemOutLineWriter;
import com.mattiasselin.linewriter.WriterLineWriter;
import com.systeminvecklare.smeta.lib.ISourceFile;
import com.systeminvecklare.smeta.lib.ISourceFileWriter;

public abstract class SourceFileWriter implements ISourceFileWriter {
	private final File sourceRootDir;
	public boolean log =  false; 
	private String tab = "\t";
	
	public SourceFileWriter(File rootDir) {
		this.sourceRootDir = rootDir;
	}
	
	public void setTab(String tab) {
		this.tab = tab;
	}

	@Override
	public ISourceFile createFile(final String fullPath) {
		return new ISourceFile() {
			private final LineWriter lineWriter = new LineWriter() {

				@Override
				protected String getIndention() {
					return tab;
				}
			};
			
			@Override
			public ILineWriter getSourceWriter() {
				return lineWriter;
			}
			
			@Override
			public void complete() {
				lineWriter.println();
				if(log) {
					System.out.println(fullPath+":");
					lineWriter.writeTo(new SystemOutLineWriter());
				}
				if(shouldCreate(fullPath)) {
					File file = new File(sourceRootDir,fullPath);
					if(file.exists()) {
						if(!overwriteAllowed(fullPath)) {
							return;
						}
						file.delete();
					}
					file.getParentFile().mkdirs();
					try(FileOutputStream fis = new FileOutputStream(file)) {
						lineWriter.writeTo(new WriterLineWriter(new OutputStreamWriter(fis, StandardCharsets.UTF_8), true));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if(log) {
						System.out.println(">>>>>> WROTE TO "+file.getAbsolutePath());
					}
				}
			}
		};
	}

	protected abstract boolean overwriteAllowed(String fullPath);

	protected abstract boolean shouldCreate(String fullPath);
}
