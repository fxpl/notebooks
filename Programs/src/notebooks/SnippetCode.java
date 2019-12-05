package notebooks;

import java.util.Objects;

/**
 * Represents the code inside a snippet.
 */
public class SnippetCode {
	private final int LOC;
	private final String hash;
	
	public SnippetCode(int LOC, String hash) {
		this.LOC = LOC;
		this.hash = hash;
	}
	
	public String getHash() {
		return hash;
	}
	
	public int getLOC() {
		return LOC;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other.getClass() != this.getClass()) {
			return false;
		}
		SnippetCode otherCode = (SnippetCode)other;
		return this.hash.equals(otherCode.getHash())
				&& this.LOC == otherCode.LOC;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(hash);
	}
	
	@Override
	public String toString() {
		return hash;
	}
	
}
