package org.but4reuse.adapters.android.element;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

/**
 * Feature element
 * 
 * @author anasshatnawi
 * 
 */
public class FeatureElement extends ManifestElement {

	private String featureElement = "";

	public FeatureElement(String id, ManifestElementType type) {
		super(id, type);
		featureElement = id;
	}
}
