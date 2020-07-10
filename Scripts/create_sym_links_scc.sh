#!/bin/bash

################################################################################
# Create symbolic links needed by statistics R script.
################################################################################

source create_sym_link.sh
prefixes=( "cloneLoc" "cloneFrequency" "connections" )

for prefix in ${prefixes[@]};
do
	createSymLink $prefix
done

