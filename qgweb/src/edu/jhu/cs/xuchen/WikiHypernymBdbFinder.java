/**
 *
 */
package edu.jhu.cs.xuchen;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import edu.cmu.ark.AnalysisUtilities;
import edu.cmu.ark.GlobalProperties;
import edu.cmu.ark.ParseResult;
import edu.stanford.nlp.trees.Tree;

import wikinet.access.low.API.implement.WikiNet;
import wikinet.access.low.entry.data.concept.Concept;
import wikinet.build.util.PropertiesManager;

/**
 * @author Xuchen Yao
 *
 */
public class WikiHypernymBdbFinder implements HypernymFinder {

	private static WikiNet acc = null;

	private static int id_is_a = -1;

	private static int id_subcat_of = -1;

	private static HashSet<String> peoplePronouns = null;
	private static HashSet<String> hypernymWordnetSet = null;

	public WikiHypernymBdbFinder(String param_file) {
		if (acc != null) return;
		PropertiesManager pm = new PropertiesManager(param_file);
		try {
			// initiate the WikiNet API with some
			// implementation of Access
			acc = new WikiNet(pm, true);
			id_is_a = acc.getRelationType("IS_A").getID();
			id_subcat_of = acc.getRelationType("SUBCAT_OF").getID();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		if (peoplePronouns == null) {
			peoplePronouns = new HashSet<String>();
			String[] tokens = "its my her hers his their theirs our ours your yours i he her him me she us we you myself yourself ourselves herself himself it this that they these those".split("\\s+");
			for(int i=0; i<tokens.length; i++){
				peoplePronouns.add(tokens[i]);
			}
		}
		if (hypernymWordnetSet == null) {
			String hfile = GlobalProperties.getProperties().getProperty("hypernymList", "");
			if (hfile != null) {
				hypernymWordnetSet = new HashSet<String>();
			    FileInputStream fin;
				try {
					fin = new FileInputStream(hfile);
			    GZIPInputStream gzis = new GZIPInputStream(fin);
			    InputStreamReader xover = new InputStreamReader(gzis);
			    BufferedReader is = new BufferedReader(xover);

			    String line;
			    // Now read lines of text: the BufferedReader puts them in lines,
			    // the InputStreamReader does Unicode conversion, and the
			    // GZipInputStream "gunzip"s the data from the FileInputStream.
			    while ((line = is.readLine()) != null)
			    	hypernymWordnetSet.add(line.toLowerCase());
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			  }
			}
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.xuchen.HypernymFinder#getHypernym(java.lang.String)
	 */
	@Override
	public HashSet<String> getHypernym(String name) {
		System.out.println("Orig NP: "+name);
		name = AnalysisUtilities.getInstance().getContentWords(name);
		System.out.println("NP: " + name);
		name = name.toLowerCase();
		int word_count = name.split("\\s+").length;
		HashSet<Concept> cSet = acc.getConceptsByName(name, false);
		HashSet<String> hSet = new HashSet<String>();
		if (peoplePronouns.contains(name)) return hSet;
		System.out.println("modified NP: " + name);

	    Map<Concept,Integer> distanceMap = new HashMap<Concept,Integer>();
		for (Concept concept:cSet) {
			if (concept == null) continue;
			if (!concept.hasRelations()) continue;
			if (!concept.isNE()) continue;
			String cName = concept.getOneCanonicalName("en").toLowerCase();
			// have to store ID to avoid name collision
			distanceMap.put(concept, Utils.LevenshteinDistance(name, cName));
		}
		if (distanceMap.size() == 0) return hSet;
		//if (GlobalProperties.getDebug())
			System.out.println("\tbefore levenshtein: " + cSet);

		int lowest = Collections.min(distanceMap.values());

		cSet.clear();
		/* concepts retrieved from BDB seems to be too permissive
		 * e.g. for 'new york' BDB even returns "pennsylvania station"
		 * so we have to use minimal edit distance to filter out some.
		 * the algorithm used here is to find out the set of hypernyms
		 * with the minimum Levenshtein distance.
		*/
		for (Entry<Concept,Integer> entry:distanceMap.entrySet()) {
			if (entry.getValue() == lowest && entry.getValue() < word_count) cSet.add(entry.getKey());
		}
		//if (GlobalProperties.getDebug())
			System.out.println("\tafter levenshtein: " + cSet);

		String hypernym;
		for (Concept concept:cSet) {
			String cName = concept.getOneCanonicalName("en");
			System.out.println("\tConcept: " + cName);
			if (concept.hasRelationsWith(id_is_a)) {
				for (int ID : concept.getRelationsWith(id_is_a)) {
					Concept linked = acc.getConcept(ID);
					hypernym = linked.getOneCanonicalName("en");
					/*
					 * Wikipedia seems to contain too much info about movies and music
					 * Dev set doesn't contain any of those, thus we hope Test set does neither
					 */
					if (hypernym.matches(".*(album|film|music|movie|media|culture|works|song|genre)+.*")) continue;

					// deal with plurals: Edingburgh: ports_and_harbours_of_scotland
					ParseResult result = AnalysisUtilities.getInstance().parseSentence(hypernym);
					List<Tree> leaves = result.parse.getLeaves();
					List<String> hList = new ArrayList<String>();
					System.out.println("Leaves: " + leaves);
					Tree head = result.parse.headTerminal(AnalysisUtilities.getInstance().getHeadFinder());
					String headWord = head.yield().toString();
					for (int i=0; i<leaves.size(); i++) {
						String word = leaves.get(i).label().toString();
						Tree preterm = leaves.get(i).parent(result.parse);
						String pos = preterm.label().toString();
						String lemma = word;
						if (pos.equals("NNS")) {
							lemma = AnalysisUtilities.getInstance().getLemma(word, pos);
							hList.add(lemma);
							// don't work when headWord is "cities" "universities" etc
							//if (headWord.startsWith(lemma)) {
							if (head == leaves.get(i)) {
								// incase the head word is plural
								System.out.println("Changing plural: " + headWord + " -> " + lemma);
								headWord = lemma;
							}
						}
						else
							hList.add(word);
						System.out.println(word + "/" + pos);
					}
					hypernym = "";
					for (String s:hList)
						hypernym += " "+s;
					hypernym = hypernym.substring(1);
					if (hypernymWordnetSet != null && hypernymWordnetSet.contains(headWord))
						hSet.add(hypernym);
					else {
						System.out.println("Hypernym excluded: " + hypernym + " HEAD: " + headWord);
					}
				}
			}
//			if (concept.hasRelationsWith(id_subcat_of)) {
//				for (int ID : concept.getRelationsWith(id_subcat_of)) {
//					Concept linked = acc.getConcept(ID);
//					hSet.add(linked.getOneCanonicalName("en"));
//				}
//			}
		}
		// TODO: add disambiguation here for the most plausible hypernym
		System.out.println(hSet);
		return hSet;
	}

	public static void main (String[] argv) {
		System.out.println(Utils.LevenshteinDistance("new york", "the new times"));
		System.out.println(Utils.LevenshteinDistance("new york", " "));
		System.out.println(Utils.LevenshteinDistance("new york,", "new york"));
	}
}
