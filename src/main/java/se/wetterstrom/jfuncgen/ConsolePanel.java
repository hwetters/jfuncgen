package se.wetterstrom.jfuncgen;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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
public class ConsolePanel extends JPanel implements FuncTab {

	private static final long serialVersionUID = 1L;

	/** received data panel */
	private final JTextArea received = new JTextArea();
	/** send command field */
	private final JTextField cmdField = new JTextField();
	/** received data scroll pane */
	private final JScrollPane scrollPane = new JScrollPane(received);
	/** the serial cmd */
	private transient AbstractSerialCom cmd;
	/** the console stream */
	private final transient ConsoleStream consoleStream = new ConsoleStream(received);

	/** Constructor */
	public ConsolePanel() {
		this.setup();
	}

	/**
	 * @param cmd the cmd
	 */
	public void setSerial(AbstractSerialCom cmd) {
		this.cmd = cmd;
	}

	private void setup() {
		setLayout(new GridBagLayout());
		var gbc = new GridBagConstraints();

		received.setEditable(false);
		received.setLineWrap(true);

		GuiUtils.addToGridBag(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.BOTH, gbc, this, scrollPane);
		GuiUtils.addToGridBag(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, this, cmdField);

		cmdField.addActionListener(e -> cmd.writeSerial(cmdField.getText().trim() + '\n'));

		var popMenu = new JPopupMenu();
		GuiUtils.addMenuItem(popMenu, "Clear", 'c', event -> received.setText(""));
		GuiUtils.addMenuItem(popMenu, "Save", 'a', event -> saveConsole());
		received.setComponentPopupMenu(popMenu);
	}

	/** @return consoel stream */
	public ConsoleStream getConsoleStream() {
		return consoleStream;
	}

	private void saveConsole() {
		String fileName = GuiUtils.openTextFileDialog(null, "console.txt", true);
		if (fileName != null) {
			try {
				Utils.writeFile(new File(fileName), received.getText());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * console stream
	 */
	public class ConsoleStream extends OutputStream {
		private final JTextArea textArea;

		/**
		 * @param textArea the text area
		 */
		public ConsoleStream(JTextArea textArea) {
			this.textArea = textArea;
		}

	    @Override
	    public void write(int b) throws IOException {
	    	append(String.valueOf((char)b));
	    }

	    @Override
	    public void write(byte[] data, int off, int len) throws IOException {
	        append(new String(data, off, len, StandardCharsets.ISO_8859_1));
	    }

	    /**
	     * @param obj the object
	     * @return the stream
	     */
	    public ConsoleStream append(Object obj) {
	    	return append(String.valueOf(obj));
	    }

	    /**
	     * @param str the string
	     * @return the stream
	     */
	    public ConsoleStream append(String str) {
	        // redirects data to the text area
	        textArea.append(str);
	        // scrolls the text area to the end of data
	        textArea.setCaretPosition(textArea.getDocument().getLength());
	    	return this;
	    }
	}

	@Override
	public void reload() {
		// not used
	}

}
