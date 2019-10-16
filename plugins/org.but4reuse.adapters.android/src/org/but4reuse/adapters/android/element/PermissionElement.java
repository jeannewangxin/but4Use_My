package org.but4reuse.adapters.android.element;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

/**
 * Activity element
 * 
 * @author anasshatnawi
 * 
 */
public class PermissionElement extends ManifestElement {

	private String permissionElement = "";

	public PermissionElement(String id, ManifestElementType type) {
		super(id, type);
		permissionElement = id;
	}
}
