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
	public static enum FINDER {SMALL, FULL};

	protected HypernymFinder finder;

	private WikiHypernymWrapper(FINDER f) {
		if (f == FINDER.SMALL)
			finder = new WikiHypernymFuzzyFinder(GlobalProperties.getProperties().getProperty("hyponymData", "models"+File.separator+"hyponym.500.ser.gz"));
		else
			finder = new WikiHypernymBdbFinder(GlobalProperties.getProperties().getProperty("WikiNetConfigFile", ""));
	}

	private static WikiHypernymWrapper instance;

	public static WikiHypernymWrapper getInstance() {
		String type = GlobalProperties.getProperties().getProperty("WikiNetType", "");
		if (instance == null) {
			if (type.equals("small"))
				instance = new WikiHypernymWrapper(FINDER.SMALL);
			else if (type.equals("full"))
				instance = new WikiHypernymWrapper(FINDER.FULL);
			else if (type.equals("null")) {
			} else {
				System.err.println("WikiNetType in property files should be either null, small or full");
				System.err.println("You have: " + type);
				System.exit(-1);
			}
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
