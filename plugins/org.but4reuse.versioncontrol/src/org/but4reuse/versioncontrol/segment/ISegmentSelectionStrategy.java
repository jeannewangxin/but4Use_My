package org.but4reuse.versioncontrol.segment;

import java.util.List;

import org.but4reuse.versioncontrol.IVersionControlClient;
import org.but4reuse.versioncontrol.IVersionControlCommit;

public interface ISegmentSelectionStrategy {

	/**
	 * Get the segments associated to the strategy
	 * 
	 * @param client
	 * @return the list of the segments
	 */
	public List<Segment> getSegments(IVersionControlClient client, 
			IVersionControlCommit startPointCommit, IVersionControlCommit endPointCommit);
}
