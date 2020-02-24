package notebooks;

import java.util.Objects;

public class PythonModule {
	private String name;
	/*private String alias = null;
	private List<String> methods;*/
	
	public PythonModule(String name) {
		this.name = name;
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
}
