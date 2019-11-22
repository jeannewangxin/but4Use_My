package org.but4reuse.versioncontrol.utils.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.but4reuse.adapters.IElement;
import org.mcavallo.opencloud.Tag;
import org.mcavallo.opencloud.Cloud;

import org.but4reuse.adapters.impl.AbstractElement;
import org.but4reuse.versioncontrol.IVersionControlCommit;
import org.but4reuse.versioncontrol.event.FeatureEvent;
import org.but4reuse.wordclouds.util.CloudRanking;
import org.but4reuse.wordclouds.util.Cloudifier;
import org.but4reuse.wordclouds.util.NewCloud;
import org.but4reuse.wordclouds.util.NewTag;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Utility fonctions used for timeline construction
 *
 * @author sandu.postaru
 *
 */

public class TimelineUtils {

	public static NewCloud getTimelineElements(List<IElement> elements, List<IElement> penalizingElements,int nb_feature) {
		List<String> mergeWords = new ArrayList<>();

		for (IElement element : elements) {
			mergeWords.addAll(((AbstractElement) element).getWords());
		}

		List<String> penalizingWords = new ArrayList<>();

		for (IElement element : penalizingElements) {
			penalizingWords.addAll(((AbstractElement) element).getWords());
		}

		List<List<String>> wordCollection = new ArrayList<List<String>>();
		HashMap<Integer,List<List<String>>> wordCollectionWithFeature = new HashMap<Integer,List<List<String>>>();
		wordCollection.add(mergeWords);
		wordCollection.add(penalizingWords);
		wordCollectionWithFeature.put(nb_feature, wordCollection);

		NewCloud cloud = Cloudifier.cloudifyTFIDFWithFeature(wordCollectionWithFeature, 0, new NullProgressMonitor());

		return cloud;
	}


	public static List<String> getTimelineWords(List<IElement> elements, List<IElement> penalizingElements,
			int maxNbWords,int nb_feature) {

		NewCloud cloud = TimelineUtils.getTimelineElements(elements, penalizingElements, nb_feature);

		// sort tags
		List<NewTag> tags = new CloudRanking(cloud).getNewRank();

		List<String> filteredWords = new ArrayList<>();

		for (int i = 0; i < maxNbWords && i < tags.size(); i++) {
			filteredWords.add(tags.get(i).getName());
		}

		return filteredWords;
	}


	public static List<FeatureEvent> getTimelineTags(List<IVersionControlCommit> tags) {
		List<FeatureEvent> tagEvents = new ArrayList<>();

		/* This will be a timeline event */
		/* The tag message will be contained in the addedFeatures */

		for (IVersionControlCommit tag : tags) {
			List<String> message = new ArrayList<String>();
			List<String> removed = Collections.emptyList();

			message.add(tag.getMessage());

			tagEvents.add(new FeatureEvent(FeatureEvent.Type.TAG, tag, tag, message, removed, 0, 0, 0, new NewCloud(),
					new NewCloud(), new ArrayList<String>()));
		}

		return tagEvents;
	}

}
