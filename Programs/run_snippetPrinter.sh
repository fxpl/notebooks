#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 1:00
#SBATCH -p core -n 1
#SBATCH -J notebook_snippetPrinter

java -XX:+UseParallelGC -cp bin:./external/json-simple-1.1.1.jar \
	notebooks.SnippetPrinter test/data/dump/nb1_str.ipynb 0

java -XX:+UseParallelGC -cp bin:./external/json-simple-1.1.1.jar \
	notebooks.SnippetPrinter test/data/dump/nb1_str.ipynb 1

