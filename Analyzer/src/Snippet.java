
public class Snippet {
	private String fileName;
	private int snippetIndex;
	
	public Snippet(String fileName, int snippetIndex) {
		this.fileName = fileName;
		this.snippetIndex = snippetIndex;
	}
	
	public boolean equals(Snippet other) {
		return other.toString().equals(this.toString());		
	}
	
	public String toString() {
		return fileName + "," + snippetIndex;
	}
}
