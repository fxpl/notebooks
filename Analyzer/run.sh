#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 3:00:00
#SBATCH -p core -n 16
#SBATCH -J notebook_analyzer
#SBATCH -M snowy

java -XX:+UseParallelGC -cp bin:./external/json-simple-1.1.1.jar Analyzer /home/maka4186/notebooks/df_over_100mb -count
#java -XX:+UseParallelGC -cp bin:./external/json-simple-1.1.1.jar Analyzer /home/maka4186/notebooks -count # --> Fler timmar!!!
