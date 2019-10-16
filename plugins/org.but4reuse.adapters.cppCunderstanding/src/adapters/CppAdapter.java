package adapters;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;

import org.but4reuse.utils.files.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import activator.Activator;
import construction.Construction;
import elements.CppElement;
import elements.CppElement.CppElementType;
import elements.UsingNSpaceDir;
import preferences.CppCunderstandingPreferencePage;
import visitors.CppParser;

public class CppAdapter implements IAdapter {

	public static String artefactUri = "";

	@Override
	public boolean isAdaptable(URI uri, IProgressMonitor monitor) {
		// check the presence of a .cpp file
		File file = FileUtils.getFile(uri);

		if (file == null || !file.exists()) {
			return false;
		}

		return FileUtils.containsFileWithExtension(file, CppParser.SOURCE_CPP)
				|| FileUtils.containsFileWithExtension(file, CppParser.SOURCE_C)
				|| FileUtils.containsFileWithExtension(file, CppParser.HEADER);

	}

	@Override
	public List<IElement> adapt(URI uri, IProgressMonitor monitor) {

		// setting the URI
		artefactUri = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);

		List<IElement> elements = Collections.emptyList();

		File file = FileUtils.getFile(uri);

		String selection = (Activator.getDefault().getPreferenceStore()
				.getString(CppCunderstandingPreferencePage.SELECTION));
		// the case of all elements CDT Adapter
		if (selection.equals("ALL")) {
			try {

				elements = CppParser.parse(file);
				return elements;
			} catch (CoreException e) {
				e.printStackTrace();
			}

		} else {
			// for the rest of element (the understanding variability)
			if ((selection.equals("REST"))) {

				try {
					elements = CppParser.parse(file);
				} catch (CoreException e) {
					e.printStackTrace();
				}

				// for other cases (packages, .cpp et .h)
				boolean a = Activator.getDefault().getPreferenceStore()
						.getBoolean(CppCunderstandingPreferencePage.PACKAGES);
				boolean b = Activator.getDefault().getPreferenceStore()
						.getBoolean(CppCunderstandingPreferencePage.CPP_FILES);
				boolean c = Activator.getDefault().getPreferenceStore()
						.getBoolean(CppCunderstandingPreferencePage.H_FILES);

				// if .cpp and .h are selected
				if (a || b || c) {

					FileStructureAdapter f = new FileStructureAdapter();
					for (IElement e : f.adapt(uri, monitor)) {
						elements.add(e);

					}

				}

			}
		}
		// we return the list of element of this product variant
		return elements;
	}

	@Override
	public void construct(URI uri, List<IElement> elements, IProgressMonitor monitor) {

		Construction c = new Construction();
		c.construct(uri, elements, monitor);
//		for (IElement iElement : elements) {
//			if (((CppElement) iElement).getType() == CppElementType.STATEMENT_IMPL) {
//				System.out.println("\nthe element : " + ((CppElement) iElement).getNode().getRawSignature()
//						+ " \n his parent : " + ((CppElement) iElement).getParent().getNode().getRawSignature()
//						+ "\nhis class " + ((CppElement) iElement).getNode().getClass().getName());
//			
//			System.out.println("\n---------------------------------------------");
//			}
//		}
	}
}
