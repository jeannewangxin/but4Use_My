package org.but4reuse.adapters.libwhitelist;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.but4reuse.adapters.android.activator.Activator;
import org.but4reuse.utils.files.FileUtils;
import org.but4reuse.utils.ui.dialogs.ScrollableMessageDialog;
import org.but4reuse.utils.workbench.WorkbenchUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Remove whitelist action
 * 
 * Whitelist source: https://github.com/serval-snt-uni-lu/CommonLibraries
 * 
 * @author jabier.martinez
 */
public class RemoveCommonLibsAction implements IObjectActionDelegate {

	private static final String WHITELIST_AD_1050_TXT = "android_libraries_whitelist/ad_1050.txt";
	private static final String WHITELIST_CL_61_TXT = "android_libraries_whitelist/cl_61.txt";
	private ISelection selection;

	@Override
	public void run(IAction action) {
		StringBuffer buff = new StringBuffer();
		if (selection instanceof IStructuredSelection) {
			Iterator<?> i = ((IStructuredSelection) selection).iterator();
			while (i.hasNext()) {
				IResource file = (IResource) i.next();
				File srcFolder = WorkbenchUtils.getFileFromIResource(file);
				buff.append("Input: " + srcFolder.getAbsolutePath() + "\n");
				try {
					URL cl = FileUtils.getFileURLFromPlugin(Activator.PLUGIN_ID, WHITELIST_CL_61_TXT);
					File whiteList = new File(FileLocator.resolve(cl).toURI());
					buff.append("Common libraries\n");
					boolean found = false;
					for (String s : FileUtils.getLinesOfFile(whiteList)) {
						if (!s.isEmpty()) {
							String path = s.replaceAll("\\.", "\\\\");
							File toDelete = new File(srcFolder, path);
							if (toDelete.exists()) {
								found = true;
								buff.append("\t" + path + "\n");
								FileUtils.deleteFile(toDelete);
							}
						}
					}
					if (!found) {
						buff.append("Not found\n");
					}
					found = false;
					URL ad = FileUtils.getFileURLFromPlugin(Activator.PLUGIN_ID, WHITELIST_AD_1050_TXT);
					whiteList = new File(FileLocator.resolve(ad).toURI());
					buff.append("Ad libraries\n");
					for (String s : FileUtils.getLinesOfFile(whiteList)) {
						if (!s.isEmpty()) {
							// String[] paths = s.split("\\.");
							String path = s.replaceAll("\\.", "\\\\");
							File toDelete = new File(srcFolder, path);
							if (toDelete.exists()) {
								found = true;
								buff.append("\t" + path + "\n");
								FileUtils.deleteFile(toDelete);
							}
						}
					}
					if (!found) {
						buff.append("Not found\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				buff.append("\n");
				WorkbenchUtils.refreshIResource(file.getProject());
			}
			ScrollableMessageDialog dialog = new ScrollableMessageDialog(Display.getCurrent().getActiveShell(),
					"Deleted Common and Ad libraries", "", buff.toString());
			dialog.open();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
