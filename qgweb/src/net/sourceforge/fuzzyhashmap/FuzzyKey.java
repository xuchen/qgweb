package net.sourceforge.fuzzyhashmap;



class FuzzyKey extends Object{

	// //////////////////////////////////////////////////////////////////////////////////////
	// Members 																		vvv
	// //////////////////////////////////////////////////////////////////////////////////////
	private String strKey;
	private int threshold;
	private FuzzyHashMap.PRE_HASHING_METHOD method;
	private FuzzyHashMap.FUZZY_MATCHING_ALGORITHM algorithm;
	// //////////////////////////////////////////////////////////////////////////////////////
	// Members 																		^^^
	// //////////////////////////////////////////////////////////////////////////////////////
	// Constructors 																vvv
	// //////////////////////////////////////////////////////////////////////////////////////
	public FuzzyKey(String key, int trashhold){
		this(key, trashhold, FuzzyHashMap.PRE_HASHING_METHOD.SOUNDEX);
	}

	public FuzzyKey(String key, int trashhold, FuzzyHashMap.PRE_HASHING_METHOD method){
		this.strKey = key;
		this.threshold = trashhold;
		this.method = method;
	}

	public FuzzyKey(String key, int trashhold, FuzzyHashMap.FUZZY_MATCHING_ALGORITHM algorithm){
		this.strKey = key;
		this.threshold = trashhold;
		this.algorithm = algorithm;
	}

	public FuzzyKey(String key, int trashhold, FuzzyHashMap.PRE_HASHING_METHOD method, FuzzyHashMap.FUZZY_MATCHING_ALGORITHM algorithm){
		this.strKey = key;
		this.threshold = trashhold;
		this.method = method;
		this.algorithm = algorithm;
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// Constructors 																^^^
	// //////////////////////////////////////////////////////////////////////////////////////
	// Key getter/setter 															vvv
	// //////////////////////////////////////////////////////////////////////////////////////
	public String getKey(){
		return strKey;
	}

	public void setKey(String newKey){
		strKey = newKey;
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// Key getter/setter 															^^^
	// //////////////////////////////////////////////////////////////////////////////////////
	// Fuzzy threshold 																vvv
	// //////////////////////////////////////////////////////////////////////////////////////
	public void setThreshold(int newThreshold){
		threshold = newThreshold;
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// Fuzzy threshold 																^^^
	// //////////////////////////////////////////////////////////////////////////////////////
	// Override hascCode and equals methods 										vvv
	// //////////////////////////////////////////////////////////////////////////////////////
	public int hashCode(){
		// PRE Hashing
		String toBeHashed = "";
		if(method != null){
			if(method == FuzzyHashMap.PRE_HASHING_METHOD.SOUNDEX){
			// SOUNDEX PreHasing function
				toBeHashed = StringMetrics.soundexHash(strKey);
			}else if(method.getType().equals(FuzzyHashMap.FIRST_TYPE)){
			// Starts with (n) PreHasing function
				int nrOfLetters = method.getValue();
				if(strKey.length() > nrOfLetters){
					// cut the word to "nrOfLetters" length, starting form the beginning
					toBeHashed = strKey.substring(0, nrOfLetters);
				}
			}
		}
		// Hashing
		return toBeHashed.hashCode();
	}

	public boolean equals(Object fuzzyKey){
		String searchedKey = null;
		if(fuzzyKey instanceof FuzzyKey){
			searchedKey = ((FuzzyKey)fuzzyKey).getKey();
		}else if (fuzzyKey instanceof String){
			searchedKey = (String)fuzzyKey;
		}
		if(searchedKey != null){
			// Don't allow fuzzy search on words shorter then 5 letters
			// (due to big error rate after experimental testing)
			if(threshold != 0 && searchedKey.length() < 5)
				return false;
			// Check for exact match before calculating words similarity
			if(searchedKey.equals(strKey))
				return true;
			// Check words similarity
			if(StringMetrics.computeLevenshteinDistance(strKey, searchedKey) <= threshold){
				return true;
			}
		}
		return false;
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// Override hascCode and equals methods											^^^
	// //////////////////////////////////////////////////////////////////////////////////////
	// to string 																	vvv
	// //////////////////////////////////////////////////////////////////////////////////////
	public String toString(){
		return strKey + " " + threshold;
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// to string 																	^^^
	// //////////////////////////////////////////////////////////////////////////////////////
}
