package construction;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTWhileStatement;

public class SimilarityHelper {

	public static String CPPASTFunctionDefinition = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition";

	public static String CPPASTCompoundStatement = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement";

	public static String CPPASTExpressionStatement = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement";

	public static String CPPASTDeclarationStatement = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement";

	public static String CPPASTReturnStatement = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement";

	public static String CPPASTForStatement = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTForStatement";

	public static String CPPASTWhileStatement = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTWhileStatement";

	public static String CPPASTIfStatement = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement";

	public static String CPPASTSwitchStatement = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSwitchStatement";

	// leur fils
	public static String CPPASTBinaryExpression = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression";

	/*
	 * take a statement and return their function parent, and his order in the sens
	 * of conpound statement
	 */
	public static IASTFunctionDefinition getFunctionNameParent(IASTNode element) {

		while ((element.getParent() != null)) {

			if (element.getParent().getClass().getName().equals(SimilarityHelper.CPPASTFunctionDefinition)) {

				return (IASTFunctionDefinition) element.getParent();
			}

			element = element.getParent();
		}
		return null;

	}

	public static int calculateOrder(IASTNode element) {
		int tmp = 0;

		while ((element.getParent() != null)) {

			if (element.getParent().getClass().getName().equals(SimilarityHelper.CPPASTCompoundStatement)) {
				tmp++;
				// System.err.println("\n order " + order + " pour "+
				// element.getRawSignature());

			}
			if (element.getParent().getClass().getName().equals(SimilarityHelper.CPPASTFunctionDefinition)) {

				return tmp;
			}

			element = element.getParent();
		}
		return 0;

	}

	public static ArrayList<IASTNode> getALLElementsFromASTNode(IASTNode node) {

		ArrayList<IASTNode> elements = new ArrayList<IASTNode>();
		if ((node instanceof CPPASTExpressionStatement) || (node instanceof CPPASTDeclarationStatement)
				|| (node instanceof CPPASTReturnStatement) || (node instanceof CPPASTForStatement)
				|| (node instanceof CPPASTWhileStatement) || (node instanceof CPPASTIfStatement)
				|| (node instanceof CPPASTSwitchStatement)) {

			elements.add(node);
		}
		for (IASTNode n : node.getChildren()) {
			elements.addAll(getALLElementsFromASTNode(n));

		}
		return elements;
	}

}
