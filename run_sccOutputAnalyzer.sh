#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 100:00:00
#SBATCH -p core -n 5
#SBATCH -J scc_output_analyzer

diskPath=/home/maka4186/notebook_disk
reproFile=$diskPath/notebook-number_repo.csv
pairFile=$diskPath/SourcererCC_output/clone.pairs
statsFile=$diskPath/SourcererCC_output/files.stats
outputDirectory="OutputSCC"
mkdir -p $outputDirectory

java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -XX:+UseParallelGC \
	-cp Programs/bin:Programs/external/json-simple-1.1.1.jar -Xms28G -Xmx28G \
	notebooks.SccOutputAnalyzer --stats_file=$statsFile --repro_file=$reproFile --pair_file=$pairFile --output_dir=$outputDirectory
