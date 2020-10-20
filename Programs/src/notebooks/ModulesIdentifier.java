package notebooks;

import java.util.List;

public class ModulesIdentifier extends Worker<List<PythonModule>> {
	private boolean heartBeat;

	ModulesIdentifier(Notebook notebook, boolean heartBeat) {
		super(notebook);
		this.heartBeat = heartBeat;
	}

	@Override
	public List<PythonModule> call() throws Exception {
		if (heartBeat) {
			Utils.heartBeat("Identifying modules in " + notebook.getName());
		}
		return notebook.modules();
	}

}
