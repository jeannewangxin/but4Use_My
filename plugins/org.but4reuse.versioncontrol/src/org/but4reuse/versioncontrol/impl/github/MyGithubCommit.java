package org.but4reuse.versioncontrol.impl.github;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.but4reuse.versioncontrol.IVersionControlCommit;

/**
 * A lightweight implementation containing the minimal required information
 * about a commit.
 * 
 * @author sandu.postaru
 * 
 */

public class MyGithubCommit implements IVersionControlCommit {

	private String sha;
	private String previousSha;
	private List<String> authors;
	private String message;
	private Path diskLocation;
	private Date date;
	private int nbAddedLines;

	public MyGithubCommit(String sha, String previousSha, List<String> authors, String message, Date date,
			int nbAddedLines) {
		this.sha = sha;
		this.previousSha = previousSha;
		this.authors = authors;
		this.message = message;
		this.diskLocation = null;
		this.date = date;
		this.nbAddedLines = nbAddedLines;
	}

	public String getSha() {
		return sha;
	}

	public String getPreviousCommitSha() {
		return previousSha;
	}

	public String getMessage() {
		return message;
	}

	public Path getDiskLocation() {
		return diskLocation;
	}

	public void setDiskLocation(Path diskLocation) {
		this.diskLocation = diskLocation;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public String toString() {
		return "MyGithubCommit [sha=" + sha + ", previousCommitSha=" + previousSha + ", message=" + message
				+ ", diskLocation=" + diskLocation + ", date=" + date + "]";
	}

	@Override
	public void setPreviousCommitSha(String previousSha) {
		this.previousSha = previousSha;
	}

	@Override
	public List<String> getAuthors() {
		return authors;
	}

	@Override
	public int getNbAddedLines() {
		return nbAddedLines;
	}

	@Override
	public void setNbAddedLines(int nbAddedLines) {
		this.nbAddedLines = nbAddedLines;
	}

}
