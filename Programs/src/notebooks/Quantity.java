package notebooks;

public class Quantity implements Comparable<Quantity> {
	private String name;
	private int count;

	public Quantity(String name, int quantity) {
		this.name = name;
		this.count = quantity;
	}

	@Override
	public int compareTo(Quantity other) {
		return this.count - other.count;
	}

	@Override
	public String toString() {
		return name + ": " + count;
	}
}
