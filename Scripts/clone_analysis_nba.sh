#!/bin/bash

source clone_functions.sh

# Create hash2files file without empty snippets
hash2filesA=`./get_last_output.sh "hash2filesA"`
hash2filesNE=`echo $hash2filesA | sed -E "s/hash2filesA/hash2filesNE/"`
emptyPattern="^[A-F,a-f,0-9]+\, 0\, nb_"	# Pattern for line representing an empty snippet
sed -E "/$emptyPattern/d" $hash2filesA > $hash2filesNE

# Perform analyses
echo ""
echo "CLONE GROUP DATA, ALL SNIPPETS:"
analyzeCloneGroups $hash2filesA
echo ""
echo "CLONE DATA, ALL SNIPPETS:"
analyzeClones 5 false
echo ""
echo "INTRA CLONES, ALL SNIPPETS:"
intraClones 7
echo ""
echo ""
echo "CLONE GROUP DATA, EMPTY SNIPPETS EXCLUDED:"
analyzeCloneGroups $hash2filesNE
echo ""
echo "CLONE DATA, EMPTY SNIPPETS EXCLUDED:"
analyzeClones 6 false
echo ""
echo "INTRA CLONES, EMPTY SNIPPETS EXCLUDED:"
intraClones 8
echo ""
echo ""

