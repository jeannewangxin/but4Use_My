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

public class AllMergesSegmentSelection implements ISegmentSelectionStrategy {

	@Override
	public List<Segment> getSegments(final IVersionControlClient client, IVersionControlCommit startPointCommit,
			IVersionControlCommit endPointCommit) {

		List<Segment> segments = new ArrayList<>();
		Segment segment = null;

		Shell currentShell = Display.getCurrent().getActiveShell();

		final List<IVersionControlCommit> merges = new ArrayList<>();

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

		List<IVersionControlCommit> mergesList = client.filterCommits(merges, startPointCommit.getSha(),
				endPointCommit.getSha());

		// creating the segments
		for (IVersionControlCommit merge : mergesList) {
			try {
				segment = new Segment(client.getCommit(merge.getPreviousCommitSha()), merge);
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
		return "All merges";
	}

}
