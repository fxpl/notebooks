package notebooks;

import java.util.Objects;

public class Snippet {
	private final String fileName;
	private final int snippetIndex;
	private String repro = "";
	
	public Snippet(String fileName, int snippetIndex) {
		this(fileName, "", snippetIndex);
	}
	
	public Snippet(String fileName, String reproName, int snippetIndex) {
		this.fileName = fileName;
		this.snippetIndex = snippetIndex;
		this.repro = reproName;
	}
	
	/**
	 * Fetch file and repro name from the notebook given as argument
	 */
	public Snippet(Notebook notebook, int snippetIndex) {
		this(notebook.getName(), notebook.getRepro(), snippetIndex);
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
	
	@Override
	public String toString() {
		return fileName + ", " + snippetIndex;
	}
}
