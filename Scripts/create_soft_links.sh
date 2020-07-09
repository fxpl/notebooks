#!/bin/bash

################################################################################
# Create soft links needed by statistics R script.
################################################################################

prefixes=( "code_cells" "snippetsPerFileNE" "loc" "languages" "cloneFrequency" "connections" )

for prefix in ${prefixes[@]};
do
	rm -f ../Output/$prefix.csv
	csvFile=`./get_last_output.sh $prefix`
	ln -s $csvFile ../Output/$prefix.csv
done

filesPerSnippetNE=`./get_last_output.sh filesPerSnippet2`
rm -f ../Output/filesPerSnippetNE.csv
ln -s $filesPerSnippetNE ../Output/filesPerSnippetNE.csv

filesPerSnippetA=`ls -1 ../Output/filesPerSnippet2* | tail -2 | head -1`
rm -f ../Output/filesPerSnippetA.csv
ln -s $filesPerSnippetA ../Output/filesPerSnippetA.csv

