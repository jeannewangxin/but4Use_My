package elements;

import java.net.URI;

import org.but4reuse.utils.files.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * This class contains the C++ element implementation for a #else directive
 * Example : #else f(x)
 * 
 */

public class ElseDirective extends CppElement {

	public ElseDirective(IASTNode node, CppElement parent, String text, String rawText) {
		super(node, parent, text, rawText, CppElementType.ELSE_DIR);
	}


	
//	public void construct(URI uri, String block) {
//
//		if (parent != null && (parent instanceof SourceFile || parent instanceof HeaderFile)) {
//
//			// Construct the parent if it doesn't exist
//			parent.construct(uri, block);
//			file = parent.file;
//
//			try {
//				
//				if (ConstructionWithAnnotationHelper.CURRENT_BLOCK != block) {
//					if (ConstructionWithAnnotationHelper.CURRENT_BLOCK != null) {
//						FileUtils.appendToFile(file, ConstructionWithAnnotationHelper.USED_ANNOTATION.annotationEnd(ConstructionWithAnnotationHelper.CURRENT_BLOCK));
//					}
//					FileUtils.appendToFile(file, ConstructionWithAnnotationHelper.USED_ANNOTATION.annotationStart(block));
//					ConstructionWithAnnotationHelper.CURRENT_BLOCK = block;
//				}
//				
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
