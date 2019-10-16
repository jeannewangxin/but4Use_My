package org.but4reuse.construction.annotation;



public class PureVariantAnnotation implements IAnnotation {

	@Override
	public String annotationStart(String param) {
		// TODO Auto-generated method stub
		return "//PV:IFCOND(pv:hasFeature ("+param.replace(" ", "_")+")";
	}

	@Override
	public String annotationEnd(String param) {
		// TODO Auto-generated method stub
		return "//PV:ENDCOND ";
	}

}
