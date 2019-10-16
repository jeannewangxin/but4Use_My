package org.but4reuse.construction.annotation;

import org.but4reuse.adapters.construction.annotations.preferences.ConstructionAnnoutatiuonPreferencePage;
import org.but4reuse.construction.annotation.activator.Activator;
import org.but4reuse.adapters.construction.*;

public class DefinedAnnotation implements IAnnotation {

	@Override
	public String annotationStart(String param) {
		// TODO Auto-generated meth=od stub
		String startAnnotation = Activator.getDefault().getPreferenceStore()
				.getString(ConstructionAnnoutatiuonPreferencePage.START_DEFINED_ANNOTATION);
		if (startAnnotation.isEmpty()) {
			return "#ifdef " + "FEATURE_" + param.replace(" ", "_") + "//" + "start FEATURE_" + param.replace(" ", "_");
		} else {
			return startAnnotation + " FEATURE_" + param.replace(" ", "_") + " //" + "start FEATURE_"
					+ param.replace(" ", "_");
		}

	}

	@Override
	public String annotationEnd(String param) {
		// TODO Auto-generated method stub
		String endAnnotation = Activator.getDefault().getPreferenceStore()
				.getString(ConstructionAnnoutatiuonPreferencePage.END_DEFINED_ANNOTATION);
		if (endAnnotation.isEmpty()) {
			return "#endif " + "//" + "FEATURE_" + param.replace(" ", "_");
		} else {
			return endAnnotation + " //" + "FEATURE_" + param.replace(" ", "_");
		}
	}

}
