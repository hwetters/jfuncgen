package se.wetterstrom.jfuncgen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

	@Test
	public void testWriteFile() throws IOException {
		var f = File.createTempFile("UnitTest", ".dat");
		f.deleteOnExit();
		Utils.writeFile(f, "1234567890");
		Assert.assertEquals("read string", "1234567890\n",Files.readString(f.toPath()));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWriteFileNull() {
		Utils.writeFile(null, "1234567890");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWriteFileNullData() throws IOException {
		Utils.writeFile(File.createTempFile("UnitTest", ".dat"),null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWriteBadFile() {
		Utils.writeFile(new File("/this/file/can/not/be/saved"), "1234567890");
	}

	@Test
	public void testParseFreq() throws ParseException {
		Assert.assertEquals("1.0", "1.0", ""+Utils.parseFreq("1.0"));
		Assert.assertEquals("123.0", "123.0", ""+Utils.parseFreq("123.0"));
		Assert.assertEquals("123.0 kHz", "123000.0", ""+Utils.parseFreq("123.0 kHz"));
		Assert.assertEquals("123.0 MHz", "1.23E8", ""+Utils.parseFreq("123.0 MHz"));
		Assert.assertEquals("123.0 MHz", "1.23E11", ""+Utils.parseFreq("123.0 GHz"));
		Assert.assertEquals("123.0 MHz", "1.23E11", ""+Utils.parseFreq("123.0 ghz"));
		Assert.assertEquals("123.0 MHz", "1.23E11", ""+Utils.parseFreq("123.0ghz"));
		Assert.assertEquals("2.34E3 kHz", "2340000.0", ""+Utils.parseFreq("2.34e3 kHz"));
	}

	@Test(expected=ParseException.class)
	public void testParseFreqNeg() throws ParseException {
		Utils.parseFreq("-1.0");
	}

	@Test
	public void testHexDump() {
		Assert.assertEquals("null",
				"null",
				Utils.hexDump(null));
		Assert.assertEquals("oneline",
				"00000000:  41 42 43 44 45 46 20 ec  ABCDEF .        ",
				Utils.hexDump(new byte[]{65,66,67,68,69,70,32,-20}));
		Assert.assertEquals("oneline",
				"00000000:  01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10  ................",
				Utils.hexDump(new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16}));
		Assert.assertEquals("twoline",
				"00000000:  01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10  ................\n00000010:  11 12  ..              ",
				Utils.hexDump(new byte[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18}));
	}

	@Test
	public void testGetResource() {
		String s = Utils.getResource("about.html");
		Assert.assertNotNull("null", s);
		Assert.assertFalse("not empty",s.isEmpty());
	}
}
