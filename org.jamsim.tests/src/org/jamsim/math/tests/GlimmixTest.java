package org.jamsim.math.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import net.casper.data.model.CBuilder;
import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.ext.file.def.CDataFileMap;
import net.casper.ext.narrow.CBuildNarrowedFile;
import net.casper.io.file.def.CDataFileDef;

import org.jamsim.io.FileLoader;
import org.jamsim.math.Glimmix;
import org.jamsim.shared.InvalidDataException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omancode.rmt.cellreader.CellReader;
import org.omancode.rmt.cellreader.CellReaders;

public class GlimmixTest {

	public static final String TEST_DIR = "resource/";

	private File glimmixFile = new File(TEST_DIR + "glimmix accom.xls");
	private static FileLoader loader;
	
	public static final CDataFileDef VALUES =
		new CDataFileDef("Glimmix values", "Effect,Estimate",
				new CellReader<?>[] { CellReaders.STRING,
						CellReaders.DOUBLE }, "Effect");

	public static final CDataFileMap<String, Double> VALUES_MAP =
		new CDataFileMap<String, Double>(VALUES,
				new LinkedHashMap<String, Double>(), "Effect", "Estimate");
	
	private static Map<String, Double> values; 

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		loader = new FileLoader();
		
		values = loader.loadMap(VALUES_MAP);
		
	}

	@Test
	public void testGlimmix() throws IOException, CDataGridException {
		CBuilder builder = new CBuildNarrowedFile(glimmixFile, null).setConvertMissing(true);
		CDataCacheContainer cdcc = new CDataCacheContainer(builder);

		Glimmix glimmix = new Glimmix(cdcc);
		
		assertEquals(60.68979427845855, glimmix.sumOfProducts(values), 0.000000000001);
	}
	
	@Test(expected=InvalidDataException.class) 
	public void testGlimmixMissingValue() throws IOException, CDataGridException {
		CBuilder builder = new CBuildNarrowedFile(glimmixFile, null).setConvertMissing(true);
		CDataCacheContainer cdcc = new CDataCacheContainer(builder);
		Glimmix glimmix = new Glimmix(cdcc);

		Map<String, Double> missing = new LinkedHashMap<String, Double>();
		missing.put("Foo", 1.0);

		glimmix.sumOfProducts(missing);
	}


}
