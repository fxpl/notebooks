import re
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

aliases = {
	"matplotlib.pyplot": "plt",
	"numpy": "np",
	"pandas": "pd"
}

_UNSPECIFIED = "kossaapaabcdefghijklmnopqrstuvwxypzåäööäåzpyxwvutsrqponmlkjihgfedcbaapakossa"

def find_risky_combs(input_path, output_dir, module, function_name):
	"""
	List statements (function_calls) in the file stored at input_path that are
	executable in isolation. Among the arguments of these calls, find risky
	combinations of values. The following output files are created in
	output_dir:
	
	* A file named <function_name>_executable.csv containing all calls that are
  	  executable in isolation.
	* One file per risky parameter combination for which the risky arguments
	  are found. The files contain lists of all risky function calls. Each
	  file is named <function_name>.<risky_combination>.csv.
	
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
					eval(aliases[module] + "." + function_call)
					output_file.write(function_call)
					# If we reach this point, the statement is executable (with literals)
					_report_risky_combs(function_call, output_dir)
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


def _report_risky_combs(function_call, output_dir):
	"""
	Report all risky argument combinations in the provided function call to
	files. The function_call shall not contain the module of the function.
	"""
	risky_combs = eval(function_call)
	for comb in risky_combs:
		comb_file_path = output_dir + "/" + comb + ".csv"
		with open(comb_file_path, "a") as comb_file:
			comb_file.write(function_call)


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
- matplotlib.pyplot.plot
- matplotlib.pyplot.show
- numpy.arange
- numpy.array
- numpy.zeros
- pandas.DataFrame
"""

def plot(*args,
		scalex=_UNSPECIFIED,
		scaley=_UNSPECIFIED,
		data=_UNSPECIFIED,
		agg_filter=_UNSPECIFIED,
		alpha=_UNSPECIFIED,
		animated=_UNSPECIFIED,
		anitaliased=_UNSPECIFIED,
		clip_box=_UNSPECIFIED,
		clip_on=_UNSPECIFIED,
		clip_path=_UNSPECIFIED,
		color=_UNSPECIFIED,
		contains=_UNSPECIFIED,
		dash_capstyle=_UNSPECIFIED,
		dash_joinstyle=_UNSPECIFIED,
		dashes=_UNSPECIFIED,
		drawstyle=_UNSPECIFIED,
		figure=_UNSPECIFIED,
		fillstyle=_UNSPECIFIED,
		gid=_UNSPECIFIED,
		in_layout=_UNSPECIFIED,
		label=_UNSPECIFIED,
		linestyle=_UNSPECIFIED,
		linewidth=_UNSPECIFIED,
		marker=_UNSPECIFIED,
		markeredgecolor=_UNSPECIFIED,
		markeredgewidth=_UNSPECIFIED,
		markerfacecolor=_UNSPECIFIED,
		markerfacecoloralt=_UNSPECIFIED,
		markersize=_UNSPECIFIED,
		markevery=_UNSPECIFIED,
		path_effects=_UNSPECIFIED,
		picker=_UNSPECIFIED,
		pickradius=_UNSPECIFIED,
		rasterized=_UNSPECIFIED,
		sketch_params=_UNSPECIFIED,
		snap=_UNSPECIFIED,
		solid_capstyle=_UNSPECIFIED,
		solid_joinstyle=_UNSPECIFIED,
		transform=_UNSPECIFIED,
		url=_UNSPECIFIED,
		visible=_UNSPECIFIED,
		xdata=_UNSPECIFIED,
		ydata=_UNSPECIFIED):
	result = []
	
	# Check content of fmt string
	fmt_markers = ['.', ',', 'o', 'v', '^', '<', '>', '1', '2', '3', '4', 's', 'p', '*', 'h', 'H', '+', 'x', 'D', 'd', '|', '_']
	fmt_linestyles = ['-', '--', '-.'':']
	fmt_colors = ['r', 'g', 'b', 'c', 'm',' y', 'k', 'w']
	fmt_marker_specified = False
	fmt_linestyle_specified = False
	fmt_linestyle_solid = False
	fmt_color_specified = False
	for arg in args:
		if isinstance(arg, str):
			for fmt_marker in fmt_markers:
				if fmt_marker in arg:
					fmt_marker_specified = True
			for fmt_linestyle in fmt_linestyles:
				if fmt_linestyle in arg:
					fmt_linestyle_specified = True
					if fmt_linestyle == "-" and not "--" in arg and not "-." in arg:
						fmt_linestyle_solid = True
			for fmt_color in fmt_colors:
				if fmt_color in arg:
					fmt_color_specified = True
			if "#" in arg:	# Color specified with hex code
				fmt_color_specified=True
	# fmt in combination with marker, linestyle and color
	if fmt_marker_specified and _is_specified(marker):
		result.append("plot.fmt-marker")
	if fmt_linestyle_specified and _is_specified(linestyle):
		result.append("plot.fmt-linestyle")
	if fmt_color_specified and _is_specified(color):
		result.append("plot.fmt-color")
	
	# linestyle and dashes
	if _is_specified(linestyle) and _is_specified(dashes):
		result.append("plot.linestyle-dashes")
	
	# dash_*style for solid line och solid_*style for dashed line
	solid = False
	if not (fmt_linestyle_specified or _is_specified(linestyle) or _is_specified(dashes)):
		# Default (solid line) used
		solid = True
	if fmt_linestyle_solid or "-" == linestyle:
		solid = True
	if _is_specified(dashes):
		solid = True
		for i in np.arange(1, len(dashes), 2):
			if 0 != dashes[i]: # There is a space between dashes
				solid = False
	
	if solid:
		if _is_specified(dash_capstyle):
			result.append("plot.fmt-dash_capstyle")
		if _is_specified(dash_joinstyle):
			result.append("plot.fmt-dash_joinstyle")
	else:
		if _is_specified(solid_capstyle):
			result.append("plot.fmt-solid_capstyle")
		if _is_specified(solid_joinstyle):
			result.append("plot.fmt-solid_joinstyle")
	
	# Marker properties set for non-marker
	if (not _is_specified(marker) or None==marker or ""==marker) and not fmt_marker_specified:
		# There is a marker
		if _is_specified(fillstyle):
			result.append("plot.marker-fillstyle")
		if _is_specified(markeredgecolor):
			result.append("plot.marker-markeredgecolor")
		if _is_specified(markeredgewidth):
			result.append("plot.marker-markeredgewidth")
		if _is_specified(markerfacecolor):
			result.append("plot.marker-markerfacecolor")
		if _is_specified(markerfacecoloralt):
			result.append("plot.marker-markerfacecoloralt")
		if _is_specified(markersize):
			result.append("plot.marker-markersize")
		if _is_specified(markevery):
			result.append("plot.marker-markevery")
	
	# pickradius without picker
	if _is_specified(pickradius):
		if not _is_specified(picker) or None==picker or False==picker:
			result.append("plot.picker-pickradius")
	
	return result

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
	
	result = []
	interval = stop-start
	if (interval > 0 and step < 0) or (interval<0 and step > 0):
		result.append("arange.start-stop-step")
	return result


def array(obj, dtype=None, *, copy=True, order=_UNSPECIFIED, subok=False, ndmin=0):
	order_set = _is_specified(order)
	order = _get_value(order, "K")
	
	result = []
	array = np.array(obj, dtype=dtype, copy=copy, order=order, subok=subok, ndmin=ndmin)
	if 1 == len(np.shape(array)) and order_set:
		# The user has specified order for a 1D array
		result.append("array.object-order")
	
	if False == copy and not np.shares_memory(array, obj):
		result.append("array.copy")
	return result


def zeros(shape, dtype=float, order=_UNSPECIFIED):
	order_set = _is_specified(order)
	order = _get_value(order, "C")
	
	result = []
	array = np.zeros(shape, dtype=dtype, order=order)
	if 1 == len(np.shape(array)) and order_set:
		# The user has specified order for a 1D array
		result.append("zeros.shape-order")
	return result

def DataFrame(data=_UNSPECIFIED,
			index=None,
			columns=None,	# We are only interested in it if it is specified AND not None
			dtype=None,		# We are only interested in it if it is specified AND not None
			copy=_UNSPECIFIED):
	result = []
	
	if _is_specified(copy):
		is2DnumpyArray = isinstance(data, np.ndarray) and 2 == len(data.shape)
		if not isinstance(data, (pd.DataFrame)) and not is2DnumpyArray:
			result.append("DataFrame.data-copy")
	else:
		copy = False
	
	if isinstance(data, dict) and None!=columns:
		result.append("DataFrame.data-columns")
	
	df = pd.DataFrame(data=data, index=index, columns=columns, dtype=dtype, copy=copy)
	if None != dtype:
		rightType = True
		for key, value in df.items():
			for index, elem in value.items():
				if type(elem) != dtype:
					result.append("DataFrame.data-dtype")
					rightType = False
					break
			if not rightType:
				break
	
	return result
