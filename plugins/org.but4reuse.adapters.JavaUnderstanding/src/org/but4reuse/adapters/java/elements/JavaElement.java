package org.but4reuse.adapters.java.elements;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;
import org.but4reuse.adapters.java.elements.activator.Activator;
import org.but4reuse.adapters.java.elements.preferences.JavaUnderstandingAdapterPreferencePage;
import org.but4reuse.adapters.java.elements.preferences.JavaUnderstandingAdapterPreferencePage.Choice;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Modifier;

public abstract class JavaElement extends AbstractElement {

	String packageName;
	String className;
	public static boolean IGNOREPATH = false;

	/**
	 * the list of words extracted from this element to be used in the wordCloud
	 * visualization
	 */
//	protected List<String> words;

	ASTMatcher matcher = new ASTMatcher();

	public JavaElement(String packageName, String className) {
		this.packageName = packageName;
		this.className = className;
		if (Activator.getDefault() != null) {
			IGNOREPATH = Activator.getDefault().getPreferenceStore()
					.getBoolean(JavaUnderstandingAdapterPreferencePage.IGNORE_PATH);
		}
//		words = Collections.emptyList();
	}

//	@Override
//	public List<String> getWords() {
//		System.err.println("java" +words.toString());
//		return words;
//	}

	@Override
	public List<String> getWords() {
		// TODO Auto-generated method stub
		return super.getWords();
	}
	
	@Override
	public double similarity(IElement anotherElement) {

		if (!(anotherElement instanceof JavaElement))
			return 0;

		JavaElement otherclass = (JavaElement) anotherElement;
		// Check the packageName and the className are the same
		if (!IGNOREPATH) {
			if (packageName.equals(otherclass.packageName) && className.equals(otherclass.className)) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return 1;
		}
	}

	@Override
	public String getText() {
		String text = "";
		if (!IGNOREPATH) {
			if (packageName.equals("") || packageName.trim().equals("MISSING")) {
				text = " Class: " + className;
			} else {
				text = " Class: " + packageName + "." + className;
			}
		}
		return text;
	}

	/**
	 * Compare two modifier list.
	 * 
	 * @param modifier1
	 * @param modifier2
	 * @return true if the two list are equals, false otherwise
	 */
	public boolean compareModifier(List<Modifier> modifier1, List<Modifier> modifier2) {
		boolean isSimilar = true;
		ASTMatcher comparator = new ASTMatcher();
		if (modifier1.size() != modifier2.size()) {
			isSimilar = false;
		} else {
			for (int i = 0; i < modifier1.size(); i++) {
				boolean found = false;
				for (int j = 0; j < modifier2.size(); j++) {
					if (comparator.match(modifier1.get(i), modifier2.get(j))) {
						found = true;
						break;
					}
				}

				if (!found) {
					isSimilar = false;
					break;
				}
			}
		}

		return isSimilar;
	}

	public <T> void addDiff(List<T> diffList, T newElement, Comparator<T> comparator) {
		boolean contains = false;
		for (T element : diffList) {
			// Check the otherModifier and modifiers are equals
			if (comparator.compare(element, newElement) == 0) {
				contains = true;
				break;
			}
		}
		if (!contains) {
			diffList.add(newElement);
		}

	}

	public <T extends ASTNode> boolean isSimilar(T astNode1, T astNode2) {
		return astNode1.subtreeMatch(matcher, astNode2);
	}

	public <T extends ASTNode> boolean isSimilarNotOrderSensitive(List<T> astNode1, List<T> astNode2) {

		if (astNode1.size() != astNode2.size()) {
			return false;
		}

		for (T node : astNode1) {
			boolean found = false;
			for (T node2 : astNode2) {
				if (node.subtreeMatch(matcher, astNode2)) {
					found = true;
					break;
				}
			}

			if (!found) {
				return false;
			}
		}
		return true;
	}

	public <T extends ASTNode> boolean isSimilarOrderSensitive(List<T> astNode1, List<T> astNode2) {

		if (astNode1.size() != astNode2.size()) {
			return false;
		}

		for (int i = 0; i < astNode1.size(); i++) {
			if (!astNode1.get(i).subtreeMatch(matcher, astNode2.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Update the two diffList by adding the two element if the choice is DIFFENT
	 * 
	 * @param option
	 * @param diffList1   diffList to upgrade
	 * @param newElement1 element to add
	 * @param diffList2   diffList to upgrade
	 * @param newElement2 element to add
	 * @param comparator  Comparator who determine if two element are equals for
	 *                    avoiding doublons on the diffList
	 */
	protected <T> void updateDiffList(Choice option, List<T> diffList1, T newElement1, List<T> diffList2, T newElement2,
			Comparator<T> comparator) {
		if (option == Choice.DIFFERENT) {
			addDiff(diffList1, newElement1, comparator);
			addDiff(diffList1, newElement2, comparator);
			addDiff(diffList2, newElement1, comparator);
			addDiff(diffList2, newElement2, comparator);
		}
	}

	public <T> String getTextList(List<T> difflist) {
		StringBuffer txt = new StringBuffer();
		txt.append("( ");
		if (difflist.size() == 0) {
			return "()";
		} else {
			for (int i = 0; i < difflist.size(); i++) {
				if (difflist.get(i) == null) {
					txt.append("Not defined");
				} else {
					txt.append(difflist.get(i).toString());
				}
				if (i != difflist.size() - 1) {
					txt.append(" | ");
				}
			}
			txt.append(" )");
		}

		return txt.toString();
	}
}
