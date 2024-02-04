package se.wetterstrom.jfuncgen;

import java.awt.Component;
import java.util.function.Function;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Mapping list renderer
 */
class MappingListRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 1L;
	private final transient Function<Object,String> mapper;

	/**
	 * Constructor
	 * @param mapper the mapper
	 */
	public MappingListRenderer(Function<Object,String> mapper) {
		this.mapper = mapper;
	}

	@Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(list, mapper.apply(value), index, isSelected, cellHasFocus);
    }
}
