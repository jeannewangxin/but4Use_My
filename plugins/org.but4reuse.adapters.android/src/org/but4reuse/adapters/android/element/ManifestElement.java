package org.but4reuse.adapters.android.element;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

/**
 * Manifest element
 * 
 * @author anasshatanwi
 * 
 */
public class ManifestElement extends AbstractElement {

	public static enum ManifestElementType {
		PACKAGE_ID, USER_PERMISSION, SDK, ACTIVITY, SERVICE, BRODCAST_RECEIVER, CONTENT_PRPVIDER, FEATURE
	};
	
	public ManifestElementType type;
	private String manifestElement = "";

	public ManifestElement(String id, ManifestElementType type) {
		super();
		manifestElement = id;
		this.type = type;
	}

	@Override
	public double similarity(IElement anotherElement) {
		if (anotherElement instanceof ManifestElement) {
			if (manifestElement.equals(((ManifestElement) anotherElement).manifestElement)) {
				return 1;
			}
		}
		return 0;
	}

	@Override
	public String getText() {
		return "[" + type + "] " + manifestElement;
	}

	public String getContnet() {
		return manifestElement;
	}
	
	@Override
	public int hashCode() {
		return manifestElement.hashCode();
	}

}
