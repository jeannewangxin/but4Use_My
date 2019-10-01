package org.but4reuse.versioncontrol.utils.dialogs;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * Generic Input Selection Dialog
 * 
 * @author sandu.postaru
 */

public class GenericInputSelectionDialog extends InputDialog {

	private String checkBoxMessage;
	private boolean checked;

	public GenericInputSelectionDialog(Shell parentShell, String dialogTitle, String dialogMessage,
			String initialValue) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		this.checkBoxMessage = null;
		checked = false;
	}

	public GenericInputSelectionDialog(Shell parentShell, String dialogTitle, String dialogMessage,
			String initialValue, String checkBoxMessage) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		this.checkBoxMessage = checkBoxMessage;		
		checked = false;
	}

	static IInputValidator validator = new IInputValidator() {

		@Override
		public String isValid(String newText) {

			if (newText.isEmpty()) {
				return "Input field must not be empty";
			}

			return null;
		}

	};

	@Override
	protected Control createDialogArea(Composite parent) {

		Control control = super.createDialogArea(parent);

		if (checkBoxMessage != null) {
			/* add new button */
			Button button = new Button(parent, SWT.CHECK);
			button.setText(checkBoxMessage);
			button.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent event) {
					Button btn = (Button) event.getSource();
					checked = btn.getSelection();
				}
			});

		}

		return control;
	}

	public boolean getCheckStatus() {
		return checked;
	}

}
