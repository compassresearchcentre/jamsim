package org.jamsim.io;

import javax.swing.table.TableModel;

public interface ParameterSet {

	void resetDefaults();
	
	void update();
	
	String getName();
	
	TableModel getTableModel();
}
