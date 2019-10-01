package org.but4reuse.versioncontrol.segment.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.versioncontrol.IVersionControlClient;
import org.but4reuse.versioncontrol.IVersionControlCommit;
import org.but4reuse.versioncontrol.segment.ISegmentSelectionStrategy;
import org.but4reuse.versioncontrol.segment.Segment;
import org.but4reuse.versioncontrol.utils.Utils;
import org.but4reuse.versioncontrol.utils.dialogs.GenericInputSelectionDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * Strategy that select the segments according to the number of changed elements
 *
 * @author aarkoub
 *
 */
public class ChangedElementsSegmentSelection implements ISegmentSelectionStrategy {

	@Override
	public List<Segment> getSegments(IVersionControlClient client, IVersionControlCommit startPointCommit,
			IVersionControlCommit endPointCommit) {

		List<Segment> segments = new ArrayList<>();
		MessageBox mb;

		int maxNumberChanges;
		int nbChanges = 0;
		IVersionControlCommit currentCommit, startCommit;
		Segment segment;

		List<IAdapter> adapters;
		List<IVersionControlCommit> commits;
		Shell currentShell = Display.getCurrent().getActiveShell();

		// selection of a download location

		mb = new MessageBox(new Shell(Display.getCurrent()));
		mb.setMessage("Please, select a download location");
		mb.open();

		DirectoryDialog dir = new DirectoryDialog(new Shell(Display.getCurrent()));
		dir.setText("Please, select a download location");

		final String downloadPath = dir.open();

		if (downloadPath == null) {
			// TODO PROPER ERROR MANAGEMENT
			System.err.println("Invalid download location");
			return segments;
		}

		// invite the user to enter a max number of changed elements
		GenericInputSelectionDialog inputDialog = new GenericInputSelectionDialog(currentShell,
				"Max number of changed elements", "Please, enter the maximum number of changed elements", "20");

		// TODO PROPER ERROR MANAGEMENT
		if (inputDialog.open() != Window.OK) {
			System.err.println("Could not open branch window");
			return segments;
		}

		try {
			maxNumberChanges = Integer.parseInt(inputDialog.getValue().trim());
		} catch (NumberFormatException e) {
			System.err.println("The input must be a number");
			return segments;
		}

		int i = 0;

		adapters = new ArrayList<>();

		try {

			commits = client.filterCommits(client.getCommits(), startPointCommit.getSha(), endPointCommit.getSha());
			startCommit = commits.remove(0);

			int nbCommits = commits.size();

			// splits the branch in segments according to the max number of
			// changed elements
			while (i < nbCommits - 1) {

				currentCommit = commits.get(i);
				segment = new Segment(startCommit, currentCommit);

				Utils.downloadCommits(client, segment, downloadPath);

				nbChanges = Utils.getNbChangedElements(startCommit, currentCommit, adapters);

				if (nbChanges == -1)
					return Collections.emptyList();

				if (nbChanges >= maxNumberChanges) {
					segments.add(segment);
					startCommit = currentCommit;
				}

				i++;
			}

			segment = new Segment(startCommit, commits.get(nbCommits - 1));
			segments.add(segment);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return segments;
	}

	@Override
	public String toString() {
		return "Number of changed elements";
	}

}
