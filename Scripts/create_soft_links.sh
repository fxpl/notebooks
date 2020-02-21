#!/bin/bash

################################################################################
# Create soft links needed by statistics R script.
################################################################################

files=( "code_cells" "loc" "languages" "cloneFrequency" "connections" )

for file in ${files[@]};
do
	csvFile=`./get_latest_output.sh $file`
	echo "ln -s $csvFile ../Output/$file.csv"
done
