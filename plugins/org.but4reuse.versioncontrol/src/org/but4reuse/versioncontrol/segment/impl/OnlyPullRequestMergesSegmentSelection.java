package org.but4reuse.versioncontrol.segment.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.but4reuse.versioncontrol.IVersionControlClient;
import org.but4reuse.versioncontrol.IVersionControlCommit;
import org.but4reuse.versioncontrol.segment.ISegmentSelectionStrategy;
import org.but4reuse.versioncontrol.segment.Segment;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class OnlyPullRequestMergesSegmentSelection implements ISegmentSelectionStrategy {

	@Override
	public List<Segment> getSegments(final IVersionControlClient client, IVersionControlCommit startPointCommit,
			IVersionControlCommit endPointCommit) {

		List<Segment> segments = new ArrayList<>();
		Segment segment = null;

		final List<IVersionControlCommit> merges = new ArrayList<>();
		List<IVersionControlCommit> pullRequestMerges;

		Shell currentShell = Display.getCurrent().getActiveShell();
		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(currentShell);

		// get merge information | network access
		try {

			progressDialog.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					int totalWork = 1;
					monitor.beginTask("Fetching version control metadata", totalWork);

					/* set working repository */
					try {
						List<IVersionControlCommit> clientMerges = client.getMerges();
						if (clientMerges != null)
							merges.addAll(clientMerges);
						monitor.worked(1);

					} catch (IOException e) {
						// TODO PROPER ERROR MANAGEMENT
						// EMPTY REPOSITORY
						e.printStackTrace();
					}
					monitor.done();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		// we keep the merges between the start point SHA and the end point SHA
		List<IVersionControlCommit> mergesList = client.filterCommits(merges, startPointCommit.getSha(),
				endPointCommit.getSha());

		// we keep the merges corresponding to pull requests only
		pullRequestMerges = client.filterOnlyPullRequests(mergesList);

		// creating the segments
		for (IVersionControlCommit pullRequestMerge : pullRequestMerges) {

			try {
				segment = new Segment(client.getCommit(pullRequestMerge.getPreviousCommitSha()), pullRequestMerge);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			segments.add(segment);
		}

		return segments;
	}

	@Override
	public String toString() {
		return "Merges corresponding to pull requests only";
	}

}
