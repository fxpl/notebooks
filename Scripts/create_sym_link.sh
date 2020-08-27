#!/bin/bash

################################################################################
# For the specified file prefix p, make sure that ../Output/p.csv doesn't exist.
# Find the last file prefixed p and create a symbolic link p.csv to this file in
# ../Output.
################################################################################
createSymLink() {
	prefix=$1
	rm -f ../Output/$prefix.csv
	csvPath=`./get_last_output.sh $prefix`
	file=`echo $csvPath | rev | cut -d'/' -f1 | rev`
	dir=`echo $csvPath | rev | cut -d'/' -f2- | rev`
	cd $dir
	ln -s $file $prefix.csv
	cd -
}

