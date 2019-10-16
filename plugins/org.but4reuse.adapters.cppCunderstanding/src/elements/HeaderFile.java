package elements;

import java.net.URI;

import org.but4reuse.utils.files.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;
/**
 * This class contains the C++ element implementation for a header file (.h
 * extension)
 * 
 */

public class HeaderFile extends CppElement {

	public HeaderFile(IASTNode node, CppElement parent, String text, String rawText) {
		
		super(node, parent, text, rawText, CppElementType.HEADER_FILE);	
		
	}

	/**
	 * Create an empty file with the required name.
	 */
	public void construct(URI uri, String block) {


	}

	
}
