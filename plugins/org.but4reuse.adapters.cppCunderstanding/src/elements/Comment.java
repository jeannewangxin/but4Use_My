package elements;

import java.net.URI;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public class Comment extends CppElement {

	public Comment(IASTNode node, CppElement parent, String text, String rawText) {
		super(node, parent, text, rawText, CppElementType.COMMENT);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Construct the comments element in the contained parent file.
	 */
	public void construct(URI uri, String block) {

	}

}
