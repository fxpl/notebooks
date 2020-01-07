#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -p core -n 1
#SBATCH -t 5:00
#SBATCH -J count_notebooks
#SBATCH -M snowy

projDir="/proj/uppstore2019098"
logFile="../Logs/analyze-all-complete.out"
#TODO: Kör även efter flytt av oparsningsbart, och analyzern körd efter flytt

analyzed=`find $projDir/notebooks -name "*ipynb" | wc -l`
forked=`find $projDir/forked_notebooks -name "*ipynb" | wc -l`
unparseable=`find $projDir/unparseable_notebooks -name "*ipynb" | wc -l`
unparseable_unmoved=`egrep "Skipping notebook\!" $logFile | wc -l`

echo "Analyzed notebooks: $analyzed"
echo "Of these, processing failed for: ${unparseable_unmoved}"
echo "Forked notebooks: $forked"
echo "Unparseable notebooks: $unparseable"
