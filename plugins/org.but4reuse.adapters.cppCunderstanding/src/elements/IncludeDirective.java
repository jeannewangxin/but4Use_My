package elements;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.but4reuse.utils.files.FileUtils;
import org.but4reuse.utils.strings.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import activator.Activator;
import adapters.CppAdapter;
import preferences.CppCunderstandingPreferencePage;

import org.but4reuse.adapters.IElement;

/**
 * This class contains the C++ element implementation for a include directive
 * Example : #include <string> or #include "super_duper_file.h"
 * 
 */

public class IncludeDirective extends CppElement {

	public IncludeDirective(IASTNode node, CppElement parent, String text, String rawText) {
		super(node, parent, text, rawText, CppElementType.INCLUDE_DIR);
		this.racine = CppAdapter.artefactUri;
		// added when integrating the two adapters : cpp and understanding
		String selection = (Activator.getDefault().getPreferenceStore()
				.getString(CppCunderstandingPreferencePage.SELECTION));
		if (!(selection.equals("ALL"))) {
			words = extractWords();

		}
	}

	@Override
	public double similarity(IElement anotherElement) {
		// added when integrating the two adapters : cpp and understanding
		String selection = (Activator.getDefault().getPreferenceStore()
				.getString(CppCunderstandingPreferencePage.SELECTION));
		if (selection.equals("ALL")) {
			if (((CppElement) anotherElement).getType() == CppElementType.INCLUDE_DIR) {
				return super.similarity(anotherElement);

			} else {
				return 0;
			}
		} else {

			if (Activator.getDefault().getPreferenceStore().getBoolean(CppCunderstandingPreferencePage.IGNORE_PATH)) {
				if ((this.node.getRawSignature()).equals(((CppElement) anotherElement).getNode().getRawSignature())) {
					return 1.;
				} else
					return 0.;
			} else {

				String path1 = text;
				String path2 = ((CppElement) anotherElement).text;
				if (path1.equals(path2))
					return 1.;
				else
					return 0.;

			}

		}
	}

	@Override
	public String getText() {
		return "[" + type + "] " + text;
	}

	public void construct(File f) {
		try {
			FileUtils.appendToFile(f, this.getRawText());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<String> extractWords() {

		// add the words for the word cloud
		List<String> wordsList = new ArrayList<String>();
		String[] tokens = rawText.split(" ");

		if (tokens.length == 1) {
			wordsList.addAll(StringUtils.tokenizeString(tokens[0]));
		} else {
			wordsList.addAll(StringUtils.tokenizeString(tokens[1]));
		}

		return wordsList;
	}

}
