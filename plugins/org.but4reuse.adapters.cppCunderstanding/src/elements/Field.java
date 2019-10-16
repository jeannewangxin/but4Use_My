package elements;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adapters.IElement;
import org.but4reuse.utils.files.FileUtils;
import org.but4reuse.utils.strings.StringUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import activator.Activator;
import adapters.CppAdapter;
import preferences.CppCunderstandingPreferencePage;
import preferences.CppCunderstandingPreferencePage.Choice;

public class Field extends CppElement {

	// values of the preference page
	public static Choice modifierC = null;

	public static Choice typeField = null;

	public static Choice nameField = null;

	public static Choice valueField = null;

	// attributes specific just for this element (Field element)
	protected String modifierId;
	protected String typeId;
	protected String id;
	protected String value;

	public Field(IASTNode node, CppElement parent, String text, String rawText, String modifier, String typeId,
			String id, String value) {
		super(node, parent, text, rawText, CppElementType.ATTRIBUTE_H);
		// added when integrating the two adapters : cpp and understanding
		String selection = (Activator.getDefault().getPreferenceStore()
				.getString(CppCunderstandingPreferencePage.SELECTION));
		if (!(selection.equals("ALL"))) {
			// this.modifierId = modifier;
			this.typeId = new String(typeId);
			this.id = new String(id);
			this.value = new String(value);

		}
		this.racine = CppAdapter.artefactUri;
		typeField = Choice.valueOf(
				Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.DATATYPE_FIELD));

		nameField = Choice.valueOf(
				Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.NAME_FIELD));

		valueField = Choice.valueOf(
				Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.ATTRIBUTE_FIELD));

		modifierC = Choice.valueOf(
				Activator.getDefault().getPreferenceStore().getString(CppCunderstandingPreferencePage.MODIFIER_FIELD));
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
			if (!(anotherElement instanceof Field)) {
				return 0;
			}

			// all field similar same type, same name and same value
			if ((typeField.toString() == "SAME") && (nameField.toString() == "SAME")
					&& (valueField.toString() == "SAME")) {

				return this.comparingTypes(anotherElement) * this.comparingIds(anotherElement)
						* this.comparingValues(anotherElement);
			}

			// comparing just types of fields

			if ((typeField.toString() == "SAME") && (nameField.toString() == "IGNORE")
					&& (valueField.toString() == "IGNORE")) {
				return this.comparingTypes(anotherElement);
			}

			// the rest is a combinaison between the three

			// combainison for fixing type
			// s g d
			if ((typeField.toString() == "SAME") && (nameField.toString() == "IGNORE")
					&& (valueField.toString() == "DIFFERENT")) {

				if ((this.comparingTypes(anotherElement) == 1.) && (this.comparingValues(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "SAME") && (nameField.toString() == "IGNORE")
					&& (valueField.toString() == "SAME")) {

				if ((this.comparingTypes(anotherElement) == 1.) && (this.comparingValues(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			// s d g
			if ((typeField.toString() == "SAME") && (nameField.toString() == "DIFFERENT")
					&& (valueField.toString() == "IGNORE")) {

				if ((this.comparingTypes(anotherElement) == 1.) && (this.comparingIds(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}
			if ((typeField.toString() == "SAME") && (nameField.toString() == "SAME")
					&& (valueField.toString() == "IGNORE")) {

				if ((this.comparingTypes(anotherElement) == 1.) && (this.comparingIds(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			// s d d
			if ((typeField.toString() == "SAME") && (nameField.toString() == "DIFFERENT")
					&& (valueField.toString() == "DIFFERENT")) {

				if ((this.comparingTypes(anotherElement) == 1.) && (this.comparingIds(anotherElement) == 0.)
						&& (comparingValues(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			// ________________________________________________________________________________________________________________________________________________________//

			// comparring just ids
			if ((typeField.toString() == "IGNORE") && (nameField.toString() == "SAME")
					&& (valueField.toString() == "IGNORE")) {
				return this.comparingIds(anotherElement);
			}

			if ((typeField.toString() == "DIFFERENT") && (nameField.toString() == "SAME")
					&& (valueField.toString() == "DIFFERENT")) {
				if (((this.comparingTypes(anotherElement) == 0.)) && (this.comparingIds(anotherElement) == 1.)
						&& (comparingValues(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}

			}

			if ((typeField.toString() == "DIFFERENT") && (nameField.toString() == "SAME")
					&& (valueField.toString() == "IGNORE")) {

				if ((this.comparingTypes(anotherElement) == 0.) && (this.comparingIds(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "DIFFERENT") && (nameField.toString() == "SAME")
					&& (valueField.toString() == "SAME")) {

				if ((this.comparingTypes(anotherElement) == 0.) && (this.comparingIds(anotherElement) == 1.)
						&& (this.comparingValues(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "IGNORE") && (nameField.toString() == "SAME")
					&& (valueField.toString() == "DIFFERENT")) {
				if ((this.comparingIds(anotherElement) == 1.) && (comparingValues(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "SAME") && (nameField.toString() == "SAME")
					&& (valueField.toString() == "DIFFERENT")) {

				if ((this.comparingTypes(anotherElement) == 1.) && (this.comparingIds(anotherElement) == 1.)
						&& (this.comparingValues(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			// ________________________________________________________________________________________________________________________________________________________//

			// comparing values
			if ((typeField.toString() == "IGNORE") && (nameField.toString() == "IGNORE")
					&& (valueField.toString() == "SAME")) {

				return this.comparingValues(anotherElement);
			}

			if ((typeField.toString() == "DIFFERENT") && (nameField.toString() == "DIFFERENT")
					&& (valueField.toString() == "SAME")) {
				if (((this.comparingTypes(anotherElement) == 0.)) && (this.comparingIds(anotherElement) == 0.)
						&& (comparingValues(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}

			}

			if ((typeField.toString() == "IGNORE") && (nameField.toString() == "DIFFERENT")
					&& (valueField.toString() == "SAME")) {
				if ((this.comparingIds(anotherElement) == 0.) && (comparingValues(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "IGNORE") && (nameField.toString() == "SAME")
					&& (valueField.toString() == "SAME")) {
				if ((this.comparingIds(anotherElement) == 1.) && (comparingValues(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "DIFFERENT") && (nameField.toString() == "IGNORE")
					&& (valueField.toString() == "SAME")) {
				if ((this.comparingTypes(anotherElement) == 0.) && (comparingValues(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			// __________________________________________________________________________________________________________________________________________________________//
			// no solution we need to test flags

			if ((typeField.toString() == "SAME") && (nameField.toString() == "DIFFERENT")
					&& (valueField.toString() == "SAME")) {
				if ((this.comparingTypes(anotherElement) == 1.) && (comparingIds(anotherElement) == 0.)
						&& (comparingValues(anotherElement) == 1.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "DIFFERENT") && (nameField.toString() == "DIFFERENT")
					&& (valueField.toString() == "DIFFERENT")) {
				if ((this.comparingTypes(anotherElement) == 0.) && (comparingIds(anotherElement) == 0.)
						&& (comparingValues(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "DIFFERENT") && (nameField.toString() == "DIFFERENT")
					&& (valueField.toString() == "IGNORE")) {
				if ((this.comparingTypes(anotherElement) == 0.) && (comparingIds(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "DIFFERENT") && (nameField.toString() == "IGNORE")
					&& (valueField.toString() == "DIFFERENT")) {
				if ((this.comparingTypes(anotherElement) == 0.) && (comparingValues(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "DIFFERENT") && (nameField.toString() == "IGNORE")
					&& (valueField.toString() == "IGNORE")) {
				if ((this.comparingTypes(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "IGNORE") && (nameField.toString() == "DIFFERENT")
					&& (valueField.toString() == "DIFFERENT")) {
				if ((comparingIds(anotherElement) == 0.) && (comparingValues(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "IGNORE") && (nameField.toString() == "DIFFERENT")
					&& (valueField.toString() == "IGNORE")) {
				if ((comparingIds(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}

			if ((typeField.toString() == "IGNORE") && (nameField.toString() == "IGNORE")
					&& (valueField.toString() == "DIFFERENT")) {
				if ((comparingValues(anotherElement) == 0.)) {
					return 1.;
				} else {
					return 0.;
				}
			}
		}
		// ________________________________________________________________________________________________________________________________________________________//
		// if all are ignored we return 1
		return 1.;

	}

	/**
	 * this function for comparing types of fields
	 * 
	 * @param anotherElement
	 * @return the value 1. if the two elements are equal considering their types,
	 *         0. otherwise
	 */
	public double comparingTypes(IElement anotherElement) {

		// anotherElement).typeId);
		if (Activator.getDefault().getPreferenceStore().getBoolean(CppCunderstandingPreferencePage.IGNORE_PATH)) {
			if ((this.typeId.equals(((Field) anotherElement).typeId))) {
				return 1.;
			} else
				return 0.;
		} else {
			// not ignoring path

			File f1 = new File(this.getParent().getNode().getContainingFilename());

			String path1 = new String(this.returnPath(f1) + "~" + this.typeId);

			File f2 = new File(((Field) anotherElement).getParent().getNode().getContainingFilename());

			String path2 = new String(((Field) anotherElement).returnPath(f2) + "~" + ((Field) anotherElement).typeId);

			if (path1.equals(path2))
				return 1.;
			else
				return 0.;

		}
	}

	/**
	 * this function for comparing Ids of fields
	 * 
	 * @param anotherElement
	 * @return the value 1. if the two elements are equal considering their names,
	 *         0. otherwise
	 */
	public double comparingIds(IElement anotherElement) {

		if (Activator.getDefault().getPreferenceStore().getBoolean(CppCunderstandingPreferencePage.IGNORE_PATH)) {
			if ((this.id.equals(((Field) anotherElement).id))) {

				return 1.;
			} else
				return 0.;
		} else {
			// not ignoring path

			File f1 = new File(this.getParent().getNode().getContainingFilename());
			String path1 = new String(this.returnPath(f1) + "~" + this.id);

			File f2 = new File(((Field) anotherElement).getParent().getNode().getContainingFilename());
			String path2 = new String(((Field) anotherElement).returnPath(f2) + "~" + ((Field) anotherElement).id);

			if ((path1.equals(path2)))
				return 1.;
			else
				return 0.;

		}
	}

	/**
	 * this function for comparing values of fields
	 * 
	 * @param anotherElement
	 * @return the value 1. if the two elements are equal considering their values,
	 *         0. otherwise
	 */
	public double comparingValues(IElement anotherElement) {

		if (Activator.getDefault().getPreferenceStore().getBoolean(CppCunderstandingPreferencePage.IGNORE_PATH)) {
			if ((this.value.equals(((Field) anotherElement).value))) {
				return 1.;
			} else
				return 0.;
		} else {
			// not ignoring path

			File f1 = new File(this.getParent().getNode().getContainingFilename());
			String returndPath = this.returnPath(f1);
			String path1 = new String(returndPath + "~" + this.value);

			File f2 = new File(((Field) anotherElement).getParent().getNode().getContainingFilename());
			returndPath = ((Field) anotherElement).returnPath(f2);
			String path2 = new String(returndPath + "~" + ((Field) anotherElement).value);

			if (path1.equals(path2)) {

				return 1.;
			} else {

				return 0.;
			}

		}
	}

	public void construct(File f) {
		try {
			FileUtils.appendToFile(f, "\n" + this.node.getParent().getParent().getRawSignature());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<String> extractWords() {

		// add the words for the word cloud
		List<String> wordsList = new ArrayList<String>();

		// we are only interested in the name
		String[] tokens = rawText.split(" ");

		if (tokens.length == 1) {
			wordsList.addAll(StringUtils.tokenizeString(tokens[0]));
		} else {
			wordsList.addAll(StringUtils.tokenizeString(tokens[1]));
		}

		return wordsList;
	}

}
