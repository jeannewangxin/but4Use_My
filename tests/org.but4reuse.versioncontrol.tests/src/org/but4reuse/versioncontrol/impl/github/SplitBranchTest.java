package org.but4reuse.versioncontrol.impl.github;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.sourcecode.JavaSourceCodeAdapter;
import org.but4reuse.versioncontrol.IVersionControlCommit;
import org.but4reuse.versioncontrol.utils.Utils;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SplitBranch
 * 
 * @author aarkoub
 *
 */
public class SplitBranchTest {

	IVersionControlCommit commit1;
	IVersionControlCommit commit2;
	List<IAdapter> adapters;

	@Before
	public void prepare() {
		// creation of the two object commits
		String absolutePath = new File("").getAbsolutePath();

		commit1 = new MyGithubCommit("commit1", null, null, null, null, 0);
		commit1.setDiskLocation(Paths.get(absolutePath, "examples/commit1"));

		commit2 = new MyGithubCommit("commit2", "commit1", null, null, null, 0);
		commit2.setDiskLocation(Paths.get(absolutePath, "examples/commit2"));

		// getting the adequate Java adapter
		JavaSourceCodeAdapter adapter = new JavaSourceCodeAdapter();
		adapters = new ArrayList<IAdapter>();
		adapters.add(adapter);
	}

	@Test
	public void numberChangedElementsTest() {

		// normal test
		assertEquals(16, Utils.getNbChangedElements(commit1, commit2, adapters));
		List<IElement> elements = Utils.getChangedElements(commit1, commit2, adapters);
		for (IElement e : elements) {
			System.out.println(e.getText());
		}

		// same content in the two commits
		assertEquals(0, Utils.getNbChangedElements(commit1, commit1, adapters));
		assertEquals(0, Utils.getNbChangedElements(commit2, commit2, adapters));

		/*
		 * StringBuilder downloadPath = new StringBuilder(new
		 * File("").getAbsolutePath()); downloadPath.append("/downloads"); File
		 * directory = new File(downloadPath.toString()); directory.mkdir();
		 * IVersionControlClient client = new MyGithubClient(); new
		 * Shell(Display.getCurrent());
		 * 
		 * try { client.setRepository("but4reuse", "but4reuse", "master");
		 * Assert.assertEquals(1140, Utils.splitBranchChangedElements(client,
		 * 1000, downloadPath.toString())); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

	}

	@Test
	public void numberNewWordsTest() {

		// normal test
		assertEquals(5, Utils.getNbAddedWords(commit1, commit2, adapters));
		List<String> words = Utils.getAddedWords(commit1, commit2, adapters);
		for (String w : words) {
			System.out.println(w);
		}

		// same content in the two commits
		assertEquals(0, Utils.getNbAddedWords(commit1, commit1, adapters));
		assertEquals(0, Utils.getNbAddedWords(commit2, commit2, adapters));

		/*
		 * StringBuilder downloadPath = new StringBuilder(new
		 * File("").getAbsolutePath()); downloadPath.append("/downloads"); File
		 * directory = new File(downloadPath.toString()); directory.mkdir();
		 * IVersionControlClient client = new MyGithubClient(); new
		 * Shell(Display.getCurrent());
		 * 
		 * try { client.setRepository("but4reuse", "but4reuse", "master");
		 * Assert.assertEquals(249, Utils.splitBranchWords(client, 200,
		 * downloadPath.toString())); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
	}

}
