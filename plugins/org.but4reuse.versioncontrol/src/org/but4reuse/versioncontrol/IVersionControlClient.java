package org.but4reuse.versioncontrol;

import java.io.IOException;
import java.util.List;

/**
 * Version Control Client Interface
 * 
 * @author sandu.postaru, aarkoub
 * 
 */

public interface IVersionControlClient {

	/**
	 * Set current working repository
	 * 
	 * @param owner
	 * @param name
	 * @param branch
	 * @throws IOException
	 */
	public void setRepository(String owner, String name) throws IOException;

	/**
	 * @param sha
	 *            commit id
	 * @return a commit structure for this id or @null if commit not found
	 */
	public IVersionControlCommit getCommit(String sha) throws IOException;

	/**
	 * @param sha
	 *            commit id
	 * @return the previous commit structure for this id or @null if commit not
	 *         found
	 */
	public IVersionControlCommit getPreviousCommitOf(String sha) throws IOException;

	/**
	 * @return a list of merges sha, for the current repository ordered from
	 *         recent to less recent
	 * @throws IOException
	 */
	public List<IVersionControlCommit> getMerges() throws IOException;

	/**
	 * @param commits
	 *            in chronological order since we use the dates of each commit!
	 * @return a filtered list of commits containing all the commits
	 *         between @param startSHA and @param endSHA
	 */
	public List<IVersionControlCommit> filterCommits(List<IVersionControlCommit> commits, String startSHA,
			String endSHA);

	/**
	 * 
	 * @param merges
	 *            in chronological order
	 * @return a list of merges corresponding to pull requests
	 */
	public List<IVersionControlCommit> filterOnlyPullRequests(List<IVersionControlCommit> merges);

	/**
	 * @return a list of tag commits, for the current repository ordered from
	 *         recent to less recent
	 * @throws IOException
	 */
	public List<IVersionControlCommit> getTags() throws IOException;

	/**
	 * Downloads a single commit at path location
	 * 
	 * @param commit
	 * @param path
	 * @throws IOException
	 */
	public void downloadCommit(IVersionControlCommit commit, String path) throws IOException;

	/**
	 * Downloads a list of commits at path location
	 * 
	 * @param commits
	 * @throws IOException
	 */
	public void downloadCommits(List<IVersionControlCommit> commits, String path) throws IOException;

	/**
	 * @return remaining API requests
	 */
	public int getRemainingRequests();

	/**
	 * Gets all the commits
	 * 
	 * @return the list of the commits
	 * @throws IOException
	 */
	public List<IVersionControlCommit> getCommits() throws IOException;

	/**
	 * Checks if the commit corresponding to this SHA exists
	 * 
	 * @param sha
	 * @return true if commit exists, else false
	 */
	public boolean commitExists(String sha);

	/**
	 * Set the number of added lines to the specific commit
	 * 
	 * @param commit
	 */
	public void setNbAddedLinesToCommit(IVersionControlCommit commit);

	/**
	 * 
	 * @return the list of the names of all repositories
	 */
	public List<String> getBranchesNames();

	/**
	 * Set the branch of the repository
	 * 
	 * @param branch
	 */
	public void setBranch(String branch);

	/**
	 * 
	 * @return the default branch
	 */
	public String getDefaultBranch();

}
