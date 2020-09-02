package notebooks;

public class ModuleQuantity implements Comparable<ModuleQuantity> {
	private String name;
	private int count;

	public ModuleQuantity(String name, int quantity) {
		this.name = name;
		this.count = quantity;
	}

	@Override
	public int compareTo(ModuleQuantity other) {
		return this.count - other.count;
	}

	@Override
	public String toString() {
		return name + ": " + count;
	}
}
