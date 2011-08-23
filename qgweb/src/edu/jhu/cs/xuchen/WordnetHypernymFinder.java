package edu.jhu.cs.xuchen;

import java.util.HashSet;

import edu.cmu.ark.AnalysisUtilities;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

public class WordnetHypernymFinder implements HypernymFinder {

	private static HashSet<String> peoplePronouns = null;

	public WordnetHypernymFinder() {
		if (peoplePronouns == null) {
			peoplePronouns = new HashSet<String>();
			String[] tokens = "its my her hers his their theirs our ours your yours i he her him me she us we you myself yourself ourselves herself himself it this that they these those".split("\\s+");
			for(int i=0; i<tokens.length; i++){
				peoplePronouns.add(tokens[i]);
			}
		}
	}

	@Override
	public HashSet<String> getHypernym(String name) {
		HashSet<String> hSet = new HashSet<String>();
		System.out.println("WordNet hypernym finder:");
		System.out.println("NP: " + name);
		name = AnalysisUtilities.getInstance().getContentWords(name).toLowerCase();
		if (peoplePronouns.contains(name)) return hSet;

		IndexWord indexWord;
		try {
			indexWord = Dictionary.getInstance().getIndexWord(POS.NOUN, name);
			if (indexWord != null) {
				Synset[] set = indexWord.getSenses();
				if (set != null) {
					for (Synset s:set) {
						Pointer[] pointerArr = s.getPointers(PointerType.HYPERNYM);
						if (pointerArr != null)
							for (Pointer x : pointerArr) {
								for (Word w:x.getTargetSynset().getWords()){
									hSet.add(w.getLemma().replaceAll("_", " "));
								}
							}
					}
				}
			}
		} catch (JWNLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println(hSet);
		return hSet;
	}

}
