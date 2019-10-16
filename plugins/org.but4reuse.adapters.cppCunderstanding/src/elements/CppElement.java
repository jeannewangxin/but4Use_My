package elements;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import activator.Activator;
import adapters.CppAdapter;
import preferences.CppCunderstandingPreferencePage;

/**
 * This class contains the C++ element implementation.
 * 
 */

public abstract class CppElement extends AbstractElement {

	public static enum CppElementType {
		CLASS_H, FUNCTION_H, FUNCTION_IMPL, STATEMENT_IMPL, HEADER_FILE, SOURCE_FILE, INCLUDE_DIR, MACRO_DIR, IFDEF_DIR,
		IFNDEF_DIR, IF_DIR, ELIF_DIR, ELSE_DIR, ENDIF_DIR, GLOBAL_VAR, STRUCT_DEF, USING_NAME_SPACE_DIR, ATTRIBUTE_H,
		COMMENT, IF_DEF, END_IF_DEF
	};

	public static final String H_EXTENSION = "~~H";
	public static final String IMPL_EXTENSION = "~~IMPL";

	/** AST node representing the current element. */
	protected IASTNode node;

	/** The parent CppElement for the current element. */
	protected CppElement parent;

	/** Textual representation. */
	protected String text;

	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Raw textual representation with no additional information such as
	 * Implementation or Header
	 */
	protected String rawText;

	public void setRawText(String rtext) {
		this.rawText = rtext;
	}

	/** Element type from the enumeration listed in the beginning of this class */
	protected CppElementType type;

	/**
	 * the list of words extracted from this element to be used in the wordCloud
	 * visualization
	 */
	protected List<String> words;

	/** to know if this element is constructed or not */
	protected boolean constructed;

	public void setConstructed(boolean b) {
		this.constructed = b;
	}

	public boolean getConstructed() {
		return this.constructed;
	}

	/** the file in which this element is contained during it's construction */
	protected File file;

	public void setFile(File f) {
		this.file = f;
	}

	public File getFile() {
		return this.file;
	}

	/** represent the uri of the product variant (the parent of this element) */
	public String racine;

	public CppElement(IASTNode node, CppElement parent, String text, String rawText, CppElementType type) {
		super();
		this.node = node;
		this.parent = parent;
		this.text = text;
		this.rawText = rawText;
		this.type = type;
		this.racine = CppAdapter.artefactUri;

		// by default we return an empty list
		// for performance reasons we avoid creating a new empty list for each
		// element,
		// instead each subclass needs to create if needed this list and defin
		// its own behavior
		words = Collections.emptyList();
		constructed = false;
	}

	@Override
	public double similarity(IElement anotherElement) {

		// System.out.println("\n this : " + this.getText() + " compare to "+
		// anotherElement.getText());
		// different text
		if (!anotherElement.getText().replaceAll(" ", "").equals(getText().replaceAll(" ", ""))) {
			return 0.;
		}

		// text is equal, check parents
		CppElement currentElementParent = this.getParent();
		CppElement anotherElementParent = ((CppElement) anotherElement).getParent();

		// both parents are null and every text is equal
		if (currentElementParent == null && anotherElementParent == null) {
			return 1.0;
		}
		// one parent is null
		else if (currentElementParent == null || anotherElementParent == null) {
			return 0.;
		}
		// check parents
		if ((currentElementParent.getParent() == null) && (anotherElementParent.getParent() == null))
			return 1.; // because all are equal just for the .h or .cpp files have different names

		return currentElementParent.similarity(anotherElementParent);

	}

	/**
	 *
	 * Given an output URI, this method should construct an artefact containing the
	 * element
	 * 
	 * @param uri output URI
	 */
	public void construct(File f) {

		// default behavior -> don't construct
	}

	public void construct(URI uri, String block) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getWords() {
		List<String> l = new ArrayList<String>();
		for (String string : this.words) {
			if ((string.contains("<")) || (string.contains(">")) || (string.contains("=")) || (string.contains("cout"))
					|| (string.contains("false")) || (string.contains("true")) || (string.contains("cin"))
					|| (string.contains("end")) || (string.contains("return"))) {

			} else {
				l.add(string);
			}
		}
		return l;
	}

	@Override
	public String getText() {
		// System.out.println("\n this element "+ this.text + " in this artefact " + +
		// CppAdapter.artefactUri );
		return "[" + type + "] " + text;
	}

	public String getRawText() {
		return rawText;
	}

	public IASTNode getNode() {
		return node;
	}

	public CppElement getParent() {
		return parent;
	}

	public CppElementType getType() {
		return type;
	}

	public void resetConstructFlag() {
		constructed = false;
	}

	/**
	 * to return the relative path of any element (path starting from the folder
	 * that contain the product variant)
	 * 
	 * @param f the file that we need his path
	 * @return path the path of this file starting from the folder that containing
	 *         the product variant
	 */
	public String returnPath(File f) {

		boolean b = true;
		String path = f.getName();

		if (((String) f.getName()).equals((String) this.racine)) {
			return path;
		}
		f = f.getParentFile();
		while (b == true) {
			if (((String) f.getName()).equals((String) this.racine)) {
				b = false;
				return path;
			} else {
				path = f.getName() + "/" + path;
				f = f.getParentFile();
			}
		}

		return path;
	}

	@Override
	/**
	 * To improve performance. We put the same hash when they have the same text.
	 */
	public int hashCode() {

		if ((Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.SELECTION))
				.equals("ALL")) {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((getText() == null) ? 0 : getText().hashCode());
			return result;
		} else {
			return -1;
		}
	}

}
