#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 100:00:00
#SBATCH -p core -n 6
#SBATCH -J notebook_analyzer_all

outputDirectory="Output"
mkdir -p $outputDirectory

java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -XX:+UseParallelGC \
	-cp Programs/bin:Programs/external/json-simple-1.1.1.jar -Xms30G -Xmx30G \
	notebooks.NotebookAnalyzer --nb_path=/proj/uppstore2019098/notebooks --repro_file=/proj/uppstore2019098/notebooks/notebook-number_repo.csv --output_dir=$outputDirectory --all

