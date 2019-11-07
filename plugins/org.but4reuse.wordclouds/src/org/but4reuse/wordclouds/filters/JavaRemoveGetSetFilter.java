package org.but4reuse.wordclouds.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

public class JavaRemoveGetSetFilter implements IWordsProcessing {

	@Override
	public List<String> processWords(List<String> words, IProgressMonitor monitor) {
		List<String> result=new ArrayList<String>();
		for(String word : words) {
			if(word.length()>=3 && (word.substring(0, 3).equals("get") || word.substring(0, 3).equals("set"))) {
				word = word.substring(3);
			}
			result.add(word);
		}
		return result;
	}


}
