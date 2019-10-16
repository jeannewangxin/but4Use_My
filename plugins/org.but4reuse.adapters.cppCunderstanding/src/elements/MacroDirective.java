package elements;

import java.io.File;
import java.net.URI;

import org.but4reuse.utils.files.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * This class contains the C++ element implementation for a macro directive
 * Example : #define SIZE 20
 * 
 */

public class MacroDirective extends CppElement {

	public MacroDirective(IASTNode node, CppElement parent, String text, String rawText) {
		super(node, parent, text, rawText, CppElementType.MACRO_DIR);
	}

	
	public void construct(File f) {

		try {
			FileUtils.appendToFile(f, "\n" +  this.getRawText() );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
