package org.but4reuse.adapters.java.elements;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.java.elements.JavaElement;
import org.eclipse.jdt.core.dom.Type;

public class SuperClassElement extends JavaElement {
	Type superClass;
	public SuperClassElement(String packageName,String className,Type superclassType){
		super(packageName,className);
		this.superClass=superclassType;
	}

	@Override
	public String getText() {
		return super.getText()+" superclass:"+superClass;
	}

	@Override
	public double similarity(IElement arg0) {
		if(!(arg0 instanceof SuperClassElement)) {
			return 0;
		}

		SuperClassElement anotherSuperElement=(SuperClassElement)arg0;

		//No similar in package and name level
		if(super.similarity(anotherSuperElement)==0) {
			return 0;
		}
		if (superClass==null) {
			if (anotherSuperElement.superClass==null) {
				return 1;
			}
			else {
				return 0;
			}
		}
		if (isSimilar(superClass, anotherSuperElement.superClass)) {
			return 1;
		}

		return 0;
	}
}


