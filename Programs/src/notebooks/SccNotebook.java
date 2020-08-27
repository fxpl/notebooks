package notebooks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SccNotebook {
	private static String dumpSubDirName = "inter_connected_repros_dump";
	private static String dumpTargetDirName = "";
	private String name;
	private int repro;
	private int intraReproConnections;
	private int interReproConnections;
	// Repros that this notebook has an inter connections to
	private Set<Integer> reprosInterConnected;
	
	public SccNotebook(String name, int repro) {
		this.name = name;
		this.repro = repro;
		reprosInterConnected = new HashSet<Integer>();
	}
	
	public SccNotebook(SccNotebook model) {
		this(model.name, model.repro);
	}
	
	/**
	 * The notebook given as an argument should be connected to this notebook.
	 * Update connection info with this connection.
	 * @param connected Notebook that should be connected
	 */
	public void connect(SccNotebook connected) {
		if (this.repro == connected.repro) {
			this.intraReproConnections++;
			connected.intraReproConnections++;
		} else {
			this.interReproConnections++;
			connected.interReproConnections++;
			final int maxReproSetSize = 100;
			this.reprosInterConnected.add(connected.repro);
			this.dumpReprosIfLargerOrEq(maxReproSetSize);
			connected.reprosInterConnected.add(this.repro);
			connected.dumpReprosIfLargerOrEq(maxReproSetSize);
		}
	}
	
	/**
	 * Dump all repros to which this notebook has inter repro connections to
	 * file if the number of repros is larger than the argument given. See
	 * dumpRepros.
	 * @param Maximum number of inter connected repros
	 */
	public void dumpReprosIfLargerOrEq(int size) {
		if (size <= reprosInterConnected.size()) {
			dumpRepros();
		}
	}
	
	/**
	 * Read all repros that have been dumped to file
	 * @return Set of repros that have earlier been dumped to file.
	 */
	private Set<Integer> dumpedRepros() {
		Set<Integer> repros = new HashSet<Integer>();
		String dumpFileName = reproDumpName();
		File dumpFile = new File(dumpFileName);
		if (dumpFile.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(dumpFile));
				String line = reader.readLine();
				while (null != line) {
					int repro = Integer.parseInt(line);
					repros.add(repro);
					line = reader.readLine();
				}
				reader.close();
			} catch (IOException e) {
				System.err.println("Could not read dumped repros from "
						+ dumpFileName + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
		return repros;
	}
	
	/**
	 * Dump all repros to which this notebook has inter repro connections to
	 * file. The file will have the same name as the notebook, but with suffix
	 * "repros" instead of "ipynb" and be placed in
	 * dumpDir/inter_connected_repros_dump, where dumpDir can be set with the
	 * method set dumpDir. Default is dumpDir = ".". The inter repro connections
	 * set will be emptied after dumping.
	 */
	public void dumpRepros() {
		String dumpFileName = reproDumpName();
		FileWriter writer;
		try {
			writer = new FileWriter(dumpFileName, true);
			for (int repro: reprosInterConnected) {
				writer.append(repro + "\n");
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("Could not dump repros to "
					+ dumpFileName + ": " + e.getMessage());
			e.printStackTrace();
		}
		reprosInterConnected.clear();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof SccNotebook) {
			SccNotebook otherNotebook = (SccNotebook)other;
			return this.name.equals(otherNotebook.name);
		} else {
			return false;
		}
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.name);
	}
	
	public int numInterReproConnections() {
		return interReproConnections;
	}
	
	public int numIntraReproConnections() {
		return intraReproConnections;
	}
	
	/**
	 * @return The number of repros to which this notebook is connected, except the one it self lives in
	 */
	public int numReprosInterConnected() {
		Set<Integer> repros = dumpedRepros();
		repros.addAll(reprosInterConnected);
		return repros.size();
	}
	
	/**
	 * Remove all inter repro connections dumps.
	 */
	public static void removeDumpDirContents() {
		File dumpTargetDir = new File(dumpTargetDirName);
		for (String reproFile: dumpTargetDir.list()) {
			new File(dumpTargetDir + File.separator + reproFile).delete();
		}
		dumpTargetDir.delete();
	}
	
	/**
	 * @return Name of the repro dump file for this notebook.
	 */
	private String reproDumpName() {
		String[] nameParts = this.name.split("\\.");
		String fileName = "";
		for (int i=0; i<nameParts.length-1; i++) {
			fileName += nameParts[i];
		}
		return dumpTargetDirName + File.separator + fileName + ".repros";
	}
	
	/**
	 * Change to which directory the files with dumped inter repro connections
	 * will be placed. The directory will be created if nonexistent.
	 * @param dumpDir Name of directory where the dumps will be placed.
	 */
	public static void setDumpDir(String dumpDir) {
		dumpTargetDirName = dumpDir + File.separator + dumpSubDirName;
		new File(dumpTargetDirName).mkdirs();
	}
}
