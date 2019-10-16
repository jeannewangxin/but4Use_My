package org.but4reuse.adapters.android.element;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

/**
 * SDK element
 * 
 * @author anasshatnawi
 * 
 */
public class SdkElement extends ManifestElement {

	private String sdkElement = "";

	public SdkElement(String id, ManifestElementType type) {
		super(id, type);
		sdkElement = id;
	}
}
