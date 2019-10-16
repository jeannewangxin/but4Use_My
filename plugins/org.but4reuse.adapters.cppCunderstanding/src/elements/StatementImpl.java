package elements;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.but4reuse.adapters.IElement;
import org.but4reuse.utils.strings.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;

import callhierarchy.xml.FunctionSignatureParser;
import construction.SimilarityHelper;

/**
 * This class contains the C++ element implementation for statement node.
 * Example : int x = 40;
 * 
 */

public class StatementImpl extends CppElement {

	// this variable to save the deapth of an instruction inside the body, using the
	// compound class
	protected int order = 0;

	public StatementImpl(IASTNode node, CppElement parent, String text, String rawText, int order) {
		super(node, parent, text, rawText, CppElementType.STATEMENT_IMPL);
		this.order = order;
		words = extractWords();

	}

	@Override
	public double similarity(IElement anotherElement) {

		if (((CppElement) anotherElement).getType() == CppElementType.STATEMENT_IMPL) {
			if (this.node.getClass() != ((CppElement) anotherElement).getNode().getClass()) {
				return 0;
			} else {
				// we are sure that same class
				if ((this.node.getClass().getName().equals(SimilarityHelper.CPPASTDeclarationStatement))
						|| (this.node.getClass().getName().equals(SimilarityHelper.CPPASTExpressionStatement))
						|| ((this.node.getClass().getName().equals(SimilarityHelper.CPPASTReturnStatement)))) {
					return similarityInstruction(anotherElement);
				} else {
					// we compare the the compaound class type, {}
					if (this.node.getClass().getName().equals(SimilarityHelper.CPPASTCompoundStatement)) {

						return similarityCompound(anotherElement);
					} else {

						// comparing other type as if for and while

						// for the if , if esle, while and switch statements we have the same behavior
						// because the binary expression in in the first child
						if ((this.node.getClass().getName().toString().equals(SimilarityHelper.CPPASTIfStatement))
								|| (this.getClass().getName().toString()
										.equals(SimilarityHelper.CPPASTWhileStatement.toString()))
								|| (this.node.getClass().toString().equals(SimilarityHelper.CPPASTSwitchStatement))) {
							System.err.println("\n yes");
							return similartyBinaryExpression(anotherElement);
						}

						// pour l'instruction for, on a separer car la codition est dans le deuxieme
						// child dans ce noeud
						if (this.getClass().getName().equals(SimilarityHelper.CPPASTForStatement)) {
							return similarityForExpression(anotherElement);
						}
						// default one, pour ne pas fausser la similarité au moins (des instruction
						// quand consider pas)
						return 1.;
					}
				}
			}

		} else {
			return 0;
		}
	}

	private double similarityInstruction(IElement anotherElement) {

		if (this.getRawText().equals(((CppElement) anotherElement).getRawText()) == false) {
			return 0;
		} else {
			// verife le grand parent qui est la fonction

			IASTFunctionDefinition p1 = SimilarityHelper.getFunctionNameParent(this.getNode());
			IASTFunctionDefinition p2 = SimilarityHelper.getFunctionNameParent(((CppElement) anotherElement).getNode());

			if ((p1 != null) && (p2 != null)) {

				ICPPASTFunctionDeclarator d1 = (ICPPASTFunctionDeclarator) p1.getDeclarator();
				ICPPASTFunctionDeclarator d2 = (ICPPASTFunctionDeclarator) p1.getDeclarator();
				if ((d1.getName().toString().equals(d2.getName().toString()) == false)
						|| (this.order != ((StatementImpl) anotherElement).order)) {

					// because they don not have the same function parent, or they are not in the
					// same level
					return 0;
				} else {
					// verifier order
					return 1;
				}

			} else {
				return 0;
			}
		}
	}

	private double similarityCompound(IElement anotherElement) {
		IASTFunctionDefinition p1 = SimilarityHelper.getFunctionNameParent(this.getNode());
		IASTFunctionDefinition p2 = SimilarityHelper.getFunctionNameParent(((CppElement) anotherElement).getNode());
		ICPPASTFunctionDeclarator d1 = (ICPPASTFunctionDeclarator) p1.getDeclarator();
		ICPPASTFunctionDeclarator d2 = (ICPPASTFunctionDeclarator) p2.getDeclarator();

		if ((p1 != null) && (p2 != null)) {
			// we check just if they have same parent function
			if ((d1.getName().toString().equals(d2.getName().toString()) == false)) {
				// because they don not have the same function parent

				return 0.;
			} else {
				if ((this.order == ((StatementImpl) anotherElement).order)
				/*
				 * && (this.getRawText().equals(((StatementImpl) anotherElement).getRawText()))
				 * == true
				 */) {
					return 1.;
				} else {

					return 0;
				}
			}

		} else {
			return 0;
		}
	}

	private double similartyBinaryExpression(IElement anotherElement) {
		IASTFunctionDefinition p1 = SimilarityHelper.getFunctionNameParent(this.getNode());
		IASTFunctionDefinition p2 = SimilarityHelper.getFunctionNameParent(((CppElement) anotherElement).getNode());
		ICPPASTFunctionDeclarator d1 = (ICPPASTFunctionDeclarator) p1.getDeclarator();
		ICPPASTFunctionDeclarator d2 = (ICPPASTFunctionDeclarator) p2.getDeclarator();

		if ((p1 != null) && (p2 != null)) {
			// we check just if they have same parent function
			if ((d1.getName().toString().equals(d2.getName().toString()) == false)) {
				// because they don not have the same function parent
				return 0.;
			} else {
				String binariE1 = this.getNode().getChildren()[0].getRawSignature();
				String binariE2 = ((CppElement) anotherElement).getNode().getChildren()[0].getRawSignature();
				if ((this.order == ((StatementImpl) anotherElement).order)
						&& (binariE1.toString().equals(binariE2.toString()))) {
					// normally we nedd to verify depth in the compound
					return 1.;
				} else {

					return 0;
				}
			}

		} else {
			return 0;
		}
	}

	private double similarityForExpression(IElement anotherElement) {
		// TODO Auto-generated method stub
		IASTFunctionDefinition p1 = SimilarityHelper.getFunctionNameParent(this.getNode());
		IASTFunctionDefinition p2 = SimilarityHelper.getFunctionNameParent(((CppElement) anotherElement).getNode());
		ICPPASTFunctionDeclarator d1 = (ICPPASTFunctionDeclarator) p1.getDeclarator();
		ICPPASTFunctionDeclarator d2 = (ICPPASTFunctionDeclarator) p2.getDeclarator();

		if ((p1 != null) && (p2 != null)) {
			// we check just if they have same parent function
			if ((d1.getName().toString().equals(d2.getName().toString()) == false)) {
				// because they don not have the same function parent
				return 0.;
			} else {
				String binariE1 = this.getNode().getChildren()[1].getRawSignature();
				String binariE2 = ((CppElement) anotherElement).getNode().getChildren()[1].getRawSignature();
				if ((this.order == ((StatementImpl) anotherElement).order)
						&& (binariE1.toString().equals(binariE2.toString()))) {
					// normally we nedd to verify depth in the compound
					return 1.;
				} else {
					return 0;
				}
			}

		} else {
			return 0;
		}

	}

	private List<String> extractWords() {

		// add the words for the word cloud
		List<String> wordsList = new ArrayList<String>();

		// we are only interested in the name
		String[] tokens = rawText.split("\"");

		if (tokens.length == 1) {
			wordsList.addAll(StringUtils.tokenizeString(tokens[0]));
		} else {
			wordsList.addAll(StringUtils.tokenizeString(tokens[1]));
		}

		return wordsList;
	}

	public void construct(URI uri, String block) {

	}

}
