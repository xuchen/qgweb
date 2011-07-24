/**
 *
 */
package edu.jhu.cs.xuchen;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;


import net.sourceforge.fuzzyhashmap.FuzzyHashMap;


/**
 *
 * Performance degrades with an entry size > 50,000!
 * @author Xuchen Yao
 *
 */
public class WikiHypernymFuzzyFinder implements Serializable {

	/**
	 * ~/wikinet/WikiNet TK Distribution2$ serialver -classpath bin wikinet.access.low.xuchen.HyponymIndex
	 */
	private static final long serialVersionUID = -5560619790900028846L;

	protected int capacity = 50000;
	protected FuzzyHashMap name2idFuzzy;
	protected HashMap<String, HashSet<Integer>> name2id;

	protected HashMap<Integer, Hyponym> id2hyponym;

	public WikiHypernymFuzzyFinder() {
		name2id = new HashMap<String, HashSet<Integer>>();
		name2idFuzzy = new FuzzyHashMap(FuzzyHashMap.PRE_HASHING_METHOD.SOUNDEX);
		id2hyponym = new HashMap<Integer, Hyponym>();
	}

	public WikiHypernymFuzzyFinder(String serName) {
		this();
		this.loadData(serName);
		System.out.println("Loading data done");
		System.out.println("Converting to fuzzy structure");
		this.convertFuzzy();
	}

	private void convertFuzzy () {
		int c = 0;
		System.out.println("All entries: "+this.name2id.size());
		for (String name:name2id.keySet()) {
			this.name2idFuzzy.putFuzzy(name, this.name2id.get(name));
			if (++c % 1000 == 0)
				System.out.println(c);
		}
		System.out.println("converting done");
	}

	private void loadData (String serName) {
	      FileInputStream fis = null;
	      GZIPInputStream gs;
	      ObjectInputStream ois = null;
	      int c = 0;
	      try {
		      fis = new FileInputStream(serName);
		      gs = new GZIPInputStream(fis);
		      ois = new ObjectInputStream(gs);
	    	  while (true) {
		    	  Hyponym h = (Hyponym) ois.readObject();
		    	  if (this.add(h)) {
		    		  c++;
		    	  }
		    	  if (c == this.capacity)
		    		  break;
	    	  }
	      } catch (java.io.EOFException e) {
		      try {
					ois.close();
				    fis.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
	      } catch (Exception e) {
	    	  e.printStackTrace();
	    	  System.exit(-1);
	      }
//	      for (Hyponym h:this.id2hyponym.values()) {
//	    	  System.out.println(h.getName());
//	      }
	}


	protected boolean add(Hyponym h) {
		/*
		 * names="[andre agassi]"
		 * names="[american football conference]"
		 * names="[astoria, illinois]"
		 * names="[astoria, oregon]"
		 */
		String name = h.getName();
		int id = h.getID();

		if (!this.name2id.containsKey(name)) {
			this.name2id.put(name, new HashSet<Integer>());
		}
		this.name2id.get(name).add(id);
		this.id2hyponym.put(id, h);
		if (name.contains(",")) {
			try {
				name = h.getName().split(",")[0];
			} catch (java.lang.NullPointerException e) {
				return true;
			}
			if (!this.name2id.containsKey(name)) {
				this.name2id.put(name, new HashSet<Integer>());
			}
			this.name2id.get(name).add(id);
		}
		return true;
	}

	public HashSet<String> getHypernym (String name) {
		System.out.println("### "+name+" ###");
		HashSet<String> hypernym = new HashSet<String>();
		// strict matching
		// use name2id to do exact matching since Fuzzy matching might not be precise
		// one example is that Fuzzy matching doesn't get "ames"
		//HashSet<Integer> indices = (HashSet<Integer>) this.name2idFuzzy.getFuzzy(name.toLowerCase(), 1);
		HashSet<Integer> indices = this.name2id.get(name.toLowerCase());
		if (indices == null)
			indices = (HashSet<Integer>) this.name2idFuzzy.getFuzzy(name.toLowerCase());
		if (indices == null)
			return null;
		System.out.println(indices.size()+" matching found");
		for (Integer id:indices) {
			Hyponym h = this.id2hyponym.get(id);
			if (h.getIsASet() != null) hypernym.addAll(h.getIsASet());
			if (h.getSubOfSet() != null) hypernym.addAll(h.getSubOfSet());
		}

		return hypernym;
	}

	public static void main(String argv[]) {
		WikiHypernymFuzzyFinder finder = new WikiHypernymFuzzyFinder(argv[0]);
		System.out.println(finder.getHypernym("ames, iowa"));
		System.out.println(finder.getHypernym("ames,"));
		System.out.println(finder.getHypernym("ames"));
		System.out.println(finder.getHypernym("albert alcibiades, margrave of brandenburg-kulmbach"));
		System.out.println(finder.getHypernym("albert alcibiades,"));
		System.out.println(finder.getHypernym("albert alcibiades"));
	}

}
