package notebooks;

import java.util.Objects;

public class PythonModule {
	private String name;
	private ImportType importedWith;
	/*private String alias = null;
	private List<String> methods;*/
	
	public PythonModule(String name, ImportType importedWith) {
		this.name = name;
		this.importedWith = importedWith;
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
		return this.name.equals(otherModule.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}
	
	public ImportType importedWith() {
		return importedWith;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
