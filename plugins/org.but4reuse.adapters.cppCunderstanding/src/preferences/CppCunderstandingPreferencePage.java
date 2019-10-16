package preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import org.eclipse.jface.preference.StringFieldEditor;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import activator.Activator;

public class CppCunderstandingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	// different part to analyse
	public static final String SELECTION = "SELECTION";

	public static final String ALL = "ALL";
	public static final String REST = "REST";

	public static final String PACKAGES = "PACKAGES";

	public static final String IMPORTS = "IMPORTS";
	public static final String FIELDS = "FIELDS";

	//public static final String SUPERCLASS = "SUPERCLASS";
	public static final String METHODS = "METHODS";

	public static final String IGNORE_PATH = "IGNORE_PATH";
	public static final String COMMENT = "COMMENT";

	// options on files

	public static final String CPP_FILES = "CPP_FILE";
	public static final String H_FILES = "H_FILE";

	

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

	//////////// from the cpp adapter
	public static final String USE_FUNCTION_CALL_HIERARCHY = "USE FUNCTION CALL HIERARCHY";
	public static final String DOXYGEN_PATH = "DOXYGEN PATH";
	
	//////////////////////////

	public static enum Choice {
		SAME, DIFFERENT, IGNORE, ORDER_SENSITIVE, NOT_ORDER_SENSITIVE
	};

	private String[][] comboList = { { "similar", Choice.SAME.toString() }, { "different ", Choice.DIFFERENT.toString() },
			{ "ignore", Choice.IGNORE.toString() }, };
	
	String[][] comboList2 = { { "similar", Choice.SAME.toString() },
			{ "ignore", Choice.IGNORE.toString() }, };
	
	// for the range of similarity between functions
	public static final String MIN_Range = "MIN";

	public static final String MAX_Range = "MAX";

	public CppCunderstandingPreferencePage() {
		super(GRID);
		this.setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors.
	 */
	public void createFieldEditors() {

		// the first option , the simple cpp adapter

		String[][] optionAll = { { "Parsing all elements", ALL }, { "Choosing elements to be parsed ", CppCunderstandingPreferencePage.REST } };

		RadioGroupFieldEditor rAll = new RadioGroupFieldEditor(SELECTION, "SELECT YOUR OPTION : ", 1, optionAll,
				getFieldEditorParent());

		addField(rAll);

		// ********************************************//

		Group gRest = new Group(getFieldEditorParent(), GRID);

		gRest.setText("All elements : ");
		gRest.setVisible(false);

		BooleanFieldEditor bfPackages = new BooleanFieldEditor(PACKAGES, "Package visitor", gRest);
		addField(bfPackages);

		BooleanFieldEditor bfFilesH = new BooleanFieldEditor(H_FILES, "Header files visitor ", gRest);
		addField(bfFilesH);

		BooleanFieldEditor bfFilesCpp = new BooleanFieldEditor(CPP_FILES, "Source files visitor", gRest);
		addField(bfFilesCpp);

		BooleanFieldEditor bfImports = new BooleanFieldEditor(IMPORTS, "Includes", gRest);
		addField(bfImports);

		BooleanFieldEditor bfMethods = new BooleanFieldEditor(METHODS, "Methods", gRest);
		addField(bfMethods);

		BooleanFieldEditor bfFields = new BooleanFieldEditor(FIELDS, "Fields", gRest);
		addField(bfFields);

		/* addFieldsComparisonOptions(); */

		Group g1 = new Group(getFieldEditorParent(), GRID);

		g1.setText("Data comparison configurations ");
		g1.setVisible(true);

		ComboFieldEditor cfe3 = new ComboFieldEditor(MODIFIER_FIELD, "Modifiers of fields", comboList, g1);
		addField(cfe3);

		ComboFieldEditor cfe2 = new ComboFieldEditor(DATATYPE_FIELD, "Datatypes of fields", comboList, g1);
		addField(cfe2);

		ComboFieldEditor cfe1 = new ComboFieldEditor(NAME_FIELD, "Names of fields", comboList, g1);
		addField(cfe1);

		ComboFieldEditor cfe4 = new ComboFieldEditor(ATTRIBUTE_FIELD, "Values of fields", comboList, g1);
		addField(cfe4);

		/* addMethodsComparisonOptions(); */
		Group g2 = new Group(getFieldEditorParent(), GRID);
		g2.setText("Methods comparison configurations");
		g2.setVisible(true);
		Rectangle rect = new Rectangle(2, 2, 20, 20);

		g2.setBounds(rect);

		//
		
		ComboFieldEditor cfe9 = new ComboFieldEditor(MODIFIER_METHOD, "Modifiers of methods", comboList, g2);
		addField(cfe9);

		ComboFieldEditor cfe8 = new ComboFieldEditor(RETURNTYPE, "Return type of methods", comboList, g2);
		addField(cfe8);

		ComboFieldEditor cfe6 = new ComboFieldEditor(NAME_METHOD, "Names of methods", comboList, g2);
		addField(cfe6);

		ComboFieldEditor cfe10 = new ComboFieldEditor(PARAMETERS, "Paramters of methods", comboList, g2);
		addField(cfe10);
		
		ComboFieldEditor cfe11 = new ComboFieldEditor(BODY, "Compare bodies of methods", comboList2, g2);
		addField(cfe11);

		String[][] bodyListOption = { { "statement order sensitive", Choice.ORDER_SENSITIVE.toString() },
				{ "statement order not sensitive ", Choice.NOT_ORDER_SENSITIVE.toString() }, };

		ComboFieldEditor cfe5 = new ComboFieldEditor(ORDER_SENSITIVITY, "Bodies comparison's mechanism", bodyListOption,
				g2);
		addField(cfe5);

		IntegerFieldEditor minRange = new IntegerFieldEditor(CppCunderstandingPreferencePage.MIN_Range, "Min ", g2, 3);
		minRange.setValidRange(0, 100);
		
		addField(minRange);
		
		
		IntegerFieldEditor maxRange = new IntegerFieldEditor(CppCunderstandingPreferencePage.MAX_Range, "Max ", g2, 3);
		maxRange.setValidRange(0, 100);
		
		addField(maxRange);
		
		

		// __________________________________________________________________________________________________________//

		// the boolean field for ignore path

		BooleanFieldEditor bfe = new BooleanFieldEditor(IGNORE_PATH, "Ignore path", getFieldEditorParent());
		addField(bfe);
		
		// ignore or not comment
		
		BooleanFieldEditor bfcomments = new BooleanFieldEditor(COMMENT, "Consider comments", getFieldEditorParent());
		addField(bfcomments);

		// getting all the chiled control of the group radio button

		Control c[] = rAll.getRadioBoxControl(getFieldEditorParent()).getChildren();

		org.eclipse.swt.events.MouseListener all = new org.eclipse.swt.events.MouseListener() {

			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				gRest.setVisible(false);
				g1.setVisible(false);
				g2.setVisible(false);

			}

			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub

			}
		};

		c[0].addMouseListener(all);

		org.eclipse.swt.events.MouseListener rest = new org.eclipse.swt.events.MouseListener() {

			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				gRest.setVisible(true);
				if ((Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.METHODS)
						.equals("true")) && (bfMethods.getBooleanValue())) {
					g2.setVisible(true);
				} else {
					g2.setVisible(false);
				}
				if ((Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.FIELDS)
						.equals("true")) && (bfFields.getBooleanValue())) {
					g1.setVisible(true);
				} else {
					g1.setVisible(false);
				}
			}

			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub

			}
		};

		c[1].addMouseListener(rest);

		// showing option for methods when selected
		org.eclipse.swt.events.MouseListener listenerM = new org.eclipse.swt.events.MouseListener() {

			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub
				if (bfMethods.getBooleanValue()) {
					g2.setVisible(true);
				} else {
					g2.setVisible(false);
				}

			}

			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub

			}
		};

		bfMethods.getDescriptionControl(gRest).addMouseListener(listenerM);

		// for field
		org.eclipse.swt.events.MouseListener listener = new org.eclipse.swt.events.MouseListener() {

			@Override
			public void mouseUp(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub
				if (bfFields.getBooleanValue()) {
					g1.setVisible(true);
				} else
					g1.setVisible(false);

			}

			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {
				// TODO Auto-generated method stub

			}
		};

		bfFields.getDescriptionControl(gRest).addMouseListener(listener);

		// when closing we need update
		String selection = (Activator.getDefault().getPreferenceStore()
				.getString(CppCunderstandingPreferencePage.SELECTION));
		if (selection.equals("ALL")) {
			gRest.setVisible(false);
			g1.setVisible(false);
			g2.setVisible(false);
		} else {
			gRest.setVisible(true);
			if ((Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.METHODS)
					.equals("true"))) {
				g2.setVisible(true);
			} else {
				g2.setVisible(false);
			}
			if ((Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.FIELDS)
					.equals("true"))) {
				g1.setVisible(true);
			} else {
				g1.setVisible(false);
			}
		}

		//////////////////// from the cpp adapter
		BooleanFieldEditor useFunctionCallHierarchy = new BooleanFieldEditor(USE_FUNCTION_CALL_HIERARCHY,
				"Enable function call hierarchy analysis", getFieldEditorParent());
		addField(useFunctionCallHierarchy);

		StringFieldEditor doxygenPath = new StringFieldEditor(DOXYGEN_PATH, "Doxygen path: ", getFieldEditorParent());
		addField(doxygenPath);
		////////////////////////////////////////

	}

	@Override
	public void init(IWorkbench workbench) {

	}

}
