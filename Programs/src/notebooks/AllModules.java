package notebooks;

public class AllModules extends PythonModule {
	
	public AllModules(PythonModule parent) {
		super("*", null, ImportType.ORDINARY, parent);
	}
	
	@Override
	public String toString() {
		/* The import type is obvious and doesn't need to be considered.
		   Hence, the pedigree string contains all information needed. */
		return pedigreeString();
	}
	
	// TODO: Hur få med dessa importer?!
}