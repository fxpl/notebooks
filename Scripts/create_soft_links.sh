#!/bin/bash

################################################################################
# Create soft links needed by statistics R script.
################################################################################

prefixes=( "code_cells" "loc" "languages" "cloneFrequency" "connections" )

for prefix in ${prefixes[@]};
do
	rm -f ../Output$prefix.csv
	csvFile=`./get_latest_output.sh $prefix`
	ln -s $csvFile ../Output/$prefix.csv
done

