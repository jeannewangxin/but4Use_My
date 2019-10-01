package org.but4reuse.versioncontrol;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;

/**
 * Version Control Commit interface
 * 
 * @author sandu.postaru, aarkoub
 */

public interface IVersionControlCommit {

	/**
	 * @return the sha of the previous commit. If the commit is a merge than
	 *         returns the base commit sha, else the previous commit sha
	 */
	public String getPreviousCommitSha();

	/**
	 * @return the sha of the current commit
	 */
	public String getSha();

	/**
	 * @return the name of the author of this commit (authors if this commit is
	 *         a merge)
	 */
	public List<String> getAuthors();

	/**
	 * @return the commit message when published by the commit author
	 */
	public String getMessage();

	/**
	 * @return the disk location of the downloaded commit contents
	 */
	public Path getDiskLocation();

	/**
	 * Sets the download disk location of the current commit contents
	 * 
	 * @param diskLocation
	 */
	public void setDiskLocation(Path diskLocation);

	/**
	 * @return the date of commit submission
	 */
	public Date getDate();

	/**
	 * Sets the previous Sha of this commit
	 * 
	 * @param previousSha
	 */
	public void setPreviousCommitSha(String previousSha);

	/**
	 * @return the number of added lines
	 */
	public int getNbAddedLines();

	/**
	 * Set the number of added lines
	 * 
	 * @param nbAddedLines
	 */
	public void setNbAddedLines(int nbAddedLines);

}
