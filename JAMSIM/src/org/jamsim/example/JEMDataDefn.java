package org.jamsim.example;

import java.util.HashMap;
import java.util.Map;

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

	private static final String DICTIONARY_NAME = "JEM data dictionary";

	private static final String ANNUAL_EARNINGS_SCALE_NAME =
			"Annual earnings scale by disability status";

	private static final String PROB_MORT_FEMALE_NAME =
			"Probabilities of female death by age";

	private static final String PROB_MORT_MALE_NAME =
			"Probabilities of male death by age";

	private static final String DFLE_TRANSITIONS_NAME =
			"Disability state transition probabilities";

	static final String BASEFILE_DEFINITION = "JEM base file definition";

	/**
	 * Default file locations.
	 */
	public static final Map<String, String> DEFAULT_FILE_LOCATIONS =
			defaultFileLocations(new HashMap<String, String>());

	private static Map<String, String> defaultFileLocations(
			Map<String, String> map) {
		String dataDir = System.getProperty("user.dir") + "\\JEM.data\\";
		map.put(DICTIONARY_NAME, dataDir + "Data dictionary.xlsx");
		map.put(ANNUAL_EARNINGS_SCALE_NAME, dataDir
				+ "Annual earnings scale by disability status.xlsx");
		map.put(PROB_MORT_FEMALE_NAME,
				dataDir
						+ "Probabilities of male and female death by age and sex.xlsx");
		map.put(PROB_MORT_MALE_NAME,
				dataDir
						+ "Probabilities of male and female death by age and sex.xlsx");
		map.put(DFLE_TRANSITIONS_NAME, dataDir
				+ "Disability state transition probabilities.xlsx");
		
		map.put(BASEFILE_DEFINITION, dataDir
				+ "JEM base file definition.txt");
		map.put("base file", dataDir + "Base file (people).xlsx");
		map.put("R startup file", dataDir + "JEM.r");
		
		return map;
	}

	/**
	 * Max age.
	 */
	public static final int MAX_LIFE = 99;

	/**
	 * Data dictionary.
	 */
	public static final CDataFileMap<String, String> DICTIONARY_MAP =
			new CDataFileMap<String, String>(DICTIONARY_NAME, "Variable",
					CellReaders.STRING, "Name", CellReaders.STRING);

	/**
	 * Annual earnings scale by disability status.
	 */
	public static final CDataFileIntArray ANNUAL_EARNINGS_SCALE =
			new CDataFileIntArray(ANNUAL_EARNINGS_SCALE_NAME, "Earnings");

	/**
	 * Disability state transition probabilities.
	 */
	public static final CDataFileDef DFLE_TRANSITIONS = new CDataFileDef(
			DFLE_TRANSITIONS_NAME, "Sex,Agegrp,Current disability state,"
					+ "No Disability," + "Mild Disability,"
					+ "Moderate Disability," + "Severe Disability",

			new CellReader<?>[] { CellReaders.CHARACTER, CellReaders.INTEGER,
					CellReaders.INTEGER, CellReaders.DOUBLE,
					CellReaders.DOUBLE, CellReaders.DOUBLE,
					CellReaders.DOUBLE },

			"Sex,Agegrp,Current disability state");

	/**
	 * Probabilities of male death by age.
	 */
	public static final CDataFileDoubleArray PROB_MORT_MALE =
			new CDataFileDoubleArray(PROB_MORT_MALE_NAME, "Male");

	/**
	 * Probabilities of female death by age.
	 */
	public static final CDataFileDoubleArray PROB_MORT_FEMALE =
			new CDataFileDoubleArray(PROB_MORT_FEMALE_NAME, "Female");

	private JEMDataDefn() {
		// no instantiation
	}

}
