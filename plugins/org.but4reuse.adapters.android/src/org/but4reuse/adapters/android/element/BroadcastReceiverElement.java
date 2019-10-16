package org.but4reuse.adapters.android.element;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

/**
 * BroadcastReceiver element
 * 
 * @author anasshatnawi
 * 
 */
public class BroadcastReceiverElement extends ManifestElement {

	private String broadcastReceiverElement = "";

	public BroadcastReceiverElement(String id, ManifestElementType type) {
		super(id, type);
		broadcastReceiverElement = id;
	}
}
