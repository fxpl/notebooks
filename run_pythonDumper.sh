#!/bin/bash -l

source paths.sh

mkdir -p $pythonDumpDir
java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -XX:+UseParallelGC \
	-cp Programs/bin:Programs/external/json-20190722.jar -Xms12G -Xmx12G \
	notebooks.PythonZipDumper $nbPath $pythonDumpDir


