package notebooks;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SccSnippetId {
    public final int nbID;
    public final int snippetID;

    public int linesOfCode = 0;
    
    public static final java.util.HashMap<String, Integer> resolver = new java.util.HashMap<String, Integer>();
    public static final java.util.ArrayList<SccSnippetId> directory = new java.util.ArrayList<SccSnippetId>();
    
	public SccSnippetId(int nbID, int snippetID) {
      this.nbID = nbID;
      this.snippetID = snippetID;
	}

    private SccSnippetId(int nbID, int snippetID, int linesOfCode) {
        this(nbID, snippetID);
        this.linesOfCode = linesOfCode;
    }

	public SccSnippetId(final String sid) throws NumberFormatException {
      final int commaPos = sid.indexOf(',');

      final String id1 = sid.substring(0, commaPos);
      final String id2 = sid.substring(commaPos + 1);

      this.nbID = Integer.parseInt(id1);
      this.snippetID = Integer.parseInt(id2);
	}
    
    public SccSnippetId(final String sid, char separator) throws NumberFormatException {
      final int commaPos = sid.indexOf(separator);

      final String id1 = sid.substring(0, commaPos);
      final String id2 = sid.substring(commaPos + 1);

      this.nbID = Integer.parseInt(id1);
      this.snippetID = Integer.parseInt(id2);
	}

    public NotebookFile getNotebook() {
        return NotebookFile.getById(this.nbID);
    }

    public boolean isEmpty() {
        return this.linesOfCode == 0; // Confirm with Malin what the correct definition is 
    }
    
    // static int create(int a, int b) throws NumberFormatException {
    //     return create(a + "," + b);
    // }
    
    // static int create(String sid) throws NumberFormatException {
    //     int sidHash = sid.hashCode();
    //     if (directory.containsKey(sidHash) == false) {
    //         directory.put(sidHash, new SccSnippetId(sid));
    //     }
    //     return sidHash;
    // }

    static SccSnippetId getByPair(String sidPair) throws NumberFormatException {
        final int at = sidPair.indexOf('@');
        final String id = sidPair.substring(0, at) + "," + sidPair.substring(at + 1);
        return directory.get(resolver.get(id));
    }

    static SccSnippetId getByCommaSeparatedPair(String id) throws NumberFormatException {
        return directory.get(resolver.get(id));
    }
    
    static Integer getId(String id) throws NumberFormatException {
        return resolver.get(id);
    }
    
    public static void register(String sid, int nbIB, int snippetID, int linesOfCode) {
        directory.add(new SccSnippetId(nbIB, snippetID, linesOfCode));
        resolver.put(sid, directory.size());
    }
    
    public static void register(String info) {
        final int firstComma = info.indexOf(',');
        final int secondComma = info.indexOf(',', firstComma + 1);
        final int ultimateComma = info.lastIndexOf(',');
        final int penUltimateComma = info.lastIndexOf(',', ultimateComma - 1);

        final int nbID = Integer.parseInt(info.substring(0, firstComma));
        final int snippetID = Integer.parseInt(info.substring(firstComma + 1, secondComma));
        final int nbNumber = Integer.parseInt(info.substring(info.indexOf('_') + 1,
                                                             info.indexOf('.')));
        final int linesOfCode = Integer.parseInt(info.substring(penUltimateComma + 1, ultimateComma));
        
        SccSnippetId.register(info.substring(0, secondComma), nbID, snippetID, linesOfCode);
        NotebookFile.register(nbID, nbNumber); 
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

    public String toString() {
        return this.nbID + "@" + this.snippetID;
    }
}
