package org.jamsim.io.tests;

import java.io.IOException;
import java.util.prefs.Preferences;

import org.jamsim.io.FileUtil;
import org.junit.Test;

public class FileUtilTest {

	protected Preferences prefs =
			Preferences.userNodeForPackage(this.getClass());

	protected FileUtil fileutil = new FileUtil(prefs);

	@Test
	public void testGetSaveFileFromPrefsOrPrompt() throws IOException {

		fileutil.getSaveFileFromPrefsOrPrompt(
				"testGetSaveFileFromPrefsOrPrompt", "Save to...", null, true,
				true);
	}

}
