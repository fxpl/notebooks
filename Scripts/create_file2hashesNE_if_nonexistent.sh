#!/bin/bash

################################################################################
# Find the last file file2hashesA<timestamp>.csv in ../Output. If
# hash2files<timestamp>.csv (same timestamp!) does not exist in ../Output,
# create it and populate it with a copy of file2hashesA<timestamp>.csv, but with
# hashes representing empty snippets removed.
################################################################################

file2hashesA=`./get_last_output.sh file2hashesA`
file2hashesNE=`echo $file2hashesA | sed -E "s/file2hashesA/file2hashesNE/"`
if [ ! -f $file2hashesNE ]; then
       hash2filesA=`./get_last_output.sh hash2filesA`
       emptyHashes="("`grep -E "^[A-F,a-f,0-9]+\, 0\, nb_" $hash2filesA | cut -d',' -f1 | paste -sd "|" | sed -E "s/\|/\)\|\(/g"`")"
       sed -E "s/\, $emptyHashes//g" $file2hashesA > $file2hashesNE
fi


