#!/bin/bash -l

source paths.sh
outputDir=$outputNBA

mkdir -p $outputDir
java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -XX:+UseParallelGC \
	-cp Programs/bin:Programs/external/json-20190722.jar -Xms24G -Xmx24G \
	notebooks.NotebookAnalyzer --nb_path=$nbPath --repro_file=$reproFile --output_dir=$outputDir --all

