package elements;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.but4reuse.utils.files.FileUtils;
import org.but4reuse.utils.strings.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * This class contains the C++ element implementation of a class header
 * definition. Example : class Animal { public: Animal(); };
 * 
 */

public class ClassHeader extends CppElement {

	public ClassHeader(IASTNode node, CppElement parent, String text, String rawText) {
		super(node, parent, text, rawText, CppElementType.CLASS_H);
		words = extractWords();
	}

	private List<String> extractWords() {

		// add the words for the word cloud
		List<String> wordList = new ArrayList<String>();
		wordList.addAll(StringUtils.tokenizeString(rawText));

		return wordList;
	}

	/**
	 * Construct the function element in the contained parent file.
	 */
	public void construct(File f) {
		String nodeRepresentation = this.node.getParent().getRawSignature();

		try {
			FileUtils.appendToFile(f, "\n" + nodeRepresentation.substring(0, nodeRepresentation.indexOf("{")+1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
