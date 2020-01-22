#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 70:00:00
#SBATCH -p core -n 2
#SBATCH -J notebook_pythonZipDumper

java -XX:+UseParallelGC -cp Programs/bin:Programs/external/json-simple-1.1.1.jar -Xmx12G \
	notebooks.PythonZipDumper /proj/uppstore2019098/notebooks /proj/uppstore2019098/snippets


