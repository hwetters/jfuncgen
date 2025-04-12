package se.wetterstrom.jfuncgen;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

/**
 * Status bar
 */
public class StatusBar extends JPanel {

	private static final long serialVersionUID = 1L;

	/** the port status field */
	private final JTextField portStatus = new JTextField("", 8);
	/** the message field */
	private final JTextField message = new JTextField("");

	/**
	 * Constructor
	 */
	public StatusBar() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		portStatus.setBorder(new BevelBorder(BevelBorder.LOWERED));
		portStatus.setEditable(false);
		setPortStatus(Status.OFFLINE);

		message.setBorder(new BevelBorder(BevelBorder.LOWERED));
		message.setEditable(false);

		GuiUtils.addToGridBag(0, 0, 0.0, 0.0, GridBagConstraints.NONE, gbc, this, portStatus);
		GuiUtils.addToGridBag(1, 0, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, this, message);
	}

	/**
	 * Set port status
	 * @param status the port status
	 */
	public void setPortStatus(Status status) {
		portStatus.setText(status.msg);
		portStatus.setBackground(status.bg);
		portStatus.setForeground(status.fg);
	}

	/**
	 * Set message
	 * @param msg the message
	 */
	public void setMessage(String msg) {
		message.setText(msg);
	}

	/**
	 * Set status and message
	 * @param status the status
	 * @param msg the message
	 */
	public void set(Status status, String msg) {
		setPortStatus(status);
		message.setText(msg);
	}

	/**
	 * Status
	 */
	enum Status {
		/** ONLINE */
		ONLINE("Online", new JLabel().getBackground(), new JLabel().getForeground()),
		/** OFFLINE */
		OFFLINE("Offline", Color.BLACK, Color.YELLOW),
		/** ERROR */
		ERROR("Error", Color.BLACK, Color.RED);
		final String msg;
		final Color fg;
		final Color bg;
		private Status(String msg, Color bg, Color fg) {
			this.msg = msg;
			this.bg = bg;
			this.fg = fg;
		}
	}
}
