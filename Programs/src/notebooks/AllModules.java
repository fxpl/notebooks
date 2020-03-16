package notebooks;

public class AllModules extends PythonModule {
	public AllModules(PythonModule parent) {
		super("*", null, ImportType.FROM, parent);
	}
}