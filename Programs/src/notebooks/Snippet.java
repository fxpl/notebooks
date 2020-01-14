package notebooks;

import java.util.Objects;

public class Snippet {
	private final String fileName;
	private final int snippetIndex;
	
	public Snippet(String fileName, int snippetIndex) {
		this.fileName = fileName;
		this.snippetIndex = snippetIndex;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other.getClass() != this.getClass()) {
			return false;
		}
		Snippet otherSnippet = (Snippet)other;
		return otherSnippet.fileName.equals(this.fileName)
				&& otherSnippet.snippetIndex == this.snippetIndex;
	}
	
	/**
	 * @return The file name of the notebook where the snippet resides
	 */
	String getFileName() {
		return fileName;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(fileName, snippetIndex);
	}
	
	@Override
	public String toString() {
		return fileName + ", " + snippetIndex;
	}
}
