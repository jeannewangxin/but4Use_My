package org.but4reuse.construction.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.construction.annotations.preferences.ConstructionAnnoutatiuonPreferencePage;
import org.but4reuse.construction.annotation.activator.Activator;

public class ConstructionWithAnnotationHelper {

	public static Map<IElement, String> MAP_ELEMENTS = new HashMap<IElement, String>();

	public static String CURRENT_BLOCK = null;

	public static IAnnotation USED_ANNOTATION = new EmptyAnnotation();

	public static void initialize() {

		// preferences of construction method
		String variable = Activator.getDefault().getPreferenceStore()
				.getString(ConstructionAnnoutatiuonPreferencePage.SELECTION);

		if (variable.equals(ConstructionAnnoutatiuonPreferencePage.PURE_VARIANTS)) {
			USED_ANNOTATION = new PureVariantAnnotation();

		}else {
			if (variable.equals(ConstructionAnnoutatiuonPreferencePage.IF_DEF)) {
				USED_ANNOTATION = new IFDEFAnnotation();

			}else {
				USED_ANNOTATION = new DefinedAnnotation();
			}
		}
		
	}

	public static String getEndText(IElement element) {
		// TODO Auto-generated method stub

		String block = ConstructionWithAnnotationHelper.MAP_ELEMENTS.get(element);

		if (ConstructionWithAnnotationHelper.CURRENT_BLOCK != block) {
			System.err.println("Added ENDTEXT BLOCK :" + block);
			ConstructionWithAnnotationHelper.CURRENT_BLOCK = block;
			if (ConstructionWithAnnotationHelper.CURRENT_BLOCK != null)
				return USED_ANNOTATION.annotationEnd(CURRENT_BLOCK);
		}
		return "";
	}

	public static String getStartText(IElement element) {

		String block = ConstructionWithAnnotationHelper.MAP_ELEMENTS.get(element);
		if (ConstructionWithAnnotationHelper.CURRENT_BLOCK != block) {
			System.err.println("Added STARTTEXT BLOCK :" + block);

			ConstructionWithAnnotationHelper.CURRENT_BLOCK = block;
			return USED_ANNOTATION.annotationStart(block);
		}

		return "";
	}

	public static List<String> getAnnotationText(IElement element) {

		List<String> result = new ArrayList<String>();
		result = null;

		String block = ConstructionWithAnnotationHelper.MAP_ELEMENTS.get(element);

		if (CURRENT_BLOCK != block) {

			if (CURRENT_BLOCK != null)

				result.add(USED_ANNOTATION.annotationEnd(block));
			result.add(USED_ANNOTATION.annotationStart(block));
			ConstructionWithAnnotationHelper.CURRENT_BLOCK = block;

		}

		return result;

	}

//	
//	public String get(IElement element) {
//		
//		
//		IAnnotation annotation=new EmptyAnnotation();
//		
//		
//		String result="";
//		
//		
//		
//		String block = ConstructionWithAnnotationHelper.MAP_ELEMENTS.get(element);
//		 
//		  if (CURRENT_ANNOTATION!=block) {
//			  
//			  if (CURRENT_ANNOTATION!=null)
//				 
//				 	FileUtils.appendToFile(file,annotation.annotationEnd(ConstructionWithAnnotationHelper.currentAnnotation));
//			  
//			  		FileUtils.appendToFile(file,annotation.annotationStart(block));
//			 		 ConstructionWithAnnotationHelper.CURRENT_ANNOTATION=block;
//			 
//		  }
//	}

}
