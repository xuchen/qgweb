/**
 *
 */
package edu.jhu.cs.xuchen;

import java.util.HashMap;
import java.util.HashSet;

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
	}

	/* (non-Javadoc)
	 * @see edu.jhu.cs.xuchen.HypernymFinder#getHypernym(java.lang.String)
	 */
	@Override
	public HashSet<String> getHypernym(String name) {
		HashSet<Concept> cSet = acc.getConceptsByName(name.toLowerCase(), false);
		HashSet<String> hSet = new HashSet<String>();
		System.out.println("NP: " + name);
		for (Concept concept:cSet) {
			if (!concept.hasRelations()) continue;
			String cName = concept.getOneCanonicalName("en");
			/* concepts retrieved from BDB seems to be too permissive
			 * e.g. for 'new york' BDB even returns "pennsylvania station"
			 * so we have to use minimal edit distance to filter out some
			*/
			if (WikiHypernymBdbFinder.LevenshteinDistance(name, cName) > 2)
					continue;
			System.out.println("\tConcept: " + cName);
			if (concept.hasRelationsWith(id_is_a)) {
				for (int ID : concept.getRelationsWith(id_is_a)) {
					Concept linked = acc.getConcept(ID);
					hSet.add(linked.getOneCanonicalName("en"));
				}
			}
			if (concept.hasRelationsWith(id_subcat_of)) {
				for (int ID : concept.getRelationsWith(id_subcat_of)) {
					Concept linked = acc.getConcept(ID);
					hSet.add(linked.getOneCanonicalName("en"));
				}
			}
		}
		// TODO: add disambiguation here for the most plausible hypernym
		System.out.println(hSet);
		return hSet;
	}

	public static int LevenshteinDistance (String s, String t) {
		s = s.replaceAll("\\p{Punct}", " ");
		t = t.replaceAll("\\p{Punct}", " ");
		String[] ss = s.split("\\s+");
		String[] tt = t.split("\\s+");
		int m=ss.length, n=tt.length;
		int[][] d = new int [m+1][n+1];
		if (m==0) return n;
		if (n==0) return m;
		int[] slist = new int[m+1];
		int[] tlist = new int[n+1];
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		int idx, counter=0;
		for (int i=0; i<m; i++) {
			d[i][0] = i;
			if (map.containsKey(ss[i]))
				idx = map.get(ss[i]);
			else {
				idx = counter;
				map.put(ss[i], counter++);
			}
			slist[i+1] = idx;
		}
		d[m][0] = m;
		for (int i=0; i<n; i++) {
			d[0][i] = i;
			if (map.containsKey(tt[i]))
				idx = map.get(tt[i]);
			else {
				idx = counter;
				map.put(tt[i], counter++);
			}
			tlist[i+1] = idx;
		}
		d[0][n] = n;
		for (int j=1; j<=n; j++) {
			for (int i=1; i<=m; i++) {
				if (slist[i] == tlist[j])
					d[i][j] = d[i-1][j-1];
				else {
					d[i][j] = Math.min(Math.min(d[i-1][j]+1, d[i][j-1]+1), d[i-1][j-1]+2);
				}
			}
		}
		return d[m][n];
	}

	public static void main (String[] argv) {
		System.out.println(WikiHypernymBdbFinder.LevenshteinDistance("new york", "the new times"));
		System.out.println(WikiHypernymBdbFinder.LevenshteinDistance("new york", " "));
		System.out.println(WikiHypernymBdbFinder.LevenshteinDistance("new york,", "new york"));
	}
}
