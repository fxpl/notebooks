#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 100:00:00
#SBATCH -p core -n 5
#SBATCH -J scc_output_analyzer

outputDirectory="Output"
mkdir -p $outputDirectory

java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -XX:+UseParallelGC \
	-cp Programs/bin:Programs/external/json-simple-1.1.1.jar -Xmx36G \
	notebooks.SccOutputAnalyzer --stats_file=Programs/test/data/scc/file_stats --repro_file=Programs/test/data/hash/repros.csv --pair_file=Programs/test/data/scc/clone_pairs --output_dir=$outputDirectory
