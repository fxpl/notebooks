#!/bin/bash -l

#SBATCH -A snic2019-8-228
#SBATCH -t 5:00
#SBATCH -p core -n 1
#SBATCH -J language_analysis
#SBATCH -M snowy

################################################################################
# Count the number of notebooks written in Python, Julia, R and Scala
# respectively. Also count the number of notebooks with another language, or no
# language at all, specified. Report result in pure numbers and percentages.
################################################################################

LC_NUMERIC="en_US.UTF-8"

languages=( PYTHON JULIA R SCALA OTHER UNKNOWN )
file=`./get_latest_output.sh "languages"`
total=`sed -n "2,$ p" $file | wc -l`
percentages="("
numbers="("

i=0
for language in ${languages[@]}; do
	num=`sed -n "2,$ p" $file | cut -d',' -f2 | egrep " $language$" | wc -l`
	perc=`echo 100*$num/$total | bc -l`
	perc=`printf "%.4f" $perc`
	echo "$language: $num/$total ($perc%)"
	if [ $i -gt 0 ]; then
		percentages="$percentages,"
		numbers="$numbers,"
	fi
	numbers="$numbers $num"
	percentages="$percentages $perc"
	((i++))
done
numbers="$numbers )"
percentages="$percentages )"

echo "Numbers: $numbers"
echo "Percentages: $percentages"

