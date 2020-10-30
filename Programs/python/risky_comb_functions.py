import re
import numpy
import matplotlib.pyplot

_UNSPECIFIED = "kossaapaabcdefghijklmnopqrstuvwxypzåäööäåzpyxwvutsrqponmlkjihgfedcbaapakossa"

def find_risky_pairs(input_path, output_dir, module, function_name):
	"""
	List statements (function_calls) in the file stored at input_path that are
	executable in isolation. Among the arguments of these calls, find risky
	combinations of values. The following output files are created in
	output_dir:
	
	* A file named <function_name>_executable.csv containing all calls that are
  	  executable in isolation.
	* One file per risky pair for which the risky values are found. The files
	  contain lists of all risky function calls. Each file is named
	  <function_name>.<risky_pair>.csv.
	
	Arguments:
	input : str
		The path to the input file, containing function calls to be checked,
		one per line. Every function call shall target the same function. The
		statements may start with names/aliases for the module. These will be
		ignored. The name of the file should be on the format
		[<directory>/]<module>.<function_name>-<any string>, where <module> is
		the name of the module where the function can be found and
		<function_name> is the name of the function that is called.
	output_dir : str
		The path to the directory where the output will be stored
	module : str
		Name of the module where the function to be checked resides
	function_name : str
		Name of function called in the statements in the input file 
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
					_report_risky_pairs(function_call, output_dir)
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


def _report_risky_pairs(function_call, output_dir):
	"""
	Report all risky argument combinations in the provided function call to
	files. The function_call shall not contain the module of the function.
	"""
	risky_pairs = eval(function_call)
	for pair in risky_pairs:
		pair_file_path = output_dir + "/" + pair + ".csv"
		with open(pair_file_path, "a") as pair_file:
			pair_file.write(function_call)


def _get_value(param, default_value):
	"""
	Return the value of param unless this value is _UNSPECIFIED. Then return
	default_value instead.
	"""
	if _is_specified(param):
		return param
	else:
		return default_value


def _is_specified(param):
	return _UNSPECIFIED != param


"""
Each of the functions below check for risky argument combinations in a call to
the function (one of those listed in this comment) that has the same name. The
names of the parameters correspond those of the function to be checked. Each
function returns a list of strings representing the risky argument combinations
(or en empty list if no such combination is found). The strings are on the
format <function_name>.<parameter1>-<parameter2>.csv

Currently supported functions:
- matplotlib.pyplot.show
- numpy.arange
- numpy.array
- numpy.zeros
"""

def show(block=None):
	return []	# There are no risky combinations, since there is only 1 parameter.


def arange(start=0, stop=_UNSPECIFIED, step=1, dtype=None):
	if not _is_specified(stop):
		# Only 1 parameter is given. It should be interpreted as stop instead of start.
		# (We know it's an executable call!)
		stop = start
		start = _UNSPECIFIED
	#  If step is specified as a position argument, start must also be given.
	start = int(_get_value(start, 0))
	step = int(_get_value(step, 1))
	# stop is always specified
	
	pairs = []
	interval = stop-start
	if (interval > 0 and step < 0) or (interval<0 and step > 0):
		pairs.append("arange.start-stop-step")
	return pairs


def array(obj, dtype=None, *, copy=True, order=_UNSPECIFIED, subok=False, ndmin=0):
	order_set = _is_specified(order)
	order = _get_value(order, "K")
	
	pairs = []
	array = numpy.array(obj, dtype=dtype, copy=copy, order=order, subok=subok, ndmin=ndmin)
	if 1 == len(numpy.shape(array)) and order_set:
		# The user has specified order for a 1D array
		pairs.append("array.object-order")
	
	if False == copy and not numpy.shares_memory(array, obj):
		pairs.append("array.copy")
	return pairs


def zeros(shape, dtype=float, order=_UNSPECIFIED):
	order_set = _is_specified(order)
	order = _get_value(order, "C")
	
	pairs = []
	array = numpy.zeros(shape, dtype=dtype, order=order)
	if 1 == len(numpy.shape(array)) and order_set:
		# The user has specified order for a 1D array
		pairs.append("zeros.shape-order")
	return pairs
