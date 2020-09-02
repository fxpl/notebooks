package notebooks;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonModule {
	final static public String IDENTIFIER = "[A-Za-z_][A-Za-z0-9_\\.]*";
	final static private String ARGUMENTIDENTIFIER = "(\\\"|\\\')?[A-Za-z0-9_\\\\.]*(\\\"|\\\')?";
	final static private String ARGUMENTLIST = "(\\s*" + ARGUMENTIDENTIFIER + "\\s*\\,\\s*)*" + ARGUMENTIDENTIFIER + "?\\s*";
	
	protected String name;
	protected ImportType importedWith;
	protected String alias;
	protected PythonModule parent;
	protected Map<String, Integer> functionUsages;
	
	public PythonModule(String name) {
		this(name, null, null, null);
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
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public boolean equals(Object other) {
		if (null == other || other.getClass() != this.getClass()) {
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
		if (null == parent) {
			return null == other.parent && this.name.equals(other.name);
		} else {
			return this.name.equals(other.name) && this.parent.is(other.parent);
		}
	}
	
	/**
	 * If the line given as argument contains a usage of this module, register
	 * the function called.
	 * @param line Code line to check for usage
	 */
	public void registerUsage(String line) {
		// TODO: Ta höjd för nästlade uttryck och uttryck i listor/arrayer!
		Pattern usagePattern = Pattern.compile(
				"(\\s*" + IDENTIFIER + "\\s*\\=\\s*)?" + this.qualifier() + "\\s*\\.\\s*(" + IDENTIFIER + ")\\s*\\(" + ARGUMENTLIST + "\\).*");
		Matcher usageMatcher = usagePattern.matcher(line);
		if (usageMatcher.matches()) {
			Utils.addOrIncrease(functionUsages, usageMatcher.group(2));
		}
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
		String result = "";
		if (null != parent) {
			result += parent.toString() + ".";
		}
		result += name;
		if (null != alias) {
			result += "(" + alias + ")";
		}
		return result;
	}
	
	private String qualifier() {
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
