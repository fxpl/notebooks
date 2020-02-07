#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 70:00:00
#SBATCH -p core -n 2
#SBATCH -J notebook_pythonZipDumper

diskPath=/home/maka4186/notebook_disk
nbPath=$diskPath/notebooks
targetDir=$diskPath/snippets

java -XX:+UseParallelGC -cp Programs/bin:Programs/external/json-20190722.jar -Xmx12G \
	notebooks.PythonZipDumper $nbPath $targetDir


