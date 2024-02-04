package se.wetterstrom.jfuncgen;

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
public enum ConfigParameter {
	/** device_type */
	DEVICE_TYPE("device_type"),
	/** font_size */
	FONT_SIZE("font_size"),
	/** look */
	LOOK("look"),
	/** port */
	PORT("port");

	/** the key */
	final String key;

	/**
	 * Constructor
	 * @param key the key
	 */
	private ConfigParameter(String key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return key;
	}

	/**
	 * @param key the key
	 * @return true if key exists
	 */
	public static boolean exists(String key) {
		for (var v : values()) {
			if (v.key.equals(key)) {
				return true;
			}
		}
		return false;
	}
}
