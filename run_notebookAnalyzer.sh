#!/bin/bash -l

source paths.sh
outputDirectory=$outputNBA

mkdir -p $outputDirectory
java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -XX:+UseParallelGC \
	-cp Programs/bin:Programs/external/json-20190722.jar -Xms24G -Xmx24G \
	notebooks.NotebookAnalyzer --nb_path=$nbPath --repro_file=$reproFile --output_dir=$outputDirectory --all

