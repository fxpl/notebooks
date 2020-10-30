"""
TO TEST:
- show
- arange:
	1 parameter (OK/inte OK)
	2 parametrar (OK/inte OK)
	3 parametrar (OK/inte OK)
	Namngivna parametrar
- array
	OK
	Order för 1D
	copy = False -> kopia
- zeros
	OK
	Order för 1D

- find_risky_pairs
	Läs testfil med både giltiga och ogiltiga anrop. Kolla att de giltiga (och bara de) listas i outputfilen.
	Några anrop med risky pairs. Kolla att dessa listas i korrekt namngivna outputfiler.
	OBS: Ta bort outputfiler efter varje testomgång!
"""

import unittest
from risky_comb_functions import *

class RiskyCombTest(unittest.TestCase):
	
	def test_show(self):
		result = show()
		self.assertEqual([], result, "Non-empty list returned by show!")
	
	def test_zeros_valid(self):
		result = zeros((2, 3, 1))
		self.assertEqual([], result, "Non-empty list returned by correct call to zero!")
		result = zeros((2, 3, 1), order="F")
		self.assertEqual([], result, "Non-empty list returned by correct call to zero!")
	
	def test_zero_order_for_1D(self):
		result = zeros(3, order="F")
		expected = ["zeros.shape-order"]
		self.assertEqual(expected, result, "Wrong result when calling zero with order for 1D result!")

		