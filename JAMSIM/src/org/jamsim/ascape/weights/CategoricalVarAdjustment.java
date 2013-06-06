package org.jamsim.ascape.weights;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.prefs.Preferences;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.data.model.CDataRowSet;
import net.casper.data.model.CDataRuntimeException;
import net.casper.data.model.CRowMetaData;
import net.casper.ext.CasperUtil;
import net.casper.ext.swing.CDatasetTableModel;

import org.apache.commons.lang.NotImplementedException;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.shared.InvalidDataException;
import org.omancode.r.RFaceException;
import org.omancode.r.types.CBuildFromREXP;
import org.omancode.r.types.RDataFrame;
import org.omancode.r.types.REXPAttr;
import org.omancode.r.types.UnsupportedTypeException;
import org.omancode.rmt.cellreader.narrow.TypeCheckedValue;
import org.rosuda.REngine.REXP;

/**
 * Displays levels of a categorical variable across potentially multiple
 * iterations and allows user to specify a proportion for each cell.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CategoricalVarAdjustment extends Observable implements
		WeightCalculator, TableModelListener {

	/**
	 * The R matrix that holds the displayed/edited values.
	 */
	private final String rMatrixVarname;

	/**
	 * An amount to adjust the entered display value before application to
	 * underlying data.
	 */
	private final double displayAdjFactor;

	/**
	 * Variable description. Used for display purposes.
	 */
	private final String variableDesc;

	/**
	 * Table model.
	 */
	private CategoricalVarAdjTableModel tableModel;
	
	/**
	 * The table model for the Base Simulation Results panel in the Scenario Weightings 
	 * ({@link ScenarioBuilder}) interface.
	 */
	private CDatasetTableModel cDatasetTableModel;
	
	/**
	 * The base simulation results for this variable
	 */
	private String baseSimulationResults;

	private final ScapeRInterface scapeR;

	/**
	 * Construct an editable table of {@code rMatrixVarname} and load the
	 * initial values from {@code prefs}.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param rMatrixVarname
	 *            the already existing R matrix variable holds the
	 *            displayed/edited values.
	 * @param rVariable
	 *            name of the R variable. Used to lookup variable description in
	 *            the data dictionary.
	 * @param displayAdjFactor
	 *            an amount to adjust the entered display value before
	 *            application to underlying data, or {@code 1} for no adjustment
	 * @param prefs
	 *            Preferences that store the state of the weightings
	 * @throws IOException
	 *             if problem getting rMatrixVarname or converting to casper
	 *             dataset
	 */
	public CategoricalVarAdjustment(ScapeRInterface scapeR,
			String existingProportions, String rMatrixVarname, String rVariable, double displayAdjFactor,
			Preferences prefs) throws IOException {
		this(scapeR, existingProportions, rMatrixVarname, rVariable, displayAdjFactor);
		loadState(prefs);
	}

	/**
	 * Construct an editable table of {@code rMatrixVarname}.
	 * 
	 * @param scapeR
	 *            scape R interface
	 * @param rMatrixVarname
	 *            the already existing R matrix variable holds the
	 *            displayed/edited values.
	 * @param rVariable
	 *            name of the R variable. Used to lookup variable description in
	 *            the data dictionary.
	 * @param displayAdjFactor
	 *            an amount to adjust the entered display value before
	 *            application to underlying data, or {@code 1} for no adjustment
	 * @throws IOException
	 *             if problem getting rMatrixVarname or converting to casper
	 *             dataset
	 */
	public CategoricalVarAdjustment(ScapeRInterface scapeR,
			String baseSimulationResults, String rMatrixVarname, String rVariable, double displayAdjFactor)
			throws IOException {

		this.rMatrixVarname = rMatrixVarname;
		this.variableDesc = scapeR.getMsScape().getDictionary()
				.getDescription(rVariable);
		this.displayAdjFactor = displayAdjFactor;
		
		this.baseSimulationResults = baseSimulationResults;
		
		this.scapeR = scapeR;
		getTableModel();
	}

	/**
	 * Load matrix from R.
	 * 
	 * @param rMatrixVarname
	 *            r variable containing matrix
	 * @return casper container
	 * @throws CDataGridException
	 * @throws RFaceException
	 */
	private CDataCacheContainer loadAdjMatrix(String rMatrixVarname)
			throws IOException {
		REXP rexp = scapeR.parseEvalTry(rMatrixVarname);

		try {

			CDataCacheContainer casperMatrix = new CDataCacheContainer(
					new CBuildFromREXP(rexp, rMatrixVarname));

			return CasperUtil.scale(casperMatrix, displayAdjFactor);
		} catch (CDataGridException e) {
			throw new IOException(e.getMessage(), e);
		}

	}

	/**
	 * Set rMatrix to table values.
	 * 
	 * @throws IOException
	 *             if problem assigning.
	 */
	public void assignRMatrix() throws IOException {
		try {
			CDataCacheContainer casperMatrix = tableModel.getContainer();

			casperMatrix = CasperUtil.scale(casperMatrix,
					1.0 / displayAdjFactor);
			assignMatrix(rMatrixVarname, casperMatrix);
			String rMatrixVarnameCleaned = rMatrixVarname.replace(", drop = FALSE","");
			scapeR.printlnToConsole("Assigned adjustments to " + rMatrixVarnameCleaned);

		} catch (CDataGridException e) {
			throw new IOException(e.getMessage(), e);
		}

	}

	private void assignMatrix(String rMatrixVarname,
			CDataCacheContainer casperMatrix) throws RFaceException {
		// assign to intermediate variable and then assign
		// into rMatrixVarname because it may be a list element
		// (eg: env.scenario$catadjs$fsmoke)
		
		String rMatrixVarnameCleaned = rMatrixVarname.replace(", drop = FALSE","");
		scapeR.assignMatrix(".catadj", casperMatrix);
		scapeR.eval("attributes(.catadj) <- attributes(" + rMatrixVarnameCleaned + ")");
		scapeR.assign(rMatrixVarnameCleaned, ".catadj");
	}

	@Override
	public double getLevelWeight(Map<String, ?> vars) {
		throw new NotImplementedException("getLevelWeight not implemented.");
	}

	@Override
	public double[] getAllLevelProps() {
		throw new NotImplementedException("getAllLevelProps not implemented.");
	}
	
	@Override
	public final String getName() {
		return variableDesc;
	}

	public final CDatasetTableModel getBaseSimulationResultsTableModel(){
		
		if(cDatasetTableModel == null){
			setBaseSimulationResultsTableModel("");			
		}
		
		return cDatasetTableModel;
	}
	
	/**
	 * Sets the table model for the Base Simulation Results table in the Scenario Weightings Interface
	 * ({@link ScenarioBuilder}). 
	 * 
	 * @param subgroupExpression
	 * 			An expression passed to an R function that returns an {@link REXP} used to 
	 * 			build a {@link CDatasetTableModel} that displays the variable in terms
	 * 			of the user's subgrouping specifications.
	 */
	public final void setBaseSimulationResultsTableModel(String subgroupExpression){
		REXP rexp;
		CDataCacheContainer container;
		String rcmd;
		
		try{
			if(baseSimulationResults.contains("userFormatDichotFreqs")){
				rcmd = subgroupExpression;
				System.out.println("if " + rcmd);
			} else {
				rcmd = "getVariableSubgroupedByCategory(" + "\"" + baseSimulationResults + "\""+ "," + "\"" + subgroupExpression + "\"" + ")";
				System.out.println("else " + rcmd);
			}
			rexp = scapeR.eval(rcmd);

			try{
				container = new CDataCacheContainer(new CBuildFromREXP(rexp, variableDesc));
			
			} catch (CDataGridException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			
			try {
				cDatasetTableModel = new CDatasetTableModel(container);
				
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			
		} catch (RFaceException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		
	}
	
	@Override
	public final TableModel getTableModel() {
		try {
			// load matrix from R when table model requested
			tableModel = new CategoricalVarAdjTableModel(
					loadAdjMatrix(rMatrixVarname), displayAdjFactor);
			tableModel.addTableModelListener(this);
			return tableModel;

		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	public void validateAndNotify() throws InvalidDataException {
		// notify all observers
		// setChanged();
		notifyObservers();
	}

	@Override
	public void resetDefaults() {
		try {

			// set all to NA
			CDataCacheContainer casperMatrix = tableModel.getContainer();
			CRowMetaData meta = casperMatrix.getMetaDefinition();
			Class<?>[] colTypes = meta.getColumnTypes();

			CDataRowSet cdrs = casperMatrix.getAll();

			while (cdrs.next()) {
				for (int i = 0; i < colTypes.length; i++) {
					Class<?> type = colTypes[i];

					if (type.equals(Double.class)) {
						cdrs.setValue(i, TypeCheckedValue.MISSING_VALUE_DOUBLE);
					} else if (type.equals(Integer.class)) {
						cdrs.setValue(i, TypeCheckedValue.MISSING_VALUE_INTEGER);
					} else if (type.equals(Byte.class)) {
						cdrs.setValue(i, TypeCheckedValue.MISSING_VALUE_BYTE);
					}
				}
			}
			String rMatrixVarnameCleaned = rMatrixVarname.replace(", drop = FALSE","");
			scapeR.printlnToConsole("Reverting adjustments to " + rMatrixVarnameCleaned);
			
			tableModel.fireTableDataChanged();
			assignMatrix(rMatrixVarname, casperMatrix);

		} catch (CDataGridException e) {
			throw new CDataRuntimeException(e.getMessage(), e);
		} catch (RFaceException e) {
			throw new RuntimeException(e.getMessage(), e);
		}

	}

	@Override
	public final void loadState(Preferences prefs) {
	}

	@Override
	public void saveState(Preferences prefs) {
	}

	@Override
	public double getWeightBase() {
		return 1;
	}

	/**
	 * Create a list of {@link CategoricalVarAdjustment}s from a dataframe
	 * containing the rows rVariable, variableName, breaksExpr, breakLast,
	 * adjIncrements.
	 * 
	 * @param scapeR
	 *            scapeR
	 * @param dataframe
	 *            dataframe
	 * @return list
	 * @throws IOException
	 *             if problem creating list
	 */
	public static List<CategoricalVarAdjustment> createList(
			ScapeRInterface scapeR, REXP dataframe) throws IOException {

		if (!RDataFrame.isDataFrame(dataframe)) {
			throw new IllegalArgumentException(
					"Cannot build list from REXP of class "
							+ REXPAttr.getClassAttribute(dataframe));
		}

		try {
			RDataFrame builder = new RDataFrame(
					"CategoricalVarAdjustmentsSpec", dataframe);

			CDataCacheContainer container = new CDataCacheContainer(builder);

			CDataRowSet rowset = container.getAll();

			List<CategoricalVarAdjustment> cvas = new ArrayList<CategoricalVarAdjustment>(
					container.size());

			while (rowset.next()) {

				String existingProportions = rowset.getString("existingProportions");
				String rMatrixVarname = rowset.getString("rMatrixVarname");
				String rVariable = rowset.getString("rVariable");
				double displayAdjFactor = rowset.getDouble("displayAdjFactor");

				CategoricalVarAdjustment cva = new CategoricalVarAdjustment(
						scapeR, existingProportions, rMatrixVarname, rVariable, displayAdjFactor);

				cvas.add(cva);
			}

			return cvas;

		} catch (UnsupportedTypeException e) {
			throw new RFaceException(e.getMessage(), e);
		} catch (CDataGridException e) {
			throw new RFaceException(e.getMessage(), e);
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		setChanged();		
	}

}