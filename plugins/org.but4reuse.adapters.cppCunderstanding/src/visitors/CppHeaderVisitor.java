package visitors;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.but4reuse.adapters.IElement;
import callhierarchy.xml.FunctionSignatureParser;
import dependencies.DependencyManager;
import elements.Field;
import elements.ClassHeader;
import elements.Comment;
import elements.CppElement;
import elements.Methods;
import elements.GlobalVar;
import elements.HeaderFile;
import elements.TypeDefStruct;
import elements.UsingNSpaceDir;
import utils.Pair;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;

import activator.Activator;
import preferences.CppCunderstandingPreferencePage;

/**
 * This class is a visitor for the C++ header files. It creates the initial
 * elements from class names and method names. It resolves containment,
 * inheritance and reference dependencies.
 * 
 */

public class CppHeaderVisitor extends ASTVisitor {

	/** list of elements created by tree traversing. */
	private List<IElement> elementsList;

	/** header element used for class definition dependency. */
	private CppElement headerFileElement;

	/** parent class name used for a function definition dependency. */
	private String parentClassName;

	/** parent class element used for a function definition dependency. */
	private CppElement parentClassElement;

	/**
	 * The problem with inheritance dependencies is that we might parse a class B
	 * that inherits from class A before the class A has been parsed and its
	 * respective element has been created. So we memorize all the unresolved
	 * dependencies and resolve them when the whole AST has been created. The same
	 * goes for header inclusion and references.
	 */

	/** Class A inherits from Class B <Class A, Class B> . */
	private List<Pair<String, String>> unresolvedInheritanceDependecies;

	/** Include elements that need to be referenced to the original .h file. */
	private List<CppElement> unresolvedIncludeDependencies;

	/**
	 * Element name associated to it's respective CppElement representation.
	 */
	private Map<String, CppElement> elementsMap;

	/** String Buffer used for parameter concatenation. */
	private StringBuffer parameters;

	private File rootFile;

	// thios variable to get the selection from preference page
	private String selection;

	public CppHeaderVisitor(boolean visitAllNodes, File root) {
		super(true);
		rootFile = root;
		elementsList = new ArrayList<IElement>();
		unresolvedInheritanceDependecies = new LinkedList<Pair<String, String>>();
		unresolvedIncludeDependencies = new LinkedList<CppElement>();
		elementsMap = new HashMap<String, CppElement>();
		parameters = new StringBuffer();

		reset();

		selection = (Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.SELECTION));
	}

	/** Returns a list of the function and class elements. */
	public List<IElement> getElementsList() {
		return elementsList;
	}

	/**
	 * Returns a map (key: name, value: element) of the function and class elements.
	 */
	public Map<String, CppElement> getElementsMap() {
		return elementsMap;
	}

	/**
	 * Resets the internal data used for class and element linkage. This method
	 * should be called before a new file parsing.
	 */
	public void reset() {
		headerFileElement = null;
		parentClassName = null;
		parentClassElement = null;
	}

	// commented by yassine this function treat the .h files by extracting the .h
	// file as
	// CppElements and also all elements such as
	// all preprocessing directives (includes has specific behavior we ness to add
	// dependancy,
	// #ifdef, ....)
	@Override
	public int visit(IASTTranslationUnit translationUnit) {

		// header file node
		File file = new File(translationUnit.getContainingFilename());

		String relativePath = file.getName();// rootFile.toURI().relativize(file.toURI()).getPath();
		// new java.util.Scanner(System.in).nextLine();
		CppElement headerElement = new HeaderFile(translationUnit, null, relativePath, relativePath);
		// headerElement.setFile(new
		// File(rootFile.toURI().relativize(file.toURI()).getPath()));
		headerFileElement = headerElement;

		// System.out.println("\n header elements " + headerElement);

		if (selection.equals("ALL")) {
			elementsList.add(headerElement);
			elementsMap.put(file.getName(), headerElement);
			PreprocessorVisitor.resolveHeaderPreprocessorElements(headerElement, elementsList,
					unresolvedIncludeDependencies);

			// bringing comments elements
			if (Activator.getDefault().getPreferenceStore()
					.getBoolean(CppCunderstandingPreferencePage.COMMENT) == true) {
				for (IASTComment comment : translationUnit.getComments()) {

					CppElement commentElement = new Comment(comment, parentClassElement, comment.getRawSignature(),
							comment.getRawSignature());
					elementsList.add(commentElement);

				}
			}
		} else {
			elementsMap.put(file.getName(), headerElement);
			if (Activator.getDefault().getPreferenceStore().getBoolean(CppCunderstandingPreferencePage.IMPORTS)) {
				PreprocessorVisitor.resolveHeaderPreprocessorElements(headerElement, elementsList,
						unresolvedIncludeDependencies);
			}
		}

		return PROCESS_CONTINUE;
	}

	// cette fonction traite les classe , les heritage les methodes et les champs
	//

	@Override
	public int visit(IASTName name) {
	
		
		
		if (selection.equals("ALL")) {

			// class node
			if ((name.getParent() instanceof IASTCompositeTypeSpecifier)) {

				// name.getrawsignature give us the name of the class founded in this header
				// file
				String classElementName = name.getRawSignature() + CppElement.H_EXTENSION;

				CppElement classElement = new ClassHeader(name, headerFileElement, classElementName,
						name.getRawSignature());
				// we add a class element to the elementsList
				elementsList.add(classElement);

				// class name and element used for contained functions
				parentClassName = name.getRawSignature().trim();
				parentClassElement = classElement;

				// class name and element used for class inheritance dependencies
				elementsMap.put(parentClassName, parentClassElement);

				// containment dependency
				classElement.addDependency(DependencyManager.CONTAINMENT_DEPENDENCY_ID, headerFileElement);
			}

			// inheritance node
			else if (name.getParent() instanceof ICPPASTBaseSpecifier) {

				// inheritance link that needs to be resolved
				unresolvedInheritanceDependecies.add(new Pair<String, String>(parentClassName, name.getRawSignature()));
			}

			// function declaration node ; yassine comment the and condition because the
			// parent will
			// be either instance of ICPPASTCompositeTypeSpecifier or IASTFunctionDeclarator
			// and theres no relation
			// between them (no one is the subInterface of other)
			else if ((name.getParent() instanceof IASTFunctionDeclarator) /*
																			 * && (name.getParent() instanceof
																			 * ICPPASTCompositeTypeSpecifier)
																			 */ ) {

				IASTFunctionDeclarator declarator = (IASTFunctionDeclarator) (name.getParent());
				IASTNode[] children = declarator.getChildren();

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

				// the functionElementKey is the function signature

				String functionElementKey = (parentClassName + "::" + name.getRawSignature()).trim() + parameters;
				String functionElementName = functionElementKey + CppElement.H_EXTENSION;

				CppElement functionElement = new Methods(name, parentClassElement, functionElementName,
						functionElementKey, null, null, null, null, null);
				
				elementsList.add(functionElement);

				elementsMap.put(functionElementKey, functionElement);

				// function containment dependency
				functionElement.addDependency(DependencyManager.CONTAINMENT_DEPENDENCY_ID, parentClassElement);

			}
			// this else is for attribute of classes
			else if ((name.getParent() instanceof CPPASTDeclarator)
					&& (name.getParent().getParent().getParent() instanceof ICPPASTCompositeTypeSpecifier)) {
				CPPASTDeclarator declarator = (CPPASTDeclarator) (name.getParent());

				String attributeElementKey = parentClassName + " "
						+ name.getParent().getParent().getRawSignature().split(" ")[0] + " " + name.getRawSignature();
				String attributeElementName = attributeElementKey + CppElement.H_EXTENSION;

				CppElement attributeElement = new Field(name, parentClassElement, attributeElementName,
						attributeElementKey, null, null, null, null);
				
				
				

				elementsList.add(attributeElement);
				elementsMap.put(attributeElementKey, attributeElement);
			}
		}

		return PROCESS_CONTINUE;
	}

	// the goal of this visitor as it is, is to add global variable declaration in
	// the listElements
	// the global variable are found in the .h file
	@Override
	public int visit(IASTDeclaration declaration) {
	
		
		
		if (selection.equals("ALL")) {

			// if the using name space is used in the header file
			if (declaration instanceof ICPPASTUsingDirective) {
				// we add cppElement of type typedef using name space directive
				UsingNSpaceDir uns = new UsingNSpaceDir(declaration, headerFileElement, declaration.getRawSignature(),
						declaration.getRawSignature());
				elementsList.add(uns);
			}

			// simple declaration with no parent class (does not suffice for
			// detecting
			// global variables since this might be a class definition, typedef etc)

			// System.out.println("Declaration is 1: " + declaration.getRawSignature());

			// if(declaration instanceof IASTSimpleDeclaration && parentClassElement !=
			// null) {
			// System.out.println("Declaration IASTSimpleDeclaration: " +
			// declaration.getRawSignature());
			// }

			if (declaration instanceof IASTSimpleDeclaration) { // && parentClassElement != null) {

				IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
				// System.out.println("Declaration is 2: " + simpleDeclaration.getRawSignature()
				// + "\t TYPE: " + simpleDeclaration.getDeclSpecifier());

				IASTDeclSpecifier specifier = simpleDeclaration.getDeclSpecifier();

				// System.out.println("Specifier: " + specifier + " \t TYPE: " +
				// specifier.getClass());

				// if (!(specifier instanceof IASTCompositeTypeSpecifier)
				// && specifier.getStorageClass() == IASTDeclSpecifier.sc_unspecified) {

				// works for int, void, boolean defs(), but not for struct defs()..., or
				// vice-versa with !().
				// for class, return type .. always its 0 so the next if blocks will not :
				// unspecified , for typedef is 1,
				// extern is 2 ...
				if (!(specifier.getStorageClass() == IASTDeclSpecifier.sc_unspecified)) {
					// global variable
					if (specifier.getStorageClass() == IASTDeclSpecifier.sc_extern) {

						CppElement globalVariableElement = new GlobalVar(declaration, headerFileElement,
								declaration.getRawSignature(), declaration.getRawSignature());
						// System.out.println("\n global elements " +
						// globalVariableElement.getRawText());

						elementsList.add(globalVariableElement);

						// System.out.println("Global Variable Element: " + globalVariableElement);

						globalVariableElement.addDependency(DependencyManager.CONTAINMENT_DEPENDENCY_ID,
								headerFileElement);
					} else if (specifier.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
						// we add cppElement of type typedef struct
						TypeDefStruct struct = new TypeDefStruct(declaration, headerFileElement,
								declaration.getRawSignature(), declaration.getRawSignature());
						elementsList.add(struct);
					}
				}
			}
		}

		return PROCESS_CONTINUE;
	}

	public void resolveInheritanceDependencies() {
		if (selection.equals("ALL")) {
			DependencyManager.resolveInheritanceDependencies(unresolvedInheritanceDependecies, elementsMap);
		}
	}

	public void resolveInclusionDependencies() {
		if (selection.equals("ALL")) {
			DependencyManager.resolveIncludeDependencies(unresolvedIncludeDependencies, elementsMap);
		}
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
					CppElement field = new Field(node, headerFileElement, type + " " + id, node.getRawSignature(), null,
							type, id, value);

					if (!(Activator.getDefault().getPreferenceStore()
							.getBoolean(CppCunderstandingPreferencePage.IGNORE_PATH))) {
						File f1 = new File(headerFileElement.getNode().getContainingFilename());
						field = new Field(node, headerFileElement, field.returnPath(f1) + "~" + type + " " + id,
								node.getRawSignature(), null, type, id, value);
					}

					elementsList.add(field);
					elementsMap.put(node.getRawSignature(), field);
				}

			}

		}
		return PROCESS_CONTINUE;
	}
	

}
