package elements;

import java.io.File;
import java.net.URI;

import org.but4reuse.utils.files.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;


/**
 * This class contains the C++ element implementation for a #ifndef directive
 * Example : #ifndef RECTANGULAR super_function();
 * 
 */

public class IfndefDirective extends CppElement {

	public IfndefDirective(IASTNode node, CppElement parent, String text, String rawText) {
		super(node, parent, text, rawText, CppElementType.IFNDEF_DIR);
	}


	public void construct(File f) {

		try {
			FileUtils.appendToFile(f, "\n" + this.getRawText());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
}
