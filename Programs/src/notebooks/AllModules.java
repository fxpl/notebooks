package notebooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class AllModules extends PythonModule {
	
	private static Map<String, String[]> functionsInModules = new HashMap<String, String[]>();
	
	public AllModules(PythonModule parent) {
		super("*", null, ImportType.ORDINARY, parent);
	}
	
	@Override
	public String toString() {
		/* The import type is obvious and doesn't need to be considered.
		   Hence, the pedigree string contains all information needed. */
		return pedigreeString() + ".*";
	}
	
	@Override
	public boolean is(PythonModule other) {
		return this.sameAs(other);
	}
	
	@Override
	public boolean sameAs(PythonModule other) {
		if (null == other) {
			return false;
		}
		if (other instanceof AllModules) {
			return other.parent.is(this.parent);
		} else {
			return other.is(this.parent);
		}
	}
	
	@Override
	public String pedigreeString() {
		return parent.pedigreeString();
		// If there is not a parent, something is terribly wrong!
	}
	
	@Override
	public void registerUsage(String line) {
		storeFunctionsIfNotDone();
		String parentModule = parent.pedigreeString();
		if (functionsInModules.containsKey(parentModule)) {
			// Functions stored successfully
			storeFunctionUsages(line);
		}
	}
	
	/**
	 * @param functionName Function for which to count calls
	 * @param line Code line to look for calls in
	 * @return A list of calls to functionName in line
	 */
	@Override
	public void registerCalls(String functionName, String line) throws NotebookException {
		storeFunctionsIfNotDone();
		String[] functionsInModule = functionsInModules.get(parent.pedigreeString());
		for (String function: functionsInModule) {
			if (function.equals(functionName)) {
				Matcher usageMatcher = functionCallMatcher(functionName, line);
				functionCalls.addAll(extractFunctionCalls(usageMatcher, line));
			}
		}
	}
	
	private void storeFunctionsIfNotDone() {
		String parentModule = parent.pedigreeString();
		if (!functionsInModules.containsKey(parentModule)) {
			String scriptPath = locateFileInClassPath("importscript.py");
			storeFunctionsFor(parentModule, scriptPath);
		}
	}

	private String locateFileInClassPath(String fileName) {
		return getClass().getClassLoader().getResource(fileName).getPath();
	}

	/**
	 * If the line given as argument contains a usage of this module, register
	 * the function called.
	 * @param line
	 */
	private void storeFunctionUsages(String line) {
		String[] functionsInModule = functionsInModules.get(parent.pedigreeString());
		for (String function: functionsInModule) {
			Matcher usageMatcher = functionCallMatcher(function, line);
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
	private static void storeFunctionsFor(String module, String scriptPath) {
		ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath, module);
		try {
			Process process = processBuilder.start();
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String errorOutput = errorReader.readLine();
			errorReader.close();
			if (null != errorOutput) {
				// TODO: Finns det något testfall som kan täcka den här branchen?
				System.err.println("An error occurred when executing the Python script "
						+ scriptPath + " for module " + module + ": " + errorOutput);
			}
			BufferedReader pythonOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String pythonOutput = pythonOutputReader.readLine();
			pythonOutputReader.close();
			if (null == pythonOutput) {
				System.err.println("Module " + module + " is not available. Some function calls for this module will not be identified.");
				functionsInModules.put(module, new String[0]);
			} else {
				String[] functions = pythonOutput.split(" ");
				functionsInModules.put(module, functions);
			}
		} catch (IOException e) {
			// TODO: Finns det något testfall som kan täcka den här branchen?
			System.err.println("Couldn't get functions for module " + module + ": " + e.getMessage());
		}
	}
}