package org.but4reuse.wordclouds.util;

import java.util.Comparator;
import java.util.List;

import org.mcavallo.opencloud.Cloud;
import org.mcavallo.opencloud.Tag;

/**
 * Cloud ranking
 * 
 * @author jabier.martinez
 */
public class CloudRanking {

	private List<Tag> rank;
	private List<NewTag> newrank;

	// constructor to create ranking
	public CloudRanking(Cloud cloud) {
		setRank(cloud.allTags(new Comparator<Tag>() {
			@Override
			public int compare(Tag arg0, Tag arg1) {
				return Double.compare(arg1.getScore(), arg0.getScore());
			}
		}));
	}

	public List<Tag> getRank() {
		return rank;
	}

	public void setRank(List<Tag> rank) {
		this.rank = rank;
	}
	
	public CloudRanking(NewCloud cloud) {
		setNewRank(cloud.allNewTags(new Comparator<NewTag>() {
			@Override
			public int compare(NewTag o1, NewTag o2) {
				return Double.compare(o2.getScore(), o1.getScore());
			}
		}));
	}
	
	public List<NewTag> getNewRank() {
		return newrank;
	}

	public void setNewRank(List<NewTag> newrank) {
		this.newrank = newrank;
	}

}
