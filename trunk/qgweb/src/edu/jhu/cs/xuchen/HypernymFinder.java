package edu.jhu.cs.xuchen;

import java.util.HashSet;

public interface HypernymFinder {

	abstract public HashSet<String> getHypernym (String name);

}
