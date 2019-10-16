package construction;

import java.util.ArrayList;

import elements.CppElement;
import elements.CppElement.CppElementType;

public class ElementConstructionHelper {

	public static int calculerOrder(CppElementType t) {
		switch (t) {

		// for the .h
		case IFNDEF_DIR:
			return 1;

		case MACRO_DIR:
			return 2;

		case GLOBAL_VAR:
			return 3;

		case USING_NAME_SPACE_DIR:
			return 3;

		case CLASS_H:
			return 4;

		case ATTRIBUTE_H:
			return 5;

		case FUNCTION_H:
			return 6;

		case ENDIF_DIR:
			return 7;

		// for cpp element
		case INCLUDE_DIR:
			return 3;

		case FUNCTION_IMPL:
			return 6;

		case STATEMENT_IMPL:
			return 7;

		default:
			return 0;

		}

	}

	// this function verify if 'parent' is parent of the node 'fils'
	public static boolean parent(CppElement fils, CppElement parent) {
		while (fils.getParent() != null) {
			if (fils.getParent().getRawText().equals(parent.getRawText())) {
				return true;
			} else {
				fils = fils.getParent();
			}
		}
		return false;
	}

	// get the last parent of element c
	public static CppElement getLastParent(CppElement c) {
		while (c.getParent() != null) {

			c = c.getParent();

		}
		return c;
	}

	// true mean the last parent of the element c, is in the set of files 'files'
	public static boolean notParentHere(CppElement c, ArrayList<CppElement> files) {
		CppElement tmp;
		for (CppElement file : files) {
			tmp = c;

			while (tmp.getParent() != null) {

				if (tmp.getParent().getRawText().equals(file.getRawText())) {
					return false;
				}

				tmp = tmp.getParent();

			}

		}
		return true;
	}

}
