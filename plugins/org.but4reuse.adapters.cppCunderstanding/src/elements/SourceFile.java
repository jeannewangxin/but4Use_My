package elements;

import java.net.URI;


import org.but4reuse.utils.files.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * This class contains the C++ element implementation for a source file (.cpp
 * extension)
 * 
 */

public class SourceFile extends CppElement {

	public SourceFile(IASTNode node, CppElement parent, String text, String rawText) {
		super(node, parent, text, rawText, CppElementType.SOURCE_FILE);
	}

	/**
	 * Create an empty file with the required name.
	 */
	
	public void construct(URI uri, String block) {


	}


} 
