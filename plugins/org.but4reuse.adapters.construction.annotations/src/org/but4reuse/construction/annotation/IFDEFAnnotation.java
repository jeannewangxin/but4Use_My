package org.but4reuse.construction.annotation;



public class IFDEFAnnotation implements IAnnotation {

	@Override
	public String annotationStart(String param) {
		// TODO Auto-generated meth=od stub
		return "#ifdef "+"FEATURE_"+param.replace(" ", "_")+"//"+"start FEATURE_"+param.replace(" ", "_");
	}

	@Override
	public String annotationEnd(String param) {
		// TODO Auto-generated method stub
		return "#endif "+"//"+"FEATURE_"+param.replace(" ", "_");
	}

}
