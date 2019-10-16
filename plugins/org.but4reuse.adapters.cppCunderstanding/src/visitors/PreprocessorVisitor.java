package visitors;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.but4reuse.adapters.IElement;
import dependencies.DependencyManager;
import elements.CppElement;
import elements.IfDirective;
import elements.ElifDirective;
import elements.ElseDirective;
import elements.EndifDirective;
import elements.IfdefDirective;
import elements.IfndefDirective;
import elements.IncludeDirective;
import elements.MacroDirective;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import activator.Activator;
import preferences.CppCunderstandingPreferencePage;

/**
 * This class contains the element creation logic for all the preprocessor
 * statements (includes, defines, ifdefs ... etc)
 * 
 *
 */

public class PreprocessorVisitor {

	private PreprocessorVisitor() {
	};

	/**
	 * Resolves the creation of preprocessor elements for header files
	 * 
	 * @param headerElement                 the element for the header file
	 * @param elementsList                  the list of existing elements
	 * @param unresolvedIncludeDependencies include dependencies that need be
	 *                                      resolved
	 */
	public static void resolveHeaderPreprocessorElements(CppElement headerElement, List<IElement> elementsList,
			List<CppElement> unresolvedIncludeDependencies) {

		String selection = (Activator.getDefault().getPreferenceStore()
				.getString(CppCunderstandingPreferencePage.SELECTION));
		;

		if (headerElement.getNode() instanceof IASTTranslationUnit) {
			IASTTranslationUnit translationUnit = (IASTTranslationUnit) headerElement.getNode();
			IASTPreprocessorStatement[] statements = translationUnit.getAllPreprocessorStatements();

			for (IASTPreprocessorStatement statement : statements) {

				// include statement, particular behavior for header file
				// (reference
				// dependencies need to be resolved)
				if (statement instanceof IASTPreprocessorIncludeStatement) {

					IASTPreprocessorIncludeStatement include = (IASTPreprocessorIncludeStatement) statement;

					// the last behavior
					if (selection.equals("ALL")) {

						String name = include.getRawSignature();
						CppElement includeElement = new IncludeDirective(include, headerElement, name, name);

						includeElement.addDependency(DependencyManager.CONTAINMENT_DEPENDENCY_ID, headerElement);
						elementsList.add(includeElement);

						// if is a custom header include, resolve reference
						// dependency
						// later
						if (!include.isSystemInclude()) {
							unresolvedIncludeDependencies.add(includeElement);
						}

					} else {
						// the behavior taht i added to understand the variability
						CppElement includeElement = new IncludeDirective(include, headerElement, null, include.getRawSignature());
						if (!(Activator.getDefault().getPreferenceStore()
								.getBoolean(CppCunderstandingPreferencePage.IGNORE_PATH))) {
							File f1 = new File(includeElement.getParent().getNode().getContainingFilename());
							String path1 = includeElement.returnPath(f1) + "~"
									+ includeElement.getNode().getRawSignature();
							if (include.isSystemInclude())
								path1 = path1 + ": SYSTEM";
							includeElement.setText(path1);

						} else {
							includeElement.setText(include.getRawSignature());
						}
						
						elementsList.add(includeElement);
					}
				}

				// common behavior for both header and source files
				else {
					if (selection.equals("ALL")) {
						resolvePreprocessorDirective(statement, headerElement, elementsList);
					}
				}
			}
		} else {
			System.err.println(
					"The given element is not a IASTTranslationUnit but " + headerElement.getNode().getClass());
		}
	}

	/**
	 * Resolves the creation of preprocessor elements for source files
	 * 
	 * @param sourceElement the element for the source file
	 * @param elementsList  the existing list of elements
	 * @param elementsMap   the existing map of elements
	 */

	public static void resolveSourcePreprocessorElements(CppElement sourceElement, List<IElement> elementsList,
			Map<String, CppElement> elementsMap) {

		String selection = (Activator.getDefault().getPreferenceStore()
				.getString(CppCunderstandingPreferencePage.SELECTION));

		if (sourceElement.getNode() instanceof IASTTranslationUnit) {
			IASTTranslationUnit translationUnit = (IASTTranslationUnit) sourceElement.getNode();
			IASTPreprocessorStatement[] statements = translationUnit.getAllPreprocessorStatements();

			for (IASTPreprocessorStatement statement : statements) {

				// include statement, particular behavior for source file
				// (reference
				// dependencies have already been resolved
				if (statement instanceof IASTPreprocessorIncludeStatement) {

					IASTPreprocessorIncludeStatement include = (IASTPreprocessorIncludeStatement) statement;

					// the last behavior
					if (selection.equals("ALL")) {

						String name = include.getRawSignature();
						CppElement includeElement = new IncludeDirective(include, sourceElement, name, name);

						// include dependency
						includeElement.addDependency(DependencyManager.CONTAINMENT_DEPENDENCY_ID, sourceElement);
						elementsList.add(includeElement);

						if (!include.isSystemInclude()) {

							File header = new File(include.getName().toString());

							// reference dependency
							CppElement headerElement = elementsMap.get(header.getName());

							if (headerElement != null) {
								includeElement.addDependency(DependencyManager.REFERENCE_DEPENDENCY_ID, headerElement);
							} else {
								System.err.println(
										"PreprocessorManager: Null reference in resolving reference dependency "
												+ include.getName() + " Destination header : "
												+ sourceElement.getNode().getContainingFilename());
							}
						}
					} else {

						// the behavior if we are in the variability understanding
						CppElement includeElement = new IncludeDirective(include, sourceElement, null, include.getRawSignature());

						if (!(Activator.getDefault().getPreferenceStore()
								.getBoolean(CppCunderstandingPreferencePage.IGNORE_PATH))) {
							File f1 = new File(includeElement.getParent().getNode().getContainingFilename());
							String path1 = includeElement.returnPath(f1) + "~"
									+ includeElement.getNode().getRawSignature();
							if (include.isSystemInclude())
								path1 = path1 + ": SYSTEM";
							includeElement.setText(path1);

						} else {
							includeElement.setText(include.getRawSignature());
						}
						elementsList.add(includeElement);

					}
				}

				// common behavior for both header and source files
				else {
					if (selection.equals("ALL")) {
						resolvePreprocessorDirective(statement, sourceElement, elementsList);
					}
				}
			}
		}

		else {
			System.err.println(
					"The given element is not a IASTTranslationUnit but " + sourceElement.getNode().getClass());
		}

	}

	private static void resolvePreprocessorDirective(IASTPreprocessorStatement statement, CppElement parentElement,
			List<IElement> elementsList) {

		CppElement directiveElement = null;

		if (statement instanceof IASTPreprocessorMacroDefinition) {
			directiveElement = new MacroDirective(statement, parentElement, statement.getRawSignature(),
					statement.getRawSignature());

		} else if (statement instanceof IASTPreprocessorIfdefStatement) {
			directiveElement = new IfdefDirective(statement, parentElement, statement.getRawSignature(),
					statement.getRawSignature());

		} else if (statement instanceof IASTPreprocessorIfndefStatement) {
			directiveElement = new IfndefDirective(statement, parentElement, statement.getRawSignature(),
					statement.getRawSignature());

		} else if (statement instanceof IASTPreprocessorIfStatement) {
			directiveElement = new IfDirective(statement, parentElement, statement.getRawSignature(),
					statement.getRawSignature());

		} else if (statement instanceof IASTPreprocessorElifStatement) {
			directiveElement = new ElifDirective(statement, parentElement, statement.getRawSignature(),
					statement.getRawSignature());

		} else if (statement instanceof IASTPreprocessorElseStatement) {
			directiveElement = new ElseDirective(statement, parentElement, statement.getRawSignature(),
					statement.getRawSignature());
		} else if (statement instanceof IASTPreprocessorEndifStatement) {
			directiveElement = new EndifDirective(statement, parentElement, statement.getRawSignature(),
					statement.getRawSignature());
		}

		if (directiveElement != null) {
			elementsList.add(directiveElement);
			directiveElement.addDependency(DependencyManager.CONTAINMENT_DEPENDENCY_ID, parentElement);
		}
	}

}
