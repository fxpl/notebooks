#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 70:00:00
#SBATCH -p core -n 5
#SBATCH -J notebook_analyzer_clones

java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -XX:+UseParallelGC \
	-cp Programs/bin:Programs/external/json-simple-1.1.1.jar -Xmx30G \
	notebooks.Analyzer /proj/uppstore2019098/notebooks -clones

