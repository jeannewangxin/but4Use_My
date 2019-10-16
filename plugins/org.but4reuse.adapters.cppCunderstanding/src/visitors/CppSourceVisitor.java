package visitors;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.but4reuse.adapters.IElement;
import callhierarchy.xml.FunctionSignatureParser;
import construction.SimilarityHelper;
import dependencies.DependencyManager;
import elements.Comment;
import elements.CppElement;
import elements.Field;
import elements.Methods;
import elements.SourceFile;
import elements.StatementImpl;
import elements.TypeDefStruct;
import elements.UsingNSpaceDir;
import elements.FunctionImpl;
import elements.GlobalVar;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.model.IFunctionDeclaration;

import preferences.CppCunderstandingPreferencePage;
import activator.Activator;

/**
 * This class is a visitor for the C++ source files. It creates the initial
 * elements from method names. It resolves containment and reference
 * dependencies.
 * 
 */

public class CppSourceVisitor extends ASTVisitor {

	/** List of elements created by tree traversing. */
	private List<IElement> elementsList;

	/** Element name associated to it's respective CppElement representation. */
	private Map<String, CppElement> elementsMap;

	/**
	 * Statements map used for resolving parenthood link for statement elements.
	 */
	private Map<IASTNode, CppElement> statementsMap;

	/** String Buffer used for parameter concatenation. */
	private StringBuffer parameters;

	/** Source element used for function definition dependency. */
	private CppElement sourceFileElement;

	/** Function element used for detecting global variables */
	private CppElement sourceFunctionElement;

	private File rootFile;

	// this variable to get the selection from preference page
	private String selection;

	public CppSourceVisitor(boolean visitAll, List<IElement> elementsList, Map<String, CppElement> elementsMap,
			File root) {
		super(visitAll);
		rootFile = root;
		this.elementsList = elementsList;
		this.elementsMap = elementsMap;
		this.statementsMap = new HashMap<IASTNode, CppElement>();
		parameters = new StringBuffer();
		selection = (Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.SELECTION));
	}

	/**
	 * Resets the internal data used for class and element linkage. This method
	 * should be called before a new file parsing.
	 */
	public void reset() {
		sourceFileElement = null;
		sourceFunctionElement = null;
	}

	/**
	 * Provides the full list of created elements.
	 * 
	 * @return a list of created elements.
	 */
	public List<IElement> getElementsList() {
		return elementsList;
	}

	// here ion this function we recognize the .cpp file and we treat just include
	// statements
	@Override
	public int visit(IASTTranslationUnit translationUnit) {

		// source file node
		File file = new File(translationUnit.getContainingFilename());
		String relativePath = file.getName();// rootFile.toURI().relativize(file.toURI()).getPath();

		CppElement sourceElement = new SourceFile(translationUnit, null, relativePath, relativePath);
		sourceFileElement = sourceElement;

		// sourceElement.setFile(new
		// File(rootFile.toURI().relativize(file.toURI()).getPath()));
		if (selection.equals("ALL")) {

			elementsList.add(sourceElement);

			PreprocessorVisitor.resolveSourcePreprocessorElements(sourceElement, elementsList, elementsMap);

			// bringing comments elements
			if (Activator.getDefault().getPreferenceStore()
					.getBoolean(CppCunderstandingPreferencePage.COMMENT) == true) {
				for (IASTComment comment : translationUnit.getComments()) {

					CppElement commentElement = new Comment(comment, sourceFileElement, comment.getRawSignature(),
							comment.getRawSignature());
					elementsList.add(commentElement);

				}
			}
		} else {

			if (Activator.getDefault().getPreferenceStore().getBoolean(CppCunderstandingPreferencePage.IMPORTS)) {
				PreprocessorVisitor.resolveSourcePreprocessorElements(sourceElement, elementsList, elementsMap);
			}
		}

		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTDeclaration declaration) {
		if (selection.equals("ALL")) {

			// if the using name space is used in the header file
			if (declaration instanceof ICPPASTUsingDirective) {
				// we add cppElement of type typedef using name space directive
				UsingNSpaceDir uns = new UsingNSpaceDir(declaration, sourceFileElement, declaration.getRawSignature(),
						declaration.getRawSignature());
				elementsList.add(uns);
			}

			if (declaration instanceof IASTFunctionDefinition) {

				IASTFunctionDefinition functionDefinition = (IASTFunctionDefinition) declaration;
				ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) functionDefinition
						.getDeclarator();

				IASTNode[] children = functionDeclarator.getChildren();

				// reset the parameter list
				parameters.setLength(0);

				// compute the parameter type list
				for (IASTNode child : children) {
					if (child instanceof IASTParameterDeclaration) {
						IASTParameterDeclaration parameter = (IASTParameterDeclaration) child;
						IASTDeclSpecifier declSpecifier = parameter.getDeclSpecifier();
						String type = declSpecifier.getRawSignature();
						parameters.append(FunctionSignatureParser.TYPE_SEPARATOR + type);
					}
				}

				String functionElementKey = functionDeclarator.getName().toString().trim() + parameters;
				String functionElementName = functionElementKey + CppElement.IMPL_EXTENSION;
				CppElement functionElement = new FunctionImpl(declaration, sourceFileElement, functionElementName,
						functionElementKey);

				sourceFunctionElement = functionElement;

				elementsList.add(functionElement);

				// the first statement of the function will depend on this
				// element
				statementsMap.put(declaration, functionElement);

				CppElement functionHeaderElement = elementsMap.get(functionElementKey);

				functionElement.addDependency(DependencyManager.CONTAINMENT_DEPENDENCY_ID, sourceFileElement);

				if (functionHeaderElement != null) {
					functionElement.addDependency(DependencyManager.HEADER_DEPENDENCY_ID, functionHeaderElement);
				} else {
					System.err.println(
							"CppSourceVisitor: No header element found for function name: " + functionElementName);
				}

				String newKey = functionElementKey + CppElement.IMPL_EXTENSION;
				elementsMap.put(newKey, functionElement);
			}
			// possible global variable
			else if (declaration instanceof IASTSimpleDeclaration && sourceFunctionElement == null) {
				IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
				IASTDeclSpecifier specifier = simpleDeclaration.getDeclSpecifier();

				if (!(specifier instanceof IASTCompositeTypeSpecifier)
						&& specifier.getStorageClass() == IASTDeclSpecifier.sc_unspecified) {

					CppElement globalVariableElement = new GlobalVar(declaration, sourceFileElement,
							declaration.getRawSignature(), declaration.getRawSignature());
					elementsList.add(globalVariableElement);

					globalVariableElement.addDependency(DependencyManager.CONTAINMENT_DEPENDENCY_ID, sourceFileElement);
				}
			}
		} else {
			if (Activator.getDefault().getPreferenceStore().getBoolean(CppCunderstandingPreferencePage.METHODS)) {

				if (declaration instanceof IASTFunctionDefinition) {

					// we get the node
					IASTNode node = (IASTNode) declaration;

					IASTFunctionDefinition functionDefinition = (IASTFunctionDefinition) declaration;

					ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) functionDefinition
							.getDeclarator();

					// the return type
					String returnType = functionDeclarator.getParent().getChildren()[0].getRawSignature();

					// the name of the function
					String functionName;
					if (functionDeclarator.getParent().getChildren()[1].getChildren()[0].getRawSignature()
							.contains(":")) {
						String tmp = functionDeclarator.getParent().getChildren()[1].getChildren()[0].getRawSignature()
								.replaceFirst(":", "");
						functionName = tmp.substring(tmp.indexOf(":") + 1);

					} else {
						functionName = functionDeclarator.getParent().getChildren()[1].getChildren()[0]
								.getRawSignature();
					}

					// the parameters

					IASTNode[] children = functionDeclarator.getChildren();

					// reset the parameter list
					parameters.setLength(0);

					// compute the parameter type list
					for (IASTNode child : children) {
						if (child instanceof IASTParameterDeclaration) {
							IASTParameterDeclaration parameter = (IASTParameterDeclaration) child;
							IASTDeclSpecifier declSpecifier = parameter.getDeclSpecifier();
							String type = declSpecifier.getRawSignature();
							parameters.append(FunctionSignatureParser.TYPE_SEPARATOR + type);
						}
					}

					// the body
					IASTStatement body = ((IASTFunctionDefinition) declaration).getBody();

					// creating the element
					CppElement functionHeader = new Methods(node, sourceFileElement,
							returnType + " " + functionDeclarator.getRawSignature(), node.getRawSignature(), null,
							returnType, functionName, this.parameters, body);

					if (!(Activator.getDefault().getPreferenceStore()
							.getBoolean(CppCunderstandingPreferencePage.IGNORE_PATH))) {
						File f1 = new File(functionHeader.getParent().getNode().getContainingFilename());
						String path1 = functionHeader.returnPath(f1) + " ~ " + returnType + " "
								+ functionDeclarator.getRawSignature();
						((Methods) functionHeader).setText(path1);
					}
					elementsList.add(functionHeader);
					elementsMap.put(node.getRawSignature() + "~" + sourceFileElement.getRawText(), functionHeader);
				}
			}
		}

		return PROCESS_CONTINUE;
	}

	public int visit(IASTStatement statement) {

		if (selection.equals("ALL")) {

			if ((statement.getParent().getClass().getName().equals(SimilarityHelper.CPPASTForStatement) == false)) {
				IASTNode parent = statement.getParent();
				String statementName = statement.getRawSignature() + CppElement.IMPL_EXTENSION;
				CppElement parentElement = null;

				if (parent != null) {
					parentElement = statementsMap.get(parent);

				} else {
					System.err.println("Null parent for " + statementName);
				}

				int order = SimilarityHelper.calculateOrder(statement);
				CppElement statementElement ;
				if (statement.getClass().getName().equals(SimilarityHelper.CPPASTCompoundStatement)) {
					statementElement= new  StatementImpl(statement, parentElement,
							"{body of the bellow instructions}", statement.getRawSignature(), order);
				}else {
					statementElement= new  StatementImpl(statement, parentElement,
							statementName + Integer.toString(order), statement.getRawSignature(), order);
				}

				elementsList.add(statementElement);
				statementsMap.put(statement, statementElement);

				if (parentElement != null) {
					statementElement.addDependency(DependencyManager.STATEMENT_DEPENDENCY_ID, parentElement);
				} else {
					System.err.println("Null parent element for statement " + statementName);
				}
			}
		}
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTDeclarator declarator) {

		if (!(selection.equals("ALL"))) {

			// check if field is checked
			if (Activator.getDefault().getPreferenceStore().getBoolean(CppCunderstandingPreferencePage.FIELDS)) {
				// the condition to check that it s a field in a class
				if ((declarator.getPropertyInParent().toString()
						.equals("IASTSimpleDeclaration.DECLARATOR - IASTDeclarator for IASTSimpleDeclaration"))
						&& !(declarator.getRawSignature().contains("("))) {

					// we get the correct declaration that contains all informations
					IASTNode node = (IASTNode) declarator.getParent();
					// we get the necessary information
					String type = node.getChildren()[0].getRawSignature();

					String id = declarator.getChildren()[0].getRawSignature();

					String value = "";

					try {
						value = new String(declarator.getChildren()[1].getRawSignature().replaceFirst("[ *]", ""));
					} catch (ArrayIndexOutOfBoundsException e) {

					}

					// creating the element
					CppElement field = new Field(node, sourceFileElement, type + " " + id, node.getRawSignature(), null,
							type, id, value);

					if (!(Activator.getDefault().getPreferenceStore()
							.getBoolean(CppCunderstandingPreferencePage.IGNORE_PATH))) {
						File f1 = new File(sourceFileElement.getNode().getContainingFilename());
						field = new Field(node, sourceFileElement, field.returnPath(f1) + "~" + type + " " + id,
								node.getRawSignature(), null, type, id, value);
					}

					elementsList.add(field);
					elementsMap.put(node.getRawSignature(), field);
				}

			}
		}

		return PROCESS_CONTINUE;
	}

	private String creatText(ICPPASTFunctionDeclarator fd) {
		return "";
	}

}
