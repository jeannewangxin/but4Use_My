package org.but4reuse.wordclouds.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mcavallo.opencloud.filters.Filter;



/**
 * Class representing a tag cloud.
 * 
 * Further functionality has been added by Michael Knoll <mimi@kaktusteam.de>
 * 
 * @see org.mcavallo.opencloud.test.TestNewCloud
 */
public class NewCloud implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * NewTag name case
	 */
	public enum Case {
		LOWER,					// All tags are lower case
		UPPER,					// All tags are upper case
		CAPITALIZATION,			// First letter is upper case, other letters are lower case
		PRESERVE_CASE,			// NewTags are case insensitive and the case of the last entered
								// tag is used.
		CASE_SENSITIVE			// NewTags are case sensitive
	}
	
	/**
	 * Rounding method to convert a weight to an int.
	 */
	public enum Rounding {
		CEIL,		// Use Math.ceil()
		FLOOR,		// Use Math.floor()
		ROUND		// Use Math.round()
	}
	
    /** Map containing associations between tag names and NewTag objects. */
    private Map<String, NewTag> cloud = new HashMap<String, NewTag>();
    
	/** Format string representing the default link. */
	private String defaultLink = null;

    /** Minimum weight value. */
    private double minWeight = 0.0;

    /** Maximum weight value. */
    private double maxWeight = 4.0;

    /** Maximum number of tags present in the output cloud. */
    private int maxNewTagsToDisplay = 50;

    /** Minimum score value. NewTags having score under the threshold are excluded
     *  from the output cloud. */
    private double threshold = 0.0;

    /** Normalized threshold (between 0.0 and 1.0). NewTags having normalized score under the threshold are
     *  excluded from the output cloud. */
    private double normThreshold = 0.0;

    /** NewTag lifetime in milliseconds. Older tags are ignored. */
    private long tagLifetime = -1;
    
    /** Regular expression used to identify words in a text.
     *  By default there must be at least two alphanumeric characters with possibly
     *  a dash in between. */
    private String wordPattern = "[\\p{N}\\p{L}]+[\\p{Pd}]?[\\p{N}\\p{L}]+"; 
    
    /** Case of tags */
    private Case tagCase = Case.LOWER;
    
    /** Rounding method to convert weights to int. */
    private Rounding rounding = Rounding.CEIL;
    
    /** NewCloud locale */
    private Locale locale = Locale.getDefault();
    
    /** Filters to decide whether a tag should be added to the cloud. */
    private Set<Filter<NewTag>> inputFilters = new HashSet<Filter<NewTag>>();
    
    /** Filters to decide whether a tag should be displayed. */
    private Set<Filter<NewTag>> outputFilters = new HashSet<Filter<NewTag>>();
    
    /**
     * Default constructor.
     */
    public NewCloud() {
    }
    
    /**
     * Constructs a NewCloud object using the specified case for tag names.
     * @param tagCase NewTag case
     */
    public NewCloud(Case tagCase) {
    	setNewTagCase(tagCase);
    }

    /**
     * Constructs a NewCloud object using the specified locale.
     * @param locale Locale
     */
    public NewCloud(Locale locale) {
    	setLocale(locale);
    }

    /**
     * Constructs a NewCloud object using the specified case and locale.
     * @param tagCase NewTag case
     * @param locale Locale
     */
    public NewCloud(Case tagCase, Locale locale) {
    	setNewTagCase(tagCase);
    	setLocale(locale);
    }

    /**
     * Copy constructor.
     * @param other NewCloud to copy
     */
    public NewCloud(NewCloud other) {
    	this.setNewCloud(new HashMap<String, NewTag>(other.getNewCloud()));
        this.setMinWeight(other.getMinWeight());
        this.setMaxWeight(other.getMaxWeight());
        this.setMaxNewTagsToDisplay(other.getMaxNewTagsToDisplay());
        this.setThreshold(other.getThreshold());
        this.setNormThreshold(other.getNormThreshold());
        this.setWordPattern(other.getWordPattern());
        this.setNewTagLifetime(other.getNewTagLifetime());
        this.setNewTagCase(other.getNewTagCase());
        this.setLocale(other.getLocale());
        this.setDefaultLink(other.getDefaultLink());
        this.setRounding(other.getRounding());
        this.setInputFilters(new HashSet<Filter<NewTag>>(other.getInputFilters()));
        this.setOutputFilters(new HashSet<Filter<NewTag>>(other.getOutputFilters()));
    }

    /**
     * Adds a tag to the cloud.
     * @param tag
     */
    public void addNewTag(NewTag tag) {
    	if (! isValid(tag))
    		return;

    	String key = extractKey(tag.getName());

    	// check whether the tag satisfies the input filters
    	for (Filter<NewTag> filter : inputFilters) {
    		if (! filter.accept(tag)) {
    			return;
    		}
    	}

    	// if tag link is null, give a default link (if provided)
		if (tag.getLink() == null) {
			if (getDefaultLink() != null) {
				tag.setLink(String.format(getDefaultLink(), tag.getName()));
			}
		}

		// check whether a tag with the same name exists in the cloud
		NewTag existingNewTag = cloud.get(key);
		if (existingNewTag != null) {
			// update tag score
    		tag.add(existingNewTag.getScore());

    		// if tag link is null, keep existing link
    		if (tag.getLink() == null) {
   				tag.setLink(existingNewTag.getLink());
    		}

    		// update tag date
    		if (tag.getDate() == null || tag.getDate().before(existingNewTag.getDate())) {
    			tag.setDate(existingNewTag.getDate());
    		}
    	}

    	cloud.put(key, tag);
    }

	/**
     * Adds a tag with the specified name to the cloud.
     * @param name Name of the tag
     */
    public void addNewTag(String name) {
    	addNewTag(new NewTag(name));
    }

    /**
     * Adds a tag with the specified name and link to the cloud.
     * @param name NewTag name
     * @param link NewTag link
     */
    public void addNewTag(String name, String link) {
    	addNewTag(new NewTag(name, link));
    }

    /**
     * Add a collection of tags to the cloud.
     * @param tags
     */
    public void addNewTags(Collection<NewTag> tags) {
		if (tags == null)
			return;
		
		Iterator<NewTag> it = tags.iterator();
		while (it.hasNext()) {
			addNewTag(it.next());
		}
	}

	/**
	 * Extracts tags from a text. Each tag is assigned a link based on the provided format string.
	 * The format string can have zero or one format specifier, for example "/www.google.com/search?q=%s". If there isn't any format specifier
	 * the link is constant, otherwise the format specifier will be substituted with the tag name.
	 * @param text Text to parse
	 * @param linkFormat Format string that defines the tags link. It can have at most one parateter that will be subsituted with the tag name. 
	 */
	public void addText(String text, String linkFormat) {
		if (getWordPattern() == null || text == null)
			return;
		
    	Pattern pattern = Pattern.compile(getWordPattern());
		Matcher matcher = pattern.matcher(text);
		String word;
		
		if (linkFormat != null) {
			while (matcher.find()) {
				word = matcher.group(0);
				addNewTag(new NewTag(word, String.format(linkFormat, word)));
			}
		} else {
			while (matcher.find()) {
				word = matcher.group(0);
				addNewTag(new NewTag(word, null));
			}
		}
	}
	
	/**
	 * Extracts tags from a text. Each tag is assigned the default link.
	 * @param text Text to parse
	 */
	public void addText(String text) {
		addText(text, getDefaultLink());
	}
	
	/**
	 * Returns the tag with the given name, or null if
	 * the tag is not present in the cloud.
	 * @param name NewTag name
	 * @return The tag with the specified name
	 */
	public NewTag getNewTag(String name) {
		NewTag tag = cloud.get(extractKey(name));
		
		if (tag != null) {
			adjustNewTagCase(tag);
		}
		
		return tag;
	}

	/**
	 * Returns the tag with name equals to the given tag name, or null if
	 * the tag is not present in the cloud.
	 * @param tag NewTag to search
	 * @return The tag corresponding to the specified tag
	 */
	public NewTag getNewTag(NewTag tag) {
		if (tag == null)
			return null;
		
		return getNewTag(tag.getName());
	}

    /**
     * Removes a tag from the cloud.
     * @param name NewTag name
     */
    public void removeNewTag(String name) {
    	if (name == null)
    		return;
   	
    	cloud.remove(extractKey(name));
    }

    /**
     * Removes a tag from the cloud.
	 * @param tag NewTag to remove
	 */
	public void removeNewTag(NewTag tag) {
    	if (tag == null)
    		return;

    	removeNewTag(tag.getName());
	}

	/**
	 * Checks whether name and score value of the tag are consistent  .
	 * @return True if the tag is valid
	 */
	static public boolean isValid(NewTag tag) {
		return (tag != null && tag.getName() != null && tag.getName().length() != 0 &&
				! Double.isInfinite(tag.getScore()) && tag.getScore() > 0.0);
	}

    /**
     * Returns a list containing the tags to display,
     * sorted by name.
     * The weight of the returned tags is correctly set.
     * @return A list containing the output tags
     */
    public List<NewTag> tags() {
    	return tags(new NewTag.NameComparatorAsc());
    }
    
 	/**
     * Returns a list containing the tags to display,
     * sorted using the given comparator.
     * The weight of the returned tags is correctly set. 
     * @param comparator The Comparator that determines the ordering  
     * @return A list containing the output tags
	 */
	public List<NewTag> tags(Comparator<? super NewTag> comparator) {
		List<NewTag> result = getOutputNewTags();
		Collections.sort(result, comparator);
		return result;
	}

    /**
     * Returns the list of tags composing the resulting cloud. 
     * @return List of tags to display.
     */
    protected List<NewTag> getOutputNewTags() {
    	List<NewTag> emptyList = new LinkedList<NewTag>();
    	
    	if (getNewCloud() == null)
    		return emptyList;
    	
		double max = 0.0;
		Date now = new Date();
		List<NewTag> result = new LinkedList<NewTag>();
    	NewTag tag;
    	
		Iterator<NewTag> it = getNewCloud().values().iterator();
    	while (it.hasNext()) {
    		tag = it.next();
    		
    		// Removes non valid tags from the cloud
    		if (! isValid(tag)) {
    			it.remove();
    			continue;
    		}
    		
    		// Ignores tags with score under the threshold
    		if (tag.getScore() < getThreshold()) {
    			continue;
    		}
    		
    		// Ignores too old tags
    		if (getNewTagLifetime() > 0 && tag.getDate() != null && (now.getTime() - tag.getDate().getTime()) > getNewTagLifetime()) {
    			continue;
    		}

    		// Ignores tags not accepted by one or more output filters
    		if (isOutputNewTagFiltered(tag)) {
    			continue;
    		}

    		// Adds the tag to the temporary list
    		result.add(tag);
    		
    		// Updates max score
    		if (tag.getScore() > max) {
    			max = tag.getScore();
    		}
    	}

		if (Double.isInfinite(max) || Double.isNaN(max) || max <= 0.0)
			return emptyList;

		it = result.iterator();
		while (it.hasNext()) {
			tag = it.next();
			
			// Calculates normalized score
			tag.normalize(max);
			
			// Ignores tags with score under the threshold
    		if (tag.getNormScore() < getNormThreshold()) {
    			it.remove();
    			continue;
    		}
    		
    		// Sets the tag weight basing on the normalized score
    		tag.setWeight(getMinWeight() + tag.getNormScore() * (getMaxWeight() - getMinWeight()));
		}

    	result = removeExceedingNewTags(result);
    	
    	return result;
    }
    
	/**
	 * Returns a list containing all tags present in the cloud,
	 * sorted using the given comparator.
     * The weight of the returned tags is not set. 
	 * @param comparator The Comparator that determines the ordering
	 * @return A List containing all cloud tags
	 */
	public List<NewTag> allNewTags(Comparator<? super NewTag> comparator) {
		List<NewTag> result = allNewTags();
		Collections.sort(result, comparator);
		return result;
	}

 	/**
	 * Returns a list containing all tags present in the cloud. 
     * The weight of the returned tags is not set. 
   	 * @return A List containing all cloud tags
	 */
	public List<NewTag> allNewTags() {
		return new ArrayList<NewTag>(getNewCloud().values());
	}
	
	/**
	 * Returns tags as an array of strings
	 * @return Contained tags as an array of strings
	 */
	public String[] getNewTagsAsStringArray() {
		List<NewTag> allNewTags = this.allNewTags();
		String[] tags = new String[allNewTags.size()];
		for (int i = 0; i < allNewTags.size(); i++) {
			tags[i] = allNewTags.get(i).getName();
		}
		return tags;
	}
	
	/**
	 * Returns true, if given tag name is contained by this tag cloud
	 * @param name NewTag name to be searched within tag cloud
	 * @return True, if a tag with given name is contained by this tag cloud.
	 */
	public Boolean containsName(String name) {
		if (cloud.get(this.extractKey(name)) != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return The total number of tags contained in the cloud
	 */
	public int size() {
		if (getNewCloud() == null) {
			return 0;
		} else {
			return getNewCloud().values().size();
		}
	}

	/**
	 * Removes all tags in the cloud.
	 */
	public void clear() {
		if (getNewCloud() != null) {
			getNewCloud().clear();
		}
	}

	/**
	 * Extracts a map key from the tag name.
	 * @param tagName The tag name
	 * @return The string to use as map key 
	 */
	protected String extractKey(String tagName) {
    	if (tagCase == Case.CASE_SENSITIVE) {
    		return tagName;
    	} else {
    		// if the tag cloud is case insensitive
    		// use the lowercased name
    		return tagName.toLowerCase(locale);
    	}
	}

	/**
	 * Modifies the tag case basing on case setting
	 * @param tag The tag to modify
	 */
	protected void adjustNewTagCase(NewTag tag) {
    	if (tagCase == Case.LOWER) {
    		tag.setName(tag.getName().toLowerCase(locale));
    	} else if (tagCase == Case.UPPER) {
    		tag.setName(tag.getName().toUpperCase(locale));
    	} else if (tagCase == Case.CAPITALIZATION) {
    		tag.setName(capitalize(tag.getName()));
    	}
	}
	
	/**
	 * Returns a string where the first letter is upper case, the other letters are lower case.
	 * @param s
	 * @return The capitalized string
	 */
	protected String capitalize(String s) {
    	if (s.length() == 0) {
    		return s;
    	} else {
    		return s.substring(0, 1).toUpperCase(locale) + s.substring(1).toLowerCase(locale);
    	}
	}
	
	/**
	 * Removes the exceeding tags when the resulting cloud has more tags
	 * than the maximum allowed, and adjust the case of the tags. 
	 * @param tags List of tags
	 */
	protected List<NewTag> removeExceedingNewTags(List<NewTag> tags) {
		if (getMaxNewTagsToDisplay() < 0 || size() <= getMaxNewTagsToDisplay()) {
			// only adjusts tag case
	    	Iterator<NewTag> it = tags.iterator();  	
	    	while (it.hasNext()) {
	    		adjustNewTagCase(it.next());
	    	}
	    	
	    	return tags;
		} else {
			// removes less important elements and adjusts tag case
			List<NewTag> result = new LinkedList<NewTag>();
			
			Collections.sort(tags, new NewTag.ScoreComparatorDesc());
			
			NewTag tag;
			int counter = 1;
			
	    	Iterator<NewTag> it = tags.iterator();  	
	    	while (it.hasNext()) {
	    		tag = it.next();
	    		
	    		if (counter <= getMaxNewTagsToDisplay()) {
	    			adjustNewTagCase(tag);
	    			result.add(tag);
	    		} else {
	    			break;
	    		}
	    		
	    		counter++;
	    	}
	    	
	    	return result;
		}
	}

	/**
	 * Checks whether a tag to display satisfies output filters. 
	 * @param tag The tag to check
	 * @return True if the tag should be discarded, false if it should be accepted
	 */
	protected boolean isOutputNewTagFiltered(NewTag tag) {
		if (getOutputFilters() == null)
			return false;
		
   		for (Filter<NewTag> filter : getOutputFilters()) {
   			if (! filter.accept(tag)) {
   				return true;
   			}
   		}
   		
   		return false;
	}

	/**
	 * @return The maximum number of tags to display in the cloud
	 */
	public int getMaxNewTagsToDisplay() {
		return maxNewTagsToDisplay;
	}

	/**
	 * Sets the maximum number of tags to display in the cloud.
	 * If the argument is negative the number of displayed tags will not be limited.
	 * @param maxNewTagsToDisplay The number of tags
	 */
	public void setMaxNewTagsToDisplay(int maxNewTagsToDisplay) {
		this.maxNewTagsToDisplay = maxNewTagsToDisplay;
	}

	/**
	 * @return The normalized score threshold.
	 */
	public double getNormThreshold() {
		return normThreshold;
	}

	/**
	 * Sets the normalized score threshold. NewTags with their normalized score under the threshold will not be displayed.
	 * @param threshold The threshold value
	 */
	public void setNormThreshold(double threshold) {
		this.normThreshold = threshold;
	}

	/**
	 * @return The score threshold.
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * Sets the score threshold. NewTags with their score under the threshold will not be displayed.
	 * @param threshold The threshold value
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return the wordPattern
	 */
	public String getWordPattern() {
		return wordPattern;
	}

	/**
	 * @param wordPattern The wordPattern to set
	 */
	public void setWordPattern(String wordPattern) {
		this.wordPattern = wordPattern;
	}

	/**
	 * Adds an input filter.
	 * @param filter the filter to add
	 */
	public void addInputFilter(Filter<NewTag> filter) {
		inputFilters.add(filter);
	}

	/**
	 * Removes an input filter.
	 * @param filter The filter to remove
	 */
	public void removeInputFilter(Filter<NewTag> filter) {
		inputFilters.remove(filter);
	}

	/**
	 * Removes output filters belonging to the given class.
	 * @param cls The class of filters to remove
	 */
	public void removeInputFilters(Class<?> cls) {
		if (getInputFilters() == null)
			return;
		
		Iterator<Filter<NewTag>> it = getInputFilters().iterator();
    	while (it.hasNext()) {
    		if (cls.isInstance(it.next())) {
    			it.remove();
    		}
    	}
	}

	/**
	 * Removes all input filters.
	 */
	public void clearInputFilters() {
		inputFilters.clear();
	}

	/**
	 * Adds an output filter.
	 * @param filter The filter to add
	 */
	public void addOutputFilter(Filter<NewTag> filter) {
		outputFilters.add(filter);
	}

	/**
	 * Removes an output filter.
	 * @param filter The filter to remove
	 */
	public void removeOutputFilter(Filter<NewTag> filter) {
		outputFilters.remove(filter);
	}

	/**
	 * Removes output filters belonging to the given class.
	 * @param cls The class of filters to remove
	 */
	public void removeOutputFilters(Class<?> cls) {
		if (getOutputFilters() == null)
			return;
		
		Iterator<Filter<NewTag>> it = getOutputFilters().iterator();
    	while (it.hasNext()) {
    		if (cls.isInstance(it.next())) {
    			it.remove();
    		}
    	}
	}

	/**
	 * Removes all output filters.
	 */
	public void clearOutputFilters() {
		outputFilters.clear();
	}

	/**
     * Returns the complete map of tags present in the cloud.
     * NewTag weights are not set.
     * @return The tag map
     */
    protected Map<String, NewTag> getNewCloud() {
    	return cloud;
    }
    
	/**
	 * Sets the cloud map structure.
	 * @param cloud the cloud to set
	 */
	protected void setNewCloud(Map<String, NewTag> cloud) {
		this.cloud = cloud;
	}

	/**
	 * @return The input filters set
	 */
	public Set<Filter<NewTag>> getInputFilters() {
		return inputFilters;
	}

	/**
	 * Sets the input filters set.
	 * @param inputFilters The input filters set
	 */
	protected void setInputFilters(Set<Filter<NewTag>> inputFilters) {
		this.inputFilters = inputFilters;
	}

	/**
	 * @return The output filters set
	 */
	public Set<Filter<NewTag>> getOutputFilters() {
		return outputFilters;
	}

	/**
	 * Sets the output filters set.
	 * @param outputFilters The output filters set
	 */
	public void setOutputFilters(Set<Filter<NewTag>> outputFilters) {
		this.outputFilters = outputFilters;
	}

	/**
	 * @return The maximum lifetime of a tag in milliseconds
	 */
	public long getNewTagLifetime() {
		return tagLifetime;
	}

	/**
	 * Sets the maximum lifetime of a tag in milliseconds. Old tags are removed.
	 * @param tagLifetime the tagLifetime to set
	 */
	public void setNewTagLifetime(long tagLifetime) {
		this.tagLifetime = tagLifetime;
	}

	/**
	 * @return The tag case
	 */
	public Case getNewTagCase() {
		return tagCase;
	}

	/**
	 * Sets the case of the tags. To have a consistent behavior the case must be set
	 * before any tag is added to the cloud.
	 * @param tagCase The tag case to set
	 */
	public void setNewTagCase(Case tagCase) {
		this.tagCase = tagCase;
	}

	/**
	 * @return The locale associated with the cloud
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the locale of the cloud. This have to be set only if different from the
	 * default locale. To have a consistent behavior the locale must be set
	 * before any tag is added to the cloud.
	 * @param locale The locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return The format string representing the default link
	 */
	public String getDefaultLink() {
		return defaultLink;
	}

	/**
	 * Sets the format string representing the default link (e.g. "/www.google.com/search?q=%s").
	 * The first format specifier will be substituted b the tag name.
	 * @param defaultLink Format string representing the default link (e.g. "/www.google.com/search?q=%s")
	 */
	public void setDefaultLink(String defaultLink) {
		this.defaultLink = defaultLink;
	}

	/**
	 * @return The minimum weight value
	 */
	public double getMinWeight() {
		return minWeight;
	}

	/**
	 * @param minWeight The minimum weight value
	 */
	public void setMinWeight(double minWeight) {
		this.minWeight = minWeight;
	}

	/**
	 * @return The maximum weight value
	 */
	public double getMaxWeight() {
		return maxWeight;
	}

	/**
	 * @param maxWeight The maximum weight value
	 */
	public void setMaxWeight(double maxWeight) {
		this.maxWeight = maxWeight;
	}

	/**
	 * @return The rounding method
	 */
	public Rounding getRounding() {
		return rounding;
	}

	/**
	 * @param rounding The rounding method to set
	 */
	public void setRounding(Rounding rounding) {
		this.rounding = rounding;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cloud == null) ? 0 : cloud.hashCode());
		result = prime * result
				+ ((defaultLink == null) ? 0 : defaultLink.hashCode());
		result = prime * result
				+ ((inputFilters == null) ? 0 : inputFilters.hashCode());
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + maxNewTagsToDisplay;
		long temp;
		temp = Double.doubleToLongBits(maxWeight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minWeight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(normThreshold);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((outputFilters == null) ? 0 : outputFilters.hashCode());
		result = prime * result
				+ ((rounding == null) ? 0 : rounding.hashCode());
		result = prime * result + ((tagCase == null) ? 0 : tagCase.hashCode());
		result = prime * result + (int) (tagLifetime ^ (tagLifetime >>> 32));
		temp = Double.doubleToLongBits(threshold);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((wordPattern == null) ? 0 : wordPattern.hashCode());
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
		final NewCloud other = (NewCloud) obj;
		if (cloud == null) {
			if (other.cloud != null)
				return false;
		} else if (!cloud.equals(other.cloud))
			return false;
		if (defaultLink == null) {
			if (other.defaultLink != null)
				return false;
		} else if (!defaultLink.equals(other.defaultLink))
			return false;
		if (inputFilters == null) {
			if (other.inputFilters != null)
				return false;
		} else if (!inputFilters.equals(other.inputFilters))
			return false;
		if (locale == null) {
			if (other.locale != null)
				return false;
		} else if (!locale.equals(other.locale))
			return false;
		if (maxNewTagsToDisplay != other.maxNewTagsToDisplay)
			return false;
		if (Double.doubleToLongBits(maxWeight) != Double
				.doubleToLongBits(other.maxWeight))
			return false;
		if (Double.doubleToLongBits(minWeight) != Double
				.doubleToLongBits(other.minWeight))
			return false;
		if (Double.doubleToLongBits(normThreshold) != Double
				.doubleToLongBits(other.normThreshold))
			return false;
		if (outputFilters == null) {
			if (other.outputFilters != null)
				return false;
		} else if (!outputFilters.equals(other.outputFilters))
			return false;
		if (rounding == null) {
			if (other.rounding != null)
				return false;
		} else if (!rounding.equals(other.rounding))
			return false;
		if (tagCase == null) {
			if (other.tagCase != null)
				return false;
		} else if (!tagCase.equals(other.tagCase))
			return false;
		if (tagLifetime != other.tagLifetime)
			return false;
		if (Double.doubleToLongBits(threshold) != Double
				.doubleToLongBits(other.threshold))
			return false;
		if (wordPattern == null) {
			if (other.wordPattern != null)
				return false;
		} else if (!wordPattern.equals(other.wordPattern))
			return false;
		return true;
	}
	
	
	
	/**
	 * Returns tags that are not included in current tag cloud and given tag cloud
	 * 
	 * @param other NewTag cloud to compare with this cloud and get difference for
	 * @return NewTags not included in both tag clouds
	 */
	public List<NewTag> getDifference(NewCloud other) {
		List<NewTag> difference = new ArrayList<NewTag>();
		
		for (NewTag tag: other.tags()) {
			if (!this.containsName(tag.getName())) {
				difference.add(tag);
			}
		}
		
		for (NewTag tag : this.tags()) {
			if (!other.containsName(tag.getName())) {
				difference.add(tag);
			}
		}
		
		return difference;
	}

}
