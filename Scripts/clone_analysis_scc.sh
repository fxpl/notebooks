#!/bin/bash

source clone_functions.sh

echo ""
echo "NOTE: This analysis considers non-empty snippets only!"
echo ""
echo "CLONE DATA"
analyzeClones 6 true false
echo ""
echo "INTRA CLONE"
intraClones 8
echo ""
echo ""

