# Source Code and Scripts for Analyzing Jupyter Notebooks

This repository contains the code and scripts used for analyzing notebooks as
presented in the following papers:
 * "Jupyter Notebooks on GitHub: Characteristics and Code Clones" by Källén, Sigvardsson and Wrigstad.
 * "To Err or Not to Err?" by Källén and Wrigstad

The Java code (dumpers and analyzers) can be found in the directory `Programs`
while post processing scripts are located in `Scripts`. Python scripts are found
in `Programs/python`.

 
## Execution environment
As can be seen, most scripts are written in bash and require bash in order to
be run. It may be possible to run the Java code in other environments, but it is
only tested on Linux.

Note that some of the scripts are written in R. Accordingly, if you want to
run all post processing scripts, R must be installed.

If you want to use `NotebookAnalyzer` (see below) to list imported modules and
frequently called functions, you also need Python 3, including the modules that
can be expected to be most common.


## Building and testing the Java code
To build the Java code, enter the directory `Programs` and type:

	ant build

If you also want to run the tests, instead (or additionally) type:

	ant test

in the same directory.


## Java programs
The following Java programs are build by ant:
 * `NotebookAnalyzer`: performs different analyses on the notebooks
 * `SccOutputAnalyzer`: analyzes output data from SourcererCC
 * `PythonDumper`: dumps the content of each code cell in the notebooks to a
   separate Python file
 * `PythonZipDumper`: dumps the content of each code cell in the notebooks to a
   separate Python file and wraps the Python files from each notebook in one zip file.
 * `SnippetPrinter`: prints the code of a specified snippet to standard out
(SnippetPrinter is not supposed to be run separately, but is used by the script
`print_most_common_snippets.sh`, which prints the most common snippets in the
corpus.)

### Input data
All Java programs except `SccOutputAnalyzer` take the notebooks as input data.
Each notebooks should be named `nb_<num>.ipynb`, where `<num>` is an integer.
The directory containing the `ipynb` files is specified with an argument to
`NotebookAnalyzer`, and  `*Dumper` respectively. The directory may
contain non-notebook files as well. Notebook files can be stored in sub
directories, since the programs recursively looks for files ending with `ipynb`
in all sub directories.

When function calls are to be listed (i.e. when the flag `--functions=<FILE>`
is provided), the file pointed out by the `--functions` flag should contain the
functions to which calls should be listed, one per line, on the format:
```<module_name>.<function_name>```.

`SccOutputAnalyzer` takes the output of a run of SourcererCC as input. More
specifically, it needs the pair file produced by the clone detector on zipped
format, named `clone.pairs.zip`, and the contents of `files_stats` produced by
the tokenizer, saved in one file named `files.stats`.

Additionally, `SccOutputAnalyzer` needs a file with a mapping from each
notebook number (`<num>` from the file name) to a repository, stored in a
separate file. This file is also needed by `NotebookAnalyzer` when running the
clone analysis.
The repository mapping file should be an CSV file containing one line per
notebook. Each line is supposed to contain two values: the notebook number and
the repository, for example:

```3197563,https://github.com/someUser/someRepro```

If you want to use our input data (approximately 1 TB notebooks and
the accopanying notebook-repository mapping file), it can be downloaded from
https://export.uppmax.uu.se/snic2020-6-136/notebooks.zip. The data set contains
a large number of groups of notebooks where all notebooks in a certain group
contain an identical sequence of code cells (where a code cell is considered
identical to its CMW clones). If you want to exclude all notebooks but one from
each of these groups, you can also download
https://export.uppmax.uu.se/snic2020-6-136/notebook_clones.csv and exclude all
notebooks listed in this file from the analysis.

### Execution

Below is a description of how to execute the Java programs. Examples of commands
for running each program can be found in the scripts `run_notebookAnalyzer_ccc.sh`,
`run_notebookAnalyzer_mod.sh`, `run_pythonDumper.sh` and `run_sccOutputAnalyzer.sh`
respectively.

#### Notebook Analyzer
When the notebook analyzer is run, `org.json` must be in the class path. A jar
file is provided in `Programs/external`. If you want to use the modules
functionality or function listing, i.e. if you provide any of the arguments
`--modules` and `functions=<FILE>`, also Programs/python must be in the class
path.

`NotebookAnalyzer` takes the following arguments:
 * `--nb_path=NB_PATH`, where `NB_PATH` is the path to a directory containing
    the notebook files to be analyzed. You may also set `NB_PATH` to the path
	to a notebook if you only want to analyze that notebook. If `NB_PATH`
	is a directory, all files that end with `.ipynb` in `NB_PATH` and all sub
	directories will be analyzed.
 * `--output_dir=OUTPUT_DIR`, where `OUTPUT_DIR` is the directory where the
   output of the program will be put. This directory must exist! If this
   argument is not specified, all output files are placed in the current
   directory.
 * `--repro_file=REPRO_MAPPING_PATH`, where `REPRO_MAPPING_PATH` is the path to
   the file containing the mapping from notebook number to repository. (See
   section about input data.) This argument is only needed when the clone
   analysis is to be run.
 * `--count` if the program should count the number of notebooks and code cells
   in the input data
 * `--lang` if the program should run a language analysis
 * `--lang-all` if the program should extract all languages specified in each
   notebook
 * `--loc` if the program should count the number of lines of code of the
   notebooks
 * `--clones` if the program should run the clone analysis
 * `--ccc` if all analyzes listed above (i.e. all analyses made for the paper
   "Jupyter Notebooks on GitHub: Characteristics and Code Clones") should be
   run. (The number of notebooks will not be presented explicitly, but is easily
   found by a line count of the output files.)
 * `--modules` if the program should list imported modules, and the most
   frequently called functions in the top ten most imported modules.
 * `--functions=FCN_LIST_FILE`, if all calls to the functions specified in
   `FCN_LIST_FILE` should be listed.

The arguments can be given in any order, and several analyses can be run in the
same execution (i.e. you may combine the arguments `--count`, `--lang`,
`--lang-all`, `--loc`, `--clones`, `--modules` and/or `--functions`).


#### PythonDumper and PythonZipDumper
When running the dumper programs, `org.json` must be in the class path. A jar
file is provided in `Programs/external`.

The programs take two arguments: the path to the directory containing the
notebooks and the output directory, for example:
```java -cp Programs/bin:Programs/external/json-20190722.jar ~/notebooks ~/snippets```.
For a description of the two directories, see the description of `NB_PATH` and
`OUTPUT_DIR` in the description of the arguments to `NotebookAnalyzer`. However,
note that the paths should not be preceeded by `--nb_path=` or `--output_dir`
respectively when the dumpers are run.


#### SccOutputAnalyzer
`SccOutputAnalyzer` takes the following arguments:
 * `--stats_file=STATS_FILE`, where `STATS_FILE` is the path to one(!) file
   containing the contents of the files placed in the directory `files_stats`
   produced by the SourcererCC tokenizer.
 * `--pair_file=PAIR_FILE.zip`, where `PAIR_FILE.zip` is a zipped file
   containing all clone pairs identified by the SourcererCC clone detector (i.e.
   the files `NODE_*/output8.0/query_*`).
 * `--output_dir=OUTPUT_DIR`, where `OUTPUT_DIR` is the directory where the
   output of the program will be put. This directory must exist! If this
   argument is not specified, all output files are placed in the current
   directory.
 * `--repro_file=REPRO_MAPPING_PATH`, where `REPRO_MAPPING_PATH` is the path to
   the file containing the mapping from notebook number to repository. (See
   section about input data.)
 * `--tmp-dir=TMP_DIR`, where `TMP_DIR` is the path to a directory where
   temporary data can be stored. Note that the temporory data may be very large
   (~100GB for our corpus of 2.5 million Python noteooks).


### Output data

#### PythonDumper and PythonZipDumper
The output of the dumper programs are files containing the content of the code
cells of all Python notebooks, as described at the top of this section ("Java
programs").

#### Notebook Analyzer
The output of the notebook analyzer is stored on CSV format. Below is a list of
the files produced by the different analyses.

The following files are produced by each analysis respectively:
 * Cell count: `code_cells<timestamp>.csv`, which contains the number of code
   cells in each notebook.
 * Language analysis (as run with `--lang`): `languages<timestamp>.csv`, which
   lists the programming language that each notebook is written in, and the
   field of the notebook from which the language information was collected.
 * Language analysis (as run with `--lang-all`):
   `all_languages<timestamp>.csv`, which for each notebook lists the language
   information found in the fields `metadata.language`,
   `metadata.languageinfo.name`, `metadata.kernelspec.language`,
   `metadata.kernelspec.name` and the code cells respectively.
 * Line count: `loc<timestamp>.csv`, which, for each notebook, contains the
   total source line count, the number of non-empty lines of code and the number
   of empty lines in the code cells respectively.
 * The clone analysis produces four CSV files:
 	* file2hashesA<timestamp>.csv contains a list of the MD5 hash of
	  each code cell in each notebook.
	* hash2filesA<timestamp>.csv contains one line per unique code snippet.
	  The line contains the MD5 hash and line count of the snippet, followed by
	  a list of all places where this code snippet can be found, on the format
	  `notebook_name1, cell_index1, notebook_name2, cell_index2, ...`.
	  The value 0 of a cell index means the first code cell in the
	  notebook, 1 means the second code cell in the notebook and so on.
	* cloneFrequency<timestamp>.csv contains the clone frequency of each
	  notebook. Several metrics are presented, namely:
	  * total number of unique code cells in the notebook
	  * total number of cloned code cells in the notebook
	  * number of code cells without code
	  * fraction of the snippets that are clones, all snippets included
	  * fraction of the snippets that are clones, empty snippets excluded
	  * fraction of the snippets that are intra notebook clones, all snippets
	    included
	  * fraction of the snippets that are intra notebook clones, empty snippets
	    excluded
	* connections<timestamp>.csv contains information about connections from
	  each notebook. For a definition of connection, see "Jupyter Notebooks on
	  Github: Characteristics and Code Clones" by Källén, Sigvardsson and
	  Wrigstad. The following metrics are presented:
	  	* total number of connections
		* total number of connections, normalized
	  	* total number of connections, empty snippets excluded
		* total number of connections, empty snippets excluded, normalized
		* number of intra repro connections
		* number of intra repro connections, normalized
		* mean number of inter repro connections per code cell
		* mean number of inter repro connections per code cell, empty snippets
		  excluded

	  where normalized means that the metric is divided by the total number of
	  code cells in the notebook.
 * The module listing creates 12 CSV files:
    * `modules<timestamp>.csv`, with all imported modules listed for each notebook.
    * `module_top_list<timestamp>.csv` which lists the 100 most frequently imported
      modules and the number of times each module is imported.
    * 10 files with names on the format `<module>-functions<timestamp>.csv`, one
      for each of 10 most frequently imported modules. Each of these files lists
      all functions in the module that are called in a notebook in the corpus,
      and the number of times each function is called. The list of functions is
      sorted on the number of calls to each function, in descending order.
 * The function calls listing creates one file per function listed in the file
   pointed out by `--functions`. The file names are on the format
   `<module>.<function>-calls<timestamp>.csv`. Each file lists all calls to the
   function whose name is included in the file name. Each call is preceeded by
   the notebook in which it was found, followed by ':'.

For details on how the data is collected, see "Jupyter Notebooks on GitHub:
Characteristics and Code Clones" by Källén, Sigvardsson and Wrigstad and
"To Err or Not to Err?" by Källén and Wrigstad.

#### SccOutputAnalyzer
Just as the output from the notebook analyzer, that of `SccOutputAnalyzer`
is stored on CSV format. The following files are created:
 * `cloneLoc<timestamp>.csv` has no header and contains the line count
   for each snippet that has at least one clone.
 * `cloneFrequency<timestamp>.csv`contains information about clone frequencies
   as described for `NotebookAnalyzer`.
 * `connections<timestamp>.csv` contains information about connections as
   described for `NotebookAnalyzer`.


## risky_comb_finder.py
This Python script can be used to find smelly argument combinations for in
calls to `numpy.array`, `pandas.read_csv`, `pandas.DataFrame`,
`matplotlib.pyplot.plot`, `matplotlib.pyplot.show`, `numpy.arange` and
`numpy.zeros`. Output files from `NotebookAnalyzer`'s function calls listing
are supposed to be used as input files. For details on input, output and
arguments, see the documentation in `risky_comb_finder.py`.


## Scripts
Scripts that can be used for post processing of the CSV files can be found in
the directory `Scripts`. Each bash script contains a description of its
behavior, and parameters --if any. The R scripts are used for producing plots
and perform the statistical analyses presented in the paper "Jupyter Notebooks
on GitHub: Characteristics and Clones".

The following scripts are intended to be used for post processing:
 * clone_analysis_nba.sh
 * clone_analysis_scc.sh
 * get_notebook_sizes.sh
 * language_analysis.sh
 * language_inconsistencies.sh
 * list_duplicated_notebooks.sh
 * print_most_common_snippets.sh
 * statistics_ccc_nba.R (with create_sym_links_nba.sh as preprocessing)
 * statistics_ccc_scc.R (with create_sym_links_scc.sh as preprocessing)
 * modules_post_processing.sh
 * count_param_usages.sh
 * find_keywords.sh

Before running post processing scripts whose names contain `scc`, you need to
create a symbolic link called `Output` in the root directory of this repository
pointing at the
directory where the output from `SccOutputAnalyzer` is located.
Before running any of the other post processing scripts, you need to
create a symbolic link named `Output` in the root directory of this repository
pointing at the directory where the output from `NotebookAnalyzer` is located.

After having executed the R scripts, you may want to run
`reduce_large_images.sh`.

An example of how to run the post processing of data produced when
`NotebookAnalyzer` is given the flag `--ccc` can be found in
`run_post_processing_ccc.sh` which is located in the root directory of this
repository.

Scripts whose name start with `move_` were used to move notebooks that we
didn't include in our analyses. You will not be able to run these; they are
only included for transparency. Notebooks that were moved by these scripts,
except `move_cloned_notebooks.sh`, are not included in the data set that we have
made available.

The scripts not mentioned in this section are helper scripts for the post
processing scripts.


## Repeating our results
Before performing the steps listed below, you need to build the Java code, see
description above.

### Jupyter Notebooks on GitHub: Characteristics and Code Clones
If you want to repeat the results of "Jupyter Notebooks on Github:
Characteristics and Code Clones" by Källén, Sigvardsson and Wrigstad" you need
to do the following:
1. Download all notebooks and the notebook-repository mapping file from
   https://export.uppmax.uu.se/snic2020-6-136/notebooks.zip and unzip the file.
2. Change the paths in `paths.sh` according to the inline comments.
3. Execute `run_notebookAnalyzer_ccc.sh`.
4. Execute `run_pythonDumper.sh`.
5. Do the clone analysis with SourcererCC
   (https://github.com/Mondego/SourcererCC) according to their instructions,
   including concatenation of output files.
6. Zip `results.pairs` and save the zip file with the name `clone.pairs.zip`.
   Concatenate the files in the directory `files_stats` into one file named
   `files.stats`. Place `clone.pairs.zip` and `files.stats` in the directory
   that you have provided in `paths.sh`.
7. Execute `run_sccOutputAnalyzer.sh`.
8. Execute `run_post_processing_ccc.sh`. This must not be done before completion
   of step 3 and 7. Check `outputNBA` and `outputSOA` for output.

Step 3 can be done in parallel with steps 4-7.

### To Err or Not to Err?
If you want to repeat the results of "To Err or Not to Err?" by Källén and
Wrigstad, you need to do the following:
 1. Download all notebooks from
    https://export.uppmax.uu.se/snic2020-6-136/notebooks.zip and unzip the file.
 2. Download https://export.uppmax.uu.se/snic2020-6-136/notebook_clones.csv and
    remove all notebooks listed in this file from the dataset, or move them to a
    separate directory.
 3. Update `nbPath` and `outputNBA` in `paths.sh` according to the instructions
    in the file.
 4. Make sure that all Python modules that are expected to be frequently
    imported in the courpus are installed on your system. See the paper for a
    list of frequently imported modules.
 5. Execute `run_notebookAnalyzer_mod.sh`: `./run_notebookAnalyzer_mod.sh`.

    If you get many output lines saying: "Module <name> is not available. Some
    function calls for this module will not be identified." for certain values
    of `<name>`, you might want to interrupt the execution and install these
    Python modules.
 6. Create a symbolic link to your output directory (`outputNBA` in `paths.sh`)
    The link must be named `Output` and placed in the same directory as the
    directory `Scripts` (i.e. the root directory of this repository):
	`ln -s <name-of-your-output-directory> Output`.
 7. Enter the scripts directory and execute `modules_post_processing.sh`:
    `cd Scripts`
    `./modules_post_processing.sh`
 8. If you also want to list the keywords used as module names (see Appendix C),
    execute `find_keywords.sh`: `./find_keywords.sh`.
 9. Step back to the root directory of the repository: `cd ..`.
10. In `run_notebookAnalyzer_mod.sh`, replace `--modules` with
    `--functions=$outputDir/functions_to_list.csv`.
11. Execute `run_notebookAnalyzer_mod.sh` again.
12. For each file $f named `<module>.<function>-calls<timestamp>.csv`, run:
    `python3 Programs/python/risky_comb_finder.py Output/$f Output`.
13. Remove the symbolic link created in step 4: `rm Output`.

If the files $f list calls to `numpy.array`, `pandas.read_csv`,
`pandas.DataFrame`, `matplotlib.pyplot.plot`, `matplotlib.pyplot.show`,
`numpy.arange` and `numpy.zeros`, `risky_comb_finder.py` (step 11) will identify
calls with smelly argument combinations in the lists of calls to the functions.
Risky parameter combinations are not identified, and `risky_comb_finder.py`
will not identify any smelly argument combinations, for other functions.

