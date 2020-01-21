package notebooks;

import java.util.Objects;

public class Snippet {
	private final String fileName;
	private final int snippetIndex;
	private String repro;	// TODO: Notebookreferens istället???
	
	public Snippet(String fileName, int snippetIndex) {
		this.fileName = fileName;
		this.snippetIndex = snippetIndex;
	}
	
	// TODO: Ta bort denna, eller set-metoden. Testa denna om den blir kvar!
	public Snippet(String fileName, String reproName, int snippetIndex) {
		this(fileName, snippetIndex);
		this.repro = reproName;
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
	
	String getRepro() {
		return repro;
	}
	
	int getSnippetIndex() {
		return snippetIndex;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(fileName, snippetIndex);
	}
	
	// TODO: Behövs denna?
	public void setRepro(String reproName) {
		this.repro = reproName;
	}
	
	@Override
	public String toString() {
		return fileName + ", " + snippetIndex;
	}
}
