package notebooks;

import java.util.Objects;

public class PythonModule {
	private String name;
	private ImportType importedWith;
	private String alias;
	private PythonModule parent;
	
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
		this.name = name;
		this.alias = alias;
		this.importedWith = importedWith;
		this.parent = parent;
	}
	
	public String alias() {
		return this.alias;
	}
	
	public String getName() {
		return this.name;
	}
	
	public PythonModule getParent() {
		return this.parent;
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
}
