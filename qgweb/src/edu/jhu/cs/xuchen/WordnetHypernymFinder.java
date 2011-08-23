package edu.jhu.cs.xuchen;

import java.util.HashSet;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

public class WordnetHypernymFinder implements HypernymFinder {

	@Override
	public HashSet<String> getHypernym(String name) {
		System.out.println("WordNet hypernym finder:");
		System.out.println("NP: " + name);
		name = name.toLowerCase();
		HashSet<String> hSet = new HashSet<String>();

		IndexWord indexWord;
		try {
			indexWord = Dictionary.getInstance().getIndexWord(POS.NOUN, name);
			Synset[] set = indexWord.getSenses();
			if (set != null) {
				for (Synset s:set) {
					Pointer[] pointerArr = s.getPointers(PointerType.HYPERNYM);
					if (pointerArr != null)
						for (Pointer x : pointerArr) {
							for (Word w:x.getTargetSynset().getWords()){
								hSet.add(w.getLemma());
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
