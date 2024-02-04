package se.wetterstrom.jfuncgen;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

/**
 * About
 */
public class AboutPanel extends JPanel implements FuncTab {

	private static final long serialVersionUID = 1L;

	/** Constructor */
	public AboutPanel() {
		setLayout(new BorderLayout());

		var imageIcon = new ImageIcon(getClass().getResource("jfunc5200.jpg"));
		var imagePanel = new JPanel();
		imagePanel.add(new JLabel(imageIcon, SwingConstants.CENTER), BorderLayout.CENTER);

		var textPane = new JTextPane();
		var scrollPane = new JScrollPane(textPane);
		GuiUtils.keyNavigateTextArea(textPane, scrollPane);
		textPane.setContentType("text/html");
		textPane.setCaretPosition(0);
		textPane.setEditable(false);

		textPane.setText(Utils.format("about.html", System.getProperty("java.vendor"),
				System.getProperty("java.version"), System.getProperty("os.name"), System.getProperty("os.version"),
				System.getProperty("os.arch")));

		add(imagePanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	@Override
	public void reload() {
		// ignore
	}
}
