#!/bin/bash -l

################################################################################
# For each group of cloned notebooks, move all notebooks except one from
# /huge/notebooks to /huge/notebook_clones.
################################################################################

srcDir=/huge/notebooks
targetDir=/huge/cloned_notebooks

mkdir -p $targetDir

head -n -1 ../OutputNBA/nb_clone_listA.csv | cut -d' ' -f2- | tr ' ' '\n' | while read notebook
do
	mv $srcDir/$notebook $targetDir
done

