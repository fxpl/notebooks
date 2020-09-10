package notebooks;

import java.util.Objects;

public class Quantity implements Comparable<Quantity> {
	private String identifier;
	private int count;

	public Quantity(String identifier, int quantity) {
		this.identifier = identifier;
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
		return this.identifier.equals(otherQuantity.identifier)
				&& this.count == otherQuantity.count;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(identifier, count);
	}

	@Override
	public String toString() {
		return identifier + ": " + count;
	}
	
	/**
	 * @return A string represenation suitable for CSV files
	 */
	public String toCsvString() {
		return identifier + ", " + count;
	}
}
