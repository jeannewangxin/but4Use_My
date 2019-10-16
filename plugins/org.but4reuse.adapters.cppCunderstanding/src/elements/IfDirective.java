package elements;

import java.net.URI;

import org.but4reuse.utils.files.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * This class contains the C++ element implementation for a #if directive
 * Example : #if RECTANGULAR super_function();
 * 
 */

public class IfDirective extends CppElement {

	public IfDirective(IASTNode node, CppElement parent, String text, String rawText) {
		super(node, parent, text, rawText, CppElementType.IF_DIR);
	}

//	public void construct(URI uri) {
//
//		if (parent != null && (parent instanceof SourceFile || parent instanceof HeaderFile)) {
//
//			// Construct the parent if it doesn't exist
//			parent.construct(uri);
//			file = parent.file;
//
//			try {
//				String content = node.getRawSignature() + "\n";
//				FileUtils.appendToFile(file, content);
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		} else {
//			System.err.println("Parent of " + getClass() + " not a SourceFile but a "
//					+ ((parent != null) ? parent.getClass() : parent));
//		}
//	}

	
	public void construct(URI uri, String block) {

	}
}
