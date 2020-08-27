# Directory containing all notebooks (ipynb files), possibly in subdirectories
nbPath=/huge/notebooks
# Output files from SourcererCC (clone.pairs.zip and file.stats) should be placed here:
sccDataDir=/huge/SourcererCC_output
# Directory for temporary files stored by SccOutputAnalyzer
tmpDir=/huge/soa_tmp_dir

# File with paths to all notebooks (relative to the location of the file)
notebookPathsFile=/huge/notebook_paths.txt
# File with mapping from notebook number to repro
reproFile=/huge/notebook-number_repo.csv

# Directory for output data from NotebookAnalyzer
outputNBA=./OutputNBA
# Directory for output data from SccOutputAnalyzer
outputSOA=./OutputSCC
# Directory where the files dumped by PythonDumper will be stored
pythonDumpDir=/huge/snippets_concatenated

# NOTE: No directory (or file) may be named Output and placed in this directory!
