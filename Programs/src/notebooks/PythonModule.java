package notebooks;

import java.util.Objects;

public class PythonModule {
	private String name;
	private ImportType importedWith;
	private String alias = null;
	//private List<String> methods;
	
	public PythonModule(String name, ImportType importedWith) {
		this(name, null, importedWith);
	}
	
	public PythonModule(String name, String alias, ImportType importedWith) {
		this.name = name;
		this.alias = alias;
		this.importedWith = importedWith;
	}
	
	public String alias() {
		return alias;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other.getClass() != this.getClass()) {
			return false;
		}
		PythonModule otherModule = (PythonModule) other;
		if (null == alias) {
			if (null != otherModule.alias) {
				return false;
			}
		} else {
			if (!this.alias.equals(otherModule.alias)) {
				return false;
			}
		}
		return this.name.equals(otherModule.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, alias);
	}
	
	public ImportType importedWith() {
		return importedWith;
	}
	
	@Override
	public String toString() {
		String result = name;
		if (null != alias) {
			result += "(" + alias + ")";
		}
		return result;
	}
}
