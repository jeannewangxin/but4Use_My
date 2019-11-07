package org.but4reuse.coloredclouds.preferences;

import org.but4reuse.coloredclouds.activator.Activator;
import org.but4reuse.coloredclouds.filters.IWordsProcessing;
import org.but4reuse.coloredclouds.filters.WordCloudFiltersHelper;
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
		store.setDefault(WordCloudPreferences.WORDCLOUD_NB_W, 50);
		store.setDefault(WordCloudPreferences.STOP_WORDS, "");
		store.setDefault(WordCloudPreferences.MULTI_WORDS, "");
		store.setDefault(WordCloudPreferences.SYNONYM_WORDS, "");
		store.setDefault(WordCloudPreferences.AUTORENAME_NB_WORDS, 2);
		//store.setDefault(WordCloudPreferences.JAVA_REMOVE_GET_SET,1);
		store.setDefault(WordCloudPreferences.AUTORENAME_KEEP_PREVIOUS, false);

		for (IWordsProcessing filter : WordCloudFiltersHelper.getAllFilters()) {
			String name = WordCloudFiltersHelper.getFilterName(filter);
			store.setDefault(name, 0);
		}
	}

}