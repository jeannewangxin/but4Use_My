package org.but4reuse.adapters.java.elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.java.elements.JavaElement;
import org.but4reuse.adapters.java.elements.activator.Activator;
import org.but4reuse.adapters.java.elements.jdtvsitor.BodyVisitor;
import org.but4reuse.adapters.java.elements.preferences.JavaUnderstandingAdapterPreferencePage;
import org.but4reuse.adapters.java.elements.preferences.JavaUnderstandingAdapterPreferencePage.Choice;
import org.but4reuse.utils.strings.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import cide.gast.ASTNode;

public class MethodElement extends JavaElement {

	String methodName;
	String returnType;
	List<Type> parametersTypes;
	List<Modifier> modifiers;
	ASTMatcher astComparateur=new ASTMatcher();
	Block body;
	BodyVisitor bodyVisitor;
	String bodyToString;
	MethodDeclaration methodNode;

	//

	ArrayList<String> diffMethodName=new ArrayList<>();
	ArrayList<String> diffReturnType=new ArrayList<>();
	ArrayList<List<Type>> diffParametersTypes=new ArrayList<>();
	ArrayList<List<Modifier>> diffModifiers=new ArrayList<>();
	ArrayList<String> diffBody=new ArrayList<>();

	//options
	private static Choice BODY=Choice.IGNORE;
	private static Choice NAME=Choice.SAME;
	private static Choice RETURNTYPE=Choice.IGNORE;
	private static Choice PARAMETERS=Choice.IGNORE;
	private static Choice MODIFIER=Choice.IGNORE;
	private static Choice ORDER_SENSITIVITY=Choice.ORDER_SENSITIVE;
	private static double SIMILARITY_LEVEL=1;
	//options

	

	public MethodElement(MethodDeclaration md, String packageName,String className,String methodName,Type returnType,
			List<Object> modifiers,List<SingleVariableDeclaration> parametersType, Block body){
		super(packageName,className);
		this.packageName=packageName;
		this.className=className;	
		this.methodName=className + "."+ methodName;
		this.returnType=(returnType==null)?"":returnType.toString();
		ArrayList<Modifier> tempModifiers=new ArrayList<>();
		methodNode = md;


		for (Object o:modifiers) {
			if (o instanceof Modifier) {
				tempModifiers.add((Modifier)o);
			}
		}

		this.modifiers=tempModifiers;
		this.parametersTypes=new ArrayList<>();
		this.body=body;


		bodyVisitor=new BodyVisitor();
		if (body!=null) {
			bodyToString =body.toString();
			body.accept(bodyVisitor);
		}

		for(SingleVariableDeclaration param:parametersType) {
			parametersTypes.add( param.getType());
		}


		diffMethodName.add(this.methodName);
		diffReturnType.add(this.returnType);
		diffParametersTypes.add(this.parametersTypes);
		diffModifiers.add(this.modifiers);
		diffBody.add(this.bodyToString);
		initOption();
		
//		words = extractWords();
	}

	public void initOption() {

		//Check if the adapter is load by the plugins 
		if (Activator.getDefault()!=null) {

			IGNOREPATH=Activator.getDefault().getPreferenceStore()
					.getBoolean(JavaUnderstandingAdapterPreferencePage.IGNORE_PATH);



			SIMILARITY_LEVEL=( Activator.getDefault().getPreferenceStore()
					.getInt(JavaUnderstandingAdapterPreferencePage.SIMILARITY_LEVEL) )*1.0/100;
			try {

				
				BODY=Choice.valueOf(Activator.getDefault().getPreferenceStore()
						.getString(JavaUnderstandingAdapterPreferencePage.BODY));
				ORDER_SENSITIVITY=Choice.valueOf(Activator.getDefault().getPreferenceStore()
						.getString(JavaUnderstandingAdapterPreferencePage.ORDER_SENSITIVITY));
				

				MODIFIER=Choice.valueOf(Activator.getDefault().getPreferenceStore()
						.getString(JavaUnderstandingAdapterPreferencePage.MODIFIER_METHOD));
				NAME=Choice.valueOf(Activator.getDefault().getPreferenceStore()
						.getString(JavaUnderstandingAdapterPreferencePage.NAME_METHOD));

				RETURNTYPE=Choice.valueOf(Activator.getDefault().getPreferenceStore()
						.getString(JavaUnderstandingAdapterPreferencePage.RETURNTYPE));
				PARAMETERS=Choice.valueOf(Activator.getDefault().getPreferenceStore()
						.getString(JavaUnderstandingAdapterPreferencePage.PARAMETERS));

			}catch(IllegalArgumentException e) { /* preferences are not set	*/	
				e.printStackTrace();
				Activator.getDefault().getPreferenceStore().setValue(JavaUnderstandingAdapterPreferencePage.BODY,BODY.toString());
				Activator.getDefault().getPreferenceStore().setValue(JavaUnderstandingAdapterPreferencePage.ORDER_SENSITIVITY,ORDER_SENSITIVITY.toString());
				Activator.getDefault().getPreferenceStore().setValue(JavaUnderstandingAdapterPreferencePage.MODIFIER_METHOD,MODIFIER.toString());
				Activator.getDefault().getPreferenceStore().setValue(JavaUnderstandingAdapterPreferencePage.NAME_METHOD,NAME.toString());
				Activator.getDefault().getPreferenceStore().setValue(JavaUnderstandingAdapterPreferencePage.RETURNTYPE,RETURNTYPE.toString());
				Activator.getDefault().getPreferenceStore().setValue(JavaUnderstandingAdapterPreferencePage.PARAMETERS,PARAMETERS.toString());
			}
		}
	}

	public static void setOptionBody(Choice c) {
		BODY=c;
	}

	public static void setOptionName(Choice c) {
		NAME=c;
	}
	public static void setOptionReturnType(Choice c) {
		RETURNTYPE=c;
	}
	public static void setOptionParameter(Choice c) {
		PARAMETERS=c;
	}
	public static void setOptionModifier(Choice c) {
		MODIFIER=c;
	}

	public static void setOptionSimilaritylevel(double d) {
		SIMILARITY_LEVEL=d;
	}

	public static void setOrderSensitivity(Choice c) {
		ORDER_SENSITIVITY=c;
	}

	@Override
	public String getText() {
		StringBuffer res=new StringBuffer();
		res.append(super.getText());

		res.append(" Method:");

		switch(MODIFIER) {
		case SAME: 
			for (Modifier mod:modifiers) {
				res.append(" "+mod);
			}

			break;
		case DIFFERENT: 
			res.append("(");
			for(int i=0;i<diffModifiers.size();i++) {
				List<Modifier> lm=diffModifiers.get(i);
				for (Modifier mod:lm) {
					res.append(" "+mod);
				}
				if (i!=diffModifiers.size()-1) {
					res.append(" |");
				}
			}
			res.append(")");
			break;

		default:
			break;
		}

		if (RETURNTYPE == Choice.SAME && returnType!=null) {
			res.append(returnType);
			res.append(" ");
		}

		if (RETURNTYPE == Choice.DIFFERENT ) {
			res.append(getTextList(diffReturnType));
		}

		switch (NAME) {
		case SAME:
			res.append(methodName);
			break;
		case DIFFERENT:
			res.append(getTextList(diffMethodName));
			break;

		default:
			break;
		}

		switch(PARAMETERS) {
		case SAME:
			res.append("(");
			for (int i=0;i<parametersTypes.size();i++) {
				res.append(parametersTypes.get(i));
				if (i!=parametersTypes.size()-1) {
					res.append(",");
				}
			}
			res.append(") ");
			break;
		case DIFFERENT:
			res.append("(");
			for(int index=0;index<diffParametersTypes.size();index++) {
				List<Type> l=diffParametersTypes.get(index);
				for (int i=0;i<l.size();i++) {
					res.append(l.get(i));
					if (i!=l.size()-1) {
						res.append(",");
					}
				}
				if(index!=diffParametersTypes.size()-1)
					res.append("| ");
			}
			res.append(") ");
			break;
		default:
			break;
		}
		if (BODY != Choice.IGNORE) {
			if (body==null)
				res.append("{}");
			else
				res.append(bodyToString);
		}

		return res.toString();
	}


	@Override
	public double similarity(IElement anotherElement) {
		if(!(anotherElement instanceof MethodElement)){
			return 0;
		}

		MethodElement anotherMethodElement = (MethodElement) anotherElement;

		//No similar in package and name level
		if(super.similarity(anotherMethodElement)==0) {
			return 0;
		}

		boolean name=nameComparaison(anotherMethodElement);
		boolean returnType=returnTypeComparaison(anotherMethodElement);
		boolean modifier=modifierComparaison(anotherMethodElement);
		boolean parametre=parametersComparaison(anotherMethodElement);
		boolean body=bodyComparaison(anotherMethodElement);

		boolean result=name&&returnType&&modifier&&parametre&&body;

		if (result) {
			updateDiffName(anotherMethodElement);
			updateDiffReturnType(anotherMethodElement);
			updateDiffModifier(anotherMethodElement);
			updateDiffParameter(anotherMethodElement);
			return 1;
		}
		else {
			return 0;
		}

	}



	private void updateDiffParameter(MethodElement anotherMethodElement) {
		if(PARAMETERS==Choice.DIFFERENT) {
			Comparator<List<Type>> c=new Comparator<List<Type>>() {
				@Override
				public int compare(List<Type> arg0, List<Type> arg1) {
					if (arg0.size()!=arg1.size()) {
						return 1;
					}
					for(int i=0;i<arg0.size();i++) {
						if (!isSimilar(arg0.get(i),arg1.get(i))) {
							return 1;
						}
					}
					return 0;
				}
			};

			updateDiffList(PARAMETERS,diffParametersTypes, anotherMethodElement.parametersTypes ,anotherMethodElement.diffParametersTypes, parametersTypes, c);
		}
	}

	private void updateDiffModifier(MethodElement anotherMethodElement) {
		if(MODIFIER==Choice.DIFFERENT) {
			Comparator<List<Modifier>> c=new Comparator<List<Modifier>>() {
				@Override
				public int compare(List<Modifier> arg0, List<Modifier> arg1) {
					if (compareModifier(arg0, arg1)) {
						return 0;
					}
					else {
						return 1;
					}
				}
			};
			addDiff(diffModifiers, anotherMethodElement.modifiers, c);
			addDiff(anotherMethodElement.diffModifiers, modifiers, c);
		}

	}

	private void updateDiffReturnType(MethodElement anotherMethodElement) {
		Comparator<String> c=new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.compareTo(arg1);
			}
		};
		updateDiffList(RETURNTYPE,diffReturnType, anotherMethodElement.returnType,anotherMethodElement.diffReturnType, returnType,c);

	}

	private void updateDiffName(MethodElement anotherMethodElement) {
		Comparator<String> c=new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.compareTo(arg1);
			}
		};

		//update the two difflist
		updateDiffList(NAME, diffMethodName, anotherMethodElement.methodName,anotherMethodElement.diffMethodName, methodName, c);

	}



	public boolean returnTypeComparaison(MethodElement anotherMethodElement) {
		if (RETURNTYPE==Choice.IGNORE) {
			return true;
		}

		boolean isSimilar=returnType.equals(anotherMethodElement.returnType);
		if (RETURNTYPE==Choice.SAME) {
			return isSimilar;
		}
		else {
			return !isSimilar;
		}
	}
	public boolean nameComparaison(MethodElement anotherMethodElement) {
		if ( NAME==Choice.IGNORE ) {
			return true;
		}

		boolean isSimilar=this.methodName.equals(anotherMethodElement.methodName);
		if (NAME==Choice.SAME) {
			return isSimilar;
		}else {
			return !isSimilar;
		}
	}

	public boolean modifierComparaison(MethodElement anotherMethodElement) {

		if (MODIFIER==Choice.IGNORE) {
			return true;
		}

		List<Modifier> modifiers1=modifiers,modifiers2=anotherMethodElement.modifiers;
		if (modifiers1.size()!=modifiers2.size()) {
			return false;
		}

		boolean isSimilar=true;
		for(Modifier m1:modifiers) {
			boolean found=false;
			for(Modifier m2:anotherMethodElement.modifiers) {
				if(astComparateur.match(m1, m2)) {
					found=true;
				}
			}

			//One modifier is not in the second element
			if(!found) {
				isSimilar=false;
			}
		}

		if (MODIFIER==Choice.SAME) {
			return isSimilar;
		}else {
			return !isSimilar;
		}
	}
	/**
	 * compare if the other methode have the same parameter.
	 * Note that we just look at the type of the parameter and not their name
	 * @param anotherMethodElement
	 * @return  true if they have the same parameter, false otherwise
	 */
	public boolean parametersComparaison(MethodElement anotherMethodElement) {
		if (PARAMETERS==Choice.IGNORE) {
			return true;
		}


		boolean isSimilar=true;
		if (parametersTypes.size()==anotherMethodElement.parametersTypes.size()) {
			for(int i = 0; i < this.parametersTypes.size(); i++){
				if(!isSimilarOrderSensitive(parametersTypes,anotherMethodElement.parametersTypes)) {
					isSimilar=false;
				}
			}
		}else {
			isSimilar=false;
		}

		if(PARAMETERS==Choice.SAME) {
			return isSimilar;
		}else {//Choice==differents
			return !isSimilar;
		}
	}

	public boolean bodyComparaison(MethodElement anotherMethodElement) {
		if (BODY==Choice.IGNORE) {
			return true;
		}

		boolean isSimilar=true;
		boolean orderSensitive= Choice.ORDER_SENSITIVE==ORDER_SENSITIVITY;
		if (body==null) {
			if(anotherMethodElement.body!=null) {
				isSimilar= false;
			}
		}else {
			isSimilar=bodyVisitor.pourcentageSemblable(anotherMethodElement.bodyVisitor,orderSensitive) >=SIMILARITY_LEVEL;
		}
		
		if (BODY==Choice.SAME) {
			return isSimilar;
		}
		else {//BODY==Choice.DIFFERENT
			return !isSimilar;
		}

	}
	
	private List<String> extractWords() {
		List<String> wordsListC = new ArrayList<String>();

		wordsListC.add( methodNode.getName().toString());
		return wordsListC;
}
	
}
