package se.wetterstrom.jfuncgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Configuration
 */
public class Config {

	/** The home directory */
	private static final String USER_HOME = System.getProperty("user.home");
	/** Default name of settings file (without path). */
	private static final String DEFAULT_SETTING_FILE_NAME = ".jfuncgen.cfg";
	/** Hash map with loaded parameters */
	private static final Map<String,String> properties = new HashMap<>();
	/** ConfigParamter to string */
	private static final Function<ConfigParameter,String> PARAM_TOSTRING = p -> Optional.ofNullable(properties.get(p.key)).map(v -> p.key + " = " + v + '\n').orElse("");
	/** Function to retrieve key from properties or defaultValue if absent. */
	private static final BiFunction<String, Integer, Integer> GET_INTEGER = (key, defaultValue) -> Optional.ofNullable(properties.get(key)).map(Integer::valueOf).orElse(defaultValue);
	/** the string getter operator */
	private static final BinaryOperator<String> GET_STRING = (key, defaultValue) -> Optional.ofNullable(properties.get(key)).map(String::valueOf).orElse(defaultValue);
	/** The default look and feel */
	private static final String DEFAULT_LOOK = "javax.swing.plaf.metal.MetalLookAndFeel";

	/** Hidden constructor */
	private Config() {
		super();
	}

	/**
	 * Get config file
	 * @return the name of the default configuration file
	 */
	protected static File getConfigFile() {
		return new File ((USER_HOME != null ? USER_HOME + File.separator : "") + DEFAULT_SETTING_FILE_NAME);
	}

	/**
	 * Load the configuration if it exists.
	 * @throws IOException if error
	 */
	public static void load() throws IOException {
		var cf = getConfigFile();
		if (cf.exists()) {
			load(new FileReader(cf));
		}
	}

	/**
	 * Read configuration from the input stream reader
	 * @param input the input
	 * @throws IOException if error
	 */
	protected static void load(InputStreamReader input) throws IOException {
		properties.clear();
		try (var reader = new BufferedReader(input)) {
			 reader.lines()
				.map(String::trim)
				.filter(s -> !s.startsWith("#") && s.contains("="))
				.map(s -> new Pair<String,String>(s.substring(0, s.indexOf('=')).trim(), s.substring(s.indexOf('=') + 1).trim()))
				.filter(p -> ConfigParameter.exists(p.key))
				.forEach(p -> properties.put(p.key, p.value));
		}
	}

	/**
	 * Save the configuration
	 * @throws IOException if error
	 */
	public static void save() throws IOException {
		try (var output = new FileWriter(getConfigFile())) {
			save(output);
		}
	}

	/**
	 * Save config
	 * @param output the output write
	 * @throws IOException if error
	 */
	protected static void save(OutputStreamWriter output) throws IOException {
		var buf = new StringBuilder();
		buf.append("# ").append(new Date()).append('\n');
		Arrays.stream(ConfigParameter.values()).sorted().forEach(p -> buf.append(PARAM_TOSTRING.apply(p)));
		output.write(buf.toString());
	}

	/**
	 * Set font size
	 * @param fontSize the font size
	 */
	public static void setFontSize(Integer fontSize) {
		if (fontSize != null) {
			properties.put(ConfigParameter.FONT_SIZE.key, String.valueOf(fontSize));
			GuiUtils.setDefaultFonts(fontSize);
		} else {
			properties.remove(ConfigParameter.FONT_SIZE.key);
		}
	}

	/**
	 * Get font size
	 * @return the font size
	 */
	public static Integer getFontSize() {
		return GET_INTEGER.apply(ConfigParameter.FONT_SIZE.key, 11);
	}

	/**
	 * Get look and feel
	 * @return the look and feel
	 */
	public static LookAndFeelInfo getLook() {
		LookAndFeelInfo result = null;
		var className = GET_STRING.apply(ConfigParameter.LOOK.key, DEFAULT_LOOK);
		for (var laf : UIManager.getInstalledLookAndFeels()) {
			if (laf.getClassName().equals(className)) {
				result = laf;
				break;
			} else if (DEFAULT_LOOK.equals(laf.getName())) {
				result = laf;
			}
		}
		return result;
	}

	/**
	 * Set look and feel
	 * @param look the look and feel
	 */
	public static void setLook(LookAndFeelInfo look) {
		if (look != null) {
			properties.put(ConfigParameter.LOOK.key, look.getClassName());
			GuiUtils.setLookAndFeel(look);
		} else {
			properties.remove(ConfigParameter.LOOK.key);
		}
	}

	/**
	 * Set device type
	 * @param deviceType the device type
	 */
	public static void setDeviceType(DeviceType deviceType) {
		if (deviceType != null && !deviceType.deviceName.isEmpty()) {
			properties.put(ConfigParameter.DEVICE_TYPE.key, deviceType.deviceName);
		} else {
			properties.remove(ConfigParameter.DEVICE_TYPE.key);
		}
	}

	/**
	 * Get device type
	 * @return device type
	 */
	public static DeviceType getDeviceType() {
		var name = GET_STRING.apply(ConfigParameter.DEVICE_TYPE.key, "");
		for (var dt : DeviceType.values()) {
			if (dt.deviceName.equals(name)) {
				return dt;
			}
		}
		return DeviceType.NONE;
	}

	/**
	 * Set port
	 * @param port the serial port
	 */
	public static void setPort(SerialPort port) {
		if (port != null) {
			properties.put(ConfigParameter.PORT.key, port.getSystemPortName());
		} else {
			properties.remove(ConfigParameter.PORT.key);
		}
	}

	/**
	 * Get port
	 * @return the serial port
	 */
	public static SerialPort getPort() {
		var name = GET_STRING.apply(ConfigParameter.PORT.key, "");
		for (var p : AbstractSerialCom.getPorts()) {
			if (name.equals(p.getSystemPortName())) {
				return p;
			}
		}
		return null;
	}
}
