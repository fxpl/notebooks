#!/bin/bash

modulesFile=`./get_last_output.sh "modules"`

keywords=("False" "None" "True" "and" "as" "assert" "async" "await" "break" "class" "continue" "def" "del" "elif" "else" "except" "finally" "for" "from" "global" "if" "import" "in" "is" "lambda" "nonlocal" "not"  "or" "pass" "raise" "return" "try" "while" "with" "yield")

for keyword in ${keywords[@]}
do
	identifiers=`grep -Eo "(, |\.)$keyword(\(|\.)" $modulesFile | wc -l`
	aliases=`grep -Eo "\($keyword\)" $modulesFile | wc -l`
	echo $keyword, $identifiers, $aliases
done
