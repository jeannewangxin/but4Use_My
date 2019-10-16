package org.but4reuse.adapters.android.preferences;

import org.but4reuse.adapters.android.activator.Activator;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class AndroidAdapterPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	// different part to analyse	
	public static final String PACKAGE_ID= "PACKAGE_ID";
	public static final String PERMISSION = "USER_PERMISSION";
	public static final String SDK = "SDK";
	public static final String ACTIVITY = "ACTIVITY";
	public static final String SERVICE = "SERVICE";
	public static final String BROADCAST_RECEIVER = "BROADCAST_RECEIVER";
	public static final String CONTENT_PRPVIDER = "CONTENT_PRPVIDER";
	public static final String FEATURE = "FEATURE";

	
	public static final String IGNORE_PACKAGE = "IGNORE_PACKAGE";

	public AndroidAdapterPreferencePage() {
		super(GRID);
		this.setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors.
	 */
	public void createFieldEditors() {

		Group gRest = new Group(getFieldEditorParent(), GRID);

		gRest.setText("AndroidManifest elements");
		gRest.setVisible(false);

		BooleanFieldEditor bfPackages = new BooleanFieldEditor(PACKAGE_ID, "Package Name", gRest);
		addField(bfPackages);

		BooleanFieldEditor bfFilesH = new BooleanFieldEditor(PERMISSION, "User Permissions", gRest);
		addField(bfFilesH);

		BooleanFieldEditor bfImports = new BooleanFieldEditor(SDK, "Used SDK", gRest);
		addField(bfImports);
		
		BooleanFieldEditor bfFilesCpp = new BooleanFieldEditor(ACTIVITY, "Activities", gRest);
		addField(bfFilesCpp);

		BooleanFieldEditor bfMethods = new BooleanFieldEditor(SERVICE, "Services", gRest);
		addField(bfMethods);

		BooleanFieldEditor bfFields = new BooleanFieldEditor(CONTENT_PRPVIDER, "Content Providers", gRest);
		addField(bfFields);
		
		BooleanFieldEditor bfBroadcast = new BooleanFieldEditor(BROADCAST_RECEIVER, "Broadcast Receiver", gRest);
		addField(bfBroadcast);
		
		BooleanFieldEditor bfFeatures = new BooleanFieldEditor(FEATURE, "Device Compatibility Features", gRest);
		addField(bfFeatures);
		
		BooleanFieldEditor bfe = new BooleanFieldEditor(IGNORE_PACKAGE, "Ignore package for activities", getFieldEditorParent());
		addField(bfe);
		
		gRest.setVisible(true);
		
		

	}

	@Override
	public void init(IWorkbench workbench) {

	}

}
