#!/bin/bash

#SBATCH -A snic2019-8-228
#SBATCH -p core -n 1
#SBATCH -t 5:00
#SBATCH -J count_notebooks
#SBATCH -M snowy

projDir="/proj/uppstore2019098"
logFile="../logs/dumper-zip-python-complete.out"
#TODO: Kör även efter flytt av oparsningsbart, och analyzern körd efter flytt

processed=`find $projDir/notebooks -name "*ipynb" | wc -l`
forked=`find $projDir/forked_notebooks -name "*ipynb" | wc -l`
unparseable=`find $projDir/unparseable_notebooks -name "*ipynb" | wc -l`

unparseable_unmoved=`egrep "Skipping\!" $logFile | wc -l`	# TODO: Ändra till "Skipping notebook\!" innan nästa körning!
unparseable=`echo "$unparseable + ${unparseable_unmoved}" | bc`
processed=`echo "$processed - ${unparseable_unmoved}" | bc`

echo "Processed notebooks: $processed"	# TODO: Dubbelkolla med wc -l på output-fil
echo "Forked notebooks: $forked"
echo "Unparseable notebooks: $unparseable"
