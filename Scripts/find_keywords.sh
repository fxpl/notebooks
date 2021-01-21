#!/bin/bash

################################################################################
# List the number of times each Python keyword is used as a module identifier.
# Two numbers are listed for each keyword. The first number indicates how many
# times the keyword is used as name for an imported module. The second number
# indicates how many times the keyword is used as qualifier for a module.
#
# Before execution of this script, NotebookAnalyzer is supposed to be run with
# the "--modules" flag, and the resulting files must be stored in ../Output.
################################################################################

modulesFile=`./get_last_output.sh "modules"`

keywords=("False" "None" "True" "and" "as" "assert" "async" "await" "break" "class" "continue" "def" "del" "elif" "else" "except" "finally" "for" "from" "global" "if" "import" "in" "is" "lambda" "nonlocal" "not"  "or" "pass" "raise" "return" "try" "while" "with" "yield")

for keyword in ${keywords[@]}
do
	identifiers=`grep -Eo "(, |\.)$keyword(\(|\.)" $modulesFile | wc -l`
	aliases=`grep -Eo "\($keyword\)" $modulesFile | wc -l`
	echo $keyword, $identifiers, $aliases
done
