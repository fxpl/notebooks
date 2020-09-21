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
	public void testProcess_multiLineStrings() {
		String[] strings = {"import numpy as np \"\"\"This is a multi \n", "line comment string with \\\"\\\"\\\" inside\"\"\"import pandas as pd\n",
				"b = np.sin(a)\n", "'''Some other \n", "multi line string'''\n", "c = np.cos(b) '''And now, a string with # and \n and \\n and \\'\\'\\' to test\n'''\n",
				"d = np.tan(c)'''Some string'''e = np.exp(d)\n"};
		List<String> expectedOutput = new ArrayList<String>(7);
		expectedOutput.add("import numpy as np ");
		expectedOutput.add("import pandas as pd\n");
		expectedOutput.add("b = np.sin(a)\n");
		expectedOutput.add("\n");
		expectedOutput.add("c = np.cos(b) \n");
		expectedOutput.add("d = np.tan(c)");
		expectedOutput.add("e = np.exp(d)\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_mulitLineStringsWithSingleLineStringsDelimiter() {
		String[] strings = {"import numpy as np '''This is a string with \" and ' inside.''' import pandas as pd\n",
		"b = np.sin(a) \"\"\"This is a string with \" and ' inside.\"\"\" c = np.cos(b)\n"};
		List<String> expectedOutput = new ArrayList<String>(4);
		expectedOutput.add("import numpy as np ");
		expectedOutput.add(" import pandas as pd\n");
		expectedOutput.add("b = np.sin(a) ");
		expectedOutput.add(" c = np.cos(b)\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_delimitersInMultiLineString() {
		String[] strings = {"import numpy as np '''This is a string with \"\"\" inside.''' import pandas as pd\n",
				"b = np.sin(a) \"\"\"This is a string with ''' inside.\"\"\" c = np.cos(b)\n"};
		List<String> expectedOutput = new ArrayList<String>(4);
		expectedOutput.add("import numpy as np ");
		expectedOutput.add(" import pandas as pd\n");
		expectedOutput.add("b = np.sin(a) ");
		expectedOutput.add(" c = np.cos(b)\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_strings() {
		String[] strings = {"import numpy as np \"This is a comment string with \\\" inside.\"import pandas as pd\n",
				"b = np.sin(a)\n", "'Some other string '\n", "c = np.cos(b) 'And now, a string with # and \n and \\n and \\' to test\n'\n",
				"d = np.tan(c)'Some string'e = np.exp(d)\n"};
		List<String> expectedOutput = new ArrayList<String>(7);
		expectedOutput.add("import numpy as np ");
		expectedOutput.add("import pandas as pd\n");
		expectedOutput.add("b = np.sin(a)\n");
		expectedOutput.add("\n");
		expectedOutput.add("c = np.cos(b) \n");
		expectedOutput.add("d = np.tan(c)");
		expectedOutput.add("e = np.exp(d)\n");
		processAndCheck(strings, expectedOutput);
	}
	
	@Test
	public void testProcess_stringConstants() {
		String[] strings = {
				"var1 = 'apa'\n",
				"var2 = \"kossa\"\n",
				"function(a=\"milou\", b=\"oscar\")\n",
		};
		List<String> expectedOutput = new ArrayList<String>(3);
		expectedOutput.add("var1 = \n");
		expectedOutput.add("var2 = \n");
		expectedOutput.add("function(a=");
		expectedOutput.add(", b=");
		expectedOutput.add(")\n");
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
	
	//@Test
	public void testProcess_escapedNewline() {
		String[] strings = {"abc\\\n", "def\n"};
		List<String> expectedOutput = new ArrayList<String>(1);
		expectedOutput.add("abc def\n");
		processAndCheck(strings, expectedOutput);
	}
	
	//@Test
	public void testProcess_excapedNewlineInComment() {
		String[] strings = {"abc #\\\n", "def\n"};
		List<String> expectedOutput = new ArrayList<String>(2);
		expectedOutput.add(strings[0]);
		expectedOutput.add(strings[1]);
		processAndCheck(strings, expectedOutput);
	}
	
	/*
	 * TODO:
	 * Mergea alla strängar (OK)
	 * Ta bort (i ordning):
	 * Trippelfnuttade strängar (OK)
	 * Vanligt fnuttade strängar (OK)
	 * Kommentarer (och ta bort ur regexpar)
	 * Escapade radbrytningar (räknas som bortkommenterad om i kommentar! :-))
	 * Radbrytningar inom parenteser, även flera nivåer
	 * 
	 * Splitta på \n och ; (OK)
	 */

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
