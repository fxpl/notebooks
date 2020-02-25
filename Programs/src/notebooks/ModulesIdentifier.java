package notebooks;

import java.util.List;

public class ModulesIdentifier extends Worker<List<PythonModule>> {

	ModulesIdentifier(Notebook notebook) {
		super(notebook);
	}

	@Override
	public List<PythonModule> call() throws Exception {
		return notebook.modules();
	}

}
