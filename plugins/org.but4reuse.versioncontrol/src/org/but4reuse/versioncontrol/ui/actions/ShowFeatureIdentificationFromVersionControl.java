package org.but4reuse.versioncontrol.ui.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.but4reuse.adaptedmodel.AdaptedArtefact;
import org.but4reuse.adaptedmodel.AdaptedModel;
import org.but4reuse.adaptedmodel.Block;
import org.but4reuse.adaptedmodel.helpers.AdaptedModelHelper;
import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.helper.AdaptersHelper;
import org.but4reuse.adapters.ui.AdaptersSelectionDialog;
import org.but4reuse.artefactmodel.Artefact;
import org.but4reuse.artefactmodel.ArtefactModel;
import org.but4reuse.artefactmodel.ArtefactModelFactory;
import org.but4reuse.block.identification.impl.IntersectionsBlockIdentification;
import org.but4reuse.utils.files.FileUtils;
import org.but4reuse.utils.strings.StringUtils;
import org.but4reuse.versioncontrol.IVersionControlClient;
import org.but4reuse.versioncontrol.IVersionControlCommit;
import org.but4reuse.versioncontrol.IVersionControlFeature;
import org.but4reuse.versioncontrol.event.FeatureEvent;
import org.but4reuse.versioncontrol.grid.InteractiveGrid;
import org.but4reuse.versioncontrol.impl.VersionControlFeature;
import org.but4reuse.versioncontrol.impl.github.MyGithubClient;
import org.but4reuse.versioncontrol.segment.ISegmentSelectionStrategy;
import org.but4reuse.versioncontrol.segment.Segment;
import org.but4reuse.versioncontrol.segment.impl.AllMergesSegmentSelection;
import org.but4reuse.versioncontrol.segment.impl.ChangedElementsNewWordsSegmentSelection;
import org.but4reuse.versioncontrol.segment.impl.ChangedElementsSegmentSelection;
import org.but4reuse.versioncontrol.segment.impl.NewWordSegmentSelection;
import org.but4reuse.versioncontrol.segment.impl.OnlyPullRequestMergesSegmentSelection;
import org.but4reuse.versioncontrol.segment.impl.SpecificCommitsSegmentSelection;
import org.but4reuse.versioncontrol.timeline.Timeline;
import org.but4reuse.versioncontrol.utils.Utils;
import org.but4reuse.versioncontrol.utils.dialogs.GenericInputSelectionDialog;
import org.but4reuse.versioncontrol.utils.timeline.TimelineUtils;
import org.but4reuse.wordclouds.util.NewCloud;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.mcavallo.opencloud.Cloud;

/**
 * Show Feature Identification From Version Control
 * 
 * @author sandu.postaru, aarkoub
 * 
 */
public class ShowFeatureIdentificationFromVersionControl implements IObjectActionDelegate {

	/* selected artefact */
	Artefact artefact = null;

	/* virtual artefact model */
	ArtefactModel artefactModel;

	List<IAdapter> adapters;

	IntersectionsBlockIdentification interBlockIndentification = new IntersectionsBlockIdentification();

	IVersionControlClient client;

	/* base commit of the merge */
	IVersionControlCommit startCommit;

	/* cloud window width X height */
	final int cloudWinWidth = 700;
	final int cloudWinHeight = 700;

	List<IVersionControlCommit> merges;

	/* list of features filtered by the threshold */
	ArrayList<IVersionControlFeature> features;

	/* find adapters once during current launch */
	boolean adaptersFound;

	/* statistics */
	int minEndCommitElements;
	int maxEndCommitElements;
	int sumEndCommitElements;

	int minStartCommitElements;
	int maxStartCommitElements;
	int sumStartCommitElements;

	int minAddedWords;
	int maxAddedWords;
	int sumAddedWords;

	/* we could use merges.size() but count manually in case of exception */
	int nbEndCommits;
	int nbStartCommits;

	/* number of features to display on the timeline */
	final int maxFeatureNb = 5;

	IVersionControlCommit startPointCommit;
	IVersionControlCommit endPointCommit;
	List<IVersionControlCommit> commits;

	@Override
	public void run(IAction action) {
		artefact = null;
		if (selection instanceof IStructuredSelection) {
			for (Object art : ((IStructuredSelection) selection).toArray()) {
				if (art instanceof Artefact) {

					// initialisation
					merges = null;
					features = new ArrayList<IVersionControlFeature>();
					adaptersFound = false;

					minEndCommitElements = Integer.MAX_VALUE;
					maxEndCommitElements = 0;
					sumEndCommitElements = 0;

					minStartCommitElements = Integer.MAX_VALUE;
					maxStartCommitElements = 0;
					sumStartCommitElements = 0;

					minAddedWords = 0;
					maxAddedWords = 0;
					sumAddedWords = 0;

					nbEndCommits = 0;
					nbStartCommits = 0;

					artefact = ((Artefact) art);

					String uri = artefact.getArtefactURI();

					// TODO PROPER ERROR MANAGEMENT
					if (!uri.endsWith(".git")) {
						System.err.println("Invalid uri");
						return;
					}

					List<String> uriTokens = StringUtils.tokenizeString(uri, "//");

					// TODO PROPER ERROR MANAGEMENT
					if (uriTokens.size() < 4) {
						System.err.println("Invalid uri");
						return;
					}

					client = new MyGithubClient();

					final String repositoryOwner = uriTokens.get(2);
					final String repositoryName = FileUtils.getFileNameWithoutExtension(uriTokens.get(3));

					final Shell currentShell = Display.getCurrent().getActiveShell();

					GenericInputSelectionDialog inputDialog;

					ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(currentShell);

					// get merge information | network access
					try {

						progressDialog.run(true, true, new IRunnableWithProgress() {
							@Override
							public void run(IProgressMonitor monitor)
									throws InvocationTargetException, InterruptedException {

								int totalWork = 1;
								monitor.beginTask("Fetching version control metadata", totalWork);

								/* set working repository */
								try {
									client.setRepository(repositoryOwner, repositoryName);
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

					// selection of the branch of the repository
					final Shell parent = new Shell(Display.getCurrent());
					GridLayout gridLayout = new GridLayout(1, false);

					gridLayout.marginHeight = 50;
					gridLayout.marginWidth = 50;

					parent.setLayout(gridLayout);

					parent.setText("Branch selection");

					parent.setSize(300, 200);

					parent.setBounds(600, 250, 300, 200);

					final Combo combo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);

					// adding the names of the different branches of the
					// repository to the combo
					int x = 0;
					String defaultBranch = client.getDefaultBranch();
					for (String branchName : client.getBranchesNames()) {
						combo.add(branchName);
						if (branchName.equals(defaultBranch)) {
							combo.select(x);
							break;
						}
						x++;
					}

					combo.setBounds(100, 0, 50, 50);
					combo.setVisible(true);
					combo.setText("Please select a branch.");
					Composite buttonsComposite = new Composite(parent, SWT.NONE);

					GridLayout buttonsGridLayout = new GridLayout();
					buttonsGridLayout.marginLeft = 50;
					buttonsGridLayout.horizontalSpacing = 50;
					buttonsGridLayout.numColumns = 2;

					buttonsComposite.setLayout(buttonsGridLayout);

					Button cancelButton = new Button(buttonsComposite, SWT.PUSH);
					cancelButton.setText("Cancel");

					Button okButton = new Button(buttonsComposite, SWT.PUSH);
					okButton.setText("OK");

					final boolean[] done = new boolean[1];
					done[0] = false;

					cancelButton.addSelectionListener(new SelectionListener() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							parent.close();

						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							// TODO Auto-generated method stub

						}
					});

					okButton.addSelectionListener(new SelectionListener() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							client.setBranch(combo.getItem(combo.getSelectionIndex()));
							done[0] = true;
							parent.close();

						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							// TODO Auto-generated method stub

						}
					});

					Display display = currentShell.getDisplay();

					parent.open();

					while (!parent.isDisposed()) {
						if (!display.readAndDispatch())
							display.sleep();
					}

					// if the cancel button was pressed
					if (done[0] == false) {
						return;
					}

					String sha = null;
					MessageBox mb;

					// selection of the start point commit
					try {
						commits = client.getCommits();
						if (commits.isEmpty()) {
							mb = new MessageBox(new Shell(Display.getCurrent()));
							mb.setMessage("No commits have been detected.");
							mb.open();
							return;
						}
						do {

							inputDialog = new GenericInputSelectionDialog(currentShell,
									"Origin point of repository exploration",
									"Enter the commit sha (by default the first relevant commit identified by but4reuse)",
									commits.get(0).getSha());

							// TODO PROPER ERROR MANAGEMENT
							if (inputDialog.open() != Window.OK) {
								System.err.println("Could not open branch window");
								return;
							}

							sha = inputDialog.getValue().trim();

							if (!client.commitExists(sha)) {
								sha = null;
								mb = new MessageBox(new Shell(Display.getCurrent()));
								mb.setMessage("There is no commit corresponding to this sha.");
								mb.open();
							}

						} while (sha == null);

						startPointCommit = client.getCommit(sha);
						sha = null;

						// selection of the end point commit
						do {

							inputDialog = new GenericInputSelectionDialog(currentShell,
									"End point of repository exploration",
									"Enter the commit sha (by default the last relevant commit identified by but4reuse)",
									commits.get(commits.size() - 1).getSha());

							// TODO PROPER ERROR MANAGEMENT
							if (inputDialog.open() != Window.OK) {
								System.err.println("Could not open branch window");
								return;
							}

							sha = inputDialog.getValue().trim();

							if (!client.commitExists(sha)) {
								sha = null;
								mb = new MessageBox(new Shell(Display.getCurrent()));
								mb.setMessage("There is no commit corresponding to this sha.");
								mb.open();
							} else {
								if (client.getCommit(sha).getDate().compareTo(startPointCommit.getDate()) <= 0) {
									sha = null;
									mb = new MessageBox(new Shell(Display.getCurrent()));
									mb.setMessage("The end commit must be more recent than the start commit.");
									mb.open();
								}
							}

						} while (sha == null);

						endPointCommit = client.getCommit(sha);

						sha = null;

					} catch (IOException e1) {
						e1.printStackTrace();
						System.err.println("Error when getting the project");
						return;
					}

					List<ISegmentSelectionStrategy> strategies = selectStategy();
					List<Segment> segments = getSegments(strategies);

					if (segments.isEmpty()) {
						mb = new MessageBox(new Shell(Display.getCurrent()));
						mb.setMessage("There is no commit to analyse");
						mb.open();
						return;
					}

					mb = new MessageBox(currentShell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					mb.setMessage(
							"Do you want to sort the main contributors to the segments by the number of added lines"
									+ " per commit? (default: number of commits");
					mb.setText("Contributors");

					if (mb.open() == SWT.YES) {

						for (Segment segment : segments) {
							Utils.setNbAddedLinestoSegment(client, segment);
						}

					}

					// selection of a download location

					mb = new MessageBox(new Shell(Display.getCurrent()));
					mb.setMessage("Please, select a download location");
					mb.open();

					DirectoryDialog dir = new DirectoryDialog(currentShell);
					dir.setText("Please, select a download location");

					final String downloadPath = dir.open();

					if (downloadPath == null) {
						// TODO PROPER ERROR MANAGEMENT
						System.err.println("Invalid download location");
						return;
					}

					for (Segment segment : segments) {

						final IVersionControlCommit endCommit = segment.getEndCommit();
						startCommit = segment.getStartCommit();
						final List<String> contributors = segment.getDevelopers(client);

						// commits download
						Utils.downloadCommits(client, segment, downloadPath);

						if (downloadPath == null) {
							System.out.println("No download path --> end");
							return;
						}

						// create artefact model for current commit tuple
						// (merge, base)

						// merge artefact
						final Artefact endCommitArtefact = ArtefactModelFactory.eINSTANCE.createArtefact();
						endCommitArtefact.setName(endCommit.getSha());
						endCommitArtefact.setArtefactURI(endCommit.getDiskLocation().toFile().toURI().toString());

						// previous commit artefact
						Artefact startCommitArtefact = ArtefactModelFactory.eINSTANCE.createArtefact();
						startCommitArtefact.setName(startCommit.getSha());
						startCommitArtefact.setArtefactURI(startCommit.getDiskLocation().toFile().toURI().toString());

						// virtual artefact model
						artefactModel = ArtefactModelFactory.eINSTANCE.createArtefactModel();
						artefactModel.setName(endCommit.getDiskLocation().getParent().toFile().toString());
						artefactModel.getOwnedArtefacts().add(endCommitArtefact);
						artefactModel.getOwnedArtefacts().add(startCommitArtefact);

						if (!adaptersFound) {

							// find matching adapters
							List<IAdapter> defaultAdapters = AdaptersHelper
									.getAdaptersByIds(artefactModel.getAdapters());

							// invite user to select / deselect
							adapters = AdaptersSelectionDialog.show("Version Control", artefactModel, defaultAdapters);
							adaptersFound = true;
						}

						try {
							progressDialog.run(true, true, new IRunnableWithProgress() {
								@Override
								public void run(IProgressMonitor monitor)
										throws InvocationTargetException, InterruptedException {

									int totalWork = 2;

									// adapt the artefactModel
									monitor.beginTask("Feature Identification from Version Control", totalWork);
									AdaptedModel adaptedModel = AdaptedModelHelper.adapt(artefactModel, adapters,
											monitor);

									// calculate intersection blocks
									monitor.subTask("Calculating intersection blocks");

									// intersection blocks
									List<Block> intersectionBlocks = interBlockIndentification
											.identifyBlocks(adaptedModel.getOwnedAdaptedArtefacts(), monitor);

									monitor.worked(1);

									adaptedModel.getOwnedBlocks().addAll(intersectionBlocks);

									// artefacts
									AdaptedArtefact endCommitAdaptedArtefact = adaptedModel.getOwnedAdaptedArtefacts()
											.get(0);

									AdaptedArtefact startCommitAdaptedArtefact = adaptedModel.getOwnedAdaptedArtefacts()
											.get(1);

									// identify end commit blocks
									monitor.subTask("Identifying blocks");

									List<Block> endCommitBlocks = AdaptedModelHelper
											.getBlocksOfAdaptedArtefact(endCommitAdaptedArtefact);
									List<Block> startCommitBlocks = AdaptedModelHelper
											.getBlocksOfAdaptedArtefact(startCommitAdaptedArtefact);
									List<Block> commonBlocks = AdaptedModelHelper.getCommonBlocks(adaptedModel);

									// remove the common block
									endCommitBlocks.removeAll(commonBlocks);
									startCommitBlocks.removeAll(commonBlocks);
									monitor.worked(1);

									// end commit elements and penalizing
									// elements
									List<IElement> endCommitElements = AdaptedModelHelper
											.getElementsOfBlocks(endCommitBlocks);
									List<Block> endCommitPenalizingBlocks = new ArrayList<>(intersectionBlocks);

									// all the blocks except the end commit
									// blocks
									endCommitPenalizingBlocks.removeAll(endCommitBlocks);

									List<IElement> endCommitPenalizingElements = AdaptedModelHelper
											.getElementsOfBlocks(endCommitPenalizingBlocks);

									// start commit elements and penalizing
									// elements
									List<IElement> startCommitElements = AdaptedModelHelper
											.getElementsOfBlocks(startCommitBlocks);
									List<Block> startCommitPenalizingBlocks = new ArrayList<>(intersectionBlocks);

									// all the blocks except the start commit
									// blocks
									startCommitPenalizingBlocks.removeAll(startCommitBlocks);

									List<IElement> startCommitPenalizingElements = AdaptedModelHelper
											.getElementsOfBlocks(startCommitPenalizingBlocks);

									/* new feature analyzed */

									IVersionControlFeature feature = new VersionControlFeature(endCommit, startCommit,
											endCommitElements, endCommitPenalizingElements, startCommitElements,
											startCommitPenalizingElements, contributors);

									features.add(feature);

									int currEndCommitElements = endCommitElements.size();
									int currStartCommitElements = startCommitElements.size();
									int currAddedWords = feature.getNbAddedWords();

									/* update statistics */
									minEndCommitElements = Math.min(minEndCommitElements, currEndCommitElements);
									maxEndCommitElements = Math.max(maxEndCommitElements, currEndCommitElements);
									sumEndCommitElements += currEndCommitElements;
									nbEndCommits++;

									minStartCommitElements = Math.min(minStartCommitElements, currStartCommitElements);
									maxStartCommitElements = Math.max(maxStartCommitElements, currStartCommitElements);
									sumStartCommitElements += currStartCommitElements;
									nbStartCommits++;

									minAddedWords = Math.min(minAddedWords, currAddedWords);
									maxAddedWords = Math.max(maxAddedWords, currAddedWords);
									sumAddedWords += currAddedWords;

									monitor.done();
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					// all segments have been analyzed

					// end commit threshold
					int avgEndCommitElements = sumEndCommitElements / nbEndCommits;

					String message = "Total end commits: " + nbEndCommits + "\nMinimum elements: "
							+ minEndCommitElements + "\nMaximum elements: " + maxEndCommitElements
							+ "\nAverage of elements: " + avgEndCommitElements
							+ "\n\nEnter threshold value (inclusive) for added elements : ";

					inputDialog = new GenericInputSelectionDialog(currentShell, "Filter output for added elements",
							message, Integer.toString(avgEndCommitElements), "Construct the end commit block?");

					// TODO PROPER ERROR MANAGEMENT
					if (inputDialog.open() != Window.OK) {
						System.err.println("Could not open threashold window");
						return;
					}

					boolean endCommitConstructCheckox = inputDialog.getCheckStatus();
					int endCommitThreshold = Integer.valueOf(inputDialog.getValue());

					// start commit threshold

					int avgStartCommitElements = sumStartCommitElements / nbStartCommits;

					message = "Total start commits: " + nbStartCommits + "\nMinimum elements: " + minStartCommitElements
							+ "\nMaximum elements: " + maxStartCommitElements + "\nAverage of elements: "
							+ avgStartCommitElements + "\n\nEnter threshold value (inclusive) for removed elements : ";

					inputDialog = new GenericInputSelectionDialog(currentShell, "Filter output for removed elements",
							message, Integer.toString(avgStartCommitElements), "Construct the start commit block?");

					// TODO PROPER ERROR MANAGEMENT
					if (inputDialog.open() != Window.OK) {
						System.err.println("Could not open threashold window");
						return;
					}

					boolean startCommitConstructCheckox = inputDialog.getCheckStatus();
					int startCommitThreshold = Integer.valueOf(inputDialog.getValue());

					// added words threshold
					int avgAddedWords = sumAddedWords / segments.size();

					message = "Total segments: " + segments.size() + "\nMinimum added words: " + minAddedWords
							+ "\nMaximum added words: " + maxAddedWords + "\nAverage of added words: " + avgAddedWords
							+ "\n\nEnter threshold value (inclusive) for added words : ";

					inputDialog = new GenericInputSelectionDialog(currentShell, "Filter output for added words",
							message, Integer.toString(avgAddedWords));

					// TODO PROPER ERROR MANAGEMENT
					if (inputDialog.open() != Window.OK) {
						System.err.println("Could not open threashold window");
						return;
					}

					int addedWordsThreshold = Integer.valueOf(inputDialog.getValue());

					// Generate words only for features that respect the
					// threshold

					Timeline timeline = new Timeline(repositoryName, repositoryOwner);
					InteractiveGrid grid = new InteractiveGrid(repositoryName);
					int i = 0;// order of commit
					for (IVersionControlFeature feature : features) {
						i++;
						boolean eventUpdate = false;
						List<String> addedFeatures = Collections.emptyList();
						List<String> removedFeatures = Collections.emptyList();

						NewCloud addedCloud = new NewCloud();
						NewCloud removedCloud = new NewCloud();

						if (feature.getNbEndCommitElements() >= endCommitThreshold
								&& feature.getNbAddedWords() >= addedWordsThreshold) {

							eventUpdate = true;

							// construct files from elements
							if (endCommitConstructCheckox) {
								for (IAdapter adapter : adapters) {
									URI constructUri = Paths.get(downloadPath, repositoryName, "construct",
											feature.getEndCommit().getSha(), " ").toUri();
									adapter.construct(constructUri, feature.getEndCommitElements(),
											new NullProgressMonitor());
								}
							}

							addedCloud = TimelineUtils.getTimelineElements(feature.getEndCommitElements(),
									feature.getEndCommitPenalizingElements(),i);

							addedFeatures = TimelineUtils.getTimelineWords(feature.getEndCommitElements(),
									feature.getEndCommitPenalizingElements(), maxFeatureNb,i);
						}

						if (feature.getNbStartCommitElements() >= startCommitThreshold
								&& feature.getNbAddedWords() >= addedWordsThreshold) {

							eventUpdate = true;

							// construct files from elements
							if (startCommitConstructCheckox) {
								for (IAdapter adapter : adapters) {
									URI constructUri = Paths.get(downloadPath, repositoryName, "construct",
											feature.getEndCommit().getSha(), "removed").toUri();
									adapter.construct(constructUri, feature.getStartCommitElements(),
											new NullProgressMonitor());
								}

							}

							removedCloud = TimelineUtils.getTimelineElements(feature.getStartCommitElements(),
									feature.getStartCommitPenalizingElements(),i);

							removedFeatures = TimelineUtils.getTimelineWords(feature.getStartCommitElements(),
									feature.getStartCommitPenalizingElements(), maxFeatureNb,i);


						}

						if (eventUpdate) {
							FeatureEvent event = new FeatureEvent(FeatureEvent.Type.COMMIT, feature.getStartCommit(),
									feature.getEndCommit(), addedFeatures, removedFeatures,
									feature.getNbEndCommitElements(), feature.getNbStartCommitElements(),
									feature.getNbAddedWords(), addedCloud, removedCloud, feature.getContributors());
							timeline.addEvent(event);
							grid.addEvent(event);
						}
					}

					// add tag events
					try {
						List<FeatureEvent> tagEvents = TimelineUtils.getTimelineTags(client.getTags());
						timeline.addEvents(tagEvents);
						grid.addEvents(tagEvents);
					} catch (IOException e) {
						e.printStackTrace();
					}

					// construct the timeline
					timeline.construct(Paths.get(downloadPath, repositoryName, "timeline").toString());
					System.out.println("Timeline: " + repositoryName + " generated");
					grid.construct(Paths.get(downloadPath, repositoryName, "grid").toString());
					System.out.println("Grid: " + repositoryName + " generated");

					adaptersFound = false;
				}
			}
		}
	}

	/**
	 * Get all the segments of each wanted strategy
	 * 
	 * @param strategies
	 *            list of strategies
	 * @param client
	 * @return a list of segments
	 */
	private List<Segment> getSegments(List<ISegmentSelectionStrategy> strategies) {

		List<Segment> segments = new ArrayList<>();
		List<Segment> stratSegments = null;

		for (ISegmentSelectionStrategy strat : strategies) {
			stratSegments = strat.getSegments(client, startPointCommit, endPointCommit);

			if (stratSegments != null) {

				for (Segment s : stratSegments) {
					if (!segments.contains(s)) {
						segments.add(s);
					}
				}
			}
		}

		return segments;
	}

	/**
	 * Interface for selecting the strategies
	 * 
	 * @return a list of strategies
	 */
	private List<ISegmentSelectionStrategy> selectStategy() {

		final Shell parent = new Shell(Display.getCurrent());
		final List<ISegmentSelectionStrategy> strategies = new ArrayList<>();
		List<ISegmentSelectionStrategy> availableStrategies = new ArrayList<>();

		final CheckboxTableViewer tableViewer;
		GridData gridData;

		// available strategies
		availableStrategies.add(new AllMergesSegmentSelection());
		availableStrategies.add(new OnlyPullRequestMergesSegmentSelection());
		availableStrategies.add(new SpecificCommitsSegmentSelection());
		availableStrategies.add(new NewWordSegmentSelection());
		availableStrategies.add(new ChangedElementsSegmentSelection());
		availableStrategies.add(new ChangedElementsNewWordsSegmentSelection());

		GridLayout gridLayout = new GridLayout(1, false);

		gridLayout.marginHeight = 50;
		gridLayout.marginWidth = 50;

		parent.setLayout(gridLayout);

		parent.setText("Strategies selection");

		parent.setSize(500, 400);

		parent.setBounds(600, 250, 500, 400);

		tableViewer = CheckboxTableViewer.newCheckList(parent,
				SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

		gridData = new GridData(400, 300);

		for (ISegmentSelectionStrategy s : availableStrategies) {
			tableViewer.add(s);
		}

		tableViewer.getControl().setLayoutData(gridData);

		final Table table = tableViewer.getTable();

		TableLayout layout = new TableLayout();

		TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.LEAD);
		col.getColumn().setText("Please, select one or more strategies");
		layout.addColumnData(new ColumnWeightData(500));

		table.setLayout(layout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		Composite buttonsComposite = new Composite(parent, SWT.NONE);

		GridLayout buttonsGridLayout = new GridLayout();
		buttonsGridLayout.marginLeft = 50;
		buttonsGridLayout.horizontalSpacing = 200;
		buttonsGridLayout.numColumns = 2;

		buttonsComposite.setLayout(buttonsGridLayout);

		Button cancelButton = new Button(buttonsComposite, SWT.PUSH);
		cancelButton.setText("Cancel");

		Button okButton = new Button(buttonsComposite, SWT.PUSH);
		okButton.setText("OK");

		cancelButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				parent.close();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		okButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] list = tableViewer.getCheckedElements();
				for (int i = 0; i < list.length; i++)
					strategies.add((ISegmentSelectionStrategy) list[i]);
				parent.close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}
		});

		Display display = parent.getDisplay();

		parent.pack();
		parent.open();
		while (!parent.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return strategies;

	}

	ISelection selection;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {

	}
}
