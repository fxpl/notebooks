#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 24:00:00
#SBATCH -p core -n 1
#SBATCH -J notebook_pythonDumper

java -XX:+UseParallelGC -cp bin:./external/json-simple-1.1.1.jar \
	notebooks.PythonDumper /proj/uppstore2019098/notebooks /proj/uppstore2019098/snippets


