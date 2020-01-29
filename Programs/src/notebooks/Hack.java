package notebooks;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hack {
		public static final int cores = Runtime.getRuntime().availableProcessors();
    public static final ExecutorService executor = Executors.newFixedThreadPool(2 * cores);

    public static final AtomicInteger finishedJobs = new AtomicInteger(0);
    public static final AtomicInteger queuedJobs = new AtomicInteger(0);

    //* Called in lambda in processFiles to ensure the shutdown of the executor service 
    private static void done() {
        if (finishedJobs.incrementAndGet() > queuedJobs.intValue()) {
            executor.shutdown();
            LibraryAnalysis.displayResults();
        }
    }

    //* Process fileListFile line by line, calling the task function for each filename
    public static void processFilesInList(final File fileListFile,
                                    final Consumer<Notebook> task)
        throws FileNotFoundException, IOException {

        try (BufferedReader br = new BufferedReader(new FileReader(fileListFile))) {
            for(String path; (path = br.readLine()) != null; ) {
                try {
                    final Notebook b = Notebook.loadNotebookFromFile(path);
                    queuedJobs.incrementAndGet();
                    executor.submit(() -> { task.accept(b); done(); });
                } catch (NotebookException nbe) {
                    System.err.printf("Error processing notebook %s\n", path);
                }
            }
        }

        done(); // Ensures finishedJobs > queuedJobs
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, NotebookException {
        if (args.length == 1) {
            final File fileListFile = new File(args[0]);

            if (fileListFile.canRead() == false) {
                System.err.println("Cannot read file " + args[0] + ". Wrong path? (Exiting)");
                return;
            } else {
                processFilesInList(fileListFile, LibraryAnalysis::process);
            }
        } else {
            System.err.println("Usage: java Hack file_with_list_of_files");
        }
    }
}

class LibraryAnalysis {
    private static final ConcurrentHashMap<String, Integer> imports = new ConcurrentHashMap<String, Integer>();
    private static final Pattern basicImportPattern = Pattern.compile("import\\s+(\\S+?)[\\s$]");
    private static final Pattern importAsPattern = Pattern.compile("import\\s+(\\S+?)\\s+as");
    private static final Pattern fromPattern = Pattern.compile("from\\s+(\\S+?)\\s+import");
    private static final Pattern[] importPatterns = { basicImportPattern, importAsPattern, fromPattern };

    public static void displayResults() {
        System.out.printf("Results\n---------------\n");
        // imports.forEach((lib, occ) -> System.out.printf("%s: %d\n", lib, occ));

        /// 27 : lib1, lib2, ...
        HashMap<Integer, List<String>> sortedResultTable = new HashMap<Integer, List<String>>();

        /// Populate sortedResultTable from imports
        imports.forEach((lib, occ) -> {
                if (sortedResultTable.containsKey(occ) == false) sortedResultTable.put(occ, new ArrayList<String>());
                sortedResultTable.get(occ).add(lib);
            });
        
        /// Print top 100 sortedResultTable
        int cutOff = 100;
        for (int occurrences : (new TreeSet<Integer>(sortedResultTable.keySet()).descendingSet())) {
            if (--cutOff >= 0) {
                System.out.printf("%d: %s\n", occurrences, String.join(", ", sortedResultTable.get(occurrences)));
            } else {
                break;
            }
        }
    }
    
    public static void process(final Notebook n) {
        try {
            if (n.language() == Language.PYTHON) {
                ArrayList<String> linesOfCode = n.getCodeSnippetsLineByLine();

                for (String line : linesOfCode) {
                    line = line.trim();

                    for (Pattern pattern : importPatterns) {
                        final Matcher matcher = pattern.matcher(line);

                        if (matcher.find()) {
                            final String library = matcher.group(1);

                            // TODO: keep a per thread count
                            imports.put(library, imports.getOrDefault(library, 0) + 1);
                        }
                    }
                }
            } else {
                /// TODO: log the ones we skip
            }
        } catch (NotebookException e) {
            System.err.printf("Error processing notebook %s\n", n.getName());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}


