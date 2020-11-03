"""
Extract module and function name from the first argument, list statements from
this file that are executable in isolation. Among thesee statements, find risky
combinations of values.

Arguments:
1: The path to the input file, containing function calls to be checked, one per
   line. Every function call shall target the same function. The statements may
   start with names/aliases for the module. These will be ignored. The name of
   the file should be on the format
   [<directory>/]<module>.<function_name>-<any string>, where <module> is the
   name of the module where the function can be found and <function_name> is
   the name of the function that is called.
2: (Optional) The path to the directory where the output will be stored

Output:
* A file named <function_name>_executable.csv containing all calls that are
  executable in isolation.
* One file per risky parameter combination for which the risky arguments
  are found. The files contain lists of all risky function calls. Each file is
  named <function_name>.<risky_combination>.csv.
"""

import sys
from risky_comb_functions import find_risky_combs


if len(sys.argv) < 2 or len(sys.argv) > 3:
	print("One or two arguments expected: path to input file and (optionally) output directory!")
	quit()

input_path = sys.argv[1]
if len(sys.argv) > 2:
	output_dir = sys.argv[2]
else:
	output_dir = "."
	
input_file_name = input_path.split("/")[-1]
function_identifier = input_file_name.split("-")[0]
identifier_substrings = function_identifier.split(".") 
module = ".".join(identifier_substrings[0:-1])
function_name = identifier_substrings[-1]
find_risky_combs(input_path, output_dir, module, function_name)
