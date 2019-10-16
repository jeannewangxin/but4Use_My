package construction;

import java.net.URI;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.but4reuse.adapters.IElement;

import org.eclipse.core.runtime.IProgressMonitor;

import elements.CppElement;
import elements.CppElement.CppElementType;
import elements.HeaderFile;

import elements.SourceFile;

public class Construction {

	/*
	 * this array is for all the element of all pvs but in different structure not
	 * structure of CppElemnt but ConstructionModal structure
	 */
	ArrayList<ConstructionModal> orderedElement;

	/* contain set of blocs */
	private Set<String> blocs;

	/* contain set of files in all product variant */
	private Set<CppElement> allFiles;

	/* contains set of files in each bloc */
	private ArrayList<CppElement> files;

	/*
	 * this structure is for the association of each file to his stack, the string
	 * is the key wich is the name of the file , stacks is the stack representation
	 * of the file
	 */
	private HashMap<String, Stack> stacks;

	/* the stack for the current file */
	private Stack stackFile;

	/*
	 * to hold the set of corresponding element in the current bloc 'corresponding
	 * mean should be constructed directly'
	 */
	private ArrayList<ConstructionModal> correspendingElement;

	/* to hold the elements of the current bloc */
	private ArrayList<ConstructionModal> elementsOfBloc;

	/*
	 * because we need to save each set of unconstructed elements in a bloc; to
	 * constructed them after
	 */
	private ArrayList<ArrayList<ConstructionModal>> unconstructedElementsSet;

	/* this structure is to hold the unconstructed element of each file */
	private ArrayList<ArrayList<ConstructionModal>> fileElementUnconstructed;

	/*
	 * this structure will hold all unconstructed statementImplementation and will
	 * be used by FunctionImplementation to construct bodies
	 */
	public static ArrayList<ConstructionModal> unconstructedStatements = new ArrayList<ConstructionModal>();

	public Construction() {

		this.orderedElement = new ArrayList<ConstructionModal>();
		this.blocs = new HashSet<String>();
		this.allFiles = new HashSet<CppElement>();
		this.stacks = new HashMap<String, Stack>();
		this.files = new ArrayList<CppElement>();
		this.correspendingElement = new ArrayList<ConstructionModal>();
		this.elementsOfBloc = new ArrayList<ConstructionModal>();
		unconstructedElementsSet = new ArrayList<ArrayList<ConstructionModal>>();
		this.fileElementUnconstructed = new ArrayList<ArrayList<ConstructionModal>>();
	}

	public void construct(URI uri, List<IElement> elements, IProgressMonitor monitor) {

		ConstructionModal cm;
		// getting element and their blocks and their order/ and initializing blocs
		this.orderedElement = ConstructionHelper.getOrderForAllElements(elements, this.blocs);
		for (String bloc : this.blocs) {
			// get element for the specefyed bloc 'bloc'
			gettingElementOfThisBloc(bloc);
			// System.out.print("\n actual bloc " + bloc);
			gettingFiles(bloc);
			// System.out.print("\n list of files " + this.files.toString());
			for (CppElement c : this.files) {
				this.allFiles.add(c);
				this.stackFile = new Stack();
				// inserting annotation start in the beginning of the file
				this.stackFile.push(ConstructionHelper.insertAnnotationStart(bloc));
				// we get element that are in this block 'bloc' and their parent is this file
				// 'c'
				getListCorrespendingElement(c);

				// this procedure sort them from small to great so insersion directe dans la
				// pile (pas d'inversion)
				orderElementByOrder(this.correspendingElement);

				// begin insertion in the stack because this element their parent (file is here)
				// and they are in the same block
				for (int i = 0; i < this.correspendingElement.size(); i++) {
					this.stackFile.push(this.correspendingElement.get(i));
				}

				// close the annotation start
				this.stackFile.push(ConstructionHelper.insertAnnotationEnd(bloc));

				// inserting the stack (that correspend to this file) in the list of stacks

				this.stacks.put(c.getRawText(), this.stackFile);
			}

			// adding the not constructed element to the set of unconstructed element
			// because
			// this element their file parent is not here or there is no file at all
			if (this.elementsOfBloc.size() > 0) {
				this.unconstructedElementsSet.add(new ArrayList<ConstructionModal>(this.elementsOfBloc));

			}

		}

		// constructing the unconstructed elements in the stack // here normalement les
		// element n'ont
		// pas ts le meme parent (en va voir) order them by : blocs, files, order

		constructUnconstructed();

		// we have all stack in the structure 'stacks', and each stack represent a files
		// so we construct files
		try {

			ConstructionHelper.constructAllFiles(this.stacks, uri);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void gettingElementOfThisBloc(String b) {
		this.elementsOfBloc.clear();
		for (ConstructionModal cm : this.orderedElement) {
			if ( b == null) {
				System.err.println("b is null");
			}else {
				System.err.println("b is not null");
			}

			if (cm.block.equals(b)) {
				this.elementsOfBloc.add(cm);
			}
		}
	}

	private void gettingFiles(String b) {
		this.files.clear();
		for (ConstructionModal cm : this.orderedElement) {
			if (((cm.element instanceof HeaderFile) || (cm.element instanceof SourceFile)) && (cm.block.equals(b))) {

				this.files.add(cm.element);
			}
		}
	}

	public void getListCorrespendingElement(CppElement parent) {
		this.correspendingElement.clear();

		ArrayList<ConstructionModal> tmp = new ArrayList<ConstructionModal>();

		for (ConstructionModal cm : this.elementsOfBloc) {

			if (ElementConstructionHelper.parent(cm.element, parent)) {
				// we shoud add this element
				this.correspendingElement.add(cm);
			}
		}

		for (ConstructionModal cm : this.elementsOfBloc) {
			if (this.correspendingElement.contains(cm) == false) {

				if (cm.order != 0) { // to exclude files
					tmp.add(cm);
				}
			}

		}
		this.elementsOfBloc.clear();
		this.elementsOfBloc = tmp;
	}

	private void orderElementByOrder(ArrayList<ConstructionModal> cm) {

		int i = 0;
		int j = 0;
		ConstructionModal a;
		for (i = 0; i < cm.size(); i++) {
			for (j = i + 1; j < cm.size(); ++j) {
				if (cm.get(i).order > cm.get(j).order)

				{

					a = cm.get(i);
					cm.set(i, cm.get(j));
					cm.set(j, a);
				}

			}

		}

	}

	private String getStackOfUnconstructedElement(ConstructionModal array) {
		Stack tmp = new Stack();
		Iterator it = stacks.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();

			tmp = (Stack) pair.getValue();
			for (Object object : tmp) {
				if (ElementConstructionHelper.getLastParent(array.element).getRawText()
						.equals(pair.getKey().toString()))
					return pair.getKey().toString();

			}

		}
		return " ";
	}

	private void constructUnconstructed() {
		Iterator i = this.unconstructedElementsSet.iterator();

		ArrayList<ConstructionModal> array;
		String stackFile = null;
		while (i.hasNext()) {// for each set in the set of unconstructed element, that we are sure that they
								// are in the same block
			array = (ArrayList<ConstructionModal>) i.next();// this set is not ordered and may contain many files
			// we order them by file in the structure 'fileElementUnconstructed'
			orderUnconstructedElementsByFiles(array);
			// we order them each set in the corresponding file from the structure
			// 'fileElementUnconstructed'
			for (ArrayList<ConstructionModal> unconstructedOrderedElementByFile : this.fileElementUnconstructed) {

				// we order this set 'unconstructedOrderedElementByFile' that contain
				// unconstructed element in the same block and same file

				orderElementByOrder(unconstructedOrderedElementByFile);
				// we get the parent of this element // we get index 0 because all elements have
				// the same parent and we are sure that is not empty one element in file witch
				// is annotation at leaast
				stackFile = getStackOfUnconstructedElement(unconstructedOrderedElementByFile.get(0));
				// we have parent and the the set of unconstructed element ordered by order and
				// in the same file, we
				// begging constructing
				constructInStuck(stackFile, unconstructedOrderedElementByFile);

			}

		}
	}

	private void orderUnconstructedElementsByFiles(ArrayList<ConstructionModal> constructionModal) {

		this.fileElementUnconstructed.clear();
		ArrayList<ConstructionModal> tmp;

		for (CppElement file : this.allFiles) {
			tmp = new ArrayList<ConstructionModal>();
			for (ConstructionModal cm : constructionModal) {
				if (ElementConstructionHelper.parent(cm.element, file)) {
					tmp.add(cm);
				}

			}
			// we add the list of unconstructed elements correspending to this file and
			// belonging to the same bloc
			if (tmp.size() > 0) {
				this.fileElementUnconstructed.add(new ArrayList<ConstructionModal>(tmp));
			}
		}
	}

	/*
	 * this procedure make construction of a set of unconstructed elements in their
	 * correspending stack
	 */
	private void constructInStuck(String key, ArrayList<ConstructionModal> arrayCmElementOrdered) {

		boolean b;
		Stack tmp = this.stacks.get(key);

		Stack exchange = new Stack();
		ConstructionModal cm;
		Iterator it;

		if (key.substring(key.length() - 1).equals("h")) {
			// building just the .h files
			for (ConstructionModal cmElement : arrayCmElementOrdered) {
//				System.out
//						.println("\n entered element " + cmElement.element.getRawText() + " order " + cmElement.order);
				// we iterate on the stack of the file
				if (tmp != null) {
					it = tmp.iterator();
					while (it.hasNext()) {
						cm = (ConstructionModal) tmp.pop();
						// pour chaque element de cette arraylist, on prend par ordre et on insert
						// directement, comme les elements de 'arrayCmElementOrdered' sont ordonn�es
						// donc on fait le pop de la pile du fichier une seul fois

						if ((cm.order > cmElement.order) || (cm.order == -1)) {
//						System.out.println("\n the poped one in exchange " + cm.element.getRawText());
//						new java.util.Scanner(System.in).nextLine();
							exchange.push(cm);// we saved the popped element in the exchange stack
						} else {
							// we should returened the poped element
							tmp.push(cm);
							// beging processing
							if ((cm.order == cmElement.order) && (((ConstructionModal) exchange.peek()).order == -1)
									&& (cm.block.equals(cmElement.block))) {

								tmp.push(cmElement);
								tmp.push((ConstructionModal) exchange.pop());

							} else {
								b = ConstructionHelper.ifdefExistInBottom(tmp,
										((ConstructionModal) exchange.peek()).block);
								if ((cm.order == cmElement.order) && (((ConstructionModal) exchange.peek()).order == -1)
										&& (cm.block.equals(cmElement.block) == false) && (b == true)) {
									// do nothing
//								tmp.push(ConstructionHelper.insertAnnotationStart(cmElement.block));
//								tmp.push(cmElement);
//								tmp.push(ConstructionHelper.insertAnnotationEnd(cmElement.block));
								} else {
									if ((cm.order == cmElement.order)
											&& (((ConstructionModal) exchange.peek()).order == -1)
											&& (cm.block.equals(cmElement.block) == false)) {

										tmp.push((ConstructionModal) exchange.pop());

									}

								}

								// different order and different block
								// here we insert the entered element 'cmElement'
								tmp.push(ConstructionHelper.insertAnnotationStart(cmElement.block));
								tmp.push(cmElement);
								tmp.push(ConstructionHelper.insertAnnotationEnd(cmElement.block));

							}

							// we return elements popped
							it = exchange.iterator();
							while (it.hasNext()) {
								tmp.push((ConstructionModal) exchange.pop());
							}

						}
					}
				}
			}
		} else {
			// building the .cpp or .c files
			for (ConstructionModal cmElement : arrayCmElementOrdered) {
				if (cmElement.order <= 6) {
					// we iterate on the stack of the file
					System.out.println("\n key " + key);

					if (tmp != null) {
						it = tmp.iterator();
						while (it.hasNext()) {
							cm = (ConstructionModal) tmp.pop();
							// pour chaque element de cette arraylist, on prend par ordre et on insert
							// directement, comme les elements de 'arrayCmElementOrdered' sont ordonn�es
							// donc on fait le pop de la pile du fichier une seul fois

							if ((cm.order > cmElement.order) || (cm.order == -1)) {

								exchange.push(cm);// we saved the popped element in the exchange stack
							} else {
								// we should returened the poped element from the .cpp file
								tmp.push(cm);
								if ((cm.order == cmElement.order) && (((ConstructionModal) exchange.peek()).order == -1)
										&& (cm.block.equals(cmElement.block))) {

									tmp.push(cmElement);
									tmp.push((ConstructionModal) exchange.pop());

								} else {
									b = ConstructionHelper.ifdefExistInBottom(tmp,
											((ConstructionModal) exchange.peek()).block);
									if ((cm.order == cmElement.order)
											&& (((ConstructionModal) exchange.peek()).order == -1)
											&& (cm.block.equals(cmElement.block) == false) && (b == true)) {
										// skip
									} else {
										if ((cm.order == cmElement.order)
												&& (((ConstructionModal) exchange.peek()).order == -1)
												&& (cm.block.equals(cmElement.block) == false)) {

											tmp.push((ConstructionModal) exchange.pop());

										}

									}

									// different order and different block
									// here we insert the entered element 'cmElement'

									tmp.push(ConstructionHelper.insertAnnotationStart(cmElement.block));
									tmp.push(cmElement);
									tmp.push(ConstructionHelper.insertAnnotationEnd(cmElement.block));

								}

								// we return the poped elements that are saved in the 'exchange' stack
								it = exchange.iterator();
								while (it.hasNext()) {
									if (((ConstructionModal) exchange.peek()).element
											.getType() != CppElement.CppElementType.STATEMENT_IMPL) {
										tmp.push((ConstructionModal) exchange.pop());
										System.out.println("\n the pushed element : "
												+ ((ConstructionModal) tmp.peek()).element.getText());
									}

									else {
										// we should illeminate statement, because we have function and their bodies so
										// construction will be after
										unconstructedStatements.add((ConstructionModal) exchange.pop());
										System.out.println("\n size : " + unconstructedStatements.size());

									}
								}

							}
						}
					}
				}
			}
		}

	}

	public static void getInstruction(CppElement c) {
		System.out.println("\n size dakhel get : " + unconstructedStatements.size());
		for (ConstructionModal constructionModal : unconstructedStatements) {
			System.out.println("\n inst : " + constructionModal.element.getRawText());
		}
	}

	// this comments code is for desplaying and code tracking do not remove until
	// code cleanning and refactoring
//	Iterator it = this.stackFile.iterator();
//	while (it.hasNext()) {
//		cm = (ConstructionModal) it.next();
//		System.out.print("\n element  " + cm.element.getRawText() + "------------------------------ his order "
//				+ cm.order);
//	}	

	/* this procedure for displaying */
//	Stack tmp = new Stack();
//	Iterator it = stacks.entrySet().iterator();
//	while (it.hasNext()) {
//		Map.Entry pair = (Map.Entry) it.next();
//		System.out.println("\n " + pair.getKey() + " = ");
//		tmp = (Stack) pair.getValue();
//		for (Object object : tmp) {
//
//			System.out.println("\n element " + ((ConstructionModal) object).element.getRawText() + " in block  "
//					+ ((ConstructionModal) object).block + " order " + ((ConstructionModal) object).order);
//		}
//
//	}
//
//	ArrayList<ConstructionModal> array = new ArrayList<ConstructionModal>();
//	System.out.println("\n unconstructed element --------------------------------------------------");
//	Iterator i = this.unconstructedElementsSet.iterator();
//	System.out.println("\n the size + " + this.unconstructedElementsSet.size());
//	while (i.hasNext()) {
//		array = (ArrayList<ConstructionModal>)i.next();
//		for (ConstructionModal constructionModal : array) {
//			System.out.println("\n element " + constructionModal.element.getRawText() + " in block  "
//					+ constructionModal.block + " order " + constructionModal.order);
//		}
//		System.out.println("\n --------------------------------------------------");
//	}

}
