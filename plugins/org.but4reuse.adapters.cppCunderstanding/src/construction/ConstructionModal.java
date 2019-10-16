package construction;

import elements.CppElement;

public class ConstructionModal {

	/*
	 * this class is used as an enhanced structure of cppElement objects of this
	 * class will be puted in stack
	 */
	protected CppElement element;
	protected String block;
	protected int order;

	public ConstructionModal(CppElement e, String b, int o) {
		this.element = e;
		this.block = b;
		this.order = o;

	}

}
