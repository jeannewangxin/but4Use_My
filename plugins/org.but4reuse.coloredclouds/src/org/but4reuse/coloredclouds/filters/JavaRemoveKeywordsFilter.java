package org.but4reuse.coloredclouds.filters;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.SourceVersion;

import org.eclipse.core.runtime.IProgressMonitor;

public class JavaRemoveKeywordsFilter implements IWordsProcessing {

	@Override
	public List<String> processWords(List<String> words, IProgressMonitor monitor) {
		ArrayList<String> blackList = new ArrayList<String>();
		blackList.add("java");
		blackList.add("arraylist");
		blackList.add("true");
		blackList.add("false");
		List<String> result=new ArrayList<String>();
		for(String word : words) {
			if(!SourceVersion.isIdentifier(word) && !blackList.contains(word)){
				
				result.add(word);
			}
		}
		return result;
	}


}
