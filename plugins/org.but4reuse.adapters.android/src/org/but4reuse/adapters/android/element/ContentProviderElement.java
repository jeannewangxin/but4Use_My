package org.but4reuse.adapters.android.element;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

/**
 * Content Provider element
 * 
 * @author anasshatnawi
 * 
 */
public class ContentProviderElement extends ManifestElement {

	private String contentProviderElement = "";

	public ContentProviderElement(String id, ManifestElementType type) {
		super(id, type);
		contentProviderElement = id;
	}
}
