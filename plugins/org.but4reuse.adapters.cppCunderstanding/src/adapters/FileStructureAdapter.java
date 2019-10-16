package adapters;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import elements.FileElement;

import activator.Activator;
import preferences.CppCunderstandingPreferencePage;

import org.but4reuse.utils.files.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * File structure adapter
 * 
 * @author jabier.martinez
 */
public class FileStructureAdapter implements IAdapter {

	private URI rootURI;
	public Boolean ignorePath;

	@Override
	public boolean isAdaptable(URI uri, IProgressMonitor monitor) {
		// Any folder is adaptable
		File file = FileUtils.getFile(uri);
		if (file != null && file.exists() && file.isDirectory()) {
			return true;
		}
		return false;
	}

	@Override
	public List<IElement> adapt(URI uri, IProgressMonitor monitor) {
		try {
			ignorePath = Activator.getDefault().getPreferenceStore()
					.getBoolean(CppCunderstandingPreferencePage.IGNORE_PATH);

		} catch (NullPointerException e) {

		}
		return adapt(uri, monitor, null);
	}

	public List<IElement> adapt(URI uri, IProgressMonitor monitor, String option) {

		List<IElement> elements = new ArrayList<IElement>();
		File file = FileUtils.getFile(uri);
		rootURI = file.toURI();
		// start the containment tree traversal, with null as initial container
		adapt(file, elements, null);
		// in elements we have the result
		return elements;
	}

	/**
	 * adapt recursively
	 * 
	 * @param file
	 * @param elements
	 * @param container
	 */
	private void adapt(File file, List<IElement> elements, IElement container) {

		FileElement newElement = new FileElement();		

		// Set the relevant information

		newElement.setUri(file.toURI());
		// System.out.println("ignore path: "+ignorePath);
		if (ignorePath) {
			// if we ignore path, relative URI is only the file name
			if (!file.toURI().equals(rootURI))
				newElement.setRelativeURI(file.getParentFile().toURI().relativize(file.toURI()));
		} else
			newElement.setRelativeURI(rootURI.relativize(file.toURI()));

		// Add dependency to the parent folder
		/*
		 * if (option != CppCunderstandingPreferencePage.IGNORE_PATH && container !=
		 * null) { newElement.addDependency(container); }
		 */

		// Add to the list after looking option
		if (file.toURI().equals(rootURI)) {
		} else if (Activator.getDefault().getPreferenceStore().getBoolean(CppCunderstandingPreferencePage.PACKAGES)) // (option.equals(CppCunderstandingPreferencePage.FOLDERS))
		{
			if (file.isDirectory()) {
				elements.add(newElement);
				//System.out.println("\ndiractory ");
			}
		}

		if (Activator.getDefault().getPreferenceStore().getBoolean(CppCunderstandingPreferencePage.H_FILES)) {
			if (file.getName().endsWith(".h")) {
				elements.add(newElement);
				//System.out.println("\nheader ");
			}
		}

		if (Activator.getDefault().getPreferenceStore().getBoolean(CppCunderstandingPreferencePage.CPP_FILES)) {
			if (file.getName().endsWith(".cpp")) {
				elements.add(newElement);
				//System.out.println("\nsource ");
			}
		}

		// Go for the files in case of folder
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File subFile : files) {
				adapt(subFile, elements, newElement);
			}
		}
	}

	@Override
	public void construct(URI uri, List<IElement> elements, IProgressMonitor monitor) {
		for (IElement element : elements) {
			// check user cancel for each element
			if (!monitor.isCanceled()) {
				// provide user info
				monitor.subTask(element.getText());
				if (element instanceof FileElement) {
					FileElement fileElement = (FileElement) element;
					try {
						// Create parent folders structure
						URI newDirectoryURI = uri.resolve(fileElement.getRelativeURI());
						File destinationFile = FileUtils.getFile(newDirectoryURI);
						if (destinationFile != null && !destinationFile.getParentFile().exists()) {
							destinationFile.getParentFile().mkdirs();
						}
						if (destinationFile != null && !destinationFile.exists()) {
							// Copy the content. In the case of a folder, its
							// content is not copied
							File file = FileUtils.getFile(fileElement.getUri());
							Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			monitor.worked(1);
		}
	}

}
