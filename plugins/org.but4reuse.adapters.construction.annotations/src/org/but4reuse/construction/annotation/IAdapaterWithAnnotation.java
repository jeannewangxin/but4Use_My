package org.but4reuse.construction.annotation;

import java.net.URI;
import java.util.List;

import org.but4reuse.adapters.IElement;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IAdapaterWithAnnotation {

	
	public void construct(IAnnotation annotation,URI uri, List<IElement> elements, IProgressMonitor monitor);
	
}
