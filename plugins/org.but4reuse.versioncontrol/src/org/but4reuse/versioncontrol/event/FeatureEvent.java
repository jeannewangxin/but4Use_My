package org.but4reuse.versioncontrol.event;

import java.util.List;

import org.but4reuse.versioncontrol.IVersionControlCommit;
import org.but4reuse.wordclouds.util.NewCloud;
import org.mcavallo.opencloud.Cloud;

/**
 * A lightweight object containing information about a possible feature. Used in
 * timeline and interactive grid generation.
 * 
 * @author sandu.postaru, aarkoub
 * 
 */

public class FeatureEvent {

	public static enum Type {
		COMMIT, TAG
	};

	private IVersionControlCommit endCommit;
	private IVersionControlCommit startCommit;
	private List<String> addedFeatures;
	private List<String> removedFeatures;
	private Type type;
	private int nbAddedElements;
	private int nbRemovedElements;
	private int nbAddedWords;
	private NewCloud addedCloud;
	private NewCloud removedCloud;
	private List<String> contributors;

	public FeatureEvent(Type type, IVersionControlCommit startCommit, IVersionControlCommit endCommit,
			List<String> addedFeatures, List<String> removedFeatures, int nbAddedElements, int nbRemovedElements,
			int nbAddedWords, NewCloud addedCloud, NewCloud removedCloud, List<String> contributors) {
		this.type = type;
		this.endCommit = endCommit;
		this.startCommit = startCommit;
		this.addedFeatures = addedFeatures;
		this.removedFeatures = removedFeatures;
		this.nbAddedElements = nbAddedElements;
		this.nbRemovedElements = nbRemovedElements;
		this.nbAddedWords = nbAddedWords;
		this.addedCloud = addedCloud;
		this.removedCloud = removedCloud;
		this.contributors = contributors;
	}

	public IVersionControlCommit getEndCommit() {
		return endCommit;
	}

	public IVersionControlCommit getStartCommit() {
		return startCommit;
	}

	public List<String> getAddedFeatures() {
		return addedFeatures;
	}

	public List<String> getRemovedFeatures() {
		return removedFeatures;
	}

	public Type getType() {
		return type;
	}

	public int getNbAddedElements() {
		return nbAddedElements;
	}

	public int getNbRemovedElements() {
		return nbRemovedElements;
	}

	public int getNbAddedWords() {
		return nbAddedWords;
	}

	public NewCloud getAddedCloud() {
		return addedCloud;
	}

	public NewCloud getRemovedCloud() {
		return removedCloud;
	}

	public List<String> getContributors() {
		return contributors;
	}

}
