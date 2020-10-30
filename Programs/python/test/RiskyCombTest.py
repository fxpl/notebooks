import unittest
import os
import numpy
import risky_comb_functions as rcf

class RiskyCombTest(unittest.TestCase):
	
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

		