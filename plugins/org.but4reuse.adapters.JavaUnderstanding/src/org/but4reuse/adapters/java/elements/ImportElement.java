package org.but4reuse.adapters.java.elements;

import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.java.elements.JavaElement;
import org.eclipse.jdt.core.dom.ImportDeclaration;

public class ImportElement extends JavaElement{
	ImportDeclaration importName;

	public ImportElement(String packageName,String className,ImportDeclaration i) {
		super(packageName,className);
		this.importName=i;
	}

	@Override
	public double similarity(IElement anotherElement) {
		if (super.similarity(anotherElement)==0)
			return 0;
		
		if (!(anotherElement instanceof ImportElement))
			return 0;

		ImportElement anotherImportElement=(ImportElement)anotherElement;


		if (isSimilar(importName,anotherImportElement.importName)){
			return 1;
		}
		return 0;



	}

	@Override
	public List<String> getWords() {
		// TODO Auto-generated method stub
		// return null to not include imports in the wordCloud
		return new ArrayList<String>();
	}
	
	@Override
	public String getText() {
		String s;
		s=" Import: "+importName;
		return super.getText()+s;
	}

}
