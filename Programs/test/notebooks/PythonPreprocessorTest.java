package notebooks;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.junit.Test;


public class PythonPreprocessorTest {
	
	@Test
	public void testProcess_oneSimpleLine() {
		String[] str = {"abc\n"};
		List<String> expectedOutput = new ArrayList<String>(1);
		expectedOutput.add(str[0]);
		
		processAndCheck(str, expectedOutput);
	}
	
	@Test
	public void testProcess_twoSimpleLines() {
		String[] strings = {"abc\n", "def\n"};
		List<String> expectedOutput = new ArrayList<String>(2);
		expectedOutput.add(strings[0]);
		expectedOutput.add(strings[1]);
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_severalStatements() {
		String[] strings = {"import numpy as np; import pandas as pd\n", "b = np.sin(a)\n", "c = np.cos(b); d = np.tan(c)\n"};
		List<String> expectedOutput = new ArrayList<String>(5);
		expectedOutput.add("import numpy as np");
		expectedOutput.add(" import pandas as pd\n");
		expectedOutput.add("b = np.sin(a)\n");
		expectedOutput.add("c = np.cos(b)");
		expectedOutput.add(" d = np.tan(c)\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_strings() {
		String[] strings = {"import numpy as np; \"This is a comment string with \\\" and # inside.\"; import pandas as pd\n",
				"b = np.sin(a)\n",
				"'Some other string '\n",
				"c = np.cos(b)\n",
				"'And now, a string with # and \n and \\n and \\' to test\n'\n",
				"d = np.tan(c)\n",
				"'Some string'\n",
				"e = np.exp(d)\n"};
		List<String> expectedOutput = new ArrayList<String>(12);
		expectedOutput.add("import numpy as np");
		expectedOutput.add(" \"This is a comment string with \\\" and # inside.\"");
		expectedOutput.add(" import pandas as pd\n");
		expectedOutput.add("b = np.sin(a)\n");
		expectedOutput.add("'Some other string '\n");
		expectedOutput.add("c = np.cos(b)\n");
		expectedOutput.add("'And now, a string with # and \n");
		expectedOutput.add(" and \\n and \\' to test\n");
		expectedOutput.add("'\n");
		expectedOutput.add("d = np.tan(c)\n");
		expectedOutput.add("'Some string'\n");
		expectedOutput.add("e = np.exp(d)\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_comments() {
		String[] strings = {"import numpy as np # This is a comment\n",
				"import matplotlib.pyplot as plt# This comment contains \"a string\"\n",
				"b = np.sin(a) # This comment contains 'a single quoted string' # and a comment, and ends with a \\\n",
				"c = np.cos(b)\n"
		};
		List<String> expectedOutput = new ArrayList<String>(4);
		expectedOutput.add("import numpy as np \n");
		expectedOutput.add("import matplotlib.pyplot as plt\n");
		expectedOutput.add("b = np.sin(a) \n");
		expectedOutput.add("c = np.cos(b)\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_commentsWithQuotes() {
		String[] strings = {"import numpy as np # This is \" containing comment.\n",
				"import pandas as pd#This is a ' containing comment.\n"};
		List<String> expectedOutput = new ArrayList<String>(2);
		expectedOutput.add("import numpy as np \n");
		expectedOutput.add("import pandas as pd\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_escapedNewline() {
		String[] strings = {"abc\\\n", "def\n"};
		List<String> expectedOutput = new ArrayList<String>(1);
		expectedOutput.add("abc def\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_escapedEscapedNewLine() {
		String[] strings = {"abc\\\\\n", "def\n"};
		List<String> expectedOutput = new ArrayList<String>(2);
		expectedOutput.add("abc\\\\\n");
		expectedOutput.add("def\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_escapedNewlineInString() {
		String[] strings = {"\"Some string with\\\ninside\"\n"};
		List<String> expectedOutput = new ArrayList<String>(1);
		expectedOutput.add("\"Some string with inside\"\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_escapedNewlineInComment() {
		String[] strings = {"abc#\\\n", "def\n"};
		List<String> expectedOutput = new ArrayList<String>(2);
		expectedOutput.add("abc\n");
		expectedOutput.add(strings[1]);
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_newLinesInBrackets() {
		String[] strings = {
				"function(a, \n",
				"b, c\n",
				")\n",
				"f1(z, f2(f3(a), \n",
				"b),\n",
				"c)\n"
		};
		List<String> expectedOutput = new ArrayList<String>(2);
		expectedOutput.add("function(a, b, c)\n");
		expectedOutput.add("f1(z, f2(f3(a), b),c)\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_newLineInDifferentBrackets() {
		String[] strings = {
				"function(a,\n",
				"[1, 2,\n",
				" 3], [4, \n",
				"5, 6],\n",
				"b)\n"
		};
		List<String> expectedOutput = new ArrayList<String>(1);
		expectedOutput.add("function(a,[1, 2, 3], [4, 5, 6],b)\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_bracketsInMultiLineString() {
		String[] strings = {
				"\"\"\" Some string with a (\n",
				"inside\"\"\"\n"
		};
		List<String> expectedOutput = new ArrayList<String>(2);
		expectedOutput.add("\"\"\" Some string with a (\n");
		expectedOutput.add("inside\"\"\"\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_stringOnLine() {
		String[] strings = {"import pytz   pytz.timezone('US/Eastern')\n"};
		List<String> expectedOutput = new ArrayList<String>(1);
		expectedOutput.add("import pytz   pytz.timezone('US/Eastern')\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_escapedBackslash() {
		String[] strings = {"sys.path.insert(0, 'D:\\\\\\\\GitHub\\\\\\\\workspace\\\\\\\\A2_ContactAngle\\\\\\\\')\n",
				"import main # getContactAngle main\n"};
		List<String> expectedOutput = new ArrayList<String>(2);
		expectedOutput.add("sys.path.insert(0, 'D:\\\\\\\\GitHub\\\\\\\\workspace\\\\\\\\A2_ContactAngle\\\\\\\\')\n");
		expectedOutput.add("import main \n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_someThingElseEscaped() {
		String[] strings = {"abc\\\" #def\n"};
		List<String> expectedOutput = new ArrayList<String>(1);
		expectedOutput.add("abc\\\" \n");
		processAndCheck(strings, expectedOutput);
	}

	private void processAndCheck(String[] inputStrings, List<String> expectedResult) {
		JSONArray input = new JSONArray();
		for (String str: inputStrings) {
			input.put(str);
		}
		PythonPreprocessor processor = new PythonPreprocessor(input);
		List<String> result = processor.process();
		assertEquals("Errorneous output of process.", expectedResult, result);
	}

}
