package notebooks;

public class AllModules extends PythonModule {
	
	public AllModules(PythonModule parent) {
		super("*", null, ImportType.ORDINARY, parent);
	}
	
	// TODO: Hur få med dessa importer?!
}