#!/bin/bash

################################################################################
# Perform post processing of the output from NotebookAnalyzer's module counting.
################################################################################

outputDir=../Output

functionTopListFile=$outputDir/function_top_list`date -Ins`.csv
modulesFile=`./get_last_output.sh "modules"`
moduleTopListFile=`./get_last_output.sh "module_top_list"`
extendedModuleTopListFile=`echo $moduleTopListFile | sed -E "s/module_top_list/extended_module_top_list/"`

# Print total and mean number of imports in the notebooks to stdout
moduleLines=`wc -l $modulesFile | cut -d' ' -f1`
numNotebooks=$((moduleLines-1))
numImports=`sed -n "2,$ p" $modulesFile | grep -o ',' | wc -l`
meanNumImports=`echo "$numImports/$numNotebooks" | bc -l`
echo "Number of notebooks: $numNotebooks"
echo "Number of imports: $numImports"
echo "Mean number of imports per notebook: $meanNumImports"

# Create functions top list containing the 100 most frequently called functions
# from each module.
functionNames=$outputDir/functions.txt
numbers=$outputDir/numbers.txt
tmp=$outputDir/tmp.txt
ls -1rt $outputDir/*-functions* | tail | while read file
do
	module=`echo $file | cut -d'-' -f1 | rev | cut -d'/' -f1 | rev`
	sed -n "2,101p" $file | cut -d',' -f1 | sed -E "s/^/$module./" >> $functionNames
	sed -n "2,101p" $file | cut -d',' -f2 >> $numbers
done

paste --delimiters=", " $numbers $functionNames > $tmp
sort -nr $tmp > $functionTopListFile
rm $functionNames
rm $numbers
rm $tmp

# Print total number of imports and number of notebooks importing the module for top modules
cat $moduleTopListFile | while read line
do
	module=`echo $line | cut -d',' -f1`
	importing_notebooks=`grep -E "\, $module\(" $modulesFile | wc -l`
	echo "$line, ${importing_notebooks}" >> $extendedModuleTopListFile
done

# Create input file for function listing
nbaInputFile=$outputDir/functions_to_list.csv
head -7 $functionTopListFile | cut -d',' -f2 > $nbaInputFile

