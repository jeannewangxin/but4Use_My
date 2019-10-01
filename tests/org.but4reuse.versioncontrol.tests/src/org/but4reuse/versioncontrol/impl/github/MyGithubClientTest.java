package org.but4reuse.versioncontrol.impl.github;

import java.io.IOException;

import org.but4reuse.versioncontrol.IVersionControlClient;
import org.but4reuse.versioncontrol.IVersionControlCommit;
import org.junit.Assert;
import org.junit.Test;

public class MyGithubClientTest {

	private static final String COMMIT_SHA = "933def2704ddd261a93659f45378d9fbe0e12c84";

	@Test
	public void testGetCommit() {
		IVersionControlClient client = new MyGithubClient();
		try {
			client.setRepository("but4reuse", "but4reuse");
			client.setBranch("master");
			IVersionControlCommit commit = client.getCommit(COMMIT_SHA);
			String comment = commit.getMessage();
			Assert.assertEquals(comment.contains("Refactoring save wordcloud"), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
