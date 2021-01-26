#!/bin/bash -l

source paths.sh
outputDir=$outputNBA

mkdir -p $outputDir
java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -XX:+UseParallelGC \
	-cp Programs/bin:Programs/python:Programs/external/json-20190722.jar -Xms30G -Xmx30G \
	notebooks.NotebookAnalyzer --nb_path=$nbPath --repro_file=$reproFile --output_dir=$outputDir --ccc

