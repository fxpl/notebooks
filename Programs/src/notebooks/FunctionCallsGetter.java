package notebooks;

import java.util.List;
import java.util.Map;

public class FunctionCallsGetter extends Worker<Map<PythonModule, List<String>>> {

	List<PythonModule> functions;
	
	FunctionCallsGetter(Notebook notebook, List<PythonModule> functions) {
		super(notebook);
		this.functions = functions;
	}

	@Override
	public Map<PythonModule, List<String>> call() throws Exception {
		return notebook.functionCalls(functions);
	}

}
