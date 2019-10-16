package org.but4reuse.visualisation.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.but4reuse.adaptedmodel.AdaptedArtefact;
import org.but4reuse.adaptedmodel.AdaptedModel;
import org.but4reuse.adaptedmodel.Block;
import org.but4reuse.adaptedmodel.helpers.AdaptedModelHelper;
import org.but4reuse.adaptedmodel.manager.AdaptedModelManager;
import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.helper.AdaptersHelper;
import org.but4reuse.feature.constraints.impl.ConstraintsHelper;
import org.but4reuse.featurelist.Feature;
import org.but4reuse.featurelist.FeatureList;
import org.but4reuse.utils.workbench.WorkbenchUtils;
import org.but4reuse.visualisation.IVisualisation;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;

/**
 * Metrics visualisation
 * 
 * @author jabier.martinez
 */
public class MetricsVisualisation implements IVisualisation {
	FeatureList featureList;
	AdaptedModel adaptedModel;

	public static boolean isUnderstandingResults = true;

	@Override
	public void prepare(FeatureList featureList, AdaptedModel adaptedModel, Object extra, IProgressMonitor monitor) {
		this.featureList = featureList;
		this.adaptedModel = adaptedModel;
		monitor.subTask("Metrics Visualisation");
	}

	@Override
	public void show() {
		// asyncExec to avoid SWT invalid thread access
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				StringBuilder text = new StringBuilder();
				// I use this to save a vector compatible with our excel structure
				Vector<Double> metricsValuesVector = new Vector<Double>();
				Vector<String> metricsMeaningVector = new Vector<String>();
				fillMetricsMeaning(metricsMeaningVector);
							
				// General metrics of the Adapted model
				appendUsedAdapters(text);
				text.append("--------------------------------------------\n");
				text.append("Artefacts= " + adaptedModel.getOwnedAdaptedArtefacts().size() + "\n");

				// 1 : no of PVs
				metricsValuesVector.add((double) adaptedModel.getOwnedAdaptedArtefacts().size());

				// Get IElement types
				List<String> iElementTypes = new ArrayList<String>();
				for (IAdapter adapter : AdaptedModelManager.getAdapters()) {
					iElementTypes.addAll(AdaptersHelper.getAdapterIElements(adapter));
				}

				List<Double> nElementsPerArtefact = new ArrayList<Double>();
				for (AdaptedArtefact aa : adaptedModel.getOwnedAdaptedArtefacts()) {
					double nElements = aa.getOwnedElementWrappers().size();
					nElementsPerArtefact.add(nElements);
				}
				addMetrics(text, "Number of Elements per Artefact", nElementsPerArtefact);

				text.append("\n\nElement types per Artefact\n");
				text.append(";");
				for (String elementType : iElementTypes) {
					text.append(elementType.substring(elementType.lastIndexOf(".") + 1, elementType.length()) + ";");
				}
				text.setLength(text.length() - 1);
				text.append("\n");
				for (AdaptedArtefact aa : adaptedModel.getOwnedAdaptedArtefacts()) {
					text.append(aa.getArtefact().getName() + ";");
					for (String elementType : iElementTypes) {
						text.append(AdaptedModelHelper.getNumberOfElementsOfType(aa, elementType) + ";");
					}
					text.setLength(text.length() - 1);
					text.append("\n");
				}

				text.append("\n--------------------------------------------\n");
				text.append("Block statistics\n\n");

				double sizeAllBlocks = 0;
				sizeAllBlocks = 0;
				text.append("Number of blocks= " + adaptedModel.getOwnedBlocks().size() + "\n");

				// 1 : no of blocks
				metricsValuesVector.add((double) adaptedModel.getOwnedBlocks().size());

				List<Double> nElementsPerBlock = new ArrayList<Double>();
				for (Block block : adaptedModel.getOwnedBlocks()) {
					double nElements = block.getOwnedBlockElements().size();
					sizeAllBlocks += nElements;
					nElementsPerBlock.add(nElements);
				}
				addBlockMetrics(text, metricsValuesVector, "Number of Elements per Block", nElementsPerBlock);
				// add the total size of all distinguish elements in the family
				text.append("\nSize of all blocks together= " + sizeAllBlocks);
				metricsValuesVector.add((double) sizeAllBlocks);

				// identify common, shared and exclusive blocks
				Block commonblock = null;
				List<Block> sharedBlocks = new ArrayList<Block>();
				List<Block> exclusiveBlocks = new ArrayList<Block>();
				for (Block b : adaptedModel.getOwnedBlocks()) {
					int counter = 0; // counter for number of artifacts a block belongs
					for (AdaptedArtefact aa : adaptedModel.getOwnedAdaptedArtefacts()) {
						List<Block> blocksOfAA = AdaptedModelHelper.getBlocksOfAdaptedArtefact(aa);
						if (blocksOfAA.contains(b)) {
							counter++;
						}
					}
					if (counter == 1) {
						exclusiveBlocks.add(b);
					} else if (counter == adaptedModel.getOwnedAdaptedArtefacts().size()) {
						commonblock = b;
					} else {
						sharedBlocks.add(b);
					}
				}

				// add common block statistics
				if (commonblock != null) {
					text.append("\n\nCommon block size= " + commonblock.getOwnedBlockElements().size() + "\n");
					metricsValuesVector.add((double) commonblock.getOwnedBlockElements().size());

					text.append("Percentage of common block to all blocks= "
							+ (1.0 / adaptedModel.getOwnedBlocks().size()) + "\n");
					metricsValuesVector.add(1.0 / adaptedModel.getOwnedBlocks().size());

					text.append("Percentage of elements of common block= "
							+ (commonblock.getOwnedBlockElements().size() / sizeAllBlocks) + "\n");
					metricsValuesVector.add((double) (commonblock.getOwnedBlockElements().size() / sizeAllBlocks));
				} else {
					text.append("\n\nCommon block size= " + 0 + "\n");
					metricsValuesVector.add(0.0);

					text.append("Percentage of common block to all blocks= " + 0 + "\n");
					metricsValuesVector.add(0.0);

					text.append("Percentage of elements of common block= " + 0 + "\n");
					metricsValuesVector.add(0.0);

				}

				// add shared block statistics
				text.append("\n\nNumber of shared blocks= " + sharedBlocks.size() + "\n");
				metricsValuesVector.add((double) sharedBlocks.size());

				double sizeAllSharedBlocks = 0;
				List<Double> nElementsPerSharedBlocks = new ArrayList<Double>();
				for (Block block : sharedBlocks) {
					double nElements = block.getOwnedBlockElements().size();
					sizeAllSharedBlocks += nElements;
					nElementsPerSharedBlocks.add(nElements);
				}
				text.append("Percentage of shared blocks= "
						+ ((double) sharedBlocks.size() / adaptedModel.getOwnedBlocks().size()) + "\n");
				metricsValuesVector.add(((double) sharedBlocks.size() / adaptedModel.getOwnedBlocks().size()));

				text.append("Number of all elements in all shared blocks= " + sizeAllSharedBlocks + "\n");
				metricsValuesVector.add(sizeAllSharedBlocks);

				text.append("Percentage of elements of shared blocks= " + (sizeAllSharedBlocks / sizeAllBlocks) + "\n");
				metricsValuesVector.add((sizeAllSharedBlocks / sizeAllBlocks));

				addBlockMetrics(text, metricsValuesVector, "Number of Elements per Shared Blocks", nElementsPerSharedBlocks);

				// add shared block statistics
				text.append("\n\nNumber of exclusive blocks= " + exclusiveBlocks.size() + "\n");
				metricsValuesVector.add((double) exclusiveBlocks.size());

				double sizeAllExclusiveBlocks = 0;
				List<Double> nElementsPerExclusiveBlocks = new ArrayList<Double>();
				for (Block block : exclusiveBlocks) {
					double nElements = block.getOwnedBlockElements().size();
					sizeAllExclusiveBlocks += nElements;
					nElementsPerExclusiveBlocks.add(nElements);
				}

				text.append("Percentage of exclusive blocks= "
						+ ((double) exclusiveBlocks.size() / adaptedModel.getOwnedBlocks().size()) + "\n");
				metricsValuesVector.add(((double) exclusiveBlocks.size() / adaptedModel.getOwnedBlocks().size()));

				text.append("Number of all elements in all exclusive blocks= " + sizeAllExclusiveBlocks + "\n");
				metricsValuesVector.add(sizeAllExclusiveBlocks);

				text.append("Percentage of elements of exclusive blocks= " + (sizeAllExclusiveBlocks / sizeAllBlocks)
						+ "\n");
				metricsValuesVector.add((sizeAllExclusiveBlocks / sizeAllBlocks));

				addBlockMetrics(text, metricsValuesVector, "Number of Elements per Exclusive Blocks",
						nElementsPerExclusiveBlocks);

				text.append("\n\nSize of " + adaptedModel.getOwnedBlocks().size() + " blocks\n");
				for (Block block : adaptedModel.getOwnedBlocks()) {
					text.append("block " + block.getName() + " = " + block.getOwnedBlockElements().size() + "\n");
				}

				text.append("\nSize of " + sharedBlocks.size() + " shared blocks\n");
				for (Block block : sharedBlocks) {
					text.append("block " + block.getName() + " = " + block.getOwnedBlockElements().size() + "\n");
				}

				text.append("\nSize of " + exclusiveBlocks.size() + " exclusive blocks\n");
				for (Block block : exclusiveBlocks) {
					text.append("block " + block.getName() + " = " + block.getOwnedBlockElements().size() + "\n");
				}

				text.append("\n\nElement types per Block\n");
				text.append(";");
				for (String elementType : iElementTypes) {
					text.append(elementType.substring(elementType.lastIndexOf(".") + 1, elementType.length()) + ";");
				}
				text.setLength(text.length() - 1);
				text.append("\n");
				for (Block block : adaptedModel.getOwnedBlocks()) {
					text.append(block.getName() + " ; ");
					for (String elementType : iElementTypes) {
						text.append(AdaptedModelHelper.getNumberOfElementsOfType(block, elementType) + " ; ");
					}
					text.setLength(text.length() - 1);
					text.append("\n");
				}

				appendBlocksOnArtefacts(text);

				text.append("--------------------------------------------\n");
				text.append("Number of Block Constraints= "
						+ ConstraintsHelper.getCalculatedConstraints(adaptedModel).size() + "\n");

				if (featureList != null) {
					// Feature Related Metrics
					text.append("--------------------------------------------\n");
					text.append("Features= " + featureList.getOwnedFeatures().size() + "\n");
					List<Double> nBlocksInFeatures = new ArrayList<Double>();
					List<Double> nElementsInFeatures = new ArrayList<Double>();
					for (Feature feature : featureList.getOwnedFeatures()) {
						List<Block> blocks = ConstraintsHelper.getCorrespondingBlocks(adaptedModel, feature);
						double nBlocks = blocks.size();
						double nElements = 0;
						for (Block block : blocks) {
							nElements += block.getOwnedBlockElements().size();
						}
						nBlocksInFeatures.add(nBlocks);
						nElementsInFeatures.add(nElements);
					}
					addMetrics(text, "Number of Blocks assigned to a Feature", nBlocksInFeatures);

					text.append("\n\nBlocks on Features\n");
					for (Feature feature : featureList.getOwnedFeatures()) {
						text.append(feature.getName() + " = ");
						List<Block> blocks = ConstraintsHelper.getCorrespondingBlocks(adaptedModel, feature);
						for (Block b : blocks) {
							text.append(b.getName() + ", ");
						}
						// remove last comma
						if (!blocks.isEmpty()) {
							text.setLength(text.length() - 2);
						}
						text.append("\n");
					}

					text.append("\nBlocks on Features\n");
					text.append(";");
					for (Block b : adaptedModel.getOwnedBlocks()) {
						text.append(b.getName() + ";");
					}
					text.setLength(text.length() - 1);
					text.append("\n");
					for (Feature feature : featureList.getOwnedFeatures()) {
						text.append(feature.getName() + ";");
						List<Block> blocks = ConstraintsHelper.getCorrespondingBlocks(adaptedModel, feature);
						for (Block b : adaptedModel.getOwnedBlocks()) {
							if (blocks.contains(b)) {
								text.append("1;");
							} else {
								text.append("0;");
							}
						}
						text.setLength(text.length() - 1);
						text.append("\n");
					}

					addMetrics(text, "Number of Elements assigned to a Feature", nElementsInFeatures);
					text.append("\n");
				}

				text.append("--------------------------------------------\n");
				text.append("Times in milliseconds\n");
				for (Entry<String, Long> entry : AdaptedModelManager.getElapsedTimeRegistry().entrySet()) {
					String key = entry.getKey();
					if (key.equals("Adapt all artefacts")
							|| key.equals("Block identification IntersectionsBlockIdentification") || key.equals("Block identification FCABlockIdentification")) {
						Long time = entry.getValue();
						metricsValuesVector.add((double) time);
					}
					text.append(entry.getKey() + "= " + entry.getValue() + "\n");
				}

				String name = AdaptedModelHelper.getName(adaptedModel);
				if (name == null) {
					name = "";
				}

				MetricsVisualisationView view = (MetricsVisualisationView) WorkbenchUtils
						.forceShowView(MetricsVisualisationView.ID);

				String metricsVectorForExcel = "";

				for (int i = 0; i < metricsMeaningVector.size(); i++) {
					metricsVectorForExcel = metricsVectorForExcel + metricsMeaningVector.elementAt(i) + "\t";
				}
				metricsVectorForExcel += "\n";
				for (int i = 0; i < metricsValuesVector.size(); i++) {
					metricsVectorForExcel = metricsVectorForExcel + metricsValuesVector.elementAt(i) + "\t";
				}
				text.append(metricsVectorForExcel);
				view.scrollable.setText(text.toString());

				// Get the path to save the txt file (same project of the file ".artefactmodel")
				IContainer output = AdaptedModelManager.getDefaultOutput();
				File outputFile = WorkbenchUtils.getFileFromIResource(output);
				// directory name
				File graphsFolder = new File(outputFile, "metric results");
				graphsFolder.mkdir();

				// Give a name to the file with the timestamp
				DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
				Date currentDate = new Date();

				File file = new File(graphsFolder, "result_" + dateFormat.format(currentDate) + ".txt");

				try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
					writer.write(text.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (isUnderstandingResults) {
					String folderName = outputFile.getParent();
					String fileName = outputFile.getName();
					File understandingMetricsFolder = new File(folderName, "Understanding results");
					understandingMetricsFolder.mkdir();
					File undestandingFile = new File(understandingMetricsFolder,
							fileName + "  " + dateFormat.format(currentDate) + ".txt");
					try (BufferedWriter understrandingWriter = new BufferedWriter(new FileWriter(undestandingFile))) {
						understrandingWriter.write(text.toString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			private void fillMetricsMeaning(Vector<String> metricsMeaningVector){
				metricsMeaningVector.add("#_of_PVs");
				metricsMeaningVector.add("#_of_blocks");
				metricsMeaningVector.add("Min_block_size_all_blocks");
				metricsMeaningVector.add("Max_block_size_all_blocks");
				metricsMeaningVector.add("Mean_block_size_all_blocks");
				metricsMeaningVector.add("Lower_quartile_Mean_all_blocks");
				metricsMeaningVector.add("Upper_quartile_Mean_all_blocks");
				metricsMeaningVector.add("Median_block_size_all_blocks");
				metricsMeaningVector.add("Lower_quartile_Median_all_blocks");
				metricsMeaningVector.add("Upper_quartile_Median_all_blocks");
				metricsMeaningVector.add("StdDev_block_size_all_blocks");
				
				metricsMeaningVector.add("Size_of_all_blocks");
				metricsMeaningVector.add("Size_of_common_block");
				metricsMeaningVector.add("Percentage_of_common_block");
				metricsMeaningVector.add("Percentage_of_elements_of_common_block");
				
				metricsMeaningVector.add("#_of_shared_blocks");					
				metricsMeaningVector.add("Percentage_of_shared_blocks");
				metricsMeaningVector.add("#_of_elements_on_shared_blocks");
				metricsMeaningVector.add("Percentage_of_elements_of_shared_blocks");
				metricsMeaningVector.add("Min_block_size_shared_blocks");
				metricsMeaningVector.add("Max_block_size_shared_blocks");
				metricsMeaningVector.add("Mean_block_size_shared_blocks");
				metricsMeaningVector.add("Lower_quartile_Mean_shared_blocks");
				metricsMeaningVector.add("Upper_quartile_Mean_shared_blocks");
				metricsMeaningVector.add("Median_block_size_shared_blocks");
				metricsMeaningVector.add("Lower_quartile_Median_shared_blocks");
				metricsMeaningVector.add("Upper_quartile_Median_shared_blocks");
				metricsMeaningVector.add("StdDev_block_size_shared_blocks");
				
				metricsMeaningVector.add("#_of_exclusive_blocks");		
				metricsMeaningVector.add("Percentage_of_exclusive_blocks");
				metricsMeaningVector.add("#_of_elements_of_exclusive_blocks");
				metricsMeaningVector.add("Percentage_of_elements_of_exclusive_blocks");
				metricsMeaningVector.add("Min_block_size_exclusive_blocks");
				metricsMeaningVector.add("Max_block_size_exclusive_blocks");
				metricsMeaningVector.add("Mean_block_size_exclusive_blocks");
				metricsMeaningVector.add("Lower_quartile_Mean_exclusive_blocks");
				metricsMeaningVector.add("Upper_quartile_Mean_exclusive_blocks");
				metricsMeaningVector.add("Median_block_size_exclusive_blocks");
				metricsMeaningVector.add("Lower_quartile_Median_exclusive_blocks");
				metricsMeaningVector.add("Upper_quartile_Median_exclusive_blocks");
				metricsMeaningVector.add("StdDev_block_size_exclusive_blocks");
				metricsMeaningVector.add("Adapt_all_artefacts");
				metricsMeaningVector.add("Block_identification_Intersections");
			}

			private void appendUsedAdapters(StringBuilder text) {
				text.append("Adapter= ");
				for (IAdapter adapter : AdaptedModelManager.getAdapters()) {
					text.append(AdaptersHelper.getAdapterName(adapter) + ",");
				}
				text.setLength(text.length() - 1);
				text.append("\n");
			}

			private void appendBlocksOnArtefacts(StringBuilder text) {
				text.append("\nBlocks on Artefacts\n;");
				for (Block b : adaptedModel.getOwnedBlocks()) {
					text.append(b.getName() + ";");
				}
				text.setLength(text.length() - 1);
				text.append("\n");
				for (AdaptedArtefact aa : adaptedModel.getOwnedAdaptedArtefacts()) {
					text.append(aa.getArtefact().getName() + ";");
					List<Block> blocksOfAA = AdaptedModelHelper.getBlocksOfAdaptedArtefact(aa);
					for (Block b : adaptedModel.getOwnedBlocks()) {
						if (blocksOfAA.contains(b)) {
							text.append("1;");
						} else {
							text.append("0;");
						}
					}
					text.setLength(text.length() - 1);
					text.append("\n");
				}
			}
		});
	}

	public static double mean(List<Double> list) {
		double sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += list.get(i);
		}
		return sum / list.size();
	}

	// must be sorted list
	public static double median(List<Double> list) {
		int middle = list.size() / 2;
		if (list.size() % 2 == 1) {
			return list.get(middle);
		} else {
			return (list.get(middle - 1) + list.get(middle)) / 2.0;
		}
	}

	public static double standardDeviation(List<Double> list, Double mean) {
		Double numerator = 0.0;
		for (int i = 0; i < list.size(); i++) {
			numerator += Math.pow((list.get(i) - mean), 2);
		}
		numerator = numerator / list.size();
		return Math.sqrt(numerator);
	}

	// must be sorted list
	public static double lowerQuartileMedian(List<Double> list) {
		int size = list.size();
		List<Double> firstHalf = new ArrayList<>(list.subList(0, (size + 1) / 2));
		if (!firstHalf.isEmpty()) {
			return median(firstHalf);
		} else {
			return 0;
		}
	}

	// must be sorted list
	public static double upperQuartileMedian(List<Double> list) {
		int size = list.size();
		List<Double> secondHalf = new ArrayList<>(list.subList((size + 1) / 2, size));
		if (!secondHalf.isEmpty()) {
			return median(secondHalf);
		} else {
			return 0;
		}

	}

	// must be sorted list
	public static double lowerQuartileMean(List<Double> list) {
		int size = list.size();
		List<Double> firstHalf = new ArrayList<>(list.subList(0, (size + 1) / 2));
		if (!firstHalf.isEmpty()) {
			return mean(firstHalf);
		} else {
			return 0;
		}
	}

	// must be sorted list
	public static double upperQuartileMean(List<Double> list) {
		int size = list.size();
		List<Double> secondHalf = new ArrayList<>(list.subList((size + 1) / 2, size));
		if (!secondHalf.isEmpty()) {
			return mean(secondHalf);
		} else {
			return 0;
		}
	}

	public static void addMetrics(StringBuilder stringBuilder, String title, List<Double> data) {
		if (!data.isEmpty()) {
			Collections.sort(data);
			stringBuilder.append("\n" + title);
			stringBuilder.append("\nMin= " + data.get(0));
			stringBuilder.append("\nMax= " + data.get(data.size() - 1));
			Double mean = mean(data);
			stringBuilder.append("\nMean= " + mean);
			stringBuilder.append("\nMedian= " + median(data));
			stringBuilder.append("\nStdDev= " + standardDeviation(data, mean));
		}
	}

	public static void addBlockMetrics(StringBuilder stringBuilder, Vector<Double> metricsVec, String title,
			List<Double> data) {
		if (!data.isEmpty()) {
			Collections.sort(data);
			stringBuilder.append("\n" + title);

			stringBuilder.append("\nMin block size= " + data.get(0));
			metricsVec.add((double) data.get(0));

			stringBuilder.append("\nMax block size= " + data.get(data.size() - 1));
			metricsVec.add((double) (data.get(data.size() - 1)));

			Double mean = mean(data);
			stringBuilder.append("\n\nMean of block size= " + mean);
			metricsVec.add((double) mean);

			stringBuilder.append("\nLower quartile Mean= " + lowerQuartileMean(data));
			metricsVec.add((double) lowerQuartileMean(data));

			stringBuilder.append("\nUpper quartile Mean= " + upperQuartileMean(data));
			metricsVec.add((double) upperQuartileMean(data));

			Double median = median(data);
			stringBuilder.append("\n\nMedian of block size= " + median);
			metricsVec.add((double) median);

			stringBuilder.append("\nLower quartile Median= " + lowerQuartileMedian(data));
			metricsVec.add((double) lowerQuartileMedian(data));

			stringBuilder.append("\nUpper quartile Median= " + upperQuartileMedian(data));
			metricsVec.add((double) upperQuartileMedian(data));

			stringBuilder.append("\n\nStdDev of block size= " + standardDeviation(data, mean));
			metricsVec.add((double) standardDeviation(data, mean));

		} else {
			stringBuilder.append("\nMin block size= " + 0);
			metricsVec.add(0.0);
			stringBuilder.append("\nMax block size= " + 0);
			metricsVec.add(0.0);

			stringBuilder.append("\n\nMean of block size= " + 0);
			metricsVec.add(0.0);

			stringBuilder.append("\nLower quartile Mean= " + 0);
			metricsVec.add(0.0);

			stringBuilder.append("\nUpper quartile Mean= " + 0);
			metricsVec.add(0.0);

			stringBuilder.append("\n\nMedian of block size= " + 0);
			metricsVec.add(0.0);

			stringBuilder.append("\nLower quartile Median= " + 0);
			metricsVec.add(0.0);

			stringBuilder.append("\nUpper quartile Median= " + 0);
			metricsVec.add(0.0);

			stringBuilder.append("\n\nStdDev of block size= " + 0);
			metricsVec.add(0.0);
		}

	}

}
