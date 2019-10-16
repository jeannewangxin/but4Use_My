package org.but4reuse.adapters.java.elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.java.elements.JavaElement;
import org.but4reuse.adapters.java.elements.activator.Activator;
import org.but4reuse.adapters.java.elements.preferences.JavaUnderstandingAdapterPreferencePage;
import org.but4reuse.adapters.java.elements.preferences.JavaUnderstandingAdapterPreferencePage.Choice;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;

public class FieldElement  extends JavaElement{
	String fieldName;
	Type type;
	List<Modifier> modifiers;
	Expression attribute;


	ArrayList<String> diffFieldName=new ArrayList<>();
	ArrayList<Type> diffType=new ArrayList<>();
	ArrayList<List<Modifier>> diffModifiers=new ArrayList<>();
	ArrayList<Expression> diffAttribute=new ArrayList<>();


	/*Use to compare two node of the AST*/
	ASTMatcher comparator=new ASTMatcher();
	private static Choice NAME = Choice.SAME;
	private static Choice TYPE = Choice.SAME;
	private static Choice MODIFIER = Choice.IGNORE;
	private static Choice ATTRIBUTE_FIELD = Choice.IGNORE;



	public FieldElement(String packageName, String className, FieldDeclaration field,String fieldName,Expression attribute) {
		super(packageName,className);
		this.fieldName=className + "."+ fieldName;
		this.attribute=attribute;
		this.modifiers=new ArrayList<>();

		this.type=field.getType();
		for(Object modif :field.modifiers()) {
			if (modif instanceof Modifier) {
				modifiers.add((Modifier)modif);
			}
		}

		diffFieldName.add(this.fieldName);
		diffAttribute.add(this.attribute);
		diffModifiers.add(this.modifiers);
		diffType.add(this.type);
		initOption();

	}


	public void initOption() {
		if(Activator.getDefault()!=null) {
			try {
				NAME=Choice.valueOf(Activator.getDefault().getPreferenceStore()
						.getString(JavaUnderstandingAdapterPreferencePage.NAME_FIELD));
				TYPE=Choice.valueOf(Activator.getDefault().getPreferenceStore()
						.getString(JavaUnderstandingAdapterPreferencePage.DATATYPE_FIELD));
				MODIFIER=Choice.valueOf(Activator.getDefault().getPreferenceStore()
						.getString(JavaUnderstandingAdapterPreferencePage.MODIFIER_FIELD));
				ATTRIBUTE_FIELD=Choice.valueOf(Activator.getDefault().getPreferenceStore()
						.getString(JavaUnderstandingAdapterPreferencePage.ATTRIBUTE_FIELD));

			}catch(IllegalArgumentException e) { /* preferences are not set */
				Activator.getDefault().getPreferenceStore().setValue(JavaUnderstandingAdapterPreferencePage.NAME_FIELD,NAME.toString());
				Activator.getDefault().getPreferenceStore().setValue(JavaUnderstandingAdapterPreferencePage.DATATYPE_FIELD,TYPE.toString());
				Activator.getDefault().getPreferenceStore().setValue(JavaUnderstandingAdapterPreferencePage.MODIFIER_FIELD,MODIFIER.toString());
				Activator.getDefault().getPreferenceStore().setValue(JavaUnderstandingAdapterPreferencePage.ATTRIBUTE_FIELD,ATTRIBUTE_FIELD.toString());
			}
		}
	}

	public static void setOptionType(Choice c) {
		TYPE=c;
	}

	public static void setOptionFullName(Choice c){
		NAME=c;
	}

	public static void setOptionModifier(Choice c) {
		MODIFIER=c;
	}

	@Override
	public double similarity(IElement anotherElement) {
		if ( !(anotherElement instanceof FieldElement))
			return 0;

		FieldElement otherfield=(FieldElement)anotherElement;

		//Check if the two element are in the same class
		if (super.similarity(otherfield)==0)
			return 0;


		boolean nameIsSimilar=compareName(otherfield);
		boolean modifierIsSimilar=compareModifier(otherfield);
		boolean typeIsSimilar=compareType(otherfield);
		boolean attributFieldIsSimilar=compareAttributeField(otherfield);


		if (nameIsSimilar && modifierIsSimilar && typeIsSimilar && attributFieldIsSimilar) {
			updateDiffFieldName(otherfield);
			updateDiffModifier(otherfield);
			updateDiffType(otherfield);
			updateDiffAttributeField(otherfield);
			return 1;
		}else {
			return 0;
		}
	}


	private void updateDiffAttributeField(FieldElement otherField) {
		Comparator<Expression> c=new Comparator<Expression>() {

			@Override
			public int compare(Expression arg0, Expression arg1) {
				if(arg0==null) {
					if (arg1==null){
						return 0;
					}else {
						return 1;
					}
				}
				if(arg0.subtreeMatch(comparator, arg1)) {
					return 0;
				}
				else {
					return 1;
				}
			}
		};
		updateDiffList(ATTRIBUTE_FIELD,diffAttribute,otherField.attribute,otherField.diffAttribute,attribute,c);
	}


	private void updateDiffModifier(FieldElement otherField) {

		Comparator<List<Modifier>> c=new Comparator<List<Modifier>>() {

			@Override
			public int compare(List<Modifier> arg0, List<Modifier> arg1) {
				if(compareModifier(arg0, arg1)) {
					return 0;
				}
				else {
					return 1;
				}
			}
		};

		updateDiffList(MODIFIER,diffModifiers,otherField.modifiers,otherField.diffModifiers,modifiers,c);

	}

	private void updateDiffType(FieldElement otherField) {
		Comparator<Type> comparator=new Comparator<Type>() {
			@Override
			public int compare(Type arg0, Type arg1) {

				if (arg0.subtreeMatch(new ASTMatcher(), arg1)) {
					return 0;
				}else {
					return 1;
				}
			}
		};

		updateDiffList(TYPE,diffType,type,otherField.diffType,otherField.type,comparator);

	}


	private void updateDiffFieldName(FieldElement otherField) {
		Comparator<String> comparator=new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.compareTo(arg1);
			}
		};
		updateDiffList(NAME,diffFieldName,fieldName,otherField.diffFieldName,otherField.fieldName,comparator);
	}



	public boolean compareAttributeField(FieldElement otherfield) {
		if (ATTRIBUTE_FIELD==Choice.IGNORE) {
			return true;
		}

		boolean isSimilar=true;


		if (attribute==null) {
			if (otherfield.attribute==null) {
				isSimilar=true;
			}else {
				isSimilar=false;
			}
		}else {
			isSimilar=attribute.subtreeMatch(comparator, otherfield.attribute);
		}
		if(ATTRIBUTE_FIELD==Choice.SAME) {
			return isSimilar;
		}else {
			return !isSimilar;
		}
	}


	/**
	 * Compare his name with the other fields of FieldElement according to the option selected
	 * @param anotherElement
	 * @return true if similar according to the option, false otherwise
	 */
	public boolean compareName(FieldElement anotherElement) {
		if (NAME==Choice.IGNORE) {
			return true;
		}

		boolean isSimilar=fieldName.equals(anotherElement.fieldName);

		if (NAME==Choice.SAME) {
			return isSimilar;
		}
		else {
			return !isSimilar;
		}
	}

	/**
	 * Compare his modifier with the other modifier of FieldElement according to the option selected
	 * @param anotherElement
	 * @return true if similar according to the option, false otherwise
	 */
	public boolean compareModifier(FieldElement otherfield) {

		if (MODIFIER==Choice.IGNORE) {
			return true;
		}

		boolean isSimilar=true;

		if (MODIFIER==Choice.SAME) {
			return compareModifier(modifiers, otherfield.modifiers);
		}
		else {
			return !compareModifier(modifiers, otherfield.modifiers);
		}
	}
	/**
	 * Compare his name with the other FieldElement according to the option selected
	 * @param anotherElement
	 * @return true if similar according to the option, false otherwise
	 */
	public boolean compareType(FieldElement otherfield) {
		if(TYPE==Choice.IGNORE) {
			return true;
		}

		boolean isSimilar=type.subtreeMatch(comparator,otherfield.type);
		if (TYPE == Choice.SAME) {
			return isSimilar;
		}else{
			return !isSimilar;
		}	
	}


	@Override
	public String getText() {
		StringBuffer txt=new StringBuffer();
		txt.append(super.getText());

		txt.append(" Field:");
		switch(MODIFIER) {
		case SAME: 
			for (Modifier mod:modifiers) {
				txt.append(" "+mod);
			}
			break;
		case DIFFERENT: 

			if (diffModifiers.size()==0) {
				txt.append( "()");
			}
			else {
				txt.append("( ");
				for(int i=0;i<diffModifiers.size();i++) {
					for (Modifier mod:diffModifiers.get(i)) {
						txt.append(" "+mod);
					}
					if (i!=diffModifiers.size()-1) { 
						txt.append(" | ");
					}
				}
				txt.append(" )");
			}
			break;

		default:
			break;
		}


		switch(TYPE) {
		case SAME:
			txt.append(" "+type.toString());
			break;
		case DIFFERENT:
			txt.append(getTextList(diffType));
			break;

		default:
			break;
		}

		switch (NAME) {
		case SAME:
			txt.append(" "+fieldName);
			break;
		case DIFFERENT:
			txt.append(" "+getTextList(diffFieldName));
			break;

		default:
			break;
		}

		if (ATTRIBUTE_FIELD == Choice.SAME && attribute!=null) {
			txt.append(" = "+attribute);
		}

		if(ATTRIBUTE_FIELD == Choice.DIFFERENT) {
			txt.append(" = "+getTextList(diffAttribute));
		}
		return txt.toString();
	}
	
	private List<String> ectractWords() {
		List<String> wordList =  new ArrayList<String>();
	
		return wordList;
	}

}
