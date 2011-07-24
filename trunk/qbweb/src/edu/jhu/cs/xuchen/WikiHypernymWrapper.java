/**
 *
 */
package edu.jhu.cs.xuchen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.cmu.ark.GlobalProperties;
import edu.cmu.ark.TregexPatternFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.IntPair;

/**
 * @author Xuchen Yao
 *
 */
public class WikiHypernymWrapper {

	protected WikiHypernymFuzzyFinder finder;

	private WikiHypernymWrapper() {
		finder = new WikiHypernymFuzzyFinder(GlobalProperties.getProperties().getProperty("hyponymData", "models"+File.separator+"hyponym.500.ser.gz"));
	}

	private static WikiHypernymWrapper instance;

	public static WikiHypernymWrapper getInstance() {
		if (instance == null) {
			instance = new WikiHypernymWrapper();
		}
		return instance;
	}

	public List<HashSet<String>> annotateHypernym(Tree sentence) {

		String tregexOpStr, np;
		HashSet<String> hypernymSet;
		TregexPattern matchPattern;
		TregexMatcher matcher;
		Tree tmp;
		List<HashSet<String>> hypernymTagsSet = new ArrayList<HashSet<String>>();

		int numleaves = sentence.getLeaves().size();
		if(numleaves <= 1){
			return hypernymTagsSet;
		}
		for (int i=0; i<numleaves; i++) {
			hypernymTagsSet.add(new HashSet<String>());
		}
			//noun phrases
		tregexOpStr = "ROOT=root << NP=np";
		matchPattern = TregexPatternFactory.getPattern(tregexOpStr);
		matcher = matchPattern.matcher(sentence);
		List<Tree> allLeaves = sentence.getLeaves();
		while(matcher.find()){
			tmp = matcher.getNode("np");
			np = tmp.yield().toString();
			List<Tree> npLeaves = tmp.getLeaves();
			int start=-1, end=-1;
			Tree npLast = npLeaves.get(npLeaves.size()-1);
			for (int i=0; i<allLeaves.size(); i++) {
				if (allLeaves.get(i) == npLeaves.get(0)) start = i;
				if (allLeaves.get(i) == npLast) end = i;
			}
			hypernymSet = this.finder.getHypernym(np);
			if (hypernymSet.size() != 0) {
				for(int i=start; i<=end; i++)
					hypernymTagsSet.get(i).addAll(hypernymSet);
			}
		}

		return hypernymTagsSet;
	}

}
