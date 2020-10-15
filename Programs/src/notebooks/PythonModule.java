package notebooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonModule {
	// Name of single module (or function)
	final static public String IDENTIFIER = "[\\p{L}\\p{Nl}_][\\p{L}\\p{Nl}\\p{Nd}\\p{Mn}\\p{Pc}]*";
	// Module, possibly with submodule(s)
	final static public String SUB_MODULE_IDENTIFIER = IDENTIFIER + "\\s*(\\.\\s*" + IDENTIFIER + "\\s*)*";
	
	protected final String name;
	protected final ImportType importedWith;
	protected final String alias;
	protected PythonModule parent;
	protected final Map<String, Integer> functionUsages;
	
	public PythonModule(String name) {
		this(name, null, ImportType.ORDINARY, null);
	}
	
	public PythonModule(String name, ImportType importedWith) {
		this(name, null, importedWith, null);
	}
	
	public PythonModule(String name, ImportType importedWith, PythonModule parent) {
		this(name, null, importedWith, parent);
	}
	
	public PythonModule(String name, String alias, ImportType importedWith) {
		this(name, alias, importedWith, null);
	}
	
	public PythonModule(String name, String alias, ImportType importedWith, PythonModule parent) {
		if (ImportType.ALIAS == importedWith && null == alias) {
			System.err.println("Warning! You are creating a Python module imported with alias, but you are not providing an alias!");
		}
		if (ImportType.ALIAS != importedWith && null != alias) {
			System.err.println("Warning! You are providing an alias for a module that is not imported with alias!");
		}
		this.name = name;
		this.alias = alias;
		this.importedWith = importedWith;
		this.parent = parent;
		this.functionUsages = new HashMap<String, Integer>();
	}
	
	@Override
	public boolean equals(Object other) {
		if (null == other || !(other instanceof PythonModule)) {
			return false;
		}
		PythonModule otherModule = (PythonModule) other;
		if (null == alias) {
			if (null != otherModule.alias) {
				return false;
			}
		} else if (!this.alias.equals(otherModule.alias)) {
			return false;
		}
		if (null == parent) {
			if (null != otherModule.parent) {
				return false;
			}
		} else if (!this.parent.equals(otherModule.parent)) {
			return false;
		}
		if (null == importedWith) {
			if (null != otherModule.importedWith) {
				return false;
			}
		} else if (!this.importedWith.equals(otherModule.importedWith)) {
			return false;
		}
		return this.name.equals(otherModule.name);
	}
	
	@Override
	public int hashCode() {
		int result = Objects.hash(name, alias);
		if (null != parent) {
			result *= parent.hashCode();
		}
		return result;
	}
	
	public ImportType importedWith() {
		return importedWith;
	}
	
	/**
	 * Tells whether two modules represent the same module, that is have the
	 * same name and parents (on all levels) with the same names.
	 * @return true if other represents the same module as the current module, false otherwise
	 */
	public boolean is(PythonModule other) {
		if (null == other) {
			return false;
		}
		return other.sameAs(this);
		// Because we want to call sameAs in the subtype if other is a subtype instance.
	}
	
	/**
	 * Merge the function usage map of the module given as argument into the
	 * function usage map of this module: Add entries whose keys (function
	 * names) are missing. When a key is already stored in functionUsages,
	 * store the sum of the entry values in the two maps. Note that merging is
	 * only done if the modules represent the same Python module (that is have
	 * the same names and the same ancestors).
	 */
	public void merge(PythonModule other) {
		if(other.is(this)) {
			Map<String, Integer> usagesToAdd = other.functionUsages;
			for (Entry<String, Integer> usage: usagesToAdd.entrySet()) {
				Utils.addOrIncrease(functionUsages, usage.getKey(), usage.getValue());
			}
		}
	}
	
	/**
	 * Create and return a string containing the module name preceded by all
	 * ancestors' names, in descending order, separated with ".".
	 * @return The string described above
	 */
	public String pedigreeString() {
		String result = "";
		if (null != parent) {
			result += parent.pedigreeString() + ".";
		}
		result += name;
		return result;
	}
	
	/**
	 * If the line given as argument contains a usage of this module, register
	 * the function called.
	 * @param line Code line to check for usage
	 */
	public void registerUsage(String line) {
		// Usages of functions located in an imported module
		Pattern usagePattern = Pattern.compile(	// TODO: Metod för uttryck?
				"(?<!\\.)" + this.qualifier() + "\\s*\\.\\s*(" + IDENTIFIER + ")\\s*\\(");
		Matcher usageMatcher = usagePattern.matcher(line);
		while (usageMatcher.find()) {
			Utils.addOrIncrease(functionUsages, usageMatcher.group(1));
		}
		
		// Usages of imported function (only possible with from imports)
		if (null != parent && ImportType.FROM == parent.importedWith) {
			usagePattern = Pattern.compile("(?<!\\.)" + this.qualifier() + "\\s*\\(");
			usageMatcher = usagePattern.matcher(line);
			while (usageMatcher.find()) {
				Utils.addOrIncrease(functionUsages, name);
			}
		}
	}
	
	/**
	 * @param functionName Function for which to count calls
	 * @param line Code line to look for calls in
	 * @return A list of calls to functionName in line
	 */
	public List<String> callsTo(String functionName, String line) {
		List<String> result = new ArrayList<String>(1);
		// Usages of functions located in an imported module
		Pattern usagePattern = Pattern.compile(
				"(?<!\\.)" + this.qualifier() + "\\s*\\.\\s*(" + functionName + ")\\s*\\(");
		Matcher usageMatcher = usagePattern.matcher(line);
		result.addAll(extractFunctionCall(usageMatcher, line));
		
		// Usages of imported function (only possible with from imports)
		if (null != parent && ImportType.FROM == parent.importedWith && functionName.equals(this.qualifier())) {
			usagePattern = Pattern.compile("(?<!\\.)" + this.qualifier() + "\\s*\\(");
			usageMatcher = usagePattern.matcher(line);
			result.addAll(extractFunctionCall(usageMatcher, line));
		}
		
		return result;
	}

	private List<String> extractFunctionCall(Matcher usageMatcher, String line) {
		List<String> result = new ArrayList<String>(1);	// Most of the times, there will only be 1 call/line(?)
		// Comments are removed, but strings may exist
		while (usageMatcher.find()) {
			boolean bracketFound = false;
			boolean escaped = false;
			int bracketLevel = 0;
			boolean inDoubleQuoteString = false;
			boolean inSingleQuoteString = false;
			int index = usageMatcher.start();
			String call = "";
			while (!bracketFound || 0 != bracketLevel) {
				boolean inString = inDoubleQuoteString || inSingleQuoteString;
				char currentChar = line.charAt(index);
				call += currentChar;
				if (!escaped && '"' == currentChar) {
					inDoubleQuoteString = !inDoubleQuoteString;
				} else if (!escaped && '\'' == currentChar) {
					inSingleQuoteString = !inSingleQuoteString;
				} else if (!inString && '(' == currentChar) {
					bracketFound = true;
					bracketLevel++;
				} else if (!inString && ')' == currentChar) {
					bracketLevel--;
				}
				if ('\\' == currentChar) {
					escaped = !escaped;
				} else {
					escaped = false;
				}
				index++;
			}
			result.add(call);
		}
		return result;
	}
	
	public void setOldestAncestor(PythonModule ancestor) {
		if (null == parent) {
			this.parent = ancestor;
		} else {
			parent.setOldestAncestor(ancestor);
		}
	}
	
	@Override
	public String toString() {
		return pedigreeString() + "(" + qualifier() + ")";
	}
	
	/*
	 * Helper method for is.
	 */
	protected boolean sameAs(PythonModule other) {
		if (null == parent) {
			return null == other.parent && this.name.equals(other.name);
		} else {
			return this.name.equals(other.name) && this.parent.is(other.parent);
		}
	}
	
	protected String qualifier() {
		if (ImportType.ALIAS == this.importedWith) {
			return alias;
		} else if (ImportType.FROM == this.importedWith) {
			return "";
		} else if (ImportType.ORDINARY == this.importedWith) {
			String qualifier = "";
			if (null != parent) {
				qualifier += parent.qualifier();
			}
			if (!"".equals(qualifier)) {
				qualifier += ".";
			}
			return qualifier + this.name;
		} else {
			System.err.println("Unknown import type " + this.importedWith + ". No qualifier will be returned!");
			return "";
		}
	}
}
