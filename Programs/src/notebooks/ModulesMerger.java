package notebooks;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ModulesMerger extends Worker<Void> {
	PythonModule module;
	List<List<PythonModule>> allModules;
	CountDownLatch counter;

	ModulesMerger(PythonModule module, List<List<PythonModule>> allModules, CountDownLatch counter) {
		super(null);
		this.module = module;
		this.allModules = allModules;
		this.counter = counter;
	}

	@Override
	public Void call() {
		for (List<PythonModule> modulesList: allModules) {
			for (PythonModule moduleFromCorpus: modulesList) {
				if (moduleFromCorpus.is(module)) {
					module.merge(moduleFromCorpus);
				}
				// We may have registered calls at the parent as well
				if (moduleFromCorpus.parentIs(module)) {
					module.merge(moduleFromCorpus.parent);
				}
			}
		}
		counter.countDown();
		return null;
	}
}
