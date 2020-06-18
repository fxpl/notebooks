package notebooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Testa!?

public class CloneGroup {
	private CloneGroup next;
	
	// TODO Integers istället för SccSnippetIds?!

	// TODO: Ska den verkligen ligga här?
	public static void compact(HashMap<SccSnippetId, CloneGroup> clones) {
		for(Map.Entry<SccSnippetId, CloneGroup> entry : clones.entrySet()){
			CloneGroup group = entry.getValue();
			if (null != group) {
				entry.setValue(group.top());
			}
		}
	}
	
	public static List<List<SccSnippetId>> convertResult(HashMap<SccSnippetId, CloneGroup> clones) {
		return new ArrayList<List<SccSnippetId>>(invertMap(clones).values());
	}
	
	// TODO Varför klassmetod???
	private static HashMap<CloneGroup, List<SccSnippetId>> invertMap(HashMap<SccSnippetId, CloneGroup> clones) {
		// Required for correctness
		CloneGroup.compact(clones);

		final HashMap<CloneGroup, List<SccSnippetId>> outerResult = new HashMap<CloneGroup, List<SccSnippetId>>();

		final Set<SccSnippetId> keySet = clones.keySet();

		int progress = 0;
		for (SccSnippetId key : keySet) {
			// TODO: Heart beat if (progress++ % 10000 == 0) SccOutputAnalyzer.printTimeStampedMsg("Processed " + progress + " keys");

			CloneGroup cg = clones.get(key);
			List<SccSnippetId> list = outerResult.get(cg);
			
			if (list == null) {
				list = new ArrayList<SccSnippetId>();
				list.add(key);
				outerResult.put(cg, list);
			} else {
				list.add(key);
			}
		}
		
		return outerResult;
	}
	
	public CloneGroup merge(CloneGroup other) {
		// TODO: Mergea mergemetoder
		return CloneGroup.merge(this, other);
	}
	
	private static CloneGroup merge(CloneGroup a, CloneGroup b) {
		a = a.top();
		b = b.top();
		// If equal, they are already merged
		if (a != b) {
			a.next = b;
		}
		return b;
	}
	
	public CloneGroup top() {
		if (this.next == null) {
			return this;
		} else {
			this.next = this.next.top();
			return this.next;
		}
	}
}
