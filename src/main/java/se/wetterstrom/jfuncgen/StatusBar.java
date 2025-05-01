package se.wetterstrom.jfuncgen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Status bar
 */
public class StatusBar extends JPanel {

	private static final long serialVersionUID = 1L;

	/** the port status field */
	private final JTextField portStatus = new JTextField("", 8);
	/** the message field */
	private final JTextField message = new JTextField("");
	/** Show history button */
	private final JButton showHist = new JButton("...");
	/** The history of status */
	private final List<Pair<Status,String>> history = new ArrayList<>();
	/** The history table */
	private final JTable histTable = new JTable(new StatusHistoryTableModel());
	/** Current status */
	private Status status = Status.OFFLINE;
	/** The parent frame */
	private JFrame parentFrame;

	/**
	 * Constructor
	 */
	public StatusBar() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		portStatus.setBorder(new BevelBorder(BevelBorder.LOWERED));
		portStatus.setEditable(false);
		setPortStatus(Status.OFFLINE);

		showHist.setMargin(new Insets(0,0,0,0));

		message.setBorder(new BevelBorder(BevelBorder.LOWERED));
		message.setEditable(false);

		GuiUtils.addToGridBag(0, 0, 0.0, 0.0, GridBagConstraints.NONE, gbc, this, portStatus);
		GuiUtils.addToGridBag(1, 0, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, this, message);
		GuiUtils.addToGridBag(2, 0, 0.0, 0.0, GridBagConstraints.NONE, gbc, this, showHist);

		initHistoryDialog();
	}

	private void initHistoryDialog() {

		var popupMenu = new JPopupMenu();
		popupMenu.add(new JMenuItem("Clear"))
			.addActionListener(e -> { history.clear(); histTable.repaint(); });
        histTable.setComponentPopupMenu(popupMenu);

		var panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(histTable), BorderLayout.CENTER);
		panel.addHierarchyListener(e ->
			Utils.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(panel)));

		showHist.addActionListener(e ->
			JOptionPane.showMessageDialog(parentFrame, panel, "History", JOptionPane.PLAIN_MESSAGE));
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
		if (!Utils.isEmpty(msg)) {
			history.add(new Pair<>(status, msg));
		}
		message.setText(msg);
	}

	/**
	 * Set status and message
	 * @param status the status
	 * @param msg the message
	 */
	public void set(Status status, String msg) {
		if (!Utils.isEmpty(msg) && !status.equals(this.status)) {
			history.add(new Pair<>(status, msg));
		}
		setPortStatus(status);
		message.setText(msg);
	}

	/**
	 * Set the parent frame
	 * @param frame the parent frame
	 */
	public void setParentFrame(JFrame frame) {
		this.parentFrame=frame;
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

	/** Table model */
	class StatusHistoryTableModel extends DefaultTableModel {

		private static final long serialVersionUID = -2137440798734658602L;
		private static final String [] HEADERS = { "Status", "Message" };

		@Override
		public String getColumnName(int index) {
			return index >= 0 && index < HEADERS.length ? HEADERS[index] : "";
		}

		@Override
		public int getRowCount() {
			return history.size();
		}

		@Override
		public int getColumnCount() {
			return HEADERS.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex >= 0 && columnIndex >= 0 && rowIndex < getRowCount() && columnIndex < getColumnCount()) {
				switch (columnIndex) {
				case 0: return history.get(rowIndex).key;
				case 1: return history.get(rowIndex).value;
				default: return null;
				}
			}
			return null;
		}

	    @Override
	    public boolean isCellEditable(int row, int column) {
	       return false;
	    }
	}
}
