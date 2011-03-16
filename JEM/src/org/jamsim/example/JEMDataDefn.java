package org.jamsim.example;

import net.casper.ext.file.def.CDataFileDoubleArray;
import net.casper.ext.file.def.CDataFileIntArray;
import net.casper.ext.file.def.CDataFileMap;
import net.casper.io.file.def.CDataFileDef;

import org.omancode.rmt.cellreader.CellReader;
import org.omancode.rmt.cellreader.CellReaders;

/**
 * Definitions for datasets used by {@link JEMData}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public final class JEMDataDefn {

	private JEMDataDefn() {
		// no instantiation
	}

	/**
	 * Data dictionary.
	 */
	public static final CDataFileMap<String, String> DICTIONARY_MAP =
			new CDataFileMap<String, String>("JEM data dictionary",
					"Variable", CellReaders.STRING, "Name",
					CellReaders.STRING);

	public static final int MAX_LIFE = 99;

	/**
	 * Annual earnings scale by disability status.
	 */
	public static final CDataFileIntArray ANNUAL_EARNINGS_SCALE =
			new CDataFileIntArray(
					"Annual earnings scale by disability status", "Earnings");

	/**
	 * Proportion of births which are female.
	 */
	public static final CDataFileDoubleArray FEMALE_PROP =
			new CDataFileDoubleArray("Proportion of births which are female",
					"Value");

	public static final CDataFileDef DFLE_TRANSITIONS = new CDataFileDef(
			"Disability state transition probabilities",
			"Sex,Agegrp,Current disability state," + "No Disability,"
					+ "Mild Disability," + "Moderate Disability,"
					+ "Severe Disability",

			new CellReader<?>[] { CellReaders.CHARACTER, CellReaders.INTEGER,
					CellReaders.INTEGER, CellReaders.DOUBLE,
					CellReaders.DOUBLE, CellReaders.DOUBLE,
					CellReaders.DOUBLE },

			"Sex,Agegrp,Current disability state");

	public static final CDataFileDoubleArray PROB_MORT_MALE =
			new CDataFileDoubleArray("Probabilities of male death by age",
					"Male");

	public static final CDataFileDoubleArray PROB_MORT_FEMALE =
			new CDataFileDoubleArray("Probabilities of female death by age",
					"Female");

}
