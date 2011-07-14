package org.jamsim.ascape.weights;

import java.io.IOException;
import java.util.Map;
import java.util.Observable;
import java.util.prefs.Preferences;

import javax.swing.table.TableModel;

import net.casper.data.model.CDataCacheContainer;
import net.casper.data.model.CDataGridException;
import net.casper.ext.CasperUtil;
import net.casper.ext.swing.CDatasetTableModel;

import org.apache.commons.lang.NotImplementedException;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.shared.InvalidDataException;
import org.omancode.r.types.CBuildFromREXP;
import org.rosuda.REngine.REXP;

/**
 * Displays levels of a categorical variable across potentially multiple
 * iterations and allows user to specify a proportion for each cell.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class CategoricalVarAdjustment extends Observable implements
		WeightCalculator {

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
	 * A {@link TableModel} wrapped around {@link #rMatrixVarname}.
	 */
	private final CDatasetTableModel tableModel;

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
	 * @param variableDesc
	 *            description of the R variable. Used for display purposes.
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
			String rMatrixVarname, String variableDesc,
			double displayAdjFactor, Preferences prefs) throws IOException {
		this(scapeR, rMatrixVarname, variableDesc, displayAdjFactor);
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
	 * @param variableDesc
	 *            description of the R variable. Used for display purposes.
	 * @param displayAdjFactor
	 *            an amount to adjust the entered display value before
	 *            application to underlying data, or {@code 1} for no adjustment
	 * @throws IOException
	 *             if problem getting rMatrixVarname or converting to casper
	 *             dataset
	 */
	public CategoricalVarAdjustment(ScapeRInterface scapeR,
			String rMatrixVarname, String variableDesc,
			double displayAdjFactor) throws IOException {

		this.rMatrixVarname = rMatrixVarname;
		this.variableDesc = variableDesc;
		this.displayAdjFactor = displayAdjFactor;

		this.scapeR = scapeR;

		REXP rexp = scapeR.parseEvalTry(rMatrixVarname);

		try {

			CDataCacheContainer casperMatrix =
					new CDataCacheContainer(new CBuildFromREXP(rexp,
							variableDesc));

			// casperMatrix = CasperUtil.scale(casperMatrix, displayAdjFactor);

			this.tableModel =
					new CDatasetTableModel(casperMatrix, true, true, true);

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
	public void asignRMatrix() throws IOException {
		try {
			CDataCacheContainer casperMatrix = tableModel.getContainer();

			// casperMatrix =
			// CasperUtil.scale(casperMatrix, 1.0 / displayAdjFactor);

			// assign to intermediate variable and then assign
			// into rMatrixVarname because it may be a list element
			// (eg: env.scenario$catadjs$fsmoke)
			scapeR.assignMatrix(".catadj", casperMatrix);
			scapeR.assign(rMatrixVarname, ".catadj");

			scapeR.printlnToConsole("Assigned adjustments to "
					+ rMatrixVarname);

		} catch (CDataGridException e) {
			throw new IOException(e.getMessage(), e);
		}

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

	@Override
	public TableModel getTableModel() {
		return tableModel;
	}

	@Override
	public void validateAndNotify() throws InvalidDataException {
		// notify all observers
		setChanged();
		notifyObservers();
	}

	@Override
	public void resetDefaults() {

		// set all to NA
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

}
