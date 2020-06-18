package notebooks;

public class CloneGroup {
	private CloneGroup next;

	public CloneGroup merge(CloneGroup other) {
		return CloneGroup.merge(this, other);
	}
	
	private static CloneGroup merge(CloneGroup a, CloneGroup b) {
		a = a.top();
		b = b.top();
		// If equal, they are already merged
		if (a != b) {
			a.next = b;
		}
		return b;
	}
	
	public CloneGroup top() {
		if (this.next == null) {
			return this;
		} else {
			this.next = this.next.top();
			return this.next;
		}
	}
}
