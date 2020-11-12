#!/bin/bash

outputDir=../Output

functionTopListFile=$outputDir/function_top_list`date -Ins`.csv
modulesFile=`./get_last_output.sh "modules"`
moduleTopListFile=`./get_last_output.sh "module_top_list"`
extendedModuleTopListFile=`echo $moduleTopListFile | sed -E "s/module_top_list/extended_module_top_list/"`

# Create functions top list
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

