package org.but4reuse.adapters.android;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.android.activator.Activator;
import org.but4reuse.adapters.android.preferences.AndroidAdapterPreferencePage;
import org.but4reuse.utils.files.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;

import parser.ReadXML;

/**
 * Adapter for OpenSource Android Manifest
 * 
 * @author anasshatnawi
 * 
 */
public class AndroidManifestAdapter implements IAdapter {

	@Override
	public boolean isAdaptable(URI uri, IProgressMonitor monitor) {
		File file = FileUtils.getFile(uri);
		if (file == null || !file.exists()) {
			return false;
		}
		return FileUtils.containsFileWithExtension(file, "xml");
	}

	@Override
	public List<IElement> adapt(URI uri, IProgressMonitor monitor) {
		List<IElement> elements = new ArrayList<IElement>();
		File file = FileUtils.getFile(uri);
		List<File> allFiles = FileUtils.getAllFiles(file);
		File manif = null;
		for (File f : allFiles) {
			String fileName = f.getName();
			if (f.getName().contains("AndroidManifest")) {
				try {
					DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					Document doc = dBuilder.parse(f);
					if (doc.hasChildNodes()) {
						// call the parser
						List<IElement> selectedElements = new ArrayList<IElement>();
						ReadXML.getElements(doc.getChildNodes(), selectedElements);
						elements.addAll(selectedElements);
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}
		return elements;
	}

	@Override
	public void construct(URI uri, List<IElement> elements, IProgressMonitor monitor) {

	}

	public static List<String> getPermissions(File androidManifest) {
		List<String> permissions = new ArrayList<String>();
		for (String s : FileUtils.getLinesOfFile(androidManifest)) {
			if (s.startsWith("uses-permission:")) {
				permissions.add(s);
			}
		}
		return permissions;
	}

}
