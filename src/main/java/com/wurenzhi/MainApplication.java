package com.wurenzhi;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class MainApplication {
	
	public static void main(String[] args) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		String[] ops = {"-processor", "com.wurenzhi.StrTemplateProcessor",
			"/workspace/string_template/src/main/java/com/wurenzhi/Person.java"
		};
		int compilationResult = compiler.run(null, null, null, ops);
	}
	
}