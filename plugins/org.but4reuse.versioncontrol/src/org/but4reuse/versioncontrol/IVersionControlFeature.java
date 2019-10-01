package org.but4reuse.versioncontrol;

import java.util.List;

import org.but4reuse.adapters.IElement;

/**
 * Version control feature interface
 *
 * @author sandu.postaru
 *
 */

public interface IVersionControlFeature {

	/**
	 * @return the end commit object
	 */
	public IVersionControlCommit getEndCommit();

	/**
	 * @return the start commit object
	 */
	public IVersionControlCommit getStartCommit();

	/**
	 * @return the elements concerning the end commit
	 */

	/**
	 * 
	 * @return the names of the main contributors of the feature
	 */
	public List<String> getContributors();

	public List<IElement> getEndCommitElements();

	/**
	 * @return the end commit penalizing elements used when applying the TD-IDF
	 *         algorithm
	 */
	public List<IElement> getEndCommitPenalizingElements();

	/**
	 * @return the elements concerning the start commit of the end commit
	 */
	public List<IElement> getStartCommitElements();

	/**
	 * @return the start commit penalizing elements used when applying the
	 *         TD-IDF algorithm
	 */
	public List<IElement> getStartCommitPenalizingElements();

	/**
	 * @return number of end commit elements
	 */
	public int getNbEndCommitElements();

	/**
	 * @return number of start commit elements
	 */
	public int getNbStartCommitElements();

	/**
	 * @return the number of added words
	 */
	public int getNbAddedWords();

}
