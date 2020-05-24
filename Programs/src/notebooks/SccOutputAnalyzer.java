package notebooks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.*;
import java.util.Set;
import java.util.Locale;

public class SccOutputAnalyzer extends Analyzer {
    private static final long startTimeStamp = System.currentTimeMillis();
    public static void printTimeStampedMsg(String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(">>> (");
        sb.append((System.currentTimeMillis() - startTimeStamp) / 1000);
        sb.append(" sec) ");
        sb.append(msg);
        System.err.println(sb.toString());
    }

    // Information about each snippet
    Map<SccSnippetId, Integer> notebookNumbers;
    Map<SccSnippetId, Integer> snippetIndices;
    Map<SccSnippetId, Integer> linesOfCode;
    // Information about each notebook
    private Map<String, String> repros = null;
    Map<String, Integer> snippetsPerNotebook = null;
	
    /**
     * Perform the clone analysis based on SourcererCC output files. Write
     * file2hashesA<current-date-time>.csv, hash2filesA<current-date-time>.csv,
     * cloneFrequencies<current-date-time>.csv and
     * connections<current-date-time>.csv accordingly.
     * This methods initializes snippet and repro information, so you shouldn't
     * do it explicitly before the call to this method.
     * Note that the ''hashes'' written by this method are not the MD5 hashes
     * of the snippets, but just the value of a counter. However, all instances
     * of the ''hash'' of a snippet are the same.
     * @param statsFile Path to file stats file produced by the SourcererCC tokenizer
     * @param reproFile Path to file with mapping from notebook number to repro
     * @param pairFile: Path to output file with clone pairs from the SourcererCC clone detection
     * @return A map from snippets to files
     * @throws IOException
     */
    public Map<SnippetCode, List<Snippet>> clones(String statsFile, String reproFile, String pairFile) throws IOException {
        // initializeSnippetInfo(statsFile);
        // initializeReproMap(reproFile);
        registerRepos(reproFile);
        registerSnippets(statsFile);
        return clones(pairFile);
    }
	
    /**
     * Perform the clone analysis based on SourcererCC output files. Write
     * file2hashesA<current-date-time>.csv, hash2filesA<current-date-time>.csv,
     * cloneFrequencies<current-date-time>.csv and
     * connections<current-date-time>.csv accordingly.
     * Note that you have to initialize the snippet and repro information, by
     * calling initializeSnippetInfo and initializeReproMap respectively before
     * calling this method!
     * Note that the ''hashes'' written by this method are not the MD5 hashes
     * of the snippets, but just the value of a counter. However, all instances
     * of the ''hash'' of a snippet are the same.
     * @param pairFile: Path to output file with clone pairs from the SourcererCC clone detection
     * @return A map from snippets to files
     * @throws IOException
     */
    public Map<SnippetCode, List<Snippet>> clones(String pairFile) throws IOException {
        System.out.println("Analyzing clones based on SourcererCC output files!");
        System.out.println("NOTE THAT NOTEBOOKS WITHOUT SNIPPETS ARE NOT INCLUDED");
        System.out.println("since they are not included in the SourcererCC data!");
        Map<SnippetCode, List<Snippet>> snippet2file = getClones(pairFile);
        Map<Notebook, SnippetCode[]> file2snippet = getSnippets(snippet2file);
        new CloneFileWriter(outputDir).write(file2snippet, snippet2file);
        return snippet2file;
    }
	
    /**
     * Initialize repro information for each notebook.
     * @param fileName Path to file with mapping from notebook number to repro
     */
    public void initializeReproMap(String fileName) throws FileNotFoundException {
        repros = createReproMap(fileName);
    }

    public void registerRepos(String fileName) throws FileNotFoundException {
         try (BufferedReader f = new BufferedReader(new FileReader(new File(fileName)))) {
            for(String line = f.readLine(); line != null; line = f.readLine()) {
                Repository.register(Integer.parseInt(line.substring(0, line.indexOf(','))),    // Notebook ID
                                    line.substring(line.indexOf(',') + 1));                    // URL
            }

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
	
    public void registerSnippets(String statsFile) throws FileNotFoundException {
        try (BufferedReader f = new BufferedReader(new FileReader(new File(statsFile)))) {
            for(String line = f.readLine(); line != null; line = f.readLine()) {
                SccSnippetId.register(line);
            }

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public void initializeSnippetInfo(String statsFile) throws FileNotFoundException {
        BufferedReader input = new BufferedReader(new FileReader(new File(statsFile)));
        notebookNumbers = new HashMap<SccSnippetId, Integer>();
        snippetIndices = new HashMap<SccSnippetId, Integer>();
        linesOfCode = new HashMap<SccSnippetId, Integer>();
        snippetsPerNotebook = new HashMap<String, Integer>();
        try {
            while(true) {
                final String line = input.readLine();

                if (line == null) break;

                String[] columns = line.split(",");
                final SccSnippetId id = SccSnippetId
                    .getByCommaSeparatedPair(line.substring(0, line.indexOf(',', line.indexOf(',') + 1)));
          
                String path = columns[2];
                // Remove directories from filename
                String snippetFileName = path.substring(path.lastIndexOf('/') + 1);
                // Remove suffix
                snippetFileName = snippetFileName.substring(0, snippetFileName.lastIndexOf('.'));
                String[] snippetSubStrings = snippetFileName.split("_");
                int notebookNumber = Integer.parseInt(snippetSubStrings[1]);
                addOrIncrease(snippetsPerNotebook, "nb_" + snippetSubStrings[1] + ".ipynb");
                notebookNumbers.put(id, notebookNumber);
                snippetIndices.put(id, Integer.parseInt(snippetSubStrings[2]));
                /* Here we use the number of lines of source code (comments
                   excluded), which is inconsistent with the clone analysis of the 
                   notebook files, but so is the clone detection -SourcererCC
                   doesn't consider comments in clone analysis. */
                int loc = Integer.parseInt(columns[8]);
                linesOfCode.put(id, loc);
            }
            input.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
    
    
    /**
     * If map contains a value for key, increase it with 1. Else add an entry
     * with for key with the value 1.
     * @param map Map to modify as stated above
     * @param key Key for the entry that will be changed/added
     */
    private void addOrIncrease(Map<String, Integer> map, String key) {
        if (map.containsKey(key)) {
            map.put(key, map.get(key) + 1);
        } else {
            map.put(key, 1);
        }
    }

    /**
     * Create a mapping from snippets to notebooks (hash2files) using output
     * files from SourcererCC.
     */
    private Map<SnippetCode, List<Snippet>> getClones(String pairFile) throws IOException {
        List<List<SccSnippetId>> clones = getCloneLists(pairFile);

        constructConnectionGraphInfo(clones);
        // System.exit(0);
        
        return getCloneMap(clones);
    }
	
    private List<List<SccSnippetId>> getCloneLists(String pairFile) throws FileNotFoundException {
        File cachedResultsOfPreviousRun = new File("getCloneLists.result.txt");

        if (cachedResultsOfPreviousRun.exists()) {
            try (BufferedReader f = new BufferedReader(new FileReader(cachedResultsOfPreviousRun))) {
                ArrayList<List<SccSnippetId>> result = new ArrayList<List<SccSnippetId>>();

                for (String line = f.readLine(); line != null; line = f.readLine()) {
                    ArrayList<SccSnippetId> innerResult = new ArrayList<SccSnippetId>();

                    for (String entry : line.split(",")) {
                        if (entry.length() > 0) {
                            innerResult.add(SccSnippetId.getByPair(entry));
                        }
                    }

                    result.add(innerResult);
                }
                
                return result;
            } catch (IOException e) {
                /// fixme add printout
                System.exit(-1);
            }
        } 

        return computeCloneLists(pairFile);
    }

    private List<List<SccSnippetId>> computeCloneLists(String pairFile) throws FileNotFoundException {
        HashMap<Integer, CloneGroup> clones = new HashMap<Integer, CloneGroup>();

        long numRead = 0;
        try {
            final BufferedReader file = new BufferedReader(new FileReader(new File(pairFile)));

            for (String line = file.readLine(); line != null; line = file.readLine()) {
                CloneGroup.addToCloneList(clones, line);
                ++numRead;

                if ((numRead % 5000000) == 0) {
                    CloneGroup.compact(clones);
                }
                
                if ((numRead % 1000000) == 0) {
                    printTimeStampedMsg(numRead + " clone pairs read");
                }
            }

            file.close();
            printTimeStampedMsg("Done reading clone pairs (" + clones.size() + ") keys in clones");

            printTimeStampedMsg("calling convertResult");
            List<List<SccSnippetId>> result = CloneGroup.convertResult(clones, SccSnippetId.directory);
            printTimeStampedMsg("returning from convertResult");
            clones = null;

            FileWriter fw = new FileWriter(new File("getCloneLists.result.txt"));
            for (List<SccSnippetId> list : result) {
                for(SccSnippetId sid : list) {
                    fw.write(sid.toString());
                    fw.write(',');
                }
                fw.write('\n');
            }
            fw.close();

            return result;

        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        return null;
    }

    class Counter {
        int value = 1;
        public Counter inc() {
            this.value += 1;
            return this;
        }
    }

    private void postProcessRepoConnections(HashMap<NotebookFile, Counter> nbOccurrences, boolean emptyConnections) {
        List<NotebookFile> noteBooks =
            new ArrayList<NotebookFile>(nbOccurrences.keySet());

        final int noteBooksSize = noteBooks.size();

        if (noteBooksSize > 100000) {
            printTimeStampedMsg("Skipping big (" + noteBooksSize + ") clonelist");
            /// TODO: optimise
            return;
        }
        
        for (int i = 0; i < noteBooksSize; ++i) {
            NotebookFile nbi = noteBooks.get(i);
            final Repository ri = nbi.getRepo();

            for (int j = i; j < noteBooksSize; ++j) {
                NotebookFile nbj = noteBooks.get(j);
                final Repository rj = nbj.getRepo();

                if (ri == rj) {
                    nbi.addIntraRepoConnections(nbOccurrences.get(nbj).value, emptyConnections);
                    nbj.addIntraRepoConnections(nbOccurrences.get(nbi).value, emptyConnections);
                } else {
                    nbi.addInterRepoConnections(nbOccurrences.get(nbj).value, emptyConnections);
                    nbj.addInterRepoConnections(nbOccurrences.get(nbi).value, emptyConnections);
                }
            }
        }
    }
    
    private void constructConnectionGraphInfo(List<List<SccSnippetId>> cloneLists) {
        printTimeStampedMsg("constructConnectionGraphInfo start");

        /// For each list of snippets considered clones ...
        for (List<SccSnippetId> cloneList : cloneLists) {
            final int totalConnections = cloneList.size();
            
            final HashMap<NotebookFile, Counter> notebookOccurrences = new HashMap<NotebookFile, Counter>();
            final HashMap<NotebookFile, Counter> notebookOccurrencesEmptySnippets = new HashMap<NotebookFile, Counter>();

            /// Count the number of times each notebook appears (separate empty and non-empty)
            for (SccSnippetId sid : cloneList) {
                final NotebookFile nb = sid.getNotebook();
                final HashMap<NotebookFile, Counter> map = (sid.isEmpty()
                                                            ? notebookOccurrencesEmptySnippets
                                                            : notebookOccurrences);
                /// if nb --> c exists, do c.inc() else, install new counter with value 1
                map.compute(nb, (k, c) -> (c == null) ? new Counter() : c.inc());
            }

            for (Map.Entry<NotebookFile, Counter> kv : notebookOccurrences.entrySet()) {
                NotebookFile nb = kv.getKey();
                int intraConnections = kv.getValue().value;

                nb.addIntraConnections(intraConnections, false);
                nb.addInterConnections(totalConnections - intraConnections, false);
            }

            for (Map.Entry<NotebookFile, Counter> kv : notebookOccurrencesEmptySnippets.entrySet()) {
                NotebookFile nb = kv.getKey();
                int intraConnections = kv.getValue().value;

                nb.addIntraConnections(intraConnections, true);
                nb.addInterConnections(totalConnections - intraConnections, true);
            }

            postProcessRepoConnections(notebookOccurrences, false);
            postProcessRepoConnections(notebookOccurrencesEmptySnippets, true);
        }

        printTimeStampedMsg("printing starts");
        
        NotebookFile.forAll(nb -> {
                final float normalizedConnections = 0; // TODO
                final float normalizedNonEmptyConnections = 0; // TODO
                final float meanInterReproConnections = 0; // TODO
                final float meanNonEmptyInterReproConnections = 0; // TODO

                final int nonEmptyConnections = nb.intraConnections() + nb.interConnections();
                final int connections = nonEmptyConnections + nb.intraEmptyConnections() + nb.interEmptyConnections();

                final int nonEmptyIntraReproConnections = nb.intraRepoConnections();
                final int intraReproConnections = nonEmptyIntraReproConnections + nb.intraRepoEmptyConnections();

                String result = String.format(Locale.US,
                                              "%s, %d, %.4f, %d, %.4f, %d, %d, %.4f, %.4f",
                                              nb.fileName(),
                                              connections,
                                              normalizedConnections, 
                                              nonEmptyConnections,
                                              normalizedNonEmptyConnections,
                                              intraReproConnections,
                                              nonEmptyIntraReproConnections,
                                              meanInterReproConnections,
                                              meanNonEmptyInterReproConnections);

                // TODO: write this to file instead
                System.err.println(result);
            });
    }
    
    private Map<SnippetCode, List<Snippet>> getCloneMap(List<List<SccSnippetId>> clones)
        throws FileNotFoundException {
        Map<SnippetCode, List<Snippet>> result = new HashMap<SnippetCode, List<Snippet>>(clones.size());
        Set<SccSnippetId> snippetIdsToAdd = notebookNumbers.keySet();
        int hashIndex = 0;
		
        // Cloned snippets
        for (List<SccSnippetId> cloned: clones) {
            if (0 == hashIndex%100000000) {
                SccOutputAnalyzer.printTimeStampedMsg("Creating entry for " + hashIndex + " in snippet-to-files-map.");
            }
            List<Snippet> snippets = new ArrayList<Snippet>();
            int numClones = cloned.size();
            List<Integer> loc = new ArrayList<Integer>(numClones);
            for (int i=0; i<numClones; i++) {
                SccSnippetId id = cloned.get(i);
                if (id == null) {
                    SccOutputAnalyzer.printTimeStampedMsg("Skipping null cloned for i = " + i);
                    continue; 
                }
                addSnippet(id, snippets);
                snippetIdsToAdd.remove(id);
                loc.add(linesOfCode.get(cloned.get(i)));
            }
            int medianLoc = Utils.median(loc, "Different line count for snippet " + Integer.toString(hashIndex));
            SnippetCode hash = new SnippetCode(medianLoc, Integer.toString(hashIndex++));
            result.put(hash, snippets);
        }
		
        // Remaining snippets are unique. Add them!
        for (SccSnippetId id: snippetIdsToAdd) {
            if (0 == hashIndex%100000000) {
                printTimeStampedMsg("Creating entry  for " + hashIndex + " in snippet-to-files-map.");
            }
            List<Snippet> snippets = new ArrayList<>(1);
            addSnippet(id, snippets);
            /// snippetIdsToAdd.remove(id); /// FIXME: this line throws a ConcurrentModificationException
            int loc = linesOfCode.get(id);
            SnippetCode hash = new SnippetCode(loc, Integer.toString(hashIndex++));
            result.put(hash, snippets);
        }
        return result;
    }

    /**
     * Add the snippet with the specified SourcererCC snippet id to the list
     * specified.
     * @param id SourcererCC snippet id of snippet to add
     * @param snippets List of snippets, to which the snippet will be added
     */
    private void addSnippet(SccSnippetId id, List<Snippet> snippets) {
        String notebookName = getNotebookNameFromNumber(notebookNumbers.get(id)); 
        int snippetIndex = snippetIndices.get(id);
        snippets.add(new Snippet(notebookName, repros.get(notebookName), snippetIndex));
    }
	
    private Map<Notebook, SnippetCode[]> getSnippets(Map<SnippetCode, List<Snippet>> snippet2file) {
        Map<Notebook, SnippetCode[]> result = new HashMap<Notebook, SnippetCode[]>(snippetsPerNotebook.size());
        // Create arrays for snippets
        for (String notebookName: snippetsPerNotebook.keySet()) {
            String repro = repros.get(notebookName);
            result.put(new Notebook(notebookName, repro), new SnippetCode[snippetsPerNotebook.get(notebookName)]);
			
        }
        // Put snippet in notebook-to-snippet-map
        int numAdded = 0;
        for (SnippetCode hash: snippet2file.keySet()) {
            if (0 == numAdded%100000000) {
                printTimeStampedMsg("Adding snippet " + hash + " to notebook-to-snippet-map.");
            }
            for (Snippet snippet: snippet2file.get(hash)) {
                SnippetCode[] snippetsInFile = result.get(new Notebook(snippet.getFileName()));
                snippetsInFile[snippet.getSnippetIndex()] = new SnippetCode(hash);
            }
            numAdded++;
        }
        return result;
    }
	
    private static String getNotebookNameFromNumber(int notebookNumber) {
        return "nb_" + notebookNumber + ".ipynb";
    }


    void analyze(String[] args) {
        String pairFile = null;

        // Set up
        for (int i=0; i<args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--stats_file")) {
                String statsFile = getValueFromArgument(arg);
                try {
                    printTimeStampedMsg("initializeSnippetInfo start");
                    registerSnippets(statsFile);
                    // initializeSnippetInfo(statsFile);
                    printTimeStampedMsg("initializeSnippetInfo done");
    
                } catch (FileNotFoundException e) {
                    System.err.println("Stats file not found: " + e.getMessage());
                }
            } else if (arg.startsWith("--repro_file")) {
                String reproFile = getValueFromArgument(arg);
                try {
                    printTimeStampedMsg("initializeReproMap start");
                    registerRepos(reproFile);
                    // initializeReproMap(reproFile);
                    printTimeStampedMsg("initializeReproMap done");
    
                } catch (FileNotFoundException e) {
                    System.err.println("Repro file not found: " + e.getMessage());
                }
            } else if (arg.startsWith("--pair_file")) {
                pairFile = getValueFromArgument(arg);
            } else if (arg.startsWith("--output_dir")) {
                outputDir = getValueFromArgument(arg);
            } else {
                System.err.println("Unknown argument: " + arg);
            }
        }
		
        // Run
        // (If notebookNumbers is null, none of the snippet info maps are initialized.)
        if (null != pairFile && "" != pairFile) { // && null != notebookNumbers &&  null !=this.repros) {
            try {
                printTimeStampedMsg("clones start");
                this.clones(pairFile);
                printTimeStampedMsg("clones stop");
                printTimeStampedMsg("Clone files created!");
            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage() + ". Operation interrupted.");
            }
        } else {
            if (null == pairFile || "" == pairFile) {
                System.err.println("SourcererCC clones pair file path not set!");
            }
            if (null == notebookNumbers) {
                System.err.println("Snippet information is not initialized!");
            }
            if (null == this.repros) {
                System.err.println("Repro information is not initialized!");
            }
            System.err.println("Analysis will not be run!");
        }
    }
	
    public static void main(String[] args) {
        printTimeStampedMsg("Start");
        SccOutputAnalyzer analyzer = new SccOutputAnalyzer();
        analyzer.analyze(args);
        ThreadExecutor.tearDown();
        printTimeStampedMsg("Stop");
    }
}

class CloneGroup {
    private CloneGroup next;
    private static long counter = 0;

    private static CloneGroup merge(CloneGroup a, CloneGroup b) {
        a = a.top();
        b = b.top();

        if (a == b) {
            // Already joined
        } else {
            a.next = b;
        }

        return b;
    }

    public CloneGroup top() {
        if (this.next == null) {
            return this;
        } else {
            this.next = this.next.top();
            return this.next;
        }
    }
        
    public CloneGroup merge(CloneGroup s) {
        return CloneGroup.merge(this, s);
    }

    // TODO: optimise
    // NOTE: Assumes clones is compacted
    public static List<SccSnippetId> addToList(HashMap<Integer, CloneGroup> clones, CloneGroup c, Map<Integer, SccSnippetId> intToSnippet) {
        ArrayList<SccSnippetId> result = new ArrayList<SccSnippetId>();

        Set<Integer> keys = clones.keySet();
        int[] array = new int[keys.size()];
        int index = 0;
        for(Integer element : keys) array[index++] = element.intValue();
        
        for(int key : array) {
            CloneGroup value = clones.get(key);
            if (value == c) {
                result.add(intToSnippet.get(key));
                clones.remove(key);
            }
        }

        return result;
    }

    public static List<List<SccSnippetId>> convertResult(HashMap<Integer, CloneGroup> clones,
                                                         List<SccSnippetId> intToSnippet) {
            return new ArrayList<List<SccSnippetId>>(invertMap(clones, intToSnippet).values());
    }

    public static HashMap<CloneGroup, List<SccSnippetId>> invertMap(HashMap<Integer, CloneGroup> clones, List<SccSnippetId> intToSnippet) {
        // Required for correctness
        CloneGroup.compact(clones);

        final HashMap<CloneGroup, List<SccSnippetId>> outerResult = new HashMap<CloneGroup, List<SccSnippetId>>();

        final Set<Integer> keySet = clones.keySet();
        SccOutputAnalyzer.printTimeStampedMsg("inverting map with domain size: " + keySet.size());

        int progress = 0;
        for (Integer key : keySet) {
            if (progress++ % 10000 == 0) SccOutputAnalyzer.printTimeStampedMsg("Processed " + progress + " keys");

            CloneGroup cg = clones.get(key);
            List<SccSnippetId> list = outerResult.get(cg);
            
            if (list == null) {
                list = new ArrayList<SccSnippetId>();
                list.add(intToSnippet.get(key));
                outerResult.put(cg, list);
            } else {
                list.add(intToSnippet.get(key));
            }
        }
        
        return outerResult;
    }

    
    public static long compact(HashMap<Integer, CloneGroup> clones) {
        long compaction = 0;
        
        for(Map.Entry<Integer, CloneGroup> entry : clones.entrySet()){
            CloneGroup cs = entry.getValue();

            if (cs.next == null) continue;

            entry.setValue(cs.top());
            ++compaction;
        }

        return compaction;
    }
    
    public static void addToCloneList(HashMap<Integer, CloneGroup> clones, String line) {
        int middleComma = line.indexOf(',', line.indexOf(',') + 1);
        Integer id1 = null;
        Integer id2 = null;

        try {
            id1 = SccSnippetId.getId(line.substring(0, middleComma));
            id2 = SccSnippetId.getId(line.substring(middleComma + 1));
            
        } catch (NumberFormatException nfe) {
            /// Nothing to do -- happens only once!
            return;
        }
        // Integer id1 = line.substring(0, middleComma).hashCode();
        // Integer id2 = line.substring(middleComma + 1).hashCode();
            
        // assert(line.matches("[0-9]+,[0-9]+,[0-9]+,[0-9]+"));

        if (id1 == null) {
            System.err.println(id1);
            System.err.println(id2);
            System.err.println(line.substring(0, middleComma));  
            System.err.println(line.substring(middleComma + 1));
            System.err.println(line);
        }
        
        CloneGroup id1Clones = clones.get(id1);
        CloneGroup id2Clones = clones.get(id2);

        if (id1Clones == id2Clones) {
            if (id1Clones != null) {
                /// We already had them marked as clones
            } else {
                /// Create a new clone set with this data
                CloneGroup top = new CloneGroup();
                clones.put(id1, top);
                clones.put(id2, top);
            }
        } else {
            /// Merge the sets as they are both clones, and point both to same set
            if (id1Clones == null) {
                clones.put(id1, id2Clones.top());
            } else if (id2Clones == null) {
                clones.put(id2, id1Clones.top());
            } else {
                CloneGroup top = id1Clones.merge(id2Clones);
                if (id1Clones != top) clones.put(id1, top);
                if (id2Clones != top) clones.put(id2, top);
            }
        }
    }
}


