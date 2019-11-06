
public class Snippet {
	private String fileName;
	private int snippetIndex;
	
	public Snippet(String fileName, int snippetIndex) {
		this.fileName = fileName;
		this.snippetIndex = snippetIndex;
	}
	
	public boolean equals(Object other) {
		if (other.getClass() != this.getClass()) {
			return false;
		}
		Snippet otherSnippet = (Snippet)other;
		return otherSnippet.fileName.equals(this.fileName)
				&& otherSnippet.snippetIndex == this.snippetIndex;
	}
	
	public String toString() {
		return fileName + ", " + snippetIndex;
	}
}
