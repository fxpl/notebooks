#!/bin/bash

# Find the last file in ../Output whose name starts with the first argument to
# this script. In this context 'last' means the last one when the files are
# sorted by name in descending alphabetical order.
nameString=$1

ls -1 ../Output/$nameString* | tail -1

