package elements;

import java.io.File;
import java.net.URI;

import org.but4reuse.utils.files.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class UsingNSpaceDir extends CppElement {

	public UsingNSpaceDir(IASTNode node, CppElement parent, String text, String rawText) {
		super(node, parent, text, rawText, CppElementType.USING_NAME_SPACE_DIR);
	}
	
	/**
	 * Construct the using name space  element in the contained parent file.
	 */
	public void construct(File f) {

		try {
			FileUtils.appendToFile(f, "\n"+this.getRawText());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
