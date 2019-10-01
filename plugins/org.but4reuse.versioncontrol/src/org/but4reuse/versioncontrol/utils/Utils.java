package org.but4reuse.versioncontrol.utils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adaptedmodel.AdaptedArtefact;
import org.but4reuse.adaptedmodel.AdaptedModel;
import org.but4reuse.adaptedmodel.Block;
import org.but4reuse.adaptedmodel.helpers.AdaptedModelHelper;
import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.helper.AdaptersHelper;
import org.but4reuse.adapters.impl.AbstractElement;
import org.but4reuse.adapters.ui.AdaptersSelectionDialog;
import org.but4reuse.artefactmodel.Artefact;
import org.but4reuse.artefactmodel.ArtefactModel;
import org.but4reuse.artefactmodel.ArtefactModelFactory;
import org.but4reuse.block.identification.impl.IntersectionsBlockIdentification;
import org.but4reuse.versioncontrol.IVersionControlClient;
import org.but4reuse.versioncontrol.IVersionControlCommit;
import org.but4reuse.versioncontrol.segment.Segment;
import org.but4reuse.wordclouds.util.Cloudifier;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

public class Utils {

	/**
	 * Download the start and end commits of the segment
	 * 
	 * @param client
	 * @param segment
	 * @param downloadPath
	 */
	public static void downloadCommits(final IVersionControlClient client, Segment segment, final String downloadPath) {

		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());

		try {
			final IVersionControlCommit commit = segment.getEndCommit();
			final IVersionControlCommit previousCommit = segment.getStartCommit();

			progressDialog.run(true, true, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					int totalWork = 2;
					monitor.beginTask("Downloading commits", totalWork);

					try {
						monitor.subTask("Downloading " + commit.getSha());
						client.downloadCommit(commit, downloadPath);
						monitor.worked(1);

						monitor.subTask("Downloading " + previousCommit.getSha());
						client.downloadCommit(previousCommit, downloadPath);
						monitor.worked(1);

					} catch (IOException e) {
						e.printStackTrace();
					}

					monitor.done();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Set the number of added lines for each commit of the segment
	 * 
	 * @param client
	 * @param segment
	 */
	public static void setNbAddedLinestoSegment(IVersionControlClient client, Segment segment) {
		IVersionControlCommit currentCommit = segment.getEndCommit();
		String startCommitSha = segment.getStartCommit().getSha();

		while (true) {

			client.setNbAddedLinesToCommit(currentCommit);

			if (currentCommit.getSha().equals(startCommitSha))
				break;

			try {
				currentCommit = client.getCommit(currentCommit.getPreviousCommitSha());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static Artefact createCommitArtefact(IVersionControlCommit commit) {
		Artefact commitArtefact = ArtefactModelFactory.eINSTANCE.createArtefact();
		commitArtefact.setName(commit.getSha());
		commitArtefact.setArtefactURI(commit.getDiskLocation().toFile().toURI().toString());
		return commitArtefact;
	}

	public static ArtefactModel createVersionArtefactModel(IVersionControlCommit commit2, Artefact commit1Artefact,
			Artefact commit2Artefact) {
		ArtefactModel artefactModel = ArtefactModelFactory.eINSTANCE.createArtefactModel();
		artefactModel.setName(commit2.getDiskLocation().getParent().toFile().toString());
		artefactModel.getOwnedArtefacts().add(commit2Artefact);
		artefactModel.getOwnedArtefacts().add(commit1Artefact);
		return artefactModel;
	}

	/**
	 * 
	 * @param client
	 * @param maxNumberChanges
	 * @param maxNewWords
	 * @param downloadPath
	 */
	public static int splitBranch(IVersionControlClient client, int maxNumberChanges, int maxNewWords,
			String downloadPath) {

		int sumChanges = 0;
		int nbChanges = 0;
		int sumNewWords = 0;
		int nbAnalyzedCommits = 0;
		IVersionControlCommit currentCommit, previousCommit;
		Segment segment;

		List<IAdapter> adapters;
		List<IVersionControlCommit> commits;
		int i = 0;

		adapters = new ArrayList<>();

		try {
			commits = client.getCommits();
			int nbCommits = commits.size();
			while (sumChanges < maxNumberChanges && sumNewWords < maxNewWords && nbAnalyzedCommits < nbCommits) {

				currentCommit = commits.get(i);
				i++;
				previousCommit = client.getCommit(currentCommit.getPreviousCommitSha());
				if (previousCommit == null)
					continue;
				segment = new Segment(previousCommit, currentCommit);

				Utils.downloadCommits(client, segment, downloadPath);

				nbChanges = Utils.getNbChangedElements(previousCommit, currentCommit, adapters);

				if (nbChanges == -1)
					break;

				sumChanges += nbChanges;
				sumNewWords += Utils.getNbAddedWords(previousCommit, currentCommit, adapters);

				System.out.println("sumChanges : " + sumChanges);
				System.out.println("sumNewWords : " + sumNewWords);

				nbAnalyzedCommits++;

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return sumChanges + sumNewWords;
	}

	/**
	 * 
	 * @param client
	 * @param maxNewWords
	 * @param downloadPath
	 */
	public static int splitBranchWords(IVersionControlClient client, int maxNewWords, String downloadPath) {

		int nbNewWords = 0;
		int nbAnalyzedCommits = 0;
		int sumNewWords = 0;
		IVersionControlCommit currentCommit, previousCommit;
		Segment segment;

		List<IAdapter> adapters;
		List<IVersionControlCommit> commits;
		int i = 0;

		adapters = new ArrayList<>();

		try {
			commits = client.getCommits();
			int nbCommits = commits.size();
			while (sumNewWords < maxNewWords && nbAnalyzedCommits < nbCommits) {

				currentCommit = commits.get(i);
				i++;
				previousCommit = client.getCommit(currentCommit.getPreviousCommitSha());
				if (previousCommit == null)
					continue;
				segment = new Segment(previousCommit, currentCommit);

				Utils.downloadCommits(client, segment, downloadPath);

				nbNewWords = Utils.getNbAddedWords(previousCommit, currentCommit, adapters);
				if (nbNewWords == -1)
					break;

				sumNewWords += nbNewWords;

				System.out.println("sumNewWords : " + sumNewWords);

				nbAnalyzedCommits++;

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return sumNewWords;
	}

	/**
	 * 
	 * @param client
	 * @param maxNumberChanges
	 * @param downloadPath
	 */
	public static int splitBranchChangedElements(IVersionControlClient client, int maxNumberChanges,
			String downloadPath) {

		int sumChanges = 0;
		int nbChanges = 0;
		int nbAnalyzedCommits = 0;
		IVersionControlCommit currentCommit, previousCommit;
		Segment segment;

		List<IAdapter> adapters;
		List<IVersionControlCommit> commits;
		int i = 0;

		adapters = new ArrayList<>();

		try {
			commits = client.getCommits();
			int nbCommits = commits.size();
			while (sumChanges < maxNumberChanges && nbAnalyzedCommits < nbCommits) {

				currentCommit = commits.get(i);
				i++;
				previousCommit = client.getCommit(currentCommit.getPreviousCommitSha());
				if (previousCommit == null)
					continue;
				segment = new Segment(previousCommit, currentCommit);

				Utils.downloadCommits(client, segment, downloadPath);

				nbChanges = Utils.getNbChangedElements(previousCommit, currentCommit, adapters);

				if (nbChanges == -1)
					break;

				sumChanges += nbChanges;

				System.out.println("sumChanges : " + sumChanges);

				nbAnalyzedCommits++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return sumChanges;
	}

	/**
	 * 
	 * @return list of blocks 0 is the whole commit1, list of blocks 1 is the
	 *         whole commit2, list of block 2 is the specific elements to
	 *         commit1 (without the common elements), list of block 3 is the
	 *         common elements in commit1 and commit2, list of block 4 is the
	 *         specific elements to commit2 (without the common elements)
	 */
	public static List<List<Block>> getBlocks(IVersionControlCommit commit1, IVersionControlCommit commit2,
			List<IAdapter> adapters) {

		List<List<Block>> blockLists = new ArrayList<>();
		IntersectionsBlockIdentification interBlockIdentification = new IntersectionsBlockIdentification();

		Artefact commit1Artefact = Utils.createCommitArtefact(commit1);
		Artefact commit2Artefact = Utils.createCommitArtefact(commit2);

		ArtefactModel artefactModel = Utils.createVersionArtefactModel(commit2, commit1Artefact, commit2Artefact);

		if (adapters.isEmpty()) {
			// find matching adapters
			List<IAdapter> defaultAdapters = AdaptersHelper.getAdaptersByIds(artefactModel.getAdapters());

			// invite user to select / deselect
			adapters.addAll(AdaptersSelectionDialog.show("Version Control", artefactModel, defaultAdapters));
			if (adapters.isEmpty()) {
				return null;
			}
		}
		IProgressMonitor monitor = new NullProgressMonitor();

		AdaptedModel adaptedModel = AdaptedModelHelper.adapt(artefactModel, adapters, monitor);

		// intersection blocks
		List<Block> intersectionBlocks = interBlockIdentification
				.identifyBlocks(adaptedModel.getOwnedAdaptedArtefacts(), monitor);

		adaptedModel.getOwnedBlocks().addAll(intersectionBlocks);

		// artefacts
		AdaptedArtefact commit2AdaptedArtefact = adaptedModel.getOwnedAdaptedArtefacts().get(0);

		AdaptedArtefact commit1AdaptedArtefact = adaptedModel.getOwnedAdaptedArtefacts().get(1);

		// lists of blocks
		List<Block> commit2Blocks = AdaptedModelHelper.getBlocksOfAdaptedArtefact(commit2AdaptedArtefact);
		List<Block> commit1Blocks = AdaptedModelHelper.getBlocksOfAdaptedArtefact(commit1AdaptedArtefact);
		List<Block> commonBlocks = AdaptedModelHelper.getCommonBlocks(adaptedModel);

		// remove the common block
		commit1Blocks.removeAll(commonBlocks);
		commit2Blocks.removeAll(commonBlocks);

		List<Block> commit1PenalizingBlocks = new ArrayList<>(intersectionBlocks);
		// all the blocks except the commit1 blocks
		commit1PenalizingBlocks.removeAll(commit1Blocks);

		List<Block> commit2PenalizingBlocks = new ArrayList<>(intersectionBlocks);
		// all the blocks except the commit2 blocks
		commit2PenalizingBlocks.removeAll(commit2Blocks);

		blockLists.add(commit2PenalizingBlocks);
		blockLists.add(commit1PenalizingBlocks);
		blockLists.add(commit1Blocks);
		blockLists.add(commonBlocks);
		blockLists.add(commit2Blocks);

		return blockLists;

	}

	/**
	 * 
	 * @param commit1
	 * @param commit2
	 * @return the changed elements between the commit1 and the commit2
	 */
	public static List<IElement> getChangedElements(IVersionControlCommit commit1, IVersionControlCommit commit2,
			List<IAdapter> adapters) {

		List<List<Block>> blockLists;
		List<IElement> commit2Elements;
		List<Block> commit2Blocks;

		blockLists = Utils.getBlocks(commit1, commit2, adapters);
		if (blockLists == null)
			return null;

		commit2Blocks = blockLists.get(4);

		// calculating the number of elements
		commit2Elements = AdaptedModelHelper.getElementsOfBlocks(commit2Blocks);

		// number of changed elements
		return commit2Elements;
	}

	/**
	 * 
	 * @param commit1
	 * @param commit2
	 * @return the number of changed elements between the commit1 and the
	 *         commit2
	 */
	public static int getNbChangedElements(IVersionControlCommit commit1, IVersionControlCommit commit2,
			List<IAdapter> adapters) {
		List<IElement> changedElements = getChangedElements(commit1, commit2, adapters);
		if (changedElements == null) {
			return -1;
		}
		return changedElements.size();
	}

	public static List<String> getAddedWords(IVersionControlCommit commit1, IVersionControlCommit commit2,
			List<IAdapter> adapters) {
		List<List<Block>> blockLists;
		List<IElement> commit2Elements, commit2PenalizingElements;
		List<Block> commit2Blocks, commit2PenalizingBlocks;

		blockLists = Utils.getBlocks(commit1, commit2, adapters);
		if (blockLists == null)
			return null;

		commit2Blocks = blockLists.get(4);
		commit2PenalizingBlocks = blockLists.get(0);

		// calculating the number of elements
		commit2Elements = AdaptedModelHelper.getElementsOfBlocks(commit2Blocks);
		commit2PenalizingElements = AdaptedModelHelper.getElementsOfBlocks(commit2PenalizingBlocks);

		return getListWords(commit2Elements, commit2PenalizingElements);
	}

	public static List<String> getListWords(List<IElement> commitElements, List<IElement> commitPenalizingElements) {

		List<String> commitWords, commitPenalizingWords;

		commitWords = new ArrayList<String>();
		commitPenalizingWords = new ArrayList<String>();

		// getting the corresponding list of words
		for (IElement element : commitElements) {
			AbstractElement ab = (AbstractElement) element;
			for (String s : ab.getWords()) {
				if (!commitWords.contains(s))
					commitWords.add(s);
			}
		}

		for (IElement element : commitPenalizingElements) {
			AbstractElement ab = (AbstractElement) element;
			for (String s : ab.getWords()) {
				if (!commitPenalizingWords.contains(s))
					commitPenalizingWords.add(s);
			}
		}

		NullProgressMonitor npm = new NullProgressMonitor();

		commitWords = Cloudifier.processWords(commitWords, npm);
		commitPenalizingWords = Cloudifier.processWords(commitPenalizingWords, npm);

		commitWords.removeAll(commitPenalizingWords);
		
		List<String> commitUniqueWords = new ArrayList<>();
		
		for(String word : commitWords){
			if(!commitUniqueWords.contains(word)){
				commitUniqueWords.add(word);
			}
		}

		return commitUniqueWords;
	}

	public static int getNbAddedWords(IVersionControlCommit commit1, IVersionControlCommit commit2,
			List<IAdapter> adapters) {
		List<String> addedWords = getAddedWords(commit1, commit2, adapters);
		if (addedWords == null) {
			return -1;
		}
		return addedWords.size();
	}

}