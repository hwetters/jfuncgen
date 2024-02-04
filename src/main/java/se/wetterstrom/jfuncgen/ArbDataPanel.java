package se.wetterstrom.jfuncgen;

import java.awt.BorderLayout;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The arb data panel
 */
public class ArbDataPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/** the data model */
	private final DataModel model = new DataModel();
	/** the table */
	private final JTable table = new JTable(model);
	/** the scroll pane */
	private final JScrollPane scrollPane = new JScrollPane(table);

	/** Constructor */
	public ArbDataPanel() {
		setLayout(new BorderLayout());
		table.getTableHeader().setReorderingAllowed(false);
		table.setColumnSelectionAllowed(true);
		table.setRowSelectionAllowed(true);

		var cellRenderer = new DefaultTableCellRenderer();
		cellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

		table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
		table.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);

		int w1 = table.getFontMetrics(table.getFont()).stringWidth("00000");
		table.getColumnModel().getColumn(0).setPreferredWidth(w1);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);

		add(scrollPane, BorderLayout.CENTER);

		var popMenu = new JPopupMenu();
		GuiUtils.addMenuItem(popMenu, "Zero", 'z', event -> setSelectedDataValues(0.0));
		GuiUtils.addMenuItem(popMenu, "Select all", 'a', event -> table.selectAll());
		table.setComponentPopupMenu(popMenu);
	}

	private void setSelectedDataValues(double value) {
		for (int r : table.getSelectedRows()) {
			model.setValueAt(value, r, 1);
		}
	}

	/** @param data the data */
	public void setData(double[] data) {
		model.setData(Arrays.copyOf(data, data.length));
	}

	/** @param data the data */
	public void setData(int[] data) {
		model.setData(Arrays.stream(data).asDoubleStream().toArray());
	}

	/** @return the data */
	public double[] getData() {
		return model.getData();
	}

	/**
	 * Data table model
	 */
	private class DataModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private double[] data = new double[0];

		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return "#";
			} else {
				return "Value";
			}
		}

		/** @param data the data */
		public void setData(double[] data) {
			this.data = data;
		}

		/** @return the data */
		public double[] getData() {
			return data;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) {
				return row;
			} else {
				return row < data.length && row >= 0 ? data[row] : null;
			}
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return col == 0 ? Integer.class : Double.class;

		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column == 1 && row < data.length && row >= 0) {
				if (value instanceof Number n) {
					data[row] = n.doubleValue();
				} else if (value instanceof String s) {
					data[row] = Double.parseDouble(s);
				}
			}
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return col == 1;
		}
	}

}
