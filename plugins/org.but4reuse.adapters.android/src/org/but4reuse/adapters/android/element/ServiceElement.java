package org.but4reuse.adapters.android.element;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

/**
 * Service element
 * 
 * @author anasshatnawi
 * 
 */
public class ServiceElement extends ManifestElement {

	private String serviceElement = "";

	public ServiceElement(String id, ManifestElementType type) {
		super(id, type);
		serviceElement = id;
	}
}
