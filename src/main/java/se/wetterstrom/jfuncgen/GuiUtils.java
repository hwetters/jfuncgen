package se.wetterstrom.jfuncgen;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;

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
public class GuiUtils {
	private static JFileChooser textFileChooser = null;
	private static Component owner = new JFrame();

	/** Hidden constructor */
	private GuiUtils() {
		super();
	}

	/**
	 * Set look and feel
	 * @param look the look and feel
	 */
	public static void setLookAndFeel(LookAndFeelInfo look) {
		if (look != null) {
			try {
				UIManager.setLookAndFeel(look.getClassName());
			} catch (Exception e) {
				//ignore
			}
		}
	}

	/**
	 * Set owner
	 * @param newOwner the owner
	 */
	public static void setOwner(Component newOwner) {
		owner = newOwner;
	}

	/**
	 * Add component to gridbag
	 * @param x x
	 * @param y y
	 * @param weightx weightx
	 * @param weighty weighty
	 * @param fill fill
	 * @param gbc gbc
	 * @param parent parent
	 * @param component component
	 */
	public static final void addToGridBag(int x, int y, double weightx, double weighty, int fill, GridBagConstraints gbc,
			JComponent parent, JComponent component) {
		addToGridBag(x, y, 1, 1, weightx, weighty, fill, gbc, parent, component);
	}

	/**
	 * Add component to gridbag
	 * @param x x
	 * @param y y
	 * @param wdt wdt
	 * @param hgt hgt
	 * @param weightx weightx
	 * @param weighty weight y
	 * @param fill fill
	 * @param gbc gbc
	 * @param parent parent component
	 * @param component component
	 */
	public static final void addToGridBag(int x, int y, int wdt, int hgt, double weightx, double weighty, int fill,
			GridBagConstraints gbc, JComponent parent, JComponent component) {
		addToGridBag(x, y, wdt, hgt, weightx, weighty, fill, GridBagConstraints.NORTHWEST, gbc, parent, component);
	}

	/**
	 * Add component to gridbag
	 * @param x x
	 * @param y y
	 * @param wdt wdt
	 * @param hgt hgt
	 * @param weightx weightx
	 * @param weighty weighty
	 * @param fill fill
	 * @param anchor anchor
	 * @param gbc gbc
	 * @param parent parent
	 * @param component component
	 * @return parent
	 */
	public static final JComponent addToGridBag(int x, int y, int wdt, int hgt, double weightx, double weighty, int fill, int anchor,
			GridBagConstraints gbc, JComponent parent, JComponent component) {
		gbc.anchor = anchor;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = wdt;
		gbc.gridheight = hgt;
		gbc.fill = fill;
		parent.add(component, gbc);
		return parent;
	}

	/**
	 * Fill gridbag
	 * @param x x
	 * @param y y
	 * @param colCount column count
	 * @param rowCount row count
	 * @param gbc gbc
	 * @param parent parent
	 */
	public static final void fillGridBag(int x, int y, int colCount, int rowCount, GridBagConstraints gbc,
			JComponent parent) {
		addToGridBag(x,y,colCount, rowCount, 1.0, 1.0, GridBagConstraints.BOTH, gbc, parent, new JPanel());
	}

	/**
	 * Set default font sizes
	 * @param fontSize the font size
	 */
	public static final void setDefaultFonts(int fontSize) {
		var plainFont = new Font("Verdana", Font.PLAIN, fontSize);
		var labelFont = new Font("Verdana", Font.PLAIN, fontSize - fontSize/10 - 1);
		UIManager.getDefaults().keySet().stream()
			.filter(String.class::isInstance).map(String.class::cast)
			.filter(s -> s.endsWith(".font"))
			.forEach(k -> UIManager.put(k, new FontUIResource(plainFont)));
		UIManager.put("Label.font", new FontUIResource(labelFont));
	}

	/**
	 * Show message dialog
	 * @param parentComponent the parent component
	 * @param message the message
	 */
	public static void showMessage(Component parentComponent, String message) {
		JOptionPane.showMessageDialog(parentComponent, message);
	}

	/**
	 * Open text file dialog
	 * @param directory the directory
	 * @param defaultName the default name
	 * @param saveMode save mode
	 * @return the string
	 */
	public static String openTextFileDialog(File directory, String defaultName, boolean saveMode) {
		return openTextFileDialog(null, directory, defaultName, saveMode, null);
	}

	/**
	 * Show exception dialog
	 * @param parent the parent
	 * @param title the title
	 * @param throwable the throwable
	 * @param message the message
	 * @param args the arguments
	 */
	public static void showException(Component parent, String title, Throwable throwable, String message, Object...args) {
		var messageText = new JTextArea(String.format(message, args));
		messageText.setEditable(false);
		var stacktrace = new JTextArea(toString(throwable));
		stacktrace.setEditable(false);
		var panel = new JPanel(new BorderLayout());
		panel.add(messageText, BorderLayout.NORTH);
		panel.add(new JScrollPane(stacktrace), BorderLayout.CENTER);
		panel.addHierarchyListener(e -> hierarchyListenerResizer(SwingUtilities.getWindowAncestor(panel)));
		setPreferredSize(panel, 2, 3);
		JOptionPane.showMessageDialog(parent, panel, title, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Set preferred size
	 * @param component component
	 * @param widthFraction width fraction
	 * @param heightFraction height fraction
	 */
	public static void setPreferredSize(JComponent component, int widthFraction, int heightFraction) {
		var dim = Toolkit.getDefaultToolkit().getScreenSize();
		component.setPreferredSize(new Dimension(dim.width/widthFraction, dim.height/heightFraction));
	}

	/**
	 * Set hierarchy listener
	 * @param window the window
	 */
	public static void hierarchyListenerResizer(Window window) {
		if (window instanceof Dialog dialog && !dialog.isResizable()) {
			dialog.setResizable(true);
		}
	}

	/**
	 * Open text file dialog
	 * @param title the title
	 * @param directory the directory
	 * @param defaultName the default name
	 * @param saveMode the save mode
	 * @param extFilter the extension filter
	 * @return the string
	 */
	public static String openTextFileDialog(String title, File directory, String defaultName, boolean saveMode, String[] extFilter) {
		if (textFileChooser == null) {
			textFileChooser = new JFileChooser(directory);
			FileFilter fileFilter = getFileFilter("Text", extFilter);
			textFileChooser.addChoosableFileFilter(fileFilter);
			textFileChooser.setFileFilter(fileFilter);
			textFileChooser.setMultiSelectionEnabled(false);
		} else if (directory != null) {
			textFileChooser.setCurrentDirectory(directory);
		}
		textFileChooser.setSelectedFile(getDefaultFile(defaultName, extFilter));
		if (saveMode) {
			textFileChooser.setDialogTitle(title != null ? title : "Save text file ");
			if (textFileChooser.showSaveDialog(owner) == JFileChooser.APPROVE_OPTION) {
				return textFileChooser.getSelectedFile()+"";
			}
		} else {
			textFileChooser.setDialogTitle(title != null ? title : "Open text file");
			if (textFileChooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
				return textFileChooser.getSelectedFile()+"";
			}
		}
		return null;
	}

	/**
	 * Convert to stacktrace string
	 * @param throwable the throwable
	 * @return text representation of throwable
	 */
	public static String toString(Throwable throwable) {
		var writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	/**
	 * Get default file
	 * @param defaultName the default name
	 * @param extFilter the extension filter
	 * @return the file
	 */
	protected static File getDefaultFile(String defaultName, String[] extFilter) {
		if (defaultName != null && !defaultName.isEmpty()) {
			if (extFilter == null || extFilter.length == 0) {
				return new File(defaultName);
			} else {
				String lname = defaultName.toLowerCase();
				for (String ext : extFilter) {
					if (lname.endsWith(ext)) {
						return new File(defaultName);
					}
				}
				return new File(defaultName + extFilter[0]);
			}
		} else {
			return null;
		}
	}

	/**
	 * Get file filter
	 * @param extName the extension name
	 * @param extFilter the extension filter
	 * @return the file filter
	 */
	protected static FileFilter getFileFilter(final String extName, final String[] extFilter) {
		return new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory() || extFilter == null || extFilter.length == 0) {
					return true;
				} else {
					String lname = f.getName().toLowerCase();
					for (String ext : extFilter) {
						if (lname.endsWith(ext)) {
							return true;
						}
					}
				}
				return false;
			}
			@Override
			public String getDescription () { return extName; }
		};
	}

	/**
	 * Add menu item
	 * @param menu the menu
	 * @param label the label
	 * @param mnemonic the mnemonic
	 * @param listener the listener
	 * @return the menu item
	 */
	public static JMenuItem addMenuItem(JPopupMenu menu, String label, Character mnemonic, ActionListener listener) {
		var menuItem = new JMenuItem(label, mnemonic);
		menuItem.setActionCommand(label);
		menuItem.addActionListener(listener);
		menu.add (menuItem);
		return menuItem;
	}

	/**
	 * Set location
	 * @param component the component
	 * @param widthFraction the width fraction
	 * @param heightFraction the height fraction
	 * @return the container
	 */
	public static Container setLocation(Container component, int widthFraction, int heightFraction) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		component.setLocation(
				(int)((dim.width - component.getSize().getWidth()) / widthFraction),
				(int)((dim.height - component.getSize().getHeight()) / heightFraction));
		return component;
	}

	/**
	 * Add keyboard navigation to text component
	 * @param component the text component
	 * @param scrollPane the scrollpane with the scrollbars to move
	 */
	public static void keyNavigateTextArea(JTextComponent component, JScrollPane scrollPane) {
		component.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent ev) { /* Not used */ }

			@Override
			public void keyPressed(KeyEvent ev) { /* Not used */ }

			@Override
			public void keyReleased(KeyEvent ev) {
				int keyCode = ev.getKeyCode();
				if (keyCode == KeyEvent.VK_HOME) {
					var vertical = scrollPane.getVerticalScrollBar();
					vertical.setValue(vertical.getMinimum());
				} else if (keyCode == KeyEvent.VK_END) {
					var vertical = scrollPane.getVerticalScrollBar();
					vertical.setValue(vertical.getMaximum() );
				}
			}
		});
	}

	/**
	 * Get icon
	 * @param icon the icon name
	 * @param iconlabel the label
	 * @return image icon
	 */
	public static ImageIcon getIcon(String icon, String iconlabel) {
		try {
			return new ImageIcon(ImageIO.read(GuiUtils.class.getResource(icon)), iconlabel);
		} catch (IOException e1) {
			return null;
		}
	}
}
