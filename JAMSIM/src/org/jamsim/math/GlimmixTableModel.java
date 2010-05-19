package org.jamsim.math;

import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * Generalised linear mixed model (GLIMMIX) table model.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class GlimmixTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1025361400039998157L;
	private static final String[] COLUMN_NAMES = { "Effect", "Estimate" };
	private static final Class<?>[] COLUMN_TYPES =
			{ String.class, Double.class };

	private final String[] effect;
	private final Double[] estimate;

	/**
	 * Construct table model from {@link Glimmix}.
	 * 
	 * @param glimmix glimmix model
	 */
	public GlimmixTableModel(Glimmix glimmix) {
		Map<String, Double> effectEstimates = glimmix.getEffectEstimates();

		effect = new String[effectEstimates.size()];
		effectEstimates.keySet().toArray(effect);

		estimate = new Double[effectEstimates.size()];
		effectEstimates.values().toArray(estimate);

	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return effect.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return effect[rowIndex];
		} else {
			return estimate[rowIndex];
		}
	}

	@Override
	public String getColumnName(int columnIndex) {
		return COLUMN_NAMES[columnIndex];
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell.
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return COLUMN_TYPES[columnIndex];
	}

}
