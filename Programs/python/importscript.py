from sys import argv, modules
from collections import Callable

"""
List all functions located in the module whose name is given as an argument
to the function, separated with spaces. If the module doesn't exist, nothing
is printed.
"""

exec("try:\n\timport " + argv[1] + "\nexcept ImportError:\n\texit()")
variables = {}
exec("contents = dir (" + argv[1] + ")", globals(), variables)
contents = variables["contents"]
for member in contents:
	code = "is_function = isinstance(" + argv[1] + "." + member + ", Callable)"
	exec(code, globals(), variables)
	if variables["is_function"]:
		print(member, end=" ") 
print("")
