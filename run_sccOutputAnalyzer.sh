#!/bin/bash -l

source paths.sh
pairFile=$sccDataDir/clone.pairs.zip
statsFile=$sccDataDir/files.stats
outputDir=$outputSOA

mkdir -p $outputDirectory
java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -XX:+UseParallelGC \
	-cp Programs/bin -Xms28G -Xmx28G \
	notebooks.SccOutputAnalyzer --stats_file=$statsFile --repro_file=$reproFile --pair_file=$pairFile --output_dir=$outputDir --tmp_dir=$tmpDir
