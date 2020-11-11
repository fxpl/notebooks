package notebooks;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class FunctionCallsGetter extends Worker<Map<PythonModule, List<String>>> {

	private List<PythonModule> functions;
	private boolean heartBeat;
	
	FunctionCallsGetter(Notebook notebook, List<PythonModule> functions, boolean heartBeat) {
		super(notebook);
		this.functions = functions;
		this.heartBeat = heartBeat;
	}

	@Override
	public Map<PythonModule, List<String>> call() throws Exception {
		if (heartBeat) {
			Utils.heartBeat("Retreiving function calls from " + notebook.getName());
		}
		Map<PythonModule, List<String>> calls = notebook.functionCalls(functions);
		// Add notebook name before each call
		Map<PythonModule, List<String>> result = new HashMap<PythonModule, List<String>>(calls.size()); 
		for (Map.Entry<PythonModule, List<String>> entry: calls.entrySet()) {
			result.put(entry.getKey(), new ArrayList<String>(entry.getValue().size()));
			for (String call: entry.getValue()) {
				result.get(entry.getKey()).add(notebook + ": " + call);
			}
		}
		return result;
	}

}
