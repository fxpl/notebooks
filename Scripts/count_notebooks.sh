#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -p core -n 1
#SBATCH -t 5:00
#SBATCH -J count_notebooks
#SBATCH -M snowy

################################################################################
# Count the number of notebooks in input directory, in output files and in
# directories for forked and unparseable notebooks.
################################################################################

projDir="/proj/uppstore2019098"
logFile="../Logs/analyze-all-complete.out"
codeCells=`./get_latest_output.sh "code_cells"`

outputLines=`wc -l $codeCells | cut -d' ' -f1`
outputNotebooks=`echo "$outputLines - 1" | bc`
inDir=`find $projDir/notebooks -name "*ipynb" | wc -l`
duplicated=`find $projDir/duplicated_notebooks -name "*ipynb" | wc -l`
forked=`find $projDir/forked_notebooks -name "*ipynb" | wc -l`
unparseable=`find $projDir/unparseable_notebooks -name "*ipynb" | wc -l`
unparseable_unmoved=`egrep "Skipping notebook\!" $logFile | wc -l`
# TODO: Kan samma notebook loggas som skippad flera gånger? Kör om med bara en analys för att få fram denna siffra!?

echo "Notebooks in input directory: $inDir"
echo "Analyzed notebooks: $outputNotebooks"
echo "Of these, processing failed for: ${unparseable_unmoved}"
echo "Forked (moved) notebooks: $forked"
echo "Unparseable (moved) notebooks: $unparseable"
echo "Duplicated (moved) notebooks: $duplicated"
