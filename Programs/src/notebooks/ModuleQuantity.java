package notebooks;

public class ModuleQuantity implements Comparable<ModuleQuantity> {
	private String name;
	private int quantity;

	public ModuleQuantity(String name, int quantity) {
		this.name = name;
		this.quantity = quantity;
	}

	@Override
	public int compareTo(ModuleQuantity other) {
		return this.quantity - other.quantity;
	}

	@Override
	public String toString() {
		return name + ": " + quantity;
	}
}
