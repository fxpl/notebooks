package notebooks;

public class AllModules extends PythonModule {
	// TODO: Är FROM rätt importtyp här?!
	// Borde ev vara vanlig istället; from X import Y --> vanlig.)
	public AllModules(PythonModule parent) {
		super("*", null, ImportType.FROM, parent);
	}
}