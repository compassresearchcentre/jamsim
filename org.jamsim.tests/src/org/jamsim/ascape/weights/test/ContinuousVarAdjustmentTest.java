package org.jamsim.ascape.weights.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.jamsim.ascape.r.RLoader;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.ascape.weights.ContinuousVarAdjustment;
import org.junit.BeforeClass;
import org.junit.Test;
import org.omancode.r.RFaceException;
import org.rosuda.REngine.REXP;

public class ContinuousVarAdjustmentTest {

	private static ScapeRInterface scapeR;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		RLoader rLoader;
		try {
			rLoader = RLoader.INSTANCE;
		} catch (ExceptionInInitializerError e) {

			System.out.format("%s=%s%n", "java.library.path",
					System.getProperty("java.library.path"));
			System.out.format("%s=%s%n", "Path", System.getenv().get("Path"));
			System.out.format("%s=%s%n", "R_HOME", System.getenv()
					.get("R_HOME"));

			// re-throw exception that occurred in the initializer
			// as an exception our caller can deal with
			Throwable eInInit = e.getCause();
			throw new RFaceException(eInInit.getMessage(), eInInit); // NOPMD
		}

		// create R scape interface
		scapeR = new ScapeRInterface(rLoader, null, null, false);
	}

	@Test
	public void testCreateList() throws IOException {
		scapeR.eval("cva1 <- list(rVariable = 'env.scenario$simframe$bwkg', variableName = 'bwgrams', breaksExpr = 'binbreaks$bwkg', breakLast = NA, adjIncrements = (1.0 / 1000))");
		REXP rexp = scapeR.eval("data.frame(cva1)");

		List<ContinuousVarAdjustment> cvas = ContinuousVarAdjustment
				.createList(null, rexp);

		assertEquals(1, cvas.size());
	}

}
