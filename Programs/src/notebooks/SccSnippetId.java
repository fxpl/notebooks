package notebooks;

import java.util.Objects;

public class SccSnippetId {
	private final int nbID;
	private final int snippetID;
	
	public SccSnippetId(int nbID, int snippetID) {
		this.nbID = nbID;
		this.snippetID = snippetID;
	}

	@Override
	public boolean equals(Object other) {
		if (other.getClass() != this.getClass()) {
			return false;
		}
		SccSnippetId otherId = (SccSnippetId)other;
		return this.nbID == otherId.nbID && this.snippetID == otherId.snippetID;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(nbID, snippetID);
	}
}
