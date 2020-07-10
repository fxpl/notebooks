#!/bin/bash

################################################################################
# Run all relevant post processing scripts except the R script on the output of
# NotebookAnalyzer and SccOutputAnalyzer.
################################################################################

source paths.sh

# Postprocessing for NotebookAnalyzer result
ln -s $OutputNBA Output
cd Scripts
./language_analysis.sh > ../Output/output_language_analysis.txt
./language_inconsistencies.sh > ../Output/output_language_inconsistencies.txt
./clone_analysis_nba.sh > ../Output/output_clone_analysis.txt
./print_most_common_snippets.sh 100 0 1 > ../Output/top100clones_min0.txt
./print_most_common_snippets.sh 100 4 1 > ../Output/top100clones_min4loc.txt
./list_duplicated_notebooks.sh

# Statistics for NotebookAnalyzer results
./create_sym_links_nba.sh
./get_notebook_sizes.sh
Rscript statistics_paperIII_nba.R > ../Output/statistics_output.txt
# Pack plots
./reduce_large_images.sh
cd ../Output
tar -czf plots.tgz hist_clone_frequency*eps lang*eps log_hist_*eps cells*jpg *Inter_*jpg
cd -

# If a notebook is written in Python and is not included in the clone frequency
# file in OutputSCC. it is empty. Find these and add to cloneFrequency and
# connections files in OutputSCC (since empty notebooks are not included in
# SourcererCC clones).
pythonNotebooks="pythonNotebooks.txt"
langFile=`./get_latest_output.sh "languages"`	# ../Output still points at NBA direcectory
grep -E "^nb_[0-9]+\.ipynb\, PYTHON\, " $langFile | cut -d',' -f1 > $pythonNotebooks
cd ..
rm Output
ln -s $OutputSCC Output
cd Scripts
sccNotebooks="sccNotebooks.txt"
cloneFreqSCC=`./get_latest_output.sh "cloneFrequency"`
connectionsSCC=`./get_latest_output.sh "connections"`
sed -n "2,$ p" $cloneFreqSCC | cut -d',' -f1 > $sccNotebooks
grep -vFf sccNotebooks.txt pythonNotebooks.txt | while read notebook; do
	echo "$notebook, 0, 0, 0, 0, 0, 0, 0" >> $cloneFreqSCC
	echo "$notebook, 0, 0.0000, 0, 0.0000, 0, 0, 0.0000, 0.0000" >> $connectionsSCC
done
rm $pythonNotebooks
rm $sccNotebooks

# Postprocessing for SourcererCC result
./clone_analysis_scc.sh > ../Output/output_clone_analysis.txt

#Statistics for SccOutputAnanlyzer results
./create_sym_links_scc.sh
Rscript statistics_paperIII_scc.R > ../Output/statistics_output.txt
# Pack plots
./reduce_large_images.sh
cd ../Output
tar -czf plots.tgz hist_clone_frequency*eps log_hist_*eps cells*jpg *Inter_*jpg
cd ..
rm Output

