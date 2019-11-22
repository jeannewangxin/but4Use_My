package org.but4reuse.wordclouds.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

public class NewTag implements Serializable {

	int nb_feature;

	public NewTag(String word,double score,int nb_feature) {
		this.nb_feature = nb_feature;
	}

	public int getNb_feature() {
		return nb_feature;
	}

	public void setNb_feature(int nb_feature) {
		this.nb_feature = nb_feature;
	}

	private static final long serialVersionUID = 1L;

	/** NewTag name */
	private String name = null;
	
	/** Link associated with the tag */
	private String link = null;
	
	/** Numerical value associated with the tag */
	private double score = 1.0;
	
	/** Normalized score value (from 0.0 to 1.0) */
	transient private double normScore = 0.0;
	
	/** Level of importance within the cloud (higher scores correspond to higher weights) */
	transient private double weight = 0;
	
	/** Creation date of the tag */
	private Date date = new Date();

	/**
	 * Default constructor
	 */
	public NewTag()
	{
	}

	/**
	 * Constructs a NewTag using the specified name
	 * @param name NewTag name
	 */
	public NewTag(String name)
	{
		setName(name);
	}

	/**
	 * Constructs a NewTag using the specified name and link
	 * @param name NewTag name
	 * @param link NewTag link
	 */
	public NewTag(String name, String link)
	{
		setName(name);
		setLink(link);
	}

	/**
	 * Constructs a NewTag using the specified name and score
	 * @param name NewTag name
	 * @param score NewTag score
	 */
	public NewTag(String name, double score)
	{
		setName(name);
		setScore(score);
	}

	/**
	 * Constructs a NewTag using the specified name, link and score
	 * @param name NewTag name
	 * @param link NewTag link
	 * @param score NewTag score
	 */
	public NewTag(String name, String link, double score)
	{
		setName(name);
		setLink(link);
		setScore(score);
	}

	/**
	 * Constructs a NewTag using the specified name, link and creation date
	 * @param name NewTag name
	 * @param link NewTag link
	 * @param date NewTag creation date
	 */
	public NewTag(String name, String link, Date date)
	{
		setName(name);
		setLink(link);
		setDate(date);
	}

	/**
	 * Constructs a NewTag using the specified name, link, score and creation date
	 * @param name NewTag name
	 * @param link NewTag link
	 * @param score NewTag score
	 * @param date NewTag date
	 */
	public NewTag(String name, String link, double score, Date date)
	{
		setName(name);
		setLink(link);
		setScore(score);
		setDate(date);
	}

	/**
	 * Copy constructor
	 * @param tag NewTag to copy
	 */
	public NewTag(NewTag tag)
	{
		setName(tag.getName());
		setLink(tag.getLink());
		setScore(tag.getScore());
		setNormScore(tag.getNormScore());
		setWeight(getWeight());
		setDate(tag.getDate());
	}

	/**
	 * @return The tag weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Returns the tag weight as an int. The convertion is done with the Math.ceil() function.
	 * @return The tag weight converted to int
	 */
	public int getWeightInt() {
		return (int) Math.ceil(weight);
	}

	/**
	 * 
	 * @return The tag weight converted to int with the specified rounding method.
	 */
	public int getWeightInt(NewCloud.Rounding rounding) {
		if (rounding == NewCloud.Rounding.FLOOR) {
			return (int) Math.floor(weight);
		} else if (rounding == NewCloud.Rounding.ROUND) {
			return (int) Math.round(weight);
		} else { 
			return (int) Math.ceil(weight);
		}
	}

	/**
	 * Sets the tag weight
	 * @param weight The tag weight
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * @return The tag link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Sets the tag link
	 * @param link The tag link
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * @return The tag name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the tag name
	 * @param name The tag name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Adds the specified value to the tag score
	 * @param val Value to add
	 */
	public void add(double val) {
		score += val;
	}

	/**
	 * Multiplies the tag score by the specified factor
	 * @param factor
	 */
	public void multiply(double factor) {
		score *= factor;
	}

	/**
	 * Divides the tag score by the specified factor
	 * @param factor
	 */
	public void divide(double factor) {
		score /= factor;
	}

	/**
	 * Calculates the normalized score, by dividing the tag score by the specified factor.
	 * @param factor
	 */
	public void normalize(double factor) {
		normScore = score / factor;
	}

	/**
	 * @return The tag score
	 */
	public double getScore()
	{
		return score;
	}

	/**
	 * Sets the tag score
	 * @param score The tag score
	 */
	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * @return NewTag score converted to int with the Math.ceil() function
	 */
	public int getScoreInt() {
		return (int) Math.ceil(score);
	}

	/**
	 * @return NewTag score converted to int with the specified rounding method.
	 */
	public int getScoreInt(NewCloud.Rounding rounding) {
		if (rounding == NewCloud.Rounding.FLOOR) {
			return (int) Math.floor(score);
		} else if (rounding == NewCloud.Rounding.ROUND) {
			return (int) Math.round(score);
		} else { 
			return (int) Math.ceil(score);
		}
	}

	/**
	 * @return the normalized score of the tag
	 */
	public double getNormScore() {
		return normScore;
	}

	/**
	 * Sets the normalized score
	 * @param normScore the normScore to set
	 */
	public void setNormScore(double normScore) {
		this.normScore = normScore;
	}

	/**
	 * @return Normalized score converted to int with the Math.ceil() function
	 */
	public int getNormScoreInt() {
		return (int) Math.ceil(normScore);
	}

	/**
	 * @return Normalized score converted to int with the specified rounding method.
	 */
	public int getNormScoreInt(NewCloud.Rounding rounding) {
		if (rounding == NewCloud.Rounding.FLOOR) {
			return (int) Math.floor(normScore);
		} else if (rounding == NewCloud.Rounding.ROUND) {
			return (int) Math.round(normScore);
		} else { 
			return (int) Math.ceil(normScore);
		}
	}

	/**
	 * @return the tag date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the tag date
	 * @param date the tag date
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final NewTag other = (NewTag) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/**
	 * Compares two tags by name in ascending order
	 */
	static public class NameComparatorAsc implements Comparator<NewTag> {

		public int compare(NewTag o1, NewTag o2) {
			return o1.getName().compareToIgnoreCase(o2.getName());
		}
		
	}

	/**
	 * Compares two tags by name in descending order
	 */
	static public class NameComparatorDesc implements Comparator<NewTag> {

		public int compare(NewTag o1, NewTag o2) {
			return o2.getName().compareToIgnoreCase(o1.getName());
		}
		
	}

	/**
	 * Compares two tags by score in ascending order
	 */
	static public class ScoreComparatorAsc implements Comparator<NewTag> {

		public int compare(NewTag o1, NewTag o2) {
			int scoreComparison = Double.compare(o1.getScore(), o2.getScore());
			
			// if the score is the same sort by name
			if (scoreComparison == 0) {
				return (new NameComparatorAsc()).compare(o1, o2);
			} else {
				return scoreComparison;
			}
		}
	}

	/**
	 * Compares two tags by score in descending order
	 */
	static public class ScoreComparatorDesc implements Comparator<NewTag> {

		public int compare(NewTag o1, NewTag o2) {
			int scoreComparison = Double.compare(o2.getScore(), o1.getScore());
			
			// if the score is the same sort by name
			if (scoreComparison == 0) {
				return (new NameComparatorAsc()).compare(o1, o2);
			} else {
				return scoreComparison;
			}
		}
		
	}

}