package preferences;

import activator.Activator;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(CppCunderstandingPreferencePage.USE_FUNCTION_CALL_HIERARCHY, false);
		store.setDefault(CppCunderstandingPreferencePage.DOXYGEN_PATH, "/usr/bin/doxygen");
	}

}
