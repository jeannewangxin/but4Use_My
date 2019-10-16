package elements;

import java.io.File;
import java.net.URI;

import org.but4reuse.utils.files.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * This class contains the C++ element implementation for a global variable.
 * 
 */

public class GlobalVar extends CppElement {

	public GlobalVar(IASTNode node, CppElement parent, String text, String rawText) {
		super(node, parent, text, rawText, CppElementType.GLOBAL_VAR);
	}

	/**
	 * Construct the global var in the contained parent file.
	 */

	
	public void construct(File f) {

	}
}
