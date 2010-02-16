package org.omancode.util.tests;

import java.io.IOException;
import java.util.prefs.Preferences;

import org.junit.Test;
import org.omancode.util.PrefsOrSaveFileChooser;

/**
 * PrefsFileChooserTest
 * 
 * @author oman002
 *
 */
public class PrefsFileChooserTest {

	protected Preferences prefs =
			Preferences.userNodeForPackage(this.getClass());

	protected PrefsOrSaveFileChooser saveChooser = new PrefsOrSaveFileChooser(prefs);

	@Test
	public void testGetSaveFileFromPrefsOrPrompt() throws IOException {

		saveChooser.getFile(
				"testGetSaveFileFromPrefsOrPrompt", "Save to...", null, true,
				true);
	}

}
