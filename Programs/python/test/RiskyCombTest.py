import unittest
import os
import shutil
import csv
import datetime
import numpy
import pandas
import risky_comb_functions as rcf

from shutil import rmtree
from pathlib import Path


class RiskyCombTest(unittest.TestCase):
	x = [1,2]
	y = [3,4]
	output_dir = "risky_comb_unittest_tmp_dir"
	
	@classmethod
	def setUpClass(cls):
		os.makedirs(RiskyCombTest.output_dir, exist_ok=True)
	
	@classmethod
	def tearDownClass(cls):
		rmtree(RiskyCombTest.output_dir)
	
	def test_find_risky_pairs_matplotlib_plot(self):
		pass # TODO
	
	def test_find_risky_pairs_numpy_array(self):
		input_path = "data/numpy.array-calls-test.csv"
		rcf.find_risky_combs(input_path, RiskyCombTest.output_dir, "numpy", "array")
		expected_executable_lines = [
			"array(np.array([1, 2, 3]))\n",
			"array([7, 8, 9])\n",
			"array([1, 2, 3], order='F')\n",
			"array([1, 3, 5])\n"]
		executable_path = Path(RiskyCombTest.output_dir + "/array_executable.csv")
		with open(executable_path) as calls:
			lines = calls.readlines()
			self.assertEqual(expected_executable_lines, lines, "Wrong executable statements stored!")
		os.remove(executable_path)
		
		expected_risky_lines = ["array([1, 2, 3], order='F')\n"]
		risky_path = Path(RiskyCombTest.output_dir + "/array.object-order.csv")
		with open(risky_path) as risky:
			lines = risky.readlines()
			self.assertEqual(expected_risky_lines, lines, "Wrong risky pair calls stored!")
		os.remove(risky_path)
	
	def test_find_risky_pairs_pandas_DataFrame(self):
		pass # TODO
	
	def test_plot_fmt(self):
		result = rcf.plot(self.x, self.y, "go--")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, "0xA8329E")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, "olive")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, "go--", marker="D")
		self.assertEqual(["plot.fmt-marker"], result, "Wrong list returned  by call to plot!")
		result = rcf.plot(self.x, self.y, "go--", linestyle=":")
		self.assertEqual(["plot.fmt-linestyle"], result, "Wrong list returned  by call to plot!")
		result = rcf.plot(self.x, self.y, "go--", color="r")
		self.assertEqual(["plot.fmt-color"], result, "Wrong list returned  by call to plot!")
		result = rcf.plot(self.x, self.y, "go--", marker="o", linestyle="-", color="k")
		self.assertTrue("plot.fmt-marker" in result, "Double-defined marker missed!")
		self.assertTrue("plot.fmt-linestyle" in result, "Double-defined linestyle missed!")
		self.assertTrue("plot.fmt-color" in result, "Double-defined color missed!")
	
	def test_plot_linestyle_dashes(self):
		result = rcf.plot(self.x, self.y, linestyle="-.")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, dashes=(5, 2, 1, 2))
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, linestyle="-.", dashes=(5, 2, 1, 2))
		self.assertEqual(["plot.linestyle-dashes"], result, "Wrong list returned by plot call with both linestyle and dashes specified!")
	
	def test_plot_dash_capstyle(self):
		# Default
		result = rcf.plot(self.x, self.y, dash_capstyle="round")
		self.assertEqual(["plot.fmt-dash_capstyle"], result, "Wrong result returned when dash_capstyle is defined for a solid line!")
		
		# fmt
		result = rcf.plot(self.x, self.y, "-.", dash_capstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, "-", dash_capstyle="round")
		self.assertEqual(["plot.fmt-dash_capstyle"], result, "Wrong result returned when dash_capstyle is defined for a solid line!")
		
		# linestyle
		result = rcf.plot(self.x, self.y, linestyle="-.", dash_capstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, linestyle="-", dash_capstyle="round")
		self.assertEqual(["plot.fmt-dash_capstyle"], result, "Wrong result returned when dash_capstyle is defined for a solid line!")
		
		# dashes
		result = rcf.plot(self.x, self.y, dashes=(1, 1, 1, 0), dash_capstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, dashes=(1, 0, 1, 0), dash_capstyle="round")
		self.assertEqual(["plot.fmt-dash_capstyle"], result, "Wrong result returned when dash_capstyle is defined for a solid line!")
	
	def test_plot_dash_joinstyle(self):
		# Default
		result = rcf.plot(self.x, self.y, dash_joinstyle="round")
		self.assertEqual(["plot.fmt-dash_joinstyle"], result, "Wrong result returned when dash_joinstyle is defined for a solid line!")
		
		# fmt
		result = rcf.plot(self.x, self.y, "-.", dash_joinstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, "-", dash_joinstyle="round")
		self.assertEqual(["plot.fmt-dash_joinstyle"], result, "Wrong result returned when dash_joinstyle is defined for a solid line!")
		
		# linestyle
		result = rcf.plot(self.x, self.y, linestyle="-.", dash_joinstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, linestyle="-", dash_joinstyle="round")
		self.assertEqual(["plot.fmt-dash_joinstyle"], result, "Wrong result returned when dash_joinstyle is defined for a solid line!")
		
		# dashes
		result = rcf.plot(self.x, self.y, dashes=(1, 1, 1, 0), dash_joinstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, dashes=(1, 0, 1, 0), dash_joinstyle="round")
		self.assertEqual(["plot.fmt-dash_joinstyle"], result, "Wrong result returned when dash_joinstyle is defined for a solid line!")
	
	def test_plot_solid_capstyle(self):
		# Default = solid line
		result = rcf.plot(self.x, self.y, solid_capstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		
		# fmt
		result = rcf.plot(self.x, self.y, "-", solid_capstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, "--", solid_capstyle="round")
		self.assertEqual(["plot.fmt-solid_capstyle"], result, "Wrong result returned when solid_capstyle is defined for a dashed line!")
		
		# linestyle
		result = rcf.plot(self.x, self.y, linestyle="-", solid_capstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, linestyle="--", solid_capstyle="round")
		self.assertEqual(["plot.fmt-solid_capstyle"], result, "Wrong result returned when solid_capstyle is defined for a dashed line!")
		
		# dashes
		result = rcf.plot(self.x, self.y, dashes=(1, 0, 1, 0), solid_capstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, dashes=(1, 2, 1, 0), solid_capstyle="round")
		self.assertEqual(["plot.fmt-solid_capstyle"], result, "Wrong result returned when solid_capstyle is defined for a dashed line!")
	
	def test_plot_solid_joinstyle(self):
		# Default = solid line
		result = rcf.plot(self.x, self.y, solid_joinstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		
		# fmt
		result = rcf.plot(self.x, self.y, "-", solid_joinstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, "--", solid_joinstyle="round")
		self.assertEqual(["plot.fmt-solid_joinstyle"], result, "Wrong result returned when solid_joinstyle is defined for a dashed line!")
		
		# linestyle
		result = rcf.plot(self.x, self.y, linestyle="-", solid_joinstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, linestyle="--", solid_joinstyle="round")
		self.assertEqual(["plot.fmt-solid_joinstyle"], result, "Wrong result returned when solid_joinstyle is defined for a dashed line!")
		
		# dashes
		result = rcf.plot(self.x, self.y, dashes=(1, 0, 1, 0), solid_joinstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, dashes=(1, 2, 1, 0), solid_joinstyle="round")
		self.assertEqual(["plot.fmt-solid_joinstyle"], result, "Wrong result returned when dash_joinstyle is defined for a dashed line!")
	
	def test_plot_marker(self):
		# Correct call (marker set)
		result = rcf.plot(self.x, self.y, "kD-",
						fillstyle="full",
						markeredgecolor="m",
						markeredgewidth=1,
						markerfacecolor="m",
						markerfacecoloralt="b",
						markersize=7,
						markevery=2)
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		
		result = rcf.plot(self.x, self.y, marker="h",
						fillstyle="full",
						markeredgecolor="m",
						markeredgewidth=1,
						markerfacecolor="m",
						markerfacecoloralt="b",
						markersize=7,
						markevery=2)
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		
		# Incorrect calls
		incorrect_call_result = ["plot.marker-fillstyle",
								"plot.marker-markeredgecolor",
								"plot.marker-markeredgewidth",
								"plot.marker-markerfacecolor",
								"plot.marker-markerfacecoloralt",
								"plot.marker-markersize",
								"plot.marker-markevery"]
		# Default is no marker
		result = rcf.plot(self.x, self.y,
						fillstyle="full",
						markeredgecolor="m",
						markeredgewidth=1,
						markerfacecolor="m",
						markerfacecoloralt="b",
						markersize=7,
						markevery=2)
		self.assertEqual(incorrect_call_result, result, "Wrong list returned for plot call that specifies marker properties but not marker!")
		
		# Other non-marker values
		markersize_result = ["plot.marker-markersize"]
		result = rcf.plot(self.x, self.y, marker=None, markersize=7)
		self.assertEqual(markersize_result, result, "Wrong list returned for plot call that specifies markersize but marker is None!")
		result = rcf.plot(self.x, self.y, marker="", markersize=7)
		self.assertEqual(markersize_result, result, "Wrong list returned for plot call that specifies markersize but marker is empty string!")
	
	def test_plot_picker_pickradius(self):
		result = rcf.plot(self.x, self.y, picker=True, pickradius=1);
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, pickradius=1)
		self.assertEquals(["plot.picker-pickradius"], result, "Wrong list returned for plot call that specifies pickradius but not picker!")
		result = rcf.plot(self.x, self.y, picker=None, pickradius=1)
		self.assertEquals(["plot.picker-pickradius"], result, "Wrong list returned for plot call that specifies pickradius for None picker!")
		result = rcf.plot(self.x, self.y, picker=False, pickradius=1)
		self.assertEquals(["plot.picker-pickradius"], result, "Wrong list returned for plot call that specifies pickradius for False picker!")
	
	def test_show(self):
		result = rcf.show()
		self.assertEqual([], result, "Non-empty list returned by show!")
	
	def test_arange_valid(self):
		result = rcf.arange(2)
		self.assertEqual([], result, "Non-empty list returned by correct call to arange!")
		result = rcf.arange(1, 1)
		self.assertEqual([], result, "Non-empty list returned by correct call to arange!")
		result = rcf.arange(0, 5, 0.5)
		self.assertEqual([], result, "Non-empty list returned by correct call to arange!")
		result = rcf.arange(step=0.5, start=2, stop=5)
		self.assertEqual([], result, "Non-empty list returned by correct call to arange!")
	
	def test_arange_wrong_step(self):
		expected = ["arange.start-stop-step"]
		result = rcf.arange(-5)
		self.assertEqual(expected, result, "Wrong list returned by arange when span is negative and step positive.")
		result = rcf.arange(10, 5)
		self.assertEqual(expected, result, "Wrong list returned by arange when span is negative and step positive.")
		result = rcf.arange(20, 25, -1)
		self.assertEqual(expected, result, "Wrong list returned by arange when span is positive and step negative.")
	
	def test_array_valid(self):
		result = rcf.array([1, 2, 3])
		self.assertEqual([], result, "Non-empty list returned by correct call to array (default values)!")
		result = rcf.array(numpy.array([1, 2, 3]), copy=False)
		self.assertEqual([], result, "Non-empty list returned by correct call to array (copy=False)!")
		result = rcf.array([[1, 2],[3, 4]], order="F")
		self.assertEqual([], result, "Non-empty list returned by correct call to array(order set)!")
	
	def test_array_order_for_1D(self):
		result = rcf.array([1, 2, 3], order="F")
		expected = ["array.object-order"]
		self.assertEqual(expected, result, "Wrong result when calling array with order for 1D result!")
	
	def test_array_copy_false(self):
		result = rcf.array([1, 2, 3], copy=False)
		expected = ["array.copy"]
		self.assertEqual(expected, result, "Wrong result when calling array with copy=False for list!")
	
	def test_array_both_wrong(self):
		result = rcf.array([1, 2, 3], order="F", copy=False)
		self.assertEqual(2, len(result), "Wrong number of risky combinations reported by array!")
		self.assertTrue("array.object-order" in result, "object-order not reported by array call with both risks!")
		self.assertTrue("array.copy" in result, "copy not reported by array call with both risks!")
	
	def test_zeros_valid(self):
		result = rcf.zeros((2, 3, 1))
		self.assertEqual([], result, "Non-empty list returned by correct call to zero!")
		result = rcf.zeros((2, 3, 1), order="F")
		self.assertEqual([], result, "Non-empty list returned by correct call to zero!")
	
	def test_zero_order_for_1D(self):
		result = rcf.zeros(3, order="F")
		expected = ["zeros.shape-order"]
		self.assertEqual(expected, result, "Wrong result when calling zero with order for 1D result!")
	
	def test_DataFrame_data_copy(self):
		df = pandas.DataFrame([1, 2, 3])
		result = rcf.DataFrame(data=df, copy=False)
		self.assertEqual([], result, "Non-empty list returned by correct call to DataFrame!")
		result = rcf.DataFrame(numpy.array([5, 7, 8]))
		self.assertEqual([], result, "Non-empty list returned by correct call to DataFrame!")
		
		arr = numpy.array([[5, 7, 8], [13, 16, 19]])
		result = rcf.DataFrame(data=arr, copy=False)
		self.assertEqual([], result, "Non-empty list returned by correct call to DataFrame!")
		
		result = rcf.DataFrame([1, 3, 5], copy=False)
		self.assertEqual(["DataFrame.data-copy"], result, "Wrong result when calling DataFrame for a list with copy specified!")
		result = rcf.DataFrame(((1, 3, 5), (9, 0, 1)), copy=False)
		self.assertEqual(["DataFrame.data-copy"], result, "Wrong result when calling DataFrame for a tuple with copy specified!")
		arr = numpy.array([5, 7, 8])
		result = rcf.DataFrame(data=arr, copy=False)
		self.assertEqual(["DataFrame.data-copy"], result, "Wrong result when calling DataFrame for a 1D numpy array with copy specified!")
	
	def test_DataFrame_data_columns(self):
		data = {"Malin": [86, 93, 5], "Tor": [95, 2, 14], "Anna": [97, 4, 16]}
		result = rcf.DataFrame(data)
		self.assertEqual([], result, "Non-empty list returned by correct call to DataFrame!")
		result = rcf.DataFrame(data, columns=None)
		self.assertEqual([], result, "Non-empty list returned by correct call to DataFrame!")
		result = rcf.DataFrame(data, columns=["Tor", "Anna"])
		self.assertEqual([], result, "Non-empty list returned by correct call to DataFrame!")
		
		result = rcf.DataFrame(data, columns=["birth", "school", "graduation"])
		self.assertEqual(["DataFrame.data-columns"], result, "Wrong result when calling DataFrame with dict data and other columns specified!")
		result = rcf.DataFrame(data, columns=["Jonas", "Tor", "Anna"])
		self.assertEqual(["DataFrame.data-columns"], result, "Wrong result when calling DataFrame with dict data and other column specified!")
	
	def test_DataFrame_data_dtype(self):
		data = {"Malin": ["Järnåkra", "Katte", "UU"],
			"Jonas": ["Freinet", "Polhem", "Chalmers"],
			"Anna": ["LMG", "Katte", "LTH"]}
		result = rcf.DataFrame(data, dtype=str)
		self.assertEqual([], result, "Non-empty list returned by correct call to DataFrame!")
		result = rcf.DataFrame(data, dtype=None)
		self.assertEqual([], result, "Non-empty list returned by correct call to DataFrame!")
		
		result = rcf.DataFrame(data, dtype=int)
		self.assertEqual(["DataFrame.data-dtype"], result, "Wrong result when calling string DataFrame with dtype=int!")
		data = {"Age": [34, 29, 22],
			"City": ["Uppsala", "Göteborg", "Lund"]}
		result = rcf.DataFrame(data, dtype=int)
		self.assertEqual(["DataFrame.data-dtype"], result, "Wrong result when calling DataFrame with one string column and dtype=int!")
		
		data = {0: [1, "a", float("NaN")], 1: [float("NaN"), "b", 7.2]}
		result = rcf.DataFrame(data, dtype=int)
		self.assertEqual(["DataFrame.data-dtype"], result, "Wrong result when calling DataFrame with mixed type columns and dtype=int!")
	
	def test_read_csv_delims(self):
		result = rcf.read_csv("dummy", sep=",")
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", delimiter=",")
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", delim_whitespace=True)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", delim_whitespace=False, sep='.')
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		
		result = rcf.read_csv("dummy", delim_whitespace=True, sep='.')
		self.assertEqual(["read_csv.delim_whitespace-sep"], result, "Wrong result when calling read_csv with delim_whitespace=True and sep defined!")
		result = rcf.read_csv("dummy", delim_whitespace=True, delimiter=':')
		self.assertEqual(["read_csv.delim_whitespace-delimiter"], result, "Wrong result when calling read_csv with delim_whitespace=True and delimiter defined!")
		result = rcf.read_csv("dummy", sep=" ", delimiter=':')
		self.assertEqual(["read_csv.sep-delimiter"], result, "Wrong result when calling read_csv with both sep and delimiter defined!")
		result = rcf.read_csv("dummy", sep=" ", delimiter=':', delim_whitespace=True)
		self.assertEqual(["read_csv.delim_whitespace-sep", "read_csv.delim_whitespace-delimiter", "read_csv.sep-delimiter"], result, "Wrong result when calling read_csv with both sep and delimiter defined!")
	
	def test_read_csv_header(self):
		result = rcf.read_csv("dummy", header=1)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", prefix="X", header=None)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", names=["A", "B", "C"])
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		
		result = rcf.read_csv("dummy", names=["A", "B", "C"], header=None, prefix="X")
		self.assertEqual(["read_csv.names-prefix", "read_csv.names-header"], result, "Wrong result when calling read_csv with both names and prefix, and None-header!")
		result = rcf.read_csv("dummy", names=["A", "B", "C"], header=1)
		self.assertEqual(["read_csv.names-header"], result, "Wrong result when calling read_csv with both names and header!")
		result = rcf.read_csv("dummy", prefix="X")
		self.assertEqual(["read_csv.header-prefix"], result, "Wrong result when calling read_csv with both prefix without setting header=None!")
		result = rcf.read_csv("dummy", header=0, prefix="X")
		self.assertEqual(["read_csv.header-prefix"], result, "Wrong result when calling read_csv with both non-None header and prefix!")
		result = rcf.read_csv("dummy", names=["A", "B", "C"], header=0, prefix="X")
		expectedResult = ["read_csv.names-prefix", "read_csv.names-header", "read_csv.header-prefix"]
		self.assertEqual(expectedResult, result, "Wrong result when read_csv is called with both names, prefix and non-None header!")
	
	def test_read_csv_duplicated_value_ignored(self):
		result = rcf.read_csv("dummy")
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", lineterminator='\n', escapechar='\\', delimiter=',', comment='#', thousands=' ', decimal='.', quotechar='\'', na_values=["NaN", "nan"], true_values=[True, 1], false_values=[False, 0])
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		
		result = rcf.read_csv("dummy", lineterminator=',', sep=',')
		self.assertEqual(["read_csv.lineterminator-sep"], result, "Wrong result when calling read_csv with lineterminator=sep!")
		result = rcf.read_csv("dummy", delimiter=',', thousands=',')
		self.assertEqual(["read_csv.delimiter-thousands"], result, "Wrong result when calling read_csv with delimiter=thousands!")
		result = rcf.read_csv("dummy", true_values=[1, "true"], false_values=[1, "false"])
		self.assertEqual(["read_csv.true_values-false_values"], result, "Wrong result when calling read_csv with same value in true_values and false_values!")
		result = rcf.read_csv("dummy", quotechar="#", na_values=["NA", "#", "NaN"])
		self.assertEqual(["read_csv.quotechar-na_values"], result, "Wrong result when calling read_csv with quotechar value in na_values!")
		
		result = rcf.read_csv("dummy", sep=',', delimiter=',')	# Should only contain sep-delimiter once
		self.assertEqual(["read_csv.sep-delimiter"], result, "Wrong result when calling read_csv with sep and delimiter set to same value!")
	
	def test_read_csv_na(self):
		result = rcf.read_csv("dummy", na_values=["NaN", "Nan"], keep_default_na=True)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", na_filter=False, na_values=["NaN", "Nan"])
		self.assertEqual(["read_csv.na_filter-na_values"], result, "Wrong result when calling read_csv with na_value specified, but na_filter=False!")
		result = rcf.read_csv("dummy", na_filter=False, keep_default_na=True)
		self.assertEqual(["read_csv.na_filter-keep_default_na"], result, "Wrong result when calling read_csv with keep_default_na specified, but na_filter=False!")
	
	def test_read_csv_parse_dates(self):
		def f(Y, m, d):
			return datetime.date(Y, m, d)
		result = rcf.read_csv("dummy", parse_dates=True, infer_datetime_format=True, keep_date_col=False, date_parser=f, dayfirst=True, cache_dates=True)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", infer_datetime_format=True)
		self.assertEqual(["read_csv.parse_dates-infer_datetime_format"], result, "Wrong result returned when infer_datetime_format is specified but not parse_dates!")
		result = rcf.read_csv("dummy", parse_dates=False, infer_datetime_format=True)
		self.assertEqual(["read_csv.parse_dates-infer_datetime_format"], result, "Wrong result returned when infer_datetime_format is specified but parse_dates is False!")
		result = rcf.read_csv("dummy", keep_date_col=True)
		self.assertEqual(["read_csv.parse_dates-keep_date_col"], result, "Wrong result returned when keep_date_col is specified but not parse_dates!")
		result = rcf.read_csv("dummy", date_parser=f)
		self.assertEqual(["read_csv.parse_dates-date_parser"], result, "Wrong result returned when date_parser is specified but not parse_dates!")
		result = rcf.read_csv("dummy", dayfirst=True)
		self.assertEqual(["read_csv.parse_dates-dayfirst"], result, "Wrong result returned when dayfirst is specified but not parse_dates!")
		result = rcf.read_csv("dummy", cache_dates=True)
		self.assertEqual(["read_csv.parse_dates-cache_dates"], result, "Wrong result returned when cache_dates is specified but not parse_dates!")
	
	def test_read_csv_bad_lines(self):
		result = rcf.read_csv("dummy")
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", warn_bad_lines=False, error_bad_lines=False)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", error_bad_lines=True)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", warn_bad_lines=False, error_bad_lines=True)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", warn_bad_lines=True)
		self.assertEqual(["read_csv.error_bad_lines-warn_bad_lines"], result, "Wrong result when read_csv is called with warn_bad_lines=True without setting error_bad_lines to False!")
		result = rcf.read_csv("dummy", warn_bad_lines=True, error_bad_lines=True)
		self.assertEqual(["read_csv.error_bad_lines-warn_bad_lines"], result, "Wrong result when read_csv is called with both error_bad_lines and warn_bad_lines explicitly set to True!")
	
	""" TODO: Vad innebär det egentligen att "quotechar is specified". Hur gör man för att den inte ska vara det?
	def test_read_csv_quoting_doublequote(self):
		result = rcf.read_csv("dummy", doublequote=True)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", quoting=csv.QUOTE_ALL, doublequote=True)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", quoting=csv.QUOTE_NONE, doublequote=True)
		self.assertEqual(["read_csv.quoting-doublequote"], result, "Wrong result when quoting is QUOTE_NONE and doublequote is set!")
		result = rcf.read_csv("dummy", quoting=csv.QUOTE_NONE, doublequote=False)
		self.assertEqual(["read_csv.quoting-doublequote"], result, "Wrong result when quoting is QUOTE_NONE and doublequote is set!")
	"""
	
	def test_read_csv_quotechar_doublequote(self):
		result = rcf.read_csv("dummy", doublequote=True)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", doublequote=False)
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", quoting=csv.QUOTE_NONE, doublequote=True)
		self.assertEqual(["read_csv.quoting-doublequote"], result, "Wrong result when doublequote specified despite quoting being QOUTE_NONE!")
		result = rcf.read_csv("dummy", quoting=csv.QUOTE_NONE, doublequote=False)
		self.assertEqual(["read_csv.quoting-doublequote"], result, "Wrong result when doublequote specified despite quoting being QOUTE_NONE!")
	
	def test_read_csv_usecols_names(self):
		result = rcf.read_csv("dummy", usecols=[0, 1], names=["A", "B"])
		self.assertEqual([], result, "Non-empty list returned by correct call to read_csv!")
		result = rcf.read_csv("dummy", usecols=[0, 1], names=["A"])
		self.assertEqual(["read_csv.usecols-names"], result, "Wrong result when length of names < length of usecols!")
		result = rcf.read_csv("dummy", usecols=[0, 1], names=["A", "B", "C"])
		self.assertEqual(["read_csv.usecols-names"], result, "Wrong result when length of names > length of usecols!")
