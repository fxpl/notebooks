"""
Extract module and function name from the argument, list statements that are
executable in isolation. Among the parameters of theses statements, find risky
combinations of values.

Argument:
1: The path to the input file, containing function calls to be checked, one per
   line. Every function call shall target the same function. The statements may
   start with names/aliases for the module. These will be ignored. The name of
   the file should be on the format
   [<directory>/]<module>.<function_name>-<any string>, where <module> is the
   name of the module where the function can be found and <function_name> is
   the name of the function that is called.
2: The path to the directory where the output will be stored

Output:
* A file named <function_name>_executable.csv containing all calls that are
  executable in isolation.
* One file per risky pair for which the risky values are found. The files
  contain lists of all risky function calls. Each file is named
  <function_name>.<risky_pair>.csv.
"""

DEFAULT = "kossaapaabcdefghijklmnopqrstuvwxypzåäööäåzpyxwvutsrqponmlkjihgfedcbaapakossa"

import sys
import re
import numpy
import matplotlib.pyplot


def find_risky_pairs(input_path, output_dir, module, function_name):
	"""
	List executable calls and and risky arguments according to description
	above.
	"""
	executable_calls_path = output_dir + "/" + function_name + "_executable.csv"
	with open(input_path) as input_file, open(executable_calls_path, "w") as output_file:
		lines = input_file.readlines()
		for line in lines:
			if None == re.search("input\s*\(", line):
				function_call = _get_function_call(line)
				try:
					eval(module + "." + function_call)
					output_file.write(function_call)
					# If we reach this point, the statement is executable (with literals)
					_report_risky_pairs(function_call)
				except:
					# We want to skip calls that don't work
					pass


def _get_function_call(statement):
	"""
	Extract and return the function call, without preceding module names, from
	a statement.
	"""
	substrings = statement.split("(")
	if "." in substrings[0]:
		start_index = substrings[0].rindex(".") + 1
	else:
		start_index = 0
	return statement[start_index:]


def _report_risky_pairs(function_call):
	"""
	Report all risky argument combinations in the provided function call to
	files. The function_call shall not contain the module of the function.
	"""
	risky_pairs = eval(function_call)
	for pair in risky_pairs:
		pair_file_path = output_dir + "/" + pair + ".csv"
		with open(pair_file_path, "a") as pair_file:
			pair_file.write(function_call)


"""
Each of the functions below check for risky argument combinations in a call to
the function (one of those listed in this comment) that has the same name. The
names of the parameters correspond those of the function to be checked. Each
function returns a list of strings representing the risky argument combinations
(or en empty list if no such combination is found). The strings are on the
format <function_name>.<parameter1>-<parameter2>.csv

Currently supported functions:
- matplotlib.pyplot.show
- numpy.zeros
"""

def show(block=DEFAULT):
	return []	# There are no risky combinations, since there is only 1 parameter.

def arange(start=DEFAULT, stop=DEFAULT, step=DEFAULT, dtype=DEFAULT):
	if _is_defined(stop):
		# Only 1 parameter is given. It should be interpreted as stop instead of start.
		# (We know it's an executable call!)
		stop = start
		start = DEFAULT
	#  If step is specified as a position argument, start must also be given.
	start = _get_value(start, 0)
	step = _get_value(step, 1)
	# stop is always specified
	
	pairs = []
	interval = stop-start
	if (interval > 0 and step < 0) or (interval<0 and step > 0):
		pairs.append("arange.start-stop-step")
	return pairs

def zeros(shape, dtype=DEFAULT, order=DEFAULT):
	order_set = _is_defined(order)
	dtype = _get_value(dtype, float)
	order = _get_value(order, "C")
	
	pairs = []
	array = numpy.zeros(shape, dtype=dtype, order=order)
	if 1 == len(numpy.shape(array)) and order_set:
		# The user has specified order for a 1D array
		pairs.append("zeros.shape-order")
	return pairs


def _get_value(param, default_value):
	if DEFAULT == param:
		return default_value
	else:
		return param

def _is_defined(param):
	return DEFAULT != param


""" MAIN """
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
find_risky_pairs(input_path, output_dir, module, function_name)