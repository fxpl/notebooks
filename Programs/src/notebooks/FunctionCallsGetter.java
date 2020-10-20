package notebooks;

import java.util.List;
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
		return notebook.functionCalls(functions);
	}

}
