package org.but4reuse.adapters.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.but4reuse.adaptedmodel.Block;
import org.but4reuse.adaptedmodel.BlockElement;
import org.but4reuse.adaptedmodel.ElementWrapper;
import org.but4reuse.adaptedmodel.manager.AdaptedModelManager;
import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.helper.AdaptersHelper;
import org.but4reuse.artefactmodel.Artefact;
import org.but4reuse.construction.annotation.ConstructionWithAnnotationHelper;
import org.but4reuse.utils.ui.dialogs.URISelectionDialog;
import org.but4reuse.utils.workbench.WorkbenchUtils;
import org.but4reuse.visualisation.impl.visualiser.adaptedmodel.BlockElementsMarkupProvider;
import org.but4reuse.visualisation.impl.visualiser.adaptedmodel.BlockElementsOnArtefactsVisualisation;
import org.but4reuse.visualisation.impl.visualiser.adaptedmodel.BlockMarkupKind;
import org.eclipse.contribution.visualiser.core.ProviderDefinition;
import org.eclipse.contribution.visualiser.interfaces.IMarkupKind;
import org.eclipse.contribution.visualiser.views.Menu;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * 
 * 
 * @author tziadi
 */
public class ConstructWithAnnotations implements IViewActionDelegate {

	// TODO add these options to a preferences page
	static boolean KEEP_INTRINSIC_IDS = true;
	static boolean KEEP_EXTRENSIC_IDS = true;

	Menu menu;
	Map<String, List<IElement>> elementsMapByAdapter;
	Map<String, IAdapter> adaptersMap;
	URI constructionURI;
	int numberOfElements;
	URI blockConstructionURI;

	@Override
	public void run(IAction action) {
		// Get construction uri from user
		ConstructionWithAnnotationHelper.MAP_ELEMENTS.clear();
		ConstructionWithAnnotationHelper.CURRENT_BLOCK = null;
		ConstructionWithAnnotationHelper.initialize();
		String out = "/projectName";
		IContainer output = AdaptedModelManager.getDefaultOutput();
		if (output != null) {
			out = output.getFullPath().toString();
		}
		URISelectionDialog inputDialog = new URISelectionDialog(Display.getCurrent().getActiveShell(),
				"Construction URI", "Insert Construction URI", "platform:/resource" + out + "/");
		if (inputDialog.open() != Dialog.OK) {
			return;
		}
		String constructionURIString = inputDialog.getValue();
		constructionURI = null;
		try {
			constructionURI = new URI(constructionURIString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Construct
		ProviderDefinition definition = BlockElementsOnArtefactsVisualisation.getBlockElementsOnVariantsProvider();
		BlockElementsMarkupProvider markupProvider = (BlockElementsMarkupProvider) definition.getMarkupInstance();

		// Separate by adapter
		elementsMapByAdapter = new HashMap<String, List<IElement>>();
		adaptersMap = new HashMap<String, IAdapter>();
		numberOfElements = 0;

		for (Object o : markupProvider.getAllMarkupKinds()) {
			IMarkupKind kind = (IMarkupKind) o;

			if (menu.getActive(kind)) {
				Object active = kind;
				if (active instanceof BlockMarkupKind) {
					Block block = ((BlockMarkupKind) active).getBlock();
					for (BlockElement blockElement : block.getOwnedBlockElements()) {
						// TODO construct, for the moment we get the first
						// element of a blockelement
						ElementWrapper elementWrapper = blockElement.getElementWrappers().get(0);
						IElement element = (IElement) elementWrapper.getElement();
						IAdapter adapter = AdaptersHelper.getAdapter(element);

						// System.out.println("element is: " + element);
						// System.out.println("block is: " + block);
						if (adapter == null) {
							// TODO Report the error to the user
							System.out.println("No adapter declared for " + element.getClass());
							break;
						}
						String key = adapter.getClass().getName();
						adaptersMap.put(key, adapter);
						List<IElement> list = elementsMapByAdapter.get(key);
						if (list == null) {
							list = new ArrayList<IElement>();
						}
						list.add(element);
						ConstructionWithAnnotationHelper.MAP_ELEMENTS.put(element, block.getName());
						numberOfElements++;
						elementsMapByAdapter.put(key, list);

					}
				}
			}
		}

		// Launch Progress dialog
		ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());

		try {
			progressDialog.run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					// Number of adapters + total numbers of elements
					int totalWork = adaptersMap.keySet().size() + numberOfElements;
					monitor.beginTask("Construction", totalWork);

					// Construct through adapter
					for (String key : adaptersMap.keySet()) {
						IAdapter adap = adaptersMap.get(key);

						// TODO show a progress monitor
						System.out.println("Adapetr " + adap);

						adap.construct(constructionURI, elementsMapByAdapter.get(key), monitor);

						monitor.worked(1);
					}

					monitor.done();

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Refresh
		if (output != null) {
			WorkbenchUtils.refreshIResource(output);
		} else {
			WorkbenchUtils.refreshAllWorkspace();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	@Override
	public void init(IViewPart view) {
		menu = (Menu) view;
	}

}
