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
		if (other instanceof SccSnippetId) {
			SccSnippetId otherId = (SccSnippetId)other;
			return this.nbID == otherId.nbID && this.snippetID == otherId.snippetID;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(nbID, snippetID);
	}
	
	@Override
	public String toString() {
		return this.nbID + ":" + this.snippetID;
	}
}
