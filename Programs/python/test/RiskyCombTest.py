"""
TO TEST:
- show
- arange
- array
- zeros

- find_risky_pairs
- Parameter parsing
"""

import unittest
from risky_comb_functions import *

class RiskyCombTest(unittest.TestCase):
	
	def test_show(self):
		result = show()
		self.assertEqual([], result, "Non-empty list returned by show!")