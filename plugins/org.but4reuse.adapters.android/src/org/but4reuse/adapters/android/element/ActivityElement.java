package org.but4reuse.adapters.android.element;

import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.impl.AbstractElement;

/**
 * Activity element
 * 
 * @author anasshatnawi
 * 
 */
public class ActivityElement extends ManifestElement {

	public String activityElement = "";

	public ActivityElement(String id, ManifestElementType type) {
		super(id, type);
		activityElement = id;
	}

//		boolean flage = false;
//		if (! flage) {
//			System.err.println("flage is false");
//			String mainName = "";
//			try {
//				Class activity = Class.forName(this.getContnet());
//				System.err.println("activity "+ activity);
//				Class anotherActivity = Class.forName( ((ManifestElement) anotherElement).getContnet());
//				System.err.println("anotherActivity "+ anotherActivity);
//				if (activity.equals(anotherActivity)) {
//					return 1;
//				}
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			return 0;
//		}

	
}
