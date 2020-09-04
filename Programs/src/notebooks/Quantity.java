package notebooks;

import java.util.Objects;

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
	public boolean equals(Object other) {
		if (!(other instanceof Quantity)) {
			return false;
		}
		Quantity otherQuantity = (Quantity)other;
		return this.name.equals(otherQuantity.name)
				&& this.count == otherQuantity.count;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, count);
	}

	@Override
	public String toString() {
		return name + ": " + count;
	}
	
	/**
	 * @return A string represenation suitable for CSV files
	 */
	public String toCsvString() {
		return name + ", " + count;
	}
}
