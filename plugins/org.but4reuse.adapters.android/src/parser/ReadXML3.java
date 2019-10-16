package parser;

import java.io.File;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.but4reuse.adapters.android.activator.Activator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jd.cli.Main;

public class ReadXML3 {

	public static void main(String[] args) {

		try {

			File file = new File("/Users/anasshatnawi/Desktop/jdeps/metrodroid.apk");
			File outputFolder = new File("/Users/anasshatnawi/Desktop/jdeps/metrodroid");
			unpackSrc(file, outputFolder);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	
	public static void unpackSrc(File apk, File outputFolder) {
		try {
			// dex2jar
//			URL url = Platform.getBundle(Activator.PLUGIN_ID).getEntry("org.but4reuse.adapters.android/unpackUtils/dex2jar/d2j-dex2jar.bat");
			
			File filed2j = new File("org.but4reuse.adapters.android/unpackUtils/dex2jar/d2j-dex2jar.bat");
			// d2j-dex2jar.bat -f -o output_jar.jar apk_to_decompile.apk
			File jar = File.createTempFile(apk.getName(), ".jar");
			String command = filed2j.getAbsolutePath() + " -f -o " + jar + " " + apk.getAbsolutePath();
			// do not add start... it won't show the console but it will
			// waitFor() "cmd /c start "
			Process p = Runtime.getRuntime().exec("cmd /c " + command);
			p.waitFor();
			p.destroy();

			// jd-cli
			File outputSrcFolder = new File(outputFolder, "src");
			Main.main(new String[]{"-od" ,outputSrcFolder.getAbsolutePath(), jar.getAbsolutePath()});
			
			// manifest
			URL url2 = Platform.getBundle(Activator.PLUGIN_ID).getEntry("unpackUtils/manifest/aapt.exe");
			File aapt = new File(FileLocator.toFileURL(url2).toURI());
			File outManifest = new File(outputFolder, "AndroidManifest.txt");
			command = aapt.getAbsolutePath() + " dump badging " + apk.getAbsolutePath() + " " + "AndroidManifest.xml > " + outManifest.getAbsolutePath();
			Process p2 = Runtime.getRuntime().exec("cmd /c " + command);
			p2.waitFor();
			p2.destroy();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private static void printNote(NodeList nodeList) {
		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
				if (tempNode.getNodeName().contains("uses-permission")) {
					System.err.println("\nNode Name =" + tempNode.getNodeName() + " [OPEN]");
					System.err.println("Node Value =" + tempNode.getTextContent());
					if (tempNode.hasAttributes()) {
						// get attributes names and values
						NamedNodeMap nodeMap = tempNode.getAttributes();
						for (int i = 0; i < nodeMap.getLength(); i++) {
							Node node = nodeMap.item(i);
							System.err.println("attr name : " + node.getNodeName());
							System.err.println("attr value : " + node.getNodeValue());
						}
					}
				}
				if (tempNode.hasChildNodes()) {	
					// loop again if has child nodes
					printNote(tempNode.getChildNodes());

				}
			}
		}
	}

}
