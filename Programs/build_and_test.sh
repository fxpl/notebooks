#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 15:00
#SBATCH -p core -n 1
#SBATCH -J build_test_analyzer
#SBATCH -M snowy
#SBATCH --qos=short

ant test
