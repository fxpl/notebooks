package notebooks;

public class SnippetPrinter {
	
	public static void main(String[] args) {
		if (2 != args.length) {
			System.out.println("Usage: SnippetPrinter <path to notebook> <snippet index>");
			System.exit(1);
		}
		Notebook notebook = new Notebook(args[0]);
		try {
			notebook.printSnippet(Integer.valueOf(args[1]));
		} catch (NumberFormatException e) {
			System.err.println("Second argument must be an integer!");
			System.exit(1);
		} catch (RuntimeException e) {
			System.err.println("Couldn't print snippet: " + e.toString());
			System.exit(1);
		}
	}

}
