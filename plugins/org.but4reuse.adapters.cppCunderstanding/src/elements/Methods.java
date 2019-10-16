package elements;

import java.awt.datatransfer.SystemFlavorMap;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adapters.IElement;
import callhierarchy.xml.FunctionSignatureParser;
import construction.SimilarityHelper;

import org.but4reuse.utils.files.FileUtils;
import org.but4reuse.utils.strings.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTWhileStatement;

import activator.Activator;
import adapters.CppAdapter;
import preferences.CppCunderstandingPreferencePage;
import preferences.CppCunderstandingPreferencePage.Choice;

public class Methods extends CppElement {

	/** this attributes to get the state of the flags from the preference page */
	public static Choice methodModifierC = null;
	public static Choice returnTypeC = null;
	public static Choice methodNameC = null;
	public static Choice methodParametersC = null;
	public static Choice bodyC = null;
	public static Choice StatementOrderC = null;
	public static float minRangeC;
	public static float maxRangeC;

	/** attributes specific to the method element in cpp */
	protected String modifiers;
	protected String returnType;
	protected String methodName;
	protected StringBuffer parametersM;
	protected IASTStatement body;

	public Methods(IASTNode node, CppElement parent, String text, String rawText, String mod, String ret, String n,
			StringBuffer p, IASTStatement b) {
		super(node, parent, text, rawText, CppElementType.FUNCTION_H);
		this.racine = CppAdapter.artefactUri;

		// added when integrating the two adapters : cpp and understanding
		String selection = (Activator.getDefault().getPreferenceStore()
				.getString(CppCunderstandingPreferencePage.SELECTION));
		if (!(selection.equals("ALL"))) {

			// this.modifiers = new String(mod);
			this.returnType = new String(ret);
			this.methodName = new String(n);
			this.parametersM = new StringBuffer(p);
			this.body = b;
		}

		methodModifierC = Choice.valueOf(
				Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.MODIFIER_METHOD));

		returnTypeC = Choice.valueOf(
				Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.RETURNTYPE));

		methodNameC = Choice.valueOf(
				Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.NAME_METHOD));

		methodParametersC = Choice.valueOf(
				Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.PARAMETERS));

		Methods.bodyC = Choice
				.valueOf(Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.BODY));

		Methods.StatementOrderC = Choice.valueOf(Activator.getDefault().getPreferenceStore()
				.getString(CppCunderstandingPreferencePage.ORDER_SENSITIVITY));

		Methods.minRangeC = (float) ((Activator.getDefault().getPreferenceStore()
				.getInt(CppCunderstandingPreferencePage.MIN_Range)) * 1.0 / 100);

		Methods.maxRangeC = (float) ((Activator.getDefault().getPreferenceStore()
				.getInt(CppCunderstandingPreferencePage.MAX_Range)) * 1.0 / 100);

		words = extractWords();
	}

	@Override
	public double similarity(IElement anotherElement) {

		// added when integrating the two adapters : cpp and understanding
		String selection = (Activator.getDefault().getPreferenceStore()
				.getString(CppCunderstandingPreferencePage.SELECTION));
		if (selection.equals("ALL")) {
			super.similarity(anotherElement);

		} else {

			if (!(anotherElement instanceof Methods)) {
				return 0;
			}
			// System.out.println("\n this : " + this.methodName + " with " + ((Methods)
			// anotherElement).methodName);

			// just for testing body similarities
			if (((bodyC.toString() == "SAME")) && ((returnTypeC.toString() == "IGNORE")
					&& (methodNameC.toString() == "IGNORE") && (methodParametersC.toString() == "IGNORE"))) {

				return comparringBodies(anotherElement);
			}

			// means the same signature
			if ((returnTypeC.toString() == "SAME") && (methodNameC.toString() == "SAME")
					&& (methodParametersC).toString() == "SAME") {

				if ((bodyC.toString() == "SAME") && (this.methodName.equals(((Methods) anotherElement).methodName))) {

					return (comparringAll(anotherElement) * comparringBodies(anotherElement));
				}

				return comparringAll(anotherElement);
			}

			if ((returnTypeC.toString() == "SAME") && (methodNameC.toString() == "SAME")
					&& (methodParametersC).toString() == "DIFFERENT") {
				if ((comparringReturnType(anotherElement) == 1.) && (comparringMethodNames(anotherElement) == 1.)
						&& (comparringMethodParameters(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "SAME") && (methodNameC.toString() == "DIFFERENT")
					&& (methodParametersC).toString() == "SAME") {
				if ((comparringReturnType(anotherElement) == 1.) && (comparringMethodNames(anotherElement) == 0.)
						&& (comparringMethodParameters(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "SAME") && (methodNameC.toString() == "DIFFERENT")
					&& (methodParametersC).toString() == "DIFFERENT") {
				if ((comparringReturnType(anotherElement) == 1.) && (comparringMethodNames(anotherElement) == 0.)
						&& (comparringMethodParameters(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "DIFFERENT") && (methodNameC.toString() == "SAME")
					&& (methodParametersC).toString() == "SAME") {
				if ((comparringReturnType(anotherElement) == 0.) && (comparringMethodNames(anotherElement) == 1.)
						&& (comparringMethodParameters(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "DIFFERENT") && (methodNameC.toString() == "SAME")
					&& (methodParametersC).toString() == "DIFFERENT") {
				if ((comparringReturnType(anotherElement) == 0.) && (comparringMethodNames(anotherElement) == 1.)
						&& (comparringMethodParameters(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "DIFFERENT") && (methodNameC.toString() == "DIFFERENT")
					&& (methodParametersC).toString() == "SAME") {
				if ((comparringReturnType(anotherElement) == 0.) && (comparringMethodNames(anotherElement) == 0.)
						&& (comparringMethodParameters(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "DIFFERENT") && (methodNameC.toString() == "DIFFERENT")
					&& (methodParametersC).toString() == "DIFFERENT") {
				if ((comparringReturnType(anotherElement) == 0.) && (comparringMethodNames(anotherElement) == 0.)
						&& (comparringMethodParameters(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			// ____________________________________________________________________________________________________________________________________//

			if ((returnTypeC.toString() == "IGNORE") && (methodNameC.toString() == "SAME")
					&& (methodParametersC).toString() == "DIFFERENT")

			{
				if ((comparringMethodNames(anotherElement) == 1.)
						&& (comparringMethodParameters(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "IGNORE") && (methodNameC.toString() == "SAME")
					&& (methodParametersC).toString() == "SAME") {
				if ((comparringMethodNames(anotherElement) == 1.)
						&& (comparringMethodParameters(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "DIFFERENT") && (methodNameC.toString() == "SAME")
					&& (methodParametersC).toString() == "IGNORE") {
				if ((comparringReturnType(anotherElement) == 0.) && (comparringMethodNames(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "SAME") && (methodNameC.toString() == "SAME")
					&& (methodParametersC).toString() == "IGNORE") {
				if ((comparringReturnType(anotherElement) == 1.) && (comparringMethodNames(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "IGNORE") && (methodNameC.toString() == "SAME")
					&& (methodParametersC).toString() == "IGNORE") {
				return comparringMethodNames(anotherElement);
			}

			// ________________________________________________________________________________________________________________________________________________//

			if ((returnTypeC.toString() == "IGNORE") && (methodNameC.toString() == "DIFFERENT")
					&& (methodParametersC).toString() == "SAME") {
				if ((comparringMethodNames(anotherElement) == 0.)
						&& (comparringMethodParameters(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "DIFFERENT") && (methodNameC.toString() == "IGNORE")
					&& (methodParametersC).toString() == "SAME") {
				if ((comparringReturnType(anotherElement) == 0.)
						&& (comparringMethodParameters(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "SAME") && (methodNameC.toString() == "IGNORE")
					&& (methodParametersC).toString() == "SAME") {
				if ((comparringReturnType(anotherElement) == 1.)
						&& (comparringMethodParameters(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "IGNORE") && (methodNameC.toString() == "IGNORE")
					&& (methodParametersC).toString() == "SAME") {

				return comparringMethodParameters(anotherElement);
			}

			// __________________________________________________________________________________________________________________________________________________//

			if ((returnTypeC.toString() == "IGNORE") && (methodNameC.toString() == "IGNORE")
					&& (methodParametersC).toString() == "DIFFERENT") {
				if ((comparringMethodParameters(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "IGNORE") && (methodNameC.toString() == "DIFFERENT")
					&& (methodParametersC).toString() == "IGNORE") {
				if ((comparringMethodNames(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "IGNORE") && (methodNameC.toString() == "DIFFERENT")
					&& (methodParametersC).toString() == "DIFFERENT") {
				if ((comparringMethodNames(anotherElement) == 0.)
						&& (comparringMethodParameters(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "DIFFERENT") && (methodNameC.toString() == "IGNORE")
					&& (methodParametersC).toString() == "IGNORE") {
				if ((comparringReturnType(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "DIFFERENT") && (methodNameC.toString() == "IGNORE")
					&& (methodParametersC).toString() == "DIFFERENT") {
				if ((comparringReturnType(anotherElement) == 0.)
						&& (comparringMethodParameters(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "DIFFERENT") && (methodNameC.toString() == "DIFFERENT")
					&& (methodParametersC).toString() == "IGNORE") {
				if ((comparringReturnType(anotherElement) == 0.) && (comparringMethodNames(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "SAME") && (methodNameC.toString() == "IGNORE")
					&& (methodParametersC).toString() == "IGNORE") {
				if ((comparringReturnType(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "SAME") && (methodNameC.toString() == "IGNORE")
					&& (methodParametersC).toString() == "DIFFERENT") {
				if ((comparringReturnType(anotherElement) == 1.)
						&& (comparringMethodParameters(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((returnTypeC.toString() == "SAME") && (methodNameC.toString() == "DIFFERENT")
					&& (methodParametersC).toString() == "IGNORE") {
				if ((comparringReturnType(anotherElement) == 1.) && (comparringMethodNames(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}
		}
		// if all are ignored
		return 1.;

	}

	/**
	 * this function for comparing returnTypes of methods
	 * 
	 * @param anotherElement
	 * @return the value 1. if the two elements are equal considering their
	 *         ReturnType, 0. otherwise
	 */
	public double comparringReturnType(IElement anotherElement) {
		if (this.returnType.equals(((Methods) anotherElement).returnType)) {
			return 1.;
		} else {
			return 0.;
		}
	}

	/**
	 * this function for comparing Names of methods
	 * 
	 * @param anotherElement
	 * @return the value 1. if the two elements are equal considering their Method
	 *         Names, 0. otherwise
	 */
	public double comparringMethodNames(IElement anotherElement) {

		if (this.methodName.equals(((Methods) anotherElement).methodName)) {

			return 1.;
		} else {
			return 0.;
		}
	}

	/**
	 * this function for comparing parameters of methods
	 * 
	 * @param anotherElement
	 * @return the value 1. if the two elements are equal considering their list of
	 *         parameters, 0. otherwise
	 */
	public double comparringMethodParameters(IElement anotherElement) {

		if (this.parametersM.toString().equals(((Methods) anotherElement).parametersM.toString())) {
			return 1.;
		} else {
			return 0.;
		}
	}

	/**
	 * this function for comparing signatures (return type, name and parameter
	 * (all)) of methods
	 * 
	 * @param anotherElement
	 * @return the value 1. if the two elements are equal considering their
	 *         signature, 0. otherwise
	 */
	public double comparringAll(IElement anotherElement) {

		// we need to eliminate space
		String element1S = this.getText().toString().replaceAll("\\s+", "");
		String tmp1 = element1S.substring(0, element1S.indexOf("("));

		String element2S = ((CppElement) anotherElement).getText().toString().replaceAll("\\s+", "");

		String tmp2 = element2S.substring(0, element2S.indexOf("("));

		// System.out.println(tmp1 + " with " + tmp2 + " are " + tmp1.equals(tmp2));

		if (tmp1.equals(tmp2)) {

			return comparringMethodParameters(anotherElement);
		} else {
			return 0.;
		}
	}

	/**
	 * this function for comparing bodies of methods
	 * 
	 * @param anotherElement
	 * @return the value 1. if the two elements are equal considering their bodies,
	 *         0. otherwise
	 */
	public double comparringBodies(IElement anotherElement) {

		ArrayList<IASTNode> tmp = new ArrayList<IASTNode>();

//		tmp = getALLElementsFromASTNode(this.body);
//
//		for (IASTNode iastNode : tmp) {
//		
//				System.out.println(iastNode.getRawSignature());
//		}

		int numberSimilarInst = 0;
		float simPerc = 0;
		int totalOfInst = 0;

		// it s ignored, we return 1. to don't affect the result
		if (bodyC.toString() == "IGNORE") {
			return 1.;
		}
		// to eliminate the empty body behavior, as constructor for example
		ArrayList<IASTNode> currentBodyStatments = SimilarityHelper.getALLElementsFromASTNode(this.body);
		ArrayList<IASTNode> anotherBodyStatments = SimilarityHelper
				.getALLElementsFromASTNode(((Methods) anotherElement).body);
		totalOfInst = currentBodyStatments.size() + anotherBodyStatments.size();
		int maxSize = Math.max(currentBodyStatments.size(), anotherBodyStatments.size());
		if (totalOfInst == 0) {
			return 1.;
		}

		if (Methods.StatementOrderC.toString() == "NOT_ORDER_SENSITIVE") {

			// order not sensitive, we search for instructions that are used in the two body
			// (order doesn't matter)

			for (IASTNode s1 : currentBodyStatments) {
				for (IASTNode s2 : anotherBodyStatments) {
					if (s1.getRawSignature().replaceAll("\\s+", "")
							.equals(s2.getRawSignature().replaceAll("\\s+", ""))) {
						numberSimilarInst++;
					}
				}
			}

			simPerc = (float) numberSimilarInst / maxSize;

		} else {

			int i = 0;
			// this while make a verification in depth and order in the compound because get
			// all element function is recursive so give depth and calculate order give
			// order
			while ((currentBodyStatments.size() > i) && (anotherBodyStatments.size() > i)) {

				if (currentBodyStatments.get(i).getRawSignature().replaceAll("\\s+", "")
						.equals(anotherBodyStatments.get(i).getRawSignature().replaceAll("\\s+", ""))
						&& (SimilarityHelper.calculateOrder(currentBodyStatments.get(i)) == SimilarityHelper
								.calculateOrder(anotherBodyStatments.get(i)))) {
					System.err.println(currentBodyStatments.get(i).getRawSignature() + " with "
							+ anotherBodyStatments.get(i).getRawSignature());
					numberSimilarInst++;
				}
				System.err.println(i);

				i++;
			}

			simPerc = (float) numberSimilarInst / maxSize;
			

		}

		// processing
		if (bodyC.toString() == "SAME") {

			if (simPerc > Methods.maxRangeC) {
				return 0;
			}

			if (simPerc == 1.0) {
				return 1.;
			} else {
				if ((simPerc == 0.0) && (Methods.minRangeC == 0.0) && (Methods.maxRangeC == 0.0)) {
					// System.out.println("\n 0 : " + simPerc);
					return 0.;
				} else {
					// this condition to eliminate who are not 100% similar and 100% different
					if ((simPerc > 0) && (Methods.minRangeC == 0.0) && (Methods.maxRangeC == 0.0)) {
						return 1.;
					} else {
						if ((simPerc >= Methods.minRangeC) && (simPerc <= Methods.maxRangeC)) {
							// System.out.println("\n dif : " + simPerc);
							return 1.;
						} else {
							// System.out.println("\n dif else : " + simPerc);
							return 0.;
						}
					}

				}

			}

		} else {
			// because ths flag is in ignored not same, so we return 1.. The value one will
			// not affect the result
			return 1.;

		}

	}

	/**
	 * Construct the function element in the contained parent file.
	 */
	public void construct(File f) {
		try {
			FileUtils.appendToFile(f, "\n" + this.node.getParent().getParent().getChildren()[0].getRawSignature() + "  "
					+ this.node.getParent().getRawSignature() + ";");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * this function extract words from the function bodies to serve the wordCloud
	 * functionnality
	 * 
	 * @return list of words extracted from this methods
	 */

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

		// this variable because we need to eliminate the words with out meaning such us
		// <=, >>..
		List<String> wordsListC = new ArrayList<String>();

		for (String string : wordsList) {
			if ((string.length() > 2) && (string.length() < 10)) {
				wordsListC.add(string);
			}
		}

		return wordsListC;
	}

}
