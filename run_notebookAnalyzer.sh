#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 100:00:00
#SBATCH -p core -n 6
#SBATCH -J notebook_analyzer_all

diskPath=/home/maka4186/notebook_disk
nbPath=$diskPath/notebooks
reproFile=$diskPath/notebook-number_repo.csv
outputDirectory="Output"
mkdir -p $outputDirectory

java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -XX:+UseParallelGC \
	-cp Programs/bin:Programs/external/json-20190722.jar -Xms24G -Xmx24G \
	notebooks.NotebookAnalyzer --nb_path=$nbPath --repro_file=$reproFile --output_dir=$outputDirectory --all

