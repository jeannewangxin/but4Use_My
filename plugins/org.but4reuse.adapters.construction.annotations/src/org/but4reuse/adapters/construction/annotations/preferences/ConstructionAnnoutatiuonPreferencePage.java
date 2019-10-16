package org.but4reuse.adapters.construction.annotations.preferences;

import org.but4reuse.construction.annotation.activator.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class ConstructionAnnoutatiuonPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public static final String SELECTION = "SELECTION";

	// different part to analyse
	public static final String PURE_VARIANTS = "PURE_VARIANTS";
	public static final String IF_DEF = "IF_DEF";
	public static final String DEFINED = "DEFINED";

	public static final String START_DEFINED_ANNOTATION = "START_DEFINED_ANNOTATION";
	public static final String END_DEFINED_ANNOTATION = "END_DEFINED_ANNOTATION";

	public ConstructionAnnoutatiuonPreferencePage() {
		super(GRID);
		this.setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors.
	 */
	public void createFieldEditors() {

//		Group gRest = new Group(getFieldEditorParent(), GRID);
//
//		gRest.setText("Choose the annotation method of construction:");
//		gRest.setVisible(false);
//
//		BooleanFieldEditor bfPureVariants = new BooleanFieldEditor(PURE_VARIANTS, "PureVariants", gRest);
//		addField(bfPureVariants);
//
//		BooleanFieldEditor bfIfDef = new BooleanFieldEditor(IF_DEF, "Ifdef", gRest);
//		addField(bfIfDef);
//
//		gRest.setVisible(true);

		String[][] comparatorList = { { "PureVariants", PURE_VARIANTS }, { "Ifdef", IF_DEF },
				{ "Define your own annotation :", DEFINED }

		};

		RadioGroupFieldEditor r = new RadioGroupFieldEditor(SELECTION,
				"Choose the annotation method of construction : ", 1, comparatorList, getFieldEditorParent());
		addField(r);
		StringFieldEditor startAnnotation = new StringFieldEditor(START_DEFINED_ANNOTATION, "Annotation start ",
				getFieldEditorParent());
		
		addField(startAnnotation);

		StringFieldEditor endAnnotation = new StringFieldEditor(END_DEFINED_ANNOTATION, "Annotation end ",
				getFieldEditorParent());
		
		addField(endAnnotation);

	}

	@Override
	public void init(IWorkbench workbench) {

	}

}
