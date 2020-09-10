package notebooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllModules extends PythonModule {
	
	private static Map<String, String[]> functionsInModules = new HashMap<String, String[]>();
	
	public AllModules(PythonModule parent) {
		super("*", null, ImportType.ORDINARY, parent);
	}
	
	@Override
	public String toString() {
		/* The import type is obvious and doesn't need to be considered.
		   Hence, the pedigree string contains all information needed. */
		return pedigreeString();
	}
	
	@Override
	public void registerUsage(String line) {
		String parentModule = parent.pedigreeString();
		if (!functionsInModules.containsKey(parentModule)) {
			storeFunctionsFor(parentModule);
		}
		if (functionsInModules.containsKey(parentModule)) {
			storeFunctionUsages(line);
		}
	}

	/**
	 * If the line given as argument contains a usage of this module, register
	 * the function called.
	 * @param line
	 */
	private void storeFunctionUsages(String line) {
		String[] functionsInModule = functionsInModules.get(parent.pedigreeString());
		for (String function: functionsInModule) {
			Pattern usagePattern = Pattern.compile("(?<!\\.)\\s*(" + function + ")\\s*\\(");
			Matcher usageMatcher = usagePattern.matcher(line);
			while (usageMatcher.find()) {
				Utils.addOrIncrease(functionUsages, usageMatcher.group(1));
			}
		}
	}

	/**
	 * Get all functions from the module given as an argument. Store them as an
	 * array in functionsInModules with the module as key.
	 * @param module Module for which we want functions
	 */
	private static void storeFunctionsFor(String module) {
		String scriptName = "importscript.py";	// TODO: Sökväg till den här!
		ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptName, module);
		try {
			Process process = processBuilder.start();
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String errorOutput = errorReader.readLine();
			errorReader.close();
			if (null != errorOutput) {
				// TODO: Finns det något testfall som kan täcka den här branchen?
				System.err.println("An error occurred when executing the Python script "
						+ scriptName + " for module " + module + ": " + errorOutput);
			}
			BufferedReader pythonOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String pythonOutput = pythonOutputReader.readLine();
			pythonOutputReader.close();
			String[] functions = null == pythonOutput ? new String[0] : pythonOutput.split(" ");
			functionsInModules.put(module, functions);
		} catch (IOException e) {
			// TODO: Finns det något testfall som kan täcka den här branchen?
			System.err.println("Couldn't get functions for module " + module + ": " + e.getMessage());
		}
	}
}