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
 * Strategy that selects the segments according to the number of new words
 *
 * @author aarkoub
 *
 */
public class NewWordSegmentSelection implements ISegmentSelectionStrategy {

	@Override
	public List<Segment> getSegments(IVersionControlClient client, IVersionControlCommit startPointCommit,
			IVersionControlCommit endPointCommit) {

		MessageBox mb;

		List<Segment> segments = new ArrayList<>();

		int maxNewWords;
		int nbNewWords = 0;
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

		// invite the user to enter a max number of new words
		GenericInputSelectionDialog inputDialog = new GenericInputSelectionDialog(currentShell,
				"Max number of new words", "Please, enter the maximum number of new words", "5");

		// TODO PROPER ERROR MANAGEMENT
		if (inputDialog.open() != Window.OK) {
			System.err.println("Could not open branch window");
			return segments;
		}

		try {
			maxNewWords = Integer.parseInt(inputDialog.getValue().trim());
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

			// splits the branch in segments according to the max number of new
			// words
			while (i < nbCommits - 1) {

				currentCommit = commits.get(i);
				segment = new Segment(startCommit, currentCommit);
				segments.add(segment);

				Utils.downloadCommits(client, segment, downloadPath);

				nbNewWords = Utils.getNbAddedWords(startCommit, currentCommit, adapters);
				if (nbNewWords == -1)
					return Collections.emptyList();

				if (nbNewWords >= maxNewWords) {
					segments.add(segment);
					startCommit = currentCommit;
				}
				i++;
			}

			segment = new Segment(startCommit, commits.get(nbCommits - 1));
			segments.add(segment);
			System.out.println(segment);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return segments;
	}

	@Override
	public String toString() {
		return "Number of new words";
	}

}
