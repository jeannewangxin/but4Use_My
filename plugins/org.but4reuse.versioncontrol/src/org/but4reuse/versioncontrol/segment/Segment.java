package org.but4reuse.versioncontrol.segment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.but4reuse.versioncontrol.IVersionControlClient;
import org.but4reuse.versioncontrol.IVersionControlCommit;

/**
 * A segment is represented by a start commit and a end commit
 *
 * @author aarkoub
 *
 */
public class Segment {

	private IVersionControlCommit startCommit;
	private IVersionControlCommit endCommit;
	private List<String> developers = null;

	public Segment(IVersionControlCommit startCommit, IVersionControlCommit endCommit) {
		this.startCommit = startCommit;
		this.endCommit = endCommit;
	}

	@Override
	public String toString() {
		return "Start commit : " + startCommit.getSha() + " ; End commit : " + endCommit.getSha();
	}

	@Override
	/**
	 * if the two objects share the same start commit sha and the same end sha,
	 * then they are considered equal.
	 */
	public boolean equals(Object o) {

		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o.getClass() != this.getClass())
			return false;
		Segment s = (Segment) o;
		if (!s.startCommit.getSha().equals(this.startCommit.getSha())
				|| !s.endCommit.getSha().equals(this.endCommit.getSha()))
			return false;

		return true;
	}

	public List<String> getDevelopers(IVersionControlClient client) {

		if (developers == null) {

			Map<String, Integer> devMap = new HashMap<String, Integer>();
			Map<String, Integer> devCommits = new HashMap<String, Integer>();
			IVersionControlCommit currentCommit = endCommit;
			List<String> authors;
			Integer nbAddedLines;
			Integer nbCommits;

			// decreasing order
			// compares first the number of added lines, and if it is the same,
			// then the number of commits
			class ValueComparator implements Comparator<String> {

				private Map<String, Integer> map;
				private Map<String, Integer> devCommits;

				public void setMap(Map<String, Integer> map, Map<String, Integer> devCommits) {
					this.map = map;
					this.devCommits = devCommits;
				}

				@Override
				public int compare(String o1, String o2) {
					int val1 = map.get(o1).intValue();
					int val2 = map.get(o2).intValue();

					if (val1 > val2)
						return -1;
					if (val1 == val2) {

						Integer nbCommits1 = devCommits.get(o1).intValue();
						Integer nbCommits2 = devCommits.get(o2).intValue();

						if (nbCommits1 > nbCommits2)
							return -1;
						if (nbCommits1 == nbCommits2)
							return 0;
						return 1;
					}
					return 1;

				}

			}

			// for each commit of the segment
			while (true) {

				authors = currentCommit.getAuthors();

				// adding to the maps
				for (String author : authors) {

					nbAddedLines = devMap.get(author);

					if (nbAddedLines == null) {
						devMap.put(author, currentCommit.getNbAddedLines());
					} else {
						devMap.put(author, nbAddedLines + currentCommit.getNbAddedLines());
					}

					nbCommits = devCommits.get(author);

					if (nbCommits == null) {
						devCommits.put(author, 1);
					} else {
						devCommits.put(author, nbCommits + 1);
					}
				}

				if (currentCommit.getSha().equals(startCommit.getSha()))
					break;

				// getting the previous commit
				try {
					currentCommit = client.getCommit(currentCommit.getPreviousCommitSha());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			ValueComparator vc = new ValueComparator();
			vc.setMap(devMap, devCommits);

			Map<String, Integer> sortedMap = new TreeMap<String, Integer>(vc);
			sortedMap.putAll(devMap);
			developers = new ArrayList<>();
			developers.addAll(sortedMap.keySet());
		}

		return developers;
	}

	public IVersionControlCommit getStartCommit() {
		return startCommit;
	}

	public IVersionControlCommit getEndCommit() {
		return endCommit;
	}
}
