package org.but4reuse.adapters.android.unpack;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import jd.cli.Main;

import org.but4reuse.adapters.android.activator.Activator;
import org.but4reuse.utils.files.ZipUtils;
import org.but4reuse.utils.workbench.WorkbenchUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Unpack action
 * 
 * @author jabier.martinez
 */
public class UnpackAction implements IObjectActionDelegate {

	private ISelection selection;

	@Override
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			Iterator<?> i = ((IStructuredSelection) selection).iterator();
			while (i.hasNext()) {
				IResource file = (IResource) i.next();
				File apk = WorkbenchUtils.getFileFromIResource(file);
				File outputFolder = new File(apk.getParentFile(), apk.getName().substring(0,
						apk.getName().lastIndexOf(".")));
				outputFolder.mkdir();
				// unpack to extract the src
				unpackSrc(apk, outputFolder);
				// unzip content
				ZipUtils.unZip(apk, outputFolder);
				// refresh
				WorkbenchUtils.refreshIResource(file.getProject());
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public static void unpackSrc(File apk, File outputFolder) {
		try {
			// dex2jar
			URL url = Platform.getBundle(Activator.PLUGIN_ID).getEntry("unpackUtils/dex2jar/d2j-dex2jar.bat");
			File filed2j = new File(FileLocator.toFileURL(url).toURI());
			// d2j-dex2jar.bat -f -o output_jar.jar apk_to_decompile.apk
			File jar = File.createTempFile(apk.getName(), ".jar");
			String command = filed2j.getAbsolutePath() + " -f -o " + jar + " " + apk.getAbsolutePath();
			// do not add start... it won't show the console but it will
			// waitFor() "cmd /c start "
			Process p = Runtime.getRuntime().exec("cmd /c " + command);
			p.waitFor();
			p.destroy();

			// jd-cli
			File outputSrcFolder = new File(outputFolder, "src");
			Main.main(new String[]{"-od" ,outputSrcFolder.getAbsolutePath(), jar.getAbsolutePath()});
			
			// manifest
			URL url2 = Platform.getBundle(Activator.PLUGIN_ID).getEntry("unpackUtils/manifest/aapt.exe");
			File aapt = new File(FileLocator.toFileURL(url2).toURI());
			File outManifest = new File(outputFolder, "AndroidManifest.txt");
			command = aapt.getAbsolutePath() + " dump badging " + apk.getAbsolutePath() + " " + "AndroidManifest.xml > " + outManifest.getAbsolutePath();
			Process p2 = Runtime.getRuntime().exec("cmd /c " + command);
			p2.waitFor();
			p2.destroy();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
