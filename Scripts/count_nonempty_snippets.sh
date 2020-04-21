#!/bin/bash

################################################################################
# Count the number of hashes representing non-empty snippets for each file in
# the corpus. Save it in a csv file prefixed "snippetsPerFileNE".
################################################################################

# Create file with mapping from file to non-empty snippet hashes
file2hashesA=`./get_latest_output.sh file2hashesA`
file2hashesNE=`echo $file2hashesA | sed -E "s/file2hashesA/file2hashesNE/"`
emptyHash="D41D8CD98F00B204E9800998ECF8427E"
cat $file2hashesA | sed -E "s/\, $emptyHash//" > $file2hashesNE

# Count hashes
file2hashesTmp="../Output/file2hashes_with_comma.csv"
cp $file2hashesNE $file2hashesTmp
sed -Ei "s/^nb_/,nb_/" $file2hashesTmp	# In order to catch files without hashes
numSnippets=`sed -n "2,$ p" $file2hashesTmp | grep -o ',' -n | uniq -c | sed -E "s/^\s*//" | cut -d' ' -f1 | sed -E "s/^([0-9]+)$/\1-1/" | bc`
rm $file2hashesTmp

# Print result
numSnippetsFile="../Output/snippetsPerFileNE`date -Ins`.csv"
echo "file, non-empty snippets" > $numSnippetsFile
files=`sed -n "2,$ p" $file2hashesNE | cut -d',' -f1`
paste <(echo "$files") <(echo "$numSnippets") >> $numSnippetsFile
sed -Ei "s/\t/, /" $numSnippetsFile

# There are format errors in nb_1914546.ipynb, but it contains 3 non-empty code cells.
sed -Ei "s/^nb_1914546.ipynb\, 0$/nb_1914546.ipynb, 3/" $numSnippetsFile