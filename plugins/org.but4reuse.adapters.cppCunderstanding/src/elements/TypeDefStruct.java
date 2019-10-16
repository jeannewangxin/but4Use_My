package elements;

import java.net.URI;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public class TypeDefStruct extends CppElement {

	public TypeDefStruct(IASTNode node, CppElement parent, String text, String rawText) {
		
		super(node, parent, text, rawText, CppElementType.STRUCT_DEF);
		
	}

	/**
	 * Construct the typeDef element in the contained parent file.
	 */
	public void construct(URI uri, String block) {

	}
}
