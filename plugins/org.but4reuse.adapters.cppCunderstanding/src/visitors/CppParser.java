package visitors;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.but4reuse.adapters.IElement;
import callhierarchy.doxygen.DoxygenAnalyser;
import elements.CppElement;

import org.but4reuse.utils.files.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.util.ASTPrinter;
import org.eclipse.core.runtime.CoreException;

import activator.Activator;
import preferences.CppCunderstandingPreferencePage;

/**
 * This class uses the CDT plugin in order to parse C++ code . For a specific
 * project we begin by parsing all the header files and constructing class and
 * function elements and then resolving inheritance and containment
 * dependencies.
 * 
 * If the user selected the use of the function call hierarchy via the
 * preference page then we analyze it by using DOXYGEN.
 * 
 * TODO Complete once the parser is finished
 *
 */

public class CppParser {

	public final static String HEADER = "h";
	public final static String SOURCE_CPP = "cpp";
	public final static String SOURCE_C = "c";

	// enforce the static behavior by disallowing elements construction

	public CppParser() {
	};

	public static List<IElement> parse(File root) throws CoreException {

		// parser initialization boilerplate

		Map<String, String> definedSymbols = new HashMap<String, String>();
		String[] includePaths = new String[0];
		IScannerInfo info = new ScannerInfo(definedSymbols, includePaths);
		IParserLogService log = new DefaultLogService();

		IncludeFileContentProvider emptyIncludes = IncludeFileContentProvider.getEmptyFilesProvider();

		int options = 0;

		List<File> files = FileUtils.getAllFiles(root);
		List<File> headerFiles = new ArrayList<File>();
		List<File> sourceFiles = new ArrayList<File>();

		// filter between header files and source files
		for (File file : files) {

			if (FileUtils.isExtension(file, HEADER)) {
				headerFiles.add(file);
			} else if (FileUtils.isExtension(file, SOURCE_CPP) || FileUtils.isExtension(file, SOURCE_C)) {
				sourceFiles.add(file);
			}
		}

		// --------- start visiting the header files

		CppHeaderVisitor headerVisitorAll = new CppHeaderVisitor(true, root);
		;

		String selection = (Activator.getDefault().getPreferenceStore()
				.getString(CppCunderstandingPreferencePage.SELECTION));

		for (File header : headerFiles) {

			FileContent fileContent = FileContent.createForExternalFileLocation(header.getAbsolutePath());

			IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info,
					emptyIncludes, null, options, log);

			translationUnit.accept(headerVisitorAll);
			headerVisitorAll.reset();

		}
		// get all the cpp names

		List<IElement> elementsList = headerVisitorAll.getElementsList();
		Map<String, CppElement> headerElementsMap = headerVisitorAll.getElementsMap();

		if (selection.equals("ALL")) {

			// resolve inheritance dependencies
			headerVisitorAll.resolveInheritanceDependencies();

			// resolve inclusion dependencies (local header files )
			headerVisitorAll.resolveInclusionDependencies();

			// --------- start visiting the source files
			// Option for getASTTranslationUnit(FileContent, IScannerInfo,
			// IncludeFileContentProvider, IIndex, int, IParserLogService)
			// Marks the ast as being based on a source-file rather than a header-file.
			options = ILanguage.OPTION_IS_SOURCE_UNIT;

			CppSourceVisitor sourceVisitor = new CppSourceVisitor(true, elementsList, headerElementsMap, root);
			for (File source : sourceFiles) {

				FileContent fileContent = FileContent.createForExternalFileLocation(source.getAbsolutePath());

				IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info,
						emptyIncludes, null, options, log);
				//ASTPrinter.print(translationUnit);
				translationUnit.accept(sourceVisitor);
				// pour ecraser les anciens donnees
				sourceVisitor.reset();
			}

			elementsList = sourceVisitor.getElementsList();

			// DependencyManager.printElementsWithDependencies(elementsList);
			// function call hierarchy
			boolean useFunctionCallHierarchy = activator.Activator.getDefault().getPreferenceStore()
					.getBoolean(CppCunderstandingPreferencePage.USE_FUNCTION_CALL_HIERARCHY);

			if (useFunctionCallHierarchy) {
				DoxygenAnalyser analyser = new DoxygenAnalyser(root, headerElementsMap);

				analyser.analyse();
			}

			// analyser.dispose();
		} else {
			CppSourceVisitor sourceVisitor = new CppSourceVisitor(true, elementsList, headerElementsMap, root);
			for (File source : sourceFiles) {

				FileContent fileContent = FileContent.createForExternalFileLocation(source.getAbsolutePath());

				IASTTranslationUnit translationUnit = GPPLanguage.getDefault().getASTTranslationUnit(fileContent, info,
						emptyIncludes, null, options, log);

				translationUnit.accept(sourceVisitor);
				sourceVisitor.reset();
			}
			elementsList = sourceVisitor.getElementsList();

		}

		return elementsList;
	}

}
