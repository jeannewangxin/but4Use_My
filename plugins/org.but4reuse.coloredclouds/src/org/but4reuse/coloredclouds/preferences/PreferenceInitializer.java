package org.but4reuse.coloredclouds.preferences;

import org.but4reuse.coloredclouds.activator.Activator;
import org.but4reuse.coloredclouds.filters.IWordsProcessing;
import org.but4reuse.coloredclouds.filters.ColoredCloudFiltersHelper;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Preference initializer
 * 
 * @author jabier.martinez
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(ColoredCloudPreferences.WORDCLOUD_NB_W, 50);
		store.setDefault(ColoredCloudPreferences.STOP_WORDS, "");
		store.setDefault(ColoredCloudPreferences.MULTI_WORDS, "");
		store.setDefault(ColoredCloudPreferences.SYNONYM_WORDS, "");
		store.setDefault(ColoredCloudPreferences.AUTORENAME_NB_WORDS, 2);
		//store.setDefault(ColoredCloudPreferences.JAVA_REMOVE_GET_SET,1);
		store.setDefault(ColoredCloudPreferences.AUTORENAME_KEEP_PREVIOUS, false);

		for (IWordsProcessing filter : ColoredCloudFiltersHelper.getAllFilters()) {
			String name = ColoredCloudFiltersHelper.getFilterName(filter);
			store.setDefault(name, 0);
		}
	}

}