import unittest
import os
import numpy
import risky_comb_functions as rcf

class RiskyCombTest(unittest.TestCase):
	x = [1,2]
	y = [3,4]
	
	def test_find_risky_pairs(self):
		input_path = "data/numpy.array-calls-test.csv"
		output_dir = "."
		rcf.find_risky_pairs(input_path, output_dir, "numpy", "array")
		expected_executable_lines = [
			"array([7, 8, 9])\n",
			"array([1, 2, 3], order='F')\n",
			"array([1, 3, 5])\n"]
		executable_path = "array_executable.csv"
		with open(executable_path) as calls:
			lines = calls.readlines()
			self.assertEqual(expected_executable_lines, lines, "Wrong executable statements stored!")
		os.remove(executable_path)
		
		expected_risky_lines = ["array([1, 2, 3], order='F')\n"]
		risky_path = "array.object-order.csv"
		with open(risky_path) as risky:
			lines = risky.readlines()
			self.assertEqual(expected_risky_lines, lines, "Wrong risky pair calls stored!")
		os.remove(risky_path)
	
	def test_plot_fmt(self):
		result = rcf.plot(self.x, self.y, "go--")
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
		
		# fmt
		result = rcf.plot(self.x, self.y, "-", solid_capstyle="round")
		self.assertEqual([], result, "Non-empty list returned by correct call to plot!")
		result = rcf.plot(self.x, self.y, "--", solid_capstyle="round")
		self.assertEqual(["plot.fmt-solid_capstyle"], result, "Wrong result returned when solid_capstyle is defined for a dashed line!")
		# TODO: Rimliga värden på capstyle och joinstyle!
		
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

		