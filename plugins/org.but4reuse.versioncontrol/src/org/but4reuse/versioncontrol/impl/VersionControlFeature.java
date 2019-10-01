package org.but4reuse.versioncontrol.impl;

import java.util.List;

import org.but4reuse.adapters.IElement;
import org.but4reuse.versioncontrol.IVersionControlCommit;
import org.but4reuse.versioncontrol.IVersionControlFeature;
import org.but4reuse.versioncontrol.utils.Utils;

/**
 * Representation of a identified feature containing all the necessary elements
 * that allow the extraction of a list of feature names
 *
 * @author sandu.postaru
 *
 */

public class VersionControlFeature implements IVersionControlFeature {

	private IVersionControlCommit endCommit;
	private IVersionControlCommit startCommit;
	private List<IElement> endCommitElements;
	private List<IElement> endCommitPenalizingElements;

	private List<IElement> startCommitElements;
	private List<IElement> startCommitPenalizingElements;

	private int nbEndCommitElements;
	private int nbStartCommitElements;

	private int nbAddedWords;

	private List<String> contributors;

	public VersionControlFeature(IVersionControlCommit endCommit, IVersionControlCommit startCommit,
			List<IElement> endCommitElements, List<IElement> endCommitPenalizingElements,
			List<IElement> startCommitElements, List<IElement> startCommitPenalizingElements,
			List<String> contributors) {

		this.endCommit = endCommit;
		this.startCommit = startCommit;
		this.endCommitElements = endCommitElements;
		this.endCommitPenalizingElements = endCommitPenalizingElements;
		this.startCommitElements = startCommitElements;
		this.startCommitPenalizingElements = startCommitPenalizingElements;
		nbEndCommitElements = endCommitElements.size();
		nbStartCommitElements = startCommitElements.size();
		this.contributors = contributors;
		nbAddedWords = -1;
	}

	@Override
	public IVersionControlCommit getEndCommit() {
		return endCommit;
	}

	@Override
	public IVersionControlCommit getStartCommit() {
		return startCommit;
	}

	@Override
	public List<IElement> getEndCommitElements() {
		return endCommitElements;
	}

	@Override
	public List<IElement> getStartCommitElements() {
		return startCommitElements;
	}

	@Override
	public List<IElement> getEndCommitPenalizingElements() {
		return endCommitPenalizingElements;
	}

	@Override
	public List<IElement> getStartCommitPenalizingElements() {
		return startCommitPenalizingElements;
	}

	@Override
	public int getNbEndCommitElements() {
		return nbEndCommitElements;
	}

	@Override
	public int getNbStartCommitElements() {
		return nbStartCommitElements;
	}

	@Override
	public int getNbAddedWords() {

		if (nbAddedWords == -1)
			nbAddedWords = Utils.getListWords(endCommitElements, endCommitPenalizingElements).size();

		return nbAddedWords;

	}

	@Override
	public List<String> getContributors() {
		return contributors;
	}

}
