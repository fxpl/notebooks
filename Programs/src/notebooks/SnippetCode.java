package notebooks;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	
	public SnippetCode(SnippetCode model) {
		this(model.getLOC(), model.getHash());
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
	
	/**
	 * @returns True if the snippet has 0 lines, or the hash represents an empty string, false otherwise
	 */
	public boolean isEmpty() {
		if (0 == LOC) {
			return true;
		} else {
			String emptyHash = "";
			try {
				MessageDigest hasher = MessageDigest.getInstance("MD5");
				emptyHash = NotebookUtils.toHexString(hasher.digest("".getBytes()));
			} catch (NoSuchAlgorithmException e) {
				System.err.println("MessageDigest cannot hash using MD5!");
			}
			return emptyHash.equals(this.hash);
		}
	}
	
	@Override
	public String toString() {
		return hash;
	}
	
}
