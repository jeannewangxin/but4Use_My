package parser;

import java.util.List;

import org.but4reuse.adapters.android.activator.Activator;
import org.but4reuse.adapters.android.element.PackageIdElement;
import org.but4reuse.adapters.android.element.PermissionElement;
import org.but4reuse.adapters.android.element.ServiceElement;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.android.element.ActivityElement;
import org.but4reuse.adapters.android.element.BroadcastReceiverElement;
import org.but4reuse.adapters.android.element.ManifestElement;
import org.but4reuse.adapters.android.element.ManifestElement.ManifestElementType;
import org.but4reuse.adapters.android.preferences.AndroidAdapterPreferencePage;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReadXML {

	public static void getElements(NodeList nodeList, List<IElement> selectedElements) {

		// get values of flags from the preference
		boolean selectPermission = Activator.getDefault().getPreferenceStore()
				.getBoolean(AndroidAdapterPreferencePage.PERMISSION);
		boolean selectPackageId = Activator.getDefault().getPreferenceStore()
				.getBoolean(AndroidAdapterPreferencePage.PACKAGE_ID);
		boolean selectActivity = Activator.getDefault().getPreferenceStore()
				.getBoolean(AndroidAdapterPreferencePage.ACTIVITY);
		boolean selectBrodcastProvider = Activator.getDefault().getPreferenceStore()
				.getBoolean(AndroidAdapterPreferencePage.BROADCAST_RECEIVER);
		boolean selectFeature = Activator.getDefault().getPreferenceStore()
				.getBoolean(AndroidAdapterPreferencePage.FEATURE);
		boolean selectSDK = Activator.getDefault().getPreferenceStore().getBoolean(AndroidAdapterPreferencePage.SDK);
		boolean selectService = Activator.getDefault().getPreferenceStore()
				.getBoolean(AndroidAdapterPreferencePage.SERVICE);
		boolean selectContentProvider = Activator.getDefault().getPreferenceStore()
				.getBoolean(AndroidAdapterPreferencePage.CONTENT_PRPVIDER);

		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
				if (selectPackageId && tempNode.getNodeName().contains("manifest")) {
					// get node name and value
					if (tempNode.hasAttributes()) {
						// get attributes names and values
						NamedNodeMap nodeMap = tempNode.getAttributes();
						for (int i = 0; i < nodeMap.getLength(); i++) {
							Node node = nodeMap.item(i);
							if (node.getNodeName().equals("package")) {
								PackageIdElement pak = new PackageIdElement(node.getNodeValue(),
										ManifestElementType.PACKAGE_ID);
								selectedElements.add(pak);
							}
						}
					}
				} else if (selectPermission && tempNode.getNodeName().contains("uses-permission")) {
					// get node name and value
					if (tempNode.hasAttributes()) {
						// get attributes names and values
						NamedNodeMap nodeMap = tempNode.getAttributes();
						for (int i = 0; i < nodeMap.getLength(); i++) {
							Node node = nodeMap.item(i);
							if (node.getNodeName().equals("android:name")) {
								PermissionElement pak = new PermissionElement(node.getNodeValue(),
										ManifestElementType.USER_PERMISSION);
								selectedElements.add(pak);
							}
						}
					}
				} else if (selectActivity && tempNode.getNodeName().contains("activity")) {
					// get node name and value
					if (tempNode.hasAttributes()) {
						// get attributes names and values
						NamedNodeMap nodeMap = tempNode.getAttributes();
						for (int i = 0; i < nodeMap.getLength(); i++) {
							Node node = nodeMap.item(i);
							if (node.getNodeName().equals("android:name")) {
								boolean selectIgnorePackage = Activator.getDefault().getPreferenceStore()
										.getBoolean(AndroidAdapterPreferencePage.IGNORE_PACKAGE);;
								String activityName = "";
								// assign a name based on ignore path or not
								if (selectIgnorePackage) {
									String[] tokens = node.getNodeValue().split("\\.");
									if (tokens.length >= 1) {
										activityName = tokens[tokens.length - 1];
									}else {
										activityName =  node.getNodeValue();
									}
								} else {
									activityName = node.getNodeValue();
								}

								ActivityElement pak = new ActivityElement(activityName, ManifestElementType.ACTIVITY);
								selectedElements.add(pak);
							}
						}
					}
				} else if (selectBrodcastProvider && tempNode.getNodeName().contains("receiver")) {
					// get node name and value
					if (tempNode.hasAttributes()) {
						// get attributes names and values
						NamedNodeMap nodeMap = tempNode.getAttributes();
						for (int i = 0; i < nodeMap.getLength(); i++) {
							Node node = nodeMap.item(i);
							if (node.getNodeName().equals("android:name")) {
								boolean selectIgnorePackage = Activator.getDefault().getPreferenceStore()
										.getBoolean(AndroidAdapterPreferencePage.IGNORE_PACKAGE);;
								String broadcastName = "";
								// assign a name based on ignore path or not
								if (selectIgnorePackage) {
									String[] tokens = node.getNodeValue().split("\\.");
									if (tokens.length >= 1) {
										broadcastName = tokens[tokens.length - 1];
									}else {
										broadcastName =  node.getNodeValue();
									}
								} else {
									broadcastName = node.getNodeValue();
								}
								BroadcastReceiverElement pak = new BroadcastReceiverElement(broadcastName,
										ManifestElementType.BRODCAST_RECEIVER);
								selectedElements.add(pak);
							}
						}
					}
				} else if (selectService && tempNode.getNodeName().contains("service")) {
					// get node name and value
					if (tempNode.hasAttributes()) {
						// get attributes names and values
						NamedNodeMap nodeMap = tempNode.getAttributes();
						for (int i = 0; i < nodeMap.getLength(); i++) {
							Node node = nodeMap.item(i);
							if (node.getNodeName().equals("android:name")) {
								boolean selectIgnorePackage = Activator.getDefault().getPreferenceStore()
										.getBoolean(AndroidAdapterPreferencePage.IGNORE_PACKAGE);;
								String serviceName = "";
								// assign a name based on ignore path or not
								if (selectIgnorePackage) {
									String[] tokens = node.getNodeValue().split("\\.");
									if (tokens.length >= 1) {
										serviceName = tokens[tokens.length - 1];
									}else {
										serviceName =  node.getNodeValue();
									}
								} else {
									serviceName = node.getNodeValue();
								}
								ServiceElement pak = new ServiceElement(serviceName,
										ManifestElementType.SERVICE);
								selectedElements.add(pak);
							}
						}
					}
				} else if (selectSDK && tempNode.getNodeName().contains("uses-sdk")) {
					// get node name and value
					if (tempNode.hasAttributes()) {
						// get attributes names and values
						NamedNodeMap nodeMap = tempNode.getAttributes();
						for (int i = 0; i < nodeMap.getLength(); i++) {
							Node node = nodeMap.item(i);
							if (node.getNodeName().equals("android:minSdkVersion")) {
								ServiceElement pak = new ServiceElement(node.getNodeValue(), ManifestElementType.SDK);
								selectedElements.add(pak);
							}
						}
					}
				} else if (selectFeature && tempNode.getNodeName().contains("uses-feature")) {
					// get node name and value
					if (tempNode.hasAttributes()) {
						// get attributes names and values
						NamedNodeMap nodeMap = tempNode.getAttributes();
						for (int i = 0; i < nodeMap.getLength(); i++) {
							Node node = nodeMap.item(i);
							if (node.getNodeName().equals("android:name")) {
								ServiceElement pak = new ServiceElement(node.getNodeValue(),
										ManifestElementType.FEATURE);
								selectedElements.add(pak);
							}
						}
					}
				} else if (selectContentProvider && tempNode.getNodeName().contains("provider")) {
					// get node name and value
					if (tempNode.hasAttributes()) {
						// get attributes names and values
						NamedNodeMap nodeMap = tempNode.getAttributes();
						for (int i = 0; i < nodeMap.getLength(); i++) {
							Node node = nodeMap.item(i);
							if (node.getNodeName().equals("android:name")) {
								boolean selectIgnorePackage = Activator.getDefault().getPreferenceStore()
										.getBoolean(AndroidAdapterPreferencePage.IGNORE_PACKAGE);;
								String contentProviderName = "";
								// assign a name based on ignore path or not
								if (selectIgnorePackage) {
									String[] tokens = node.getNodeValue().split("\\.");
									if (tokens.length >= 1) {
										contentProviderName = tokens[tokens.length - 1];
									}else {
										contentProviderName =  node.getNodeValue();
									}
								} else {
									contentProviderName = node.getNodeValue();
								}
								ServiceElement pak = new ServiceElement(contentProviderName,
										ManifestElementType.CONTENT_PRPVIDER);
								selectedElements.add(pak);
							}
						}
					}
				}

				// check nested nodes
				if (tempNode.hasChildNodes()) {
					getElements(tempNode.getChildNodes(), selectedElements);
				}
			}
		}
	}
}