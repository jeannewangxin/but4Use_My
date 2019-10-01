package org.but4reuse.versioncontrol.segment.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.but4reuse.versioncontrol.IVersionControlClient;
import org.but4reuse.versioncontrol.IVersionControlCommit;
import org.but4reuse.versioncontrol.segment.ISegmentSelectionStrategy;
import org.but4reuse.versioncontrol.segment.Segment;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class SpecificCommitsSegmentSelection implements ISegmentSelectionStrategy {

	/**
	 * The textfile must have the .txt extension, and each line must be have
	 * this format : "START_COMMIT_SHA END_COMMIT_SHA"
	 */
	@Override
	public List<Segment> getSegments(IVersionControlClient client, IVersionControlCommit startPointCommit,
			IVersionControlCommit endPointCommit) {

		Shell currentShell;

		List<Segment> segments = new ArrayList<>();
		Segment segment;
		IVersionControlCommit startCommit, endCommit;

		currentShell = new Shell(Display.getCurrent());

		MessageBox mb = new MessageBox(new Shell(Display.getCurrent()));
		mb.setMessage("Please, select the data text file");
		mb.open();

		FileDialog fileDialog = new FileDialog(currentShell);
		fileDialog.setText("Please, select the data text file");

		String dataPath = fileDialog.open();

		if (dataPath == null) {
			// TODO PROPER ERROR MANAGEMENT
			System.err.println("Invalid download location");
			return null;
		}

		if (!dataPath.endsWith(".txt")) {
			System.out.println("Text file needed");
			return null;
		}

		File file = new File(dataPath);

		// reading the textfile
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {

				String[] commits = line.split(" ");

				endCommit = client.getCommit(commits[1]);

				if (endCommit == null) {
					System.out.println(commits[1] + " doesn't correspond to any commit of this branch");
					continue;
				} else {
					if (endCommit.getDate().compareTo(endPointCommit.getDate()) > 0) {
						System.out.println("End point " + commits[1] + " is out the range");
						continue;
					}
				}

				startCommit = client.getCommit(commits[0]);

				if (startCommit == null) {
					System.out.println(commits[0] + " doesn't correspond to any commit of this branch");
					continue;
				} else {
					if (startCommit.getDate().compareTo(startPointCommit.getDate()) < 0) {
						System.out.println("Start point " + commits[0] + " is out the range");
						continue;
					}
				}

				endCommit.setPreviousCommitSha(commits[0]);

				segment = new Segment(startCommit, endCommit);
				segments.add(segment);

			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return segments;
	}

	@Override
	public String toString() {
		return "Specific commits";
	}

}
