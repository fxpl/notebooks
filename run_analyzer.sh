#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 100:00:00
#SBATCH -p core -n 6
#SBATCH -J notebook_analyzer_all

java -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics -XX:+UseParallelGC \
	-cp Programs/bin:Programs/external/json-simple-1.1.1.jar -Xmx36G \
	notebooks.Analyzer /proj/uppstore2019098/notebooks -all

