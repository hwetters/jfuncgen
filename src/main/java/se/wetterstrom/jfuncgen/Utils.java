package se.wetterstrom.jfuncgen;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * <pre style='font-family:sans-serif;'>
 *   JFuncGen - A java GUI for the MHS5200 and FY6900 function generators
 *   Copyright (C) 2021 Henrik Wetterstrom
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * </pre>
 */
public class Utils {

	/** double parsing regexp */
	private static final String DOUBLE_REGEXP = "\\s*(\\d*\\.)?(\\d+)([eE][-+]?\\d+)?\\s*([kKmMGg]?[hH][zZ])?\\s*";
	/** double pattern */
	private static final Pattern DOUBLE_PATTERN = Pattern.compile(DOUBLE_REGEXP);
	/** hex strings */
	private static final String[] HEX = IntStream.range(0, 256).boxed().map(i -> String.format("%02x", i))
			.toArray(String[]::new);

	/** Hidden constructor */
	private Utils() {
		super();
	}

	/**
	 * Parses a double/integer, optionally trailed by {kHz|MHz|GHz}
	 *
	 * @param str the string to parse
	 * @return the parsed double
	 * @throws ParseException in case of parse errors
	 */
	public static double parseFreq(String str) throws ParseException {
		var m = DOUBLE_PATTERN.matcher(str);
		if (m.matches()) {
			String s = Optional.ofNullable(m.group(1)).orElse("") + Optional.ofNullable(m.group(2)).orElse("")
					+ Optional.ofNullable(m.group(3)).orElse("");
			double d = Double.parseDouble(s);
			switch (Optional.ofNullable(m.group(4)).map(String::toUpperCase).orElse("")) {
			case "KHZ":
				return d * 1000;
			case "MHZ":
				return d * 1000000;
			case "GHZ":
				return d * 1000000000;
			case "HZ":
			default:
				return d;
			}
		}
		throw new ParseException("Failed to parse frequency", 0);
	}

	/**
	 * Write string to file
	 * @param targetFile the file to be written to
	 * @param string     the string to write
	 */
	public static void writeFile(File targetFile, String string) {
		if (targetFile == null || string == null) {
			throw new IllegalArgumentException("No target file");
		}
		try (var output = new PrintWriter(targetFile)) {
			output.println(string);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to write file: " + e.getMessage(), e);
		}
	}

	/**
	 * Read file to string
	 * @param file the file to read
	 * @return the contents of the file
	 */
	public static String readFile(File file) {
		if (file == null) {
			throw new IllegalArgumentException("No file");
		}
		try {
			return Files.readString(file.toPath());
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to read file: " + e.getMessage(), e);
		}
	}

	/**
	 * Get resource
	 * @param resourceFile the resource file
	 * @return string
	 */
	public static String getResource(String resourceFile) {
		try (var in = Utils.class.getResourceAsStream(resourceFile);
				var scanner = new Scanner(in, StandardCharsets.UTF_8)) {
			return scanner.useDelimiter("\\Z").next();
		} catch (Exception e) { // NOSONAR
			return "Failed to read " + resourceFile + " resource: \n" + e.getMessage();
		}
	}

	/**
	 * Format
	 * @param resourceFile the resource file
	 * @param arguments    format arguments
	 * @return string
	 */
	public static String format(String resourceFile, Object... arguments) {
		try (var in = Utils.class.getResourceAsStream(resourceFile);
				var scanner = new Scanner(in, StandardCharsets.UTF_8)) {
			return MessageFormat.format(scanner.useDelimiter("\\Z").next(), arguments);
		} catch (Exception e) { // NOSONAR
			return "Failed to read " + resourceFile + " resource: \n" + e.getMessage();
		}
	}

	/**
	 * Get hexdump string from bytes
	 * @param data the data
	 * @return string with hexdump of data
	 */
	public static String hexDump(byte[] data) {
		if (data == null) {
			return "null";
		}
		var buf = new StringBuilder();
		var asc = new char[16];
		for (int i = 0; i < data.length; i++) {
			if (i % 16 == 0) {
				if (i > 0) {
					buf.append("  ").append(asc).append('\n');
				}
				buf.append(getIntHexString(i)).append(": ");
			}
			int c = data[i] & 0xff;
			asc[i % 16] = c >= 0x20 && c < 0x7f ? (char) c : '.';
			buf.append(' ').append(HEX[c]);
		}
		int l = (data.length - 1) % 16;
		if (l > 0) {
			buf.append("  ");
			for (int i = 0; i < 16; i++) {
				buf.append(i <= l ? asc[i] : ' ');
			}
		}
		return buf.toString();
	}

	/**
	 * Get integer as hex string
	 * @param i the integer
	 * @return the integer as a two character hex string
	 */
	public static String getIntHexString(int i) {
		return HEX[i >>> 24 & 0xff] + HEX[i >>> 16 & 0xff] + HEX[i >>> 8 & 0xff] + HEX[i & 0xff];
	}

	/**
	 * check if string empty
	 * @param s the string to check
	 * @return true if s is null or empty
	 */
	public static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
}
