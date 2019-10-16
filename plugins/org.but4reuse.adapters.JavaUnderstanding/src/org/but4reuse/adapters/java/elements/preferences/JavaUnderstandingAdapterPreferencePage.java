package org.but4reuse.adapters.java.elements.preferences;

import org.but4reuse.adapters.java.elements.activator.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for file structure adapter
 * 
 * @author jabier.martinez
 */
public class JavaUnderstandingAdapterPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	// different part to analyse
	public static final String SELECTION = "SELECTION";

	// different part to analyse
	public static final String PACKAGE = "PACKAGE";
	public static final String FILES = "FILES";
	public static final String IMPORTS = "IMPORTS";
	public static final String FIELDS = "FIELDS";
	public static final String INTERFACE = "INTERFACE";
	public static final String SUPERCLASS = "SUPERCLASS";
	public static final String METHODS = "METHODS";

	public static final String IGNORE_PATH = "IGNORE_PATH";

	// options on fields comparison
	public static final String NAME_FIELD = "NAME (fields)";
	public static final String DATATYPE_FIELD = "DATATYPE (fields)";
	public static final String MODIFIER_FIELD = "MODIFIER (fields)";
	public static final String ATTRIBUTE_FIELD = "ATTRIBUTE (fields)";

	// options on methods comparison
	public static final String BODY = "BODY (Method)";
	public static final String ORDER_SENSITIVITY = "ORDER_SENSITIVITY (Method)";
	public static final String SIMILARITY_LEVEL = "SIMILARITY (Method)";
	public static final String NAME_METHOD = "NAME (Method)";
	public static final String RETURNTYPE = "RETURNTYPE (method)";
	public static final String PARAMETERS = "PARAMETER (method)";
	public static final String MODIFIER_METHOD = "MODIFIER (method)";

	public static enum Choice {
		SAME, DIFFERENT, IGNORE, ORDER_SENSITIVE, NOT_ORDER_SENSITIVE
	};

	String[][] comboList = { { "similar", Choice.SAME.toString() }, { "different ", Choice.DIFFERENT.toString() },
			{ "ignore", Choice.IGNORE.toString() }, };

	public JavaUnderstandingAdapterPreferencePage() {
		super(GRID);
		this.setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors.
	 */
	public void createFieldEditors() {
		Group gRest = new Group(getFieldEditorParent(), GRID);

		gRest.setText("Please select variability analysis dimensions:");
		gRest.setVisible(false);

		BooleanFieldEditor bfPackages = new BooleanFieldEditor(PACKAGE,
				"Organization variability analysis based on packages", gRest);
		addField(bfPackages);

		BooleanFieldEditor bfFiles = new BooleanFieldEditor(FILES, "Organization variability analysis based on files",
				gRest);
		addField(bfFiles);

		BooleanFieldEditor bfMethods = new BooleanFieldEditor(METHODS, "Function variability analysis based on methods",
				gRest);
		addField(bfMethods);

		BooleanFieldEditor bfFields = new BooleanFieldEditor(FIELDS, "Data variability analysis based on fields",
				gRest);
		addField(bfFields);

		BooleanFieldEditor bfImports = new BooleanFieldEditor(IMPORTS,
				"Reused library variability analysis based imported packages and classes)", gRest);
		addField(bfImports);

		BooleanFieldEditor bfFilesCpp = new BooleanFieldEditor(INTERFACE,
				"Inherited variability analysis based on interfaces implemented by classes", gRest);
		addField(bfFilesCpp);

		BooleanFieldEditor bfSuperClass = new BooleanFieldEditor(SUPERCLASS,
				"Inherited variability analysis based on super classes", gRest);
		addField(bfSuperClass);

		gRest.setVisible(true);

		BooleanFieldEditor bfe = new BooleanFieldEditor(IGNORE_PATH, "Ignore uri of packages and classes",
				getFieldEditorParent());
		addField(bfe);

		addFieldsComparisonOptions();
		addMethodsComparisonOptions();

	}

	public void addFieldsComparisonOptions() {
		Group g1 = new Group(getFieldEditorParent(), GRID);
		g1.setText("Data comparison configurations");

		ComboFieldEditor cfe1 = new ComboFieldEditor(NAME_FIELD, "Names of fields", comboList, g1);
		addField(cfe1);

		ComboFieldEditor cfe2 = new ComboFieldEditor(DATATYPE_FIELD, "Datatypes of fields", comboList, g1);
		addField(cfe2);

		ComboFieldEditor cfe3 = new ComboFieldEditor(MODIFIER_FIELD, "Modifiers of fields", comboList, g1);
		addField(cfe3);

		ComboFieldEditor cfe4 = new ComboFieldEditor(ATTRIBUTE_FIELD, "Values of fields", comboList, g1);
		addField(cfe4);
	}

	public void addMethodsComparisonOptions() {

		Group g2 = new Group(getFieldEditorParent(), GRID);
		g2.setText("Methods comparison configurations");

		ComboFieldEditor cfe11 = new ComboFieldEditor(BODY, "Compare bodies of methods", comboList, g2);
		addField(cfe11);

		String[][] bodyListOption = { { "statement order sensitive", Choice.ORDER_SENSITIVE.toString() },
				{ "statement order not sensitive ", Choice.NOT_ORDER_SENSITIVE.toString() }, };

		ComboFieldEditor cfe5 = new ComboFieldEditor(ORDER_SENSITIVITY, "Bodies comparison's mechanism", bodyListOption,
				g2);
		addField(cfe5);

		ScaleFieldEditor sfe = new ScaleFieldEditor(SIMILARITY_LEVEL, "Bodies similarity percentage accuracy", g2);
		Scale scale = sfe.getScaleControl();
		scale.setMinimum(0);
		scale.setMaximum(100);
		scale.setPageIncrement(10);
		addField(sfe);
		scale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int value = scale.getSelection();
				sfe.setLabelText("bodies similarity accurency: " + value + "%");
			}
		});

		ComboFieldEditor cfe6 = new ComboFieldEditor(NAME_METHOD, "Names of methods", comboList, g2);
		addField(cfe6);

		ComboFieldEditor cfe8 = new ComboFieldEditor(RETURNTYPE, "Return type of methods", comboList, g2);
		addField(cfe8);

		ComboFieldEditor cfe10 = new ComboFieldEditor(PARAMETERS, "Paramters of methods", comboList, g2);
		addField(cfe10);

		ComboFieldEditor cfe9 = new ComboFieldEditor(MODIFIER_METHOD, "Modifiers of methods", comboList, g2);
		addField(cfe9);
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}