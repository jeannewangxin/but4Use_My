package construction;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.io.LineIterator;
import org.but4reuse.adapters.IElement;
import org.but4reuse.construction.annotation.ConstructionWithAnnotationHelper;
import org.but4reuse.utils.files.FileUtils;

import elements.CppElement;
import elements.IfDirective;
import elements.StatementImpl;
import elements.CppElement.CppElementType;

public class ConstructionHelper {

	public static ConstructionModal insertAnnotationStart(String block) {
		CppElement ifdef = new IfDirective(null, null, null,
				ConstructionWithAnnotationHelper.USED_ANNOTATION.annotationStart(block));
		ConstructionModal cm = new ConstructionModal(ifdef, block, -1);
		cm.element.setConstructed(false);
		return cm;
	}

	public static ConstructionModal insertAnnotationEnd(String block) {
		CppElement endIfdef = new IfDirective(null, null, null,
				ConstructionWithAnnotationHelper.USED_ANNOTATION.annotationEnd(block));
		ConstructionModal cm = new ConstructionModal(endIfdef, block, -1);
		cm.element.setConstructed(false);
		return cm;
	}

	public static ArrayList<ConstructionModal> getOrderForAllElements(List<IElement> elements, Set<String> blocs) {
		ArrayList<ConstructionModal> orderedElement = new ArrayList<ConstructionModal>();
		ConstructionModal cm;
		for (CppElementType type : CppElementType.values()) {
			for (IElement iElement : elements) {
				if (iElement instanceof CppElement) {
					CppElement element = (CppElement) iElement;
					if (element.getType() == type) {
						cm = new ConstructionModal(element, ConstructionWithAnnotationHelper.MAP_ELEMENTS.get(element),
								ElementConstructionHelper.calculerOrder(type));
						orderedElement.add(cm);
						// getting the existing blocks
						blocs.add(ConstructionWithAnnotationHelper.MAP_ELEMENTS.get(element));

					}
				}
			}

		}
		return orderedElement;
	}

	public static boolean ifdefExistInBottom(Stack s, String cmEnteredBloc) {
		Stack tmp = new Stack();
		tmp = s;
		Iterator it = tmp.iterator();
		ConstructionModal cm = null;
		if (it.hasNext()) {
			cm = (ConstructionModal) it.next();

		}

		if (cmEnteredBloc.equals(cm.block)) {

			return true;
		} else {

			return false;
		}
	}

	public static void constructAllFiles(HashMap<String, Stack> stacks, URI uri) throws Exception {
		Stack tmp = new Stack();
		Iterator it = stacks.entrySet().iterator();
		Iterator iterator;
		File tmpFile;
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();

			tmp = (Stack) pair.getValue();
			// we should construct the file, using the name witch is the key of the stack
			tmpFile = constructFile((String) pair.getKey(), uri);
			if (tmpFile != null) {
				iterator = tmp.iterator();
				while (iterator.hasNext()) {
					Object object = iterator.next();

					if (((ConstructionModal) object).element.getConstructed() == false) {

						if (((ConstructionModal) object).order == -1) {
							if (iterator.hasNext()) {
								FileUtils.appendToFile(tmpFile, ((ConstructionModal) object).element.getRawText());

							} else {
								if (FileUtils.getExtension(tmpFile).equals("h"))
									FileUtils.appendToFile(tmpFile, "\n};\n#endif");
								FileUtils.appendToFile(tmpFile, ((ConstructionModal) object).element.getRawText());
							}
						} else {

							((ConstructionModal) object).element.construct(tmpFile);
						}

					}
					// ((ConstructionModal) object).element.setConstructed(true);

				}

			} else {
				System.err.println("\nfile not constructed");
			}
		}

	}

	private static File constructFile(String name, URI uri) {

		// Use the given file or use a default name if a folder was given
		if (uri.toString().endsWith("/")) {
			try {
				uri = new URI(uri.toString() + name);

				// Create the file if it does not exist
				File file;
				file = FileUtils.getFile(uri);
				FileUtils.createFile(file);
				return file;

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		} else {
			return null;
		}
	}

	public static void initializeStructure(List<IElement> elements) {
		HashMap<String, StatementImpl> statements = new HashMap<String, StatementImpl>();
		for (IElement iElement : elements) {
			if (((CppElement) iElement).getType() == CppElementType.FUNCTION_IMPL) {

			}

			if (((CppElement) iElement).getType() == CppElementType.STATEMENT_IMPL) {

			}
		}
	}

}
