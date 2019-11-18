#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 140:00:00
#SBATCH -p core -n 4
#SBATCH -J notebook_analyzer

java -XX:+UseParallelGC -cp bin:./external/json-simple-1.1.1.jar notebooks.Analyzer /proj/uppstore2019098/notebooks -count
