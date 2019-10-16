package org.but4reuse.adapters.java.elements;


import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.java.elements.JavaElement;
import org.eclipse.jdt.core.dom.Type;

public class InterfacesElement  extends JavaElement {
	String interfaces;

	public InterfacesElement(String packageName,String className,String string){
		super(packageName,className);	
		this.interfaces=string;
	}

	@Override
	public String getText() {
		StringBuffer implement=new StringBuffer();		
		if (interfaces.equals(""))
			implement.append("implements no interface");
		implement.append(" "+interfaces);
		return super.getText()+" implements: "+implement;
	}

	@Override
	public double similarity(IElement arg0) {
		if(!(arg0 instanceof InterfacesElement)) {
			return 0;
		}

		InterfacesElement anotherInterfacesElement=(InterfacesElement)arg0;
		
		//No similar in package and name level
		if(super.similarity(anotherInterfacesElement)==0) {
			return 0;
		}

		if(interfaces.equals(anotherInterfacesElement.interfaces)) {
			return 1;
		}else {
			return 0;
		}
	}
}