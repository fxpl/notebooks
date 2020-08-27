#!/bin/bash

################################################################################
# Create symbolic links needed by statistics R script.
################################################################################

source create_sym_link.sh
prefixes=( "code_cells" "loc" "languages" "cloneFrequency" "connections" )

for prefix in ${prefixes[@]};
do
	createSymLink $prefix
done

rm -f ../Output/filesPerSnippetNE.csv
rm -f ../Output/filesPerSnippetA.csv
filesPerSnippetNE=`./get_last_output.sh filesPerSnippet2 | rev | cut -d'/' -f1 | rev`
filesPerSnippetA=`ls -1 ../Output/filesPerSnippet2* | tail -2 | head -1 | rev | cut -d'/' -f1 | rev`
cd ../Output
ln -s $filesPerSnippetNE filesPerSnippetNE.csv
ln -s $filesPerSnippetA filesPerSnippetA.csv

