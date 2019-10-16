package org.but4reuse.adapters.android.element;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

/**
 * Package Id element
 * 
 * @author anasshatnawi
 * 
 */
public class PackageIdElement extends ManifestElement {

	private String packageIdElement = "";

	public PackageIdElement(String id, ManifestElementType type) {
		super(id, type);
		packageIdElement = id;
	}
}
