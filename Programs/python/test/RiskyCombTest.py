"""
TO TEST:
- find_risky_pairs
	Läs testfil med både giltiga och ogiltiga anrop. Kolla att de giltiga (och bara de) listas i outputfilen.
	Några anrop med risky pairs. Kolla att dessa listas i korrekt namngivna outputfiler.
	Kolla att det inte skapas outputfiler för OK par.
	OBS: Ta bort outputfiler efter varje testomgång!
"""

import unittest
from risky_comb_functions import *

class RiskyCombTest(unittest.TestCase):
	
	def test_show(self):
		result = show()
		self.assertEqual([], result, "Non-empty list returned by show!")
	
	def test_arange_valid(self):
		result = arange(2)
		self.assertEqual([], result, "Non-empty list returned by correct call to arange!")
		result = arange(1, 1)
		self.assertEqual([], result, "Non-empty list returned by correct call to arange!")
		result = arange(0, 5, 0.5)
		self.assertEqual([], result, "Non-empty list returned by correct call to arange!")
		result = arange(step=0.5, start=2, stop=5)
		self.assertEqual([], result, "Non-empty list returned by correct call to arange!")
	
	def test_arange_wrong_step(self):
		expected = ["arange.start-stop-step"]
		result = arange(-5)
		self.assertEqual(expected, result, "Wrong list returned by arange when span is negative and step positive.")
		result = arange(10, 5)
		self.assertEqual(expected, result, "Wrong list returned by arange when span is negative and step positive.")
		result = arange(20, 25, -1)
		self.assertEqual(expected, result, "Wrong list returned by arange when span is positive and step negative.")
	
	def test_array_valid(self):
		result = array([1, 2, 3])
		self.assertEqual([], result, "Non-empty list returned by correct call to array (default values)!")
		result = array(numpy.array([1, 2, 3]), copy=False)
		self.assertEqual([], result, "Non-empty list returned by correct call to array (copy=False)!")
		result = array([[1, 2],[3, 4]], order="F")
		self.assertEqual([], result, "Non-empty list returned by correct call to array(order set)!")
	
	def test_array_order_for_1D(self):
		result = array([1, 2, 3], order="F")
		expected = ["array.object-order"]
		self.assertEqual(expected, result, "Wrong result when calling array with order for 1D result!")
	
	def test_array_copy_false(self):
		result = array([1, 2, 3], copy=False)
		expected = ["array.copy"]
		self.assertEqual(expected, result, "Wrong result when calling array with copy=False for list!")
	
	def test_array_both_wrong(self):
		result = array([1, 2, 3], order="F", copy=False)
		self.assertEqual(2, len(result), "Wrong number of risky combinations reported by array!")
		self.assertTrue("array.object-order" in result, "object-order not reported by array call with both risks!")
		self.assertTrue("array.copy" in result, "copy not reported by array call with both risks!")
	
	def test_zeros_valid(self):
		result = zeros((2, 3, 1))
		self.assertEqual([], result, "Non-empty list returned by correct call to zero!")
		result = zeros((2, 3, 1), order="F")
		self.assertEqual([], result, "Non-empty list returned by correct call to zero!")
	
	def test_zero_order_for_1D(self):
		result = zeros(3, order="F")
		expected = ["zeros.shape-order"]
		self.assertEqual(expected, result, "Wrong result when calling zero with order for 1D result!")

		