package elements;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;
import org.but4reuse.adapters.markers.IMarkerElement;
import org.but4reuse.utils.files.FileUtils;
import org.but4reuse.utils.workbench.WorkbenchUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import adapters.CppAdapter;

public class FileElement extends AbstractElement implements IMarkerElement {

	private URI uri;
	private URI relativeURI;

	@Override
	public double similarity(IElement anotherElement) {
		// When they have the same relative URI
		// TODO URIs can reference to the same file... check this
		if (anotherElement instanceof FileElement) {
			FileElement anotherFileElement = ((FileElement) anotherElement);

			// Same URI?
			if (this.getRelativeURI() != null) {
				if (this.getRelativeURI().equals(anotherFileElement.getRelativeURI())) {

					File file1 = FileUtils.getFile(this.getUri());
					File file2 = FileUtils.getFile(anotherFileElement.getUri());
					if ((file1.isDirectory() && file2.isDirectory())
							|| (!file1.isDirectory() && !file2.isDirectory())) {
						return 1;
					}
				}
			}
		}
		return 0;
	}

	@Override
	public String getText() {
		if (getRelativeURI() == null) {
			// because here we have not a folder we have just a file .cpp or .h
			return CppAdapter.artefactUri;
		}
		return getRelativeURI().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getRelativeURI() == null) ? 0 : getRelativeURI().hashCode());
		return result;
	}

	public URI getRelativeURI() {
		return relativeURI;
	}

	public void setRelativeURI(URI relativeURI) {
		this.relativeURI = relativeURI;
	}

	@Override
	public IMarker getMarker() {
		IMarker marker = null;
		IResource ifile = WorkbenchUtils.getIResourceFromURI(getUri());
		if (ifile != null && ifile.exists()) {
			try {
				marker = ifile.createMarker(IMarker.TEXT);
				marker.setAttribute(IMarker.LOCATION, ifile.getName());
				// marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return marker;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	@Override
	public ArrayList<String> getWords() {
		ArrayList<String> words = new ArrayList<String>();
		// We split path with chars '/' and '\' in order to have the names
		// We also split with dot for separating the extension from the file name
		if (relativeURI == null)
			try {
				// because here we have not a folder we have just a file .cpp or .h as a product variant so his relativePath is null
				relativeURI = new URI(CppAdapter.artefactUri);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		StringTokenizer tk = new StringTokenizer(relativeURI.getPath(), "/\\.");

		while (tk.hasMoreTokens()) {
			String s = tk.nextToken();
			words.add(s);
		}
		return words;
	}

}