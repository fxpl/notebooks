#!/bin/bash

file_stats_file="file_stats"
repro_file="repros"
clone_pairs_file="clone_pairs"
num_files=209
num_friends=`echo ${num_files} - 1 | bc`

rm ${file_stats_file}
rm ${repro_file}
rm ${clone_pairs_file}

for i in `seq 1 ${num_files}`; do
	echo "$i,$i,\"/some/path/to/nb_$i.zip,nb_${i}_0.py\",\"NULL/${i}_0.py\",\"abc${i}\",10002,32,25,3" >> ${file_stats_file}
	repro_number=`echo "$i / 2" | bc`
	echo "$i,repro${repro_number}" >> ${repro_file}
done

for i in `seq 2 ${num_files}`; do
	echo "1,1,$i,$i" >> ${clone_pairs_file}
done

zip ${clone_pairs_file}.zip ${clone_pairs_file}

