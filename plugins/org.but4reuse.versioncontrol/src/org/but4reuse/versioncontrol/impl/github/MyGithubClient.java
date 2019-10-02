package org.but4reuse.versioncontrol.impl.github;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.but4reuse.utils.files.ZipUtils;
import org.but4reuse.versioncontrol.IVersionControlClient;
import org.but4reuse.versioncontrol.IVersionControlCommit;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryTag;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * Github Client that uses the Github API to recover metadata about a given
 * repository.
 * 
 * @author sandu.postaru, aarkoub
 * 
 */

public class MyGithubClient implements IVersionControlClient {

	private static final String githubUrl = "https://github.com";

	private GitHubClient client;
	private RepositoryService repositoryService;
	private CommitService commitService;

	private Map<String, IVersionControlCommit> commitCache;
	private List<IVersionControlCommit> mergeCache;
	private List<IVersionControlCommit> tagCache;
	private List<IVersionControlCommit> commits;

	private Repository repository;

	private String repositoryOwner;
	private String repositoryName;
	private String repositoryBranch;

	private boolean cacheFilled;

	private static String defaultToken = "9b9bd4df46244be3c763bfe81bb4c36fd1f32a0e";

	private void initializeClient() {

		client = new GitHubClient();

		repositoryService = new RepositoryService(client);
		commitService = new CommitService(client);

		commitCache = new HashMap<>();
		mergeCache = new ArrayList<>();
		tagCache = new ArrayList<>();

		cacheFilled = false;
	}

	public MyGithubClient(String user, String password) {

		initializeClient();
		client.setCredentials(user, password);
	}

	public MyGithubClient(String oAuth2Token) {

		initializeClient();
		client.setOAuth2Token(oAuth2Token);
	}

	public MyGithubClient() {
		this(defaultToken);
	}

	@Override
	public void setRepository(String owner, String name) throws IOException {

		repositoryOwner = owner;
		repositoryName = name;

		commitCache.clear();
		mergeCache.clear();
		tagCache.clear();

		cacheFilled = false;

		repository = repositoryService.getRepository(owner, name);
	}

	/**
	 * Gets all the commits for the current repository and fills the commits and
	 * merges cache.
	 * 
	 * @throws IOException
	 */
	private void fillCommitsAndMergesCaches() throws IOException {

		List<RepositoryCommit> commits;

		commits = commitService.getCommits(repository, repositoryBranch, "");

		/* commits are sorted from recent to less recent */
		/* go the other way around */

		String previousSha = null;

		/* get the commits */

		for (int i = commits.size() - 1; i >= 0; i--) {

			RepositoryCommit commit = commits.get(i);
			String sha = commit.getSha();
			String author = commit.getCommit().getAuthor().getName();
			String message = commit.getCommit().getMessage();
			Date date = commit.getCommit().getAuthor().getDate();
			int nbAddedLines = -1;

			List<Commit> parents = commit.getParents();
			Boolean isMerge = parents.size() > 1;

			MyGithubCommit current;
			List<String> authors = new ArrayList<>();

			if (isMerge) {
				/* base parent */
				Commit baseCommit = commit.getParents().get(0);
				Commit otherParentCommit = commit.getParents().get(1);
				authors.add(commitService.getCommit(repository, baseCommit.getSha()).getCommit().getAuthor().getName());
				authors.add(commitService.getCommit(repository, otherParentCommit.getSha()).getCommit().getAuthor()
						.getName());
				current = new MyGithubCommit(sha, baseCommit.getSha(), authors, message, date, nbAddedLines);
				mergeCache.add(current);
			} else {
				authors.add(author);
				current = new MyGithubCommit(sha, previousSha, authors, message, date, nbAddedLines);
			}

			commitCache.put(sha, current);
			previousSha = sha;
		}

		/* get the tags */

		List<RepositoryTag> tagsRef = repositoryService.getTags(repository);

		for (RepositoryTag tagRef : tagsRef) {

			/* actual commit tag */
			RepositoryCommit tag = commitService.getCommit(repository, tagRef.getCommit().getSha());
			String sha = tagRef.getCommit().getSha();
			String message = tagRef.getName();
			Date date = tag.getCommit().getAuthor().getDate();

			MyGithubCommit tagCommit = new MyGithubCommit(sha, null, null, message, date, 0);
			tagCache.add(tagCommit);
		}

		cacheFilled = true;

	}

	@Override
	public IVersionControlCommit getCommit(String sha) throws IOException {

		if (!cacheFilled) {
			fillCommitsAndMergesCaches();
		}

		return commitCache.get(sha);
	}

	@Override
	public IVersionControlCommit getPreviousCommitOf(String sha) throws IOException {

		if (!cacheFilled) {
			fillCommitsAndMergesCaches();
		}

		IVersionControlCommit current = commitCache.get(sha);
		IVersionControlCommit previous = null;

		if (current != null) {
			String previousSha = current.getPreviousCommitSha();
			return commitCache.get(previousSha);
		}

		return previous;

	}

	@Override
	public List<IVersionControlCommit> getMerges() throws IOException {

		if (!cacheFilled) {
			fillCommitsAndMergesCaches();
			Collections.sort(mergeCache, new CommitsCompare());
		}

		return mergeCache;
	}

	@Override
	public List<IVersionControlCommit> getTags() throws IOException {

		if (!cacheFilled) {
			fillCommitsAndMergesCaches();
		}

		return tagCache;
	}

	@Override
	public void downloadCommit(IVersionControlCommit commit, String path) throws IOException {

		List<IVersionControlCommit> commits = new ArrayList<>();

		commits.add(commit);
		downloadCommits(commits, path);
	}

	@Override
	public void downloadCommits(List<IVersionControlCommit> commits, String path) throws IOException {

		/* directory hierarchy */
		Path folderTarget = Paths.get(path, repositoryOwner, repositoryName);

		/* create directory hierarchy */
		Files.createDirectories(folderTarget);

		/* github url base */
		String urlBase = githubUrl + "/" + repositoryOwner + "/" + repositoryName + "/archive/";

		for (IVersionControlCommit commit : commits) {

			String sha = commit.getSha();

			/* archive location */
			Path commitTarget = Paths.get(folderTarget.toString(), sha + ".zip");

			/* extracted commit location */
			Path diskLocation = Paths.get(folderTarget.toAbsolutePath().toString(), repositoryName + "-" + sha);

			/* save disk location */
			commit.setDiskLocation(diskLocation);

			/* check folder existence */
			if (Files.exists(diskLocation, LinkOption.NOFOLLOW_LINKS)) {
				System.out.println("File already downloaded -> skipping\n" + diskLocation);
				continue;
			}

			/* archive url */
			URL commitURL = new URL(urlBase + sha + ".zip");

			System.out.println("Downloading: " + commitTarget);

			try (InputStream in = commitURL.openStream()) {
				Files.copy(in, commitTarget);
			}

			catch (FileAlreadyExistsException e) {
				System.out.println("File already downloaded -> Skipping");
			}

			System.out.println("Finished: " + commitTarget + "\n");

			/* unzip downloaded archive */
			System.out.println("Unzipping " + commitTarget + "\n");

			ZipUtils.unZip(commitTarget.toFile(), folderTarget.toFile());

			System.out.println("Unzipping done " + commitTarget + "\n");

			/* remove archive */

			System.out.println("Removing archive " + commitTarget);
			commitTarget.toFile().delete();
			System.out.println("Removing done " + commitTarget + "\n");
		}

	}

	@Override
	public int getRemainingRequests() {
		return client.getRemainingRequests();
	}

	@Override
	public List<IVersionControlCommit> filterCommits(List<IVersionControlCommit> commits, String startSHA,
			String endSHA) {

		int i;

		/* we use dates since the commits are in chronological order */
		/* this avoids breaking the fillCommitsAndMergesCaches() method */

		Collections.sort(commits, new CommitsCompare());

		Date startDate = commitCache.get(startSHA).getDate();
		Date endDate = commitCache.get(endSHA).getDate();

		List<IVersionControlCommit> filteredCommits = new ArrayList<>();

		/* find the start point */
		for (i = 0; i < commits.size(); i++) {
			IVersionControlCommit commit = commits.get(i);

			if (commit.getDate().compareTo(startDate) >= 0) {
				filteredCommits.add(commit);
				break;
			}
		}

		/* add all the middle commits */
		for (int j = i + 1; j < commits.size(); j++) {
			IVersionControlCommit commit = commits.get(j);

			if (commit.getDate().compareTo(endDate) > 0) {
				break;
			}
			filteredCommits.add(commit);
		}

		return filteredCommits;
	}

	@Override
	public List<IVersionControlCommit> filterOnlyPullRequests(List<IVersionControlCommit> merges) {

		PullRequestService pullRequestService = new PullRequestService(client);
		List<IVersionControlCommit> mergesPullRequests = new ArrayList<>();

		try {
			// getting all the pull requests of this repository
			List<PullRequest> pullRequests = pullRequestService.getPullRequests(repository, "all");

			// keeping the merges corresponding to pull requests only
			for (PullRequest pr : pullRequests) {
				for (IVersionControlCommit merge : merges) {
					if (pr.getMergedAt() != null && merge.getPreviousCommitSha().equals(pr.getBase().getSha())) {
						mergesPullRequests.add(merge);
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mergesPullRequests;

	}

	@Override
	public List<IVersionControlCommit> getCommits() throws IOException {

		if (!cacheFilled) {
			fillCommitsAndMergesCaches();
			commits = new ArrayList<IVersionControlCommit>(commitCache.values());
			Collections.sort(commits, new CommitsCompare());
		}

		return commits;
	}

	/**
	 * Compare the commits by their dates
	 *
	 * @author aarkoub
	 *
	 */
	class CommitsCompare implements Comparator<IVersionControlCommit> {

		@Override
		public int compare(IVersionControlCommit o1, IVersionControlCommit o2) {
			return o1.getDate().compareTo(o2.getDate());
		}

	}

	@Override
	public boolean commitExists(String sha) {
		return commitCache.get(sha) != null;
	}

	@Override
	public void setNbAddedLinesToCommit(IVersionControlCommit commit) {
		try {
			commit.setNbAddedLines(commitService.getCommit(repository, commit.getSha()).getStats().getAdditions());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getBranchesNames() {

		if (repositoryService != null) {

			List<String> branchesNames = new ArrayList<>();

			try {
				for (RepositoryBranch branch : repositoryService.getBranches(repository))
					branchesNames.add(branch.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return branchesNames;
		}
		return null;
	}

	@Override
	public void setBranch(String branch) {
		repositoryBranch = branch;
	}

	@Override
	public String getDefaultBranch() {
		if (repository != null)
			return repository.getDefaultBranch();
		return null;
	}

}
