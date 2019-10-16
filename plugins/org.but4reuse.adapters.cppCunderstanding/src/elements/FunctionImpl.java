package elements;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import callhierarchy.xml.FunctionSignatureParser;
import construction.Construction;

import org.but4reuse.utils.files.FileUtils;
import org.but4reuse.utils.strings.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;

/**
 * This class contains the C++ element implementation for a function
 * implementation node. Example : std::string LineBorder::SVGPrintString() const
 * 
 * 
 */

public class FunctionImpl extends CppElement {

	public FunctionImpl(IASTNode node, CppElement parent, String text, String rawText) {
		super(node, parent, text, rawText, CppElementType.FUNCTION_IMPL);
		words = extractWords();
	}

	private List<String> extractWords() {

		// add the words for the word cloud
		List<String> wordsList = new ArrayList<String>();

		// the raw text contains the function signature (name + parameters
		// types)
		// we are only interested in the name
		String[] tokens = rawText.split(FunctionSignatureParser.TYPE_SEPARATOR.toString());

		String classAndFunctionName = tokens[0];
		tokens = classAndFunctionName.split("::");

		// function that doesn't belong to a class (main for example);
		if (tokens.length == 1) {
			wordsList.addAll(StringUtils.tokenizeString(tokens[0]));
		} else {
			wordsList.addAll(StringUtils.tokenizeString(tokens[1]));
		}

		return wordsList;
	}

	/**
	 * Construct the function element in the contained parent file.
	 */

	public void construct(File f) {

//		StringBuffer bodyAnnotated = new StringBuffer();
//		Set setOfInstruction = new HashSet<String>();
//		Construction.getInstruction(this);

//		for (IASTNode s1 : ((IASTFunctionDefinition) this.node).getBody().getChildren()) {
//			System.out.println(s1.getRawSignature());
//			new java.util.Scanner(System.in).nextLine();
//		}

		try {
			FileUtils.appendToFile(f,
					this.node.getRawSignature());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
