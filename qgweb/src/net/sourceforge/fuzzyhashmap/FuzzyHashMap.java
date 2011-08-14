package net.sourceforge.fuzzyhashmap;

import java.util.HashMap;

@SuppressWarnings("all")
public class FuzzyHashMap extends HashMap{

	public static final String SOUNDEX_TYPE = "soundex";
	public static final String SUBSTRING_TYPE = "substring";
	public static final String FIRST_TYPE = "first";
	// //////////////////////////////////////////////////////////////////////////////////////
	// PreHashing Functions Enumeration 											vvv
	// //////////////////////////////////////////////////////////////////////////////////////
	public static enum PRE_HASHING_METHOD{
		SOUNDEX(SOUNDEX_TYPE, 0),
		FIRST_1(FIRST_TYPE, 1),
		FIRST_2(FIRST_TYPE, 2),
		FIRST_3(FIRST_TYPE, 3),
		FIRST_4(FIRST_TYPE, 4),
		FIRST_5(FIRST_TYPE, 5);

		private String type;
		private int value;

		PRE_HASHING_METHOD(String type, int value){
			this.type = type;
			this.value = value;
		}

		public String getType(){
			return type;
		}

		public int getValue(){
			return value;
		}

	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// PreHashing Functions Enumeration 											^^^
	// //////////////////////////////////////////////////////////////////////////////////////
	// String metrics algorithms Enumeration 										vvv
	// //////////////////////////////////////////////////////////////////////////////////////
	public static enum FUZZY_MATCHING_ALGORITHM{
		LEVENSHTEIN,
		HAMMING,
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// String metrics algorithms Enumeration 										^^^
	// //////////////////////////////////////////////////////////////////////////////////////
	// Members 																		vvv
	// //////////////////////////////////////////////////////////////////////////////////////
	private PRE_HASHING_METHOD hashing_method = PRE_HASHING_METHOD.SOUNDEX;
	private FUZZY_MATCHING_ALGORITHM fuzzyMatchingAlgorithm = FUZZY_MATCHING_ALGORITHM.LEVENSHTEIN;
	private final int DEFAULT_THRESHOLD = 2;
	// //////////////////////////////////////////////////////////////////////////////////////
	// Members 																		^^^
	// //////////////////////////////////////////////////////////////////////////////////////
	// Constructors 																vvv
	// //////////////////////////////////////////////////////////////////////////////////////
	public FuzzyHashMap() {
		// by default hashing method
		this(PRE_HASHING_METHOD.FIRST_4);
	}

	public FuzzyHashMap(PRE_HASHING_METHOD method) {
		hashing_method = method;
	}

	public FuzzyHashMap(FUZZY_MATCHING_ALGORITHM fuzzyMatchingAlgorithm) {
		this.fuzzyMatchingAlgorithm = fuzzyMatchingAlgorithm;
	}

	public FuzzyHashMap(PRE_HASHING_METHOD method, FUZZY_MATCHING_ALGORITHM fuzzyMatchingAlgorithm) {
		hashing_method = method;
		this.fuzzyMatchingAlgorithm = fuzzyMatchingAlgorithm;
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// Constructors 																^^^
	// //////////////////////////////////////////////////////////////////////////////////////
	// Populating with fuzzy keys													vvv
	// //////////////////////////////////////////////////////////////////////////////////////
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     * Even if this method populates the map with fuzzy ready keys,
     * it does not override approximate matching keys, only exact keys
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
	public void putFuzzy(String key, Object value){
		// use 0 (zero) threshold fuzzy value
		// because we don't want to override approximate keys
		FuzzyKey fk = new FuzzyKey(key, 0, hashing_method);
		super.put(fk, value);
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// Populating with fuzzy keys													^^^
	// //////////////////////////////////////////////////////////////////////////////////////
	// Get values by using fuzzy search												vvv
	// //////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the value to which an APPROXIMATE matching key is mapped,
     * or {@code null} if this map contains no mapping for an approximate matching key.
     * The level of approximation is controlled by threshold parameter; only approximate
     * matching words with distance lower then threshold are considered as valid results
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.approximateEquals(k, threshold))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be multiple approximate mappings.)
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}
     * or the key was not found due to approximate imprecision.
     *
     * @see #put(Object, Object)
     */
	public Object getFuzzy(String key, int threshold){
		// adding the fuzzyKey wrapper to the string key
		FuzzyKey fk = new FuzzyKey(key, threshold, hashing_method);
		// try to get a fuzzy match
		Object result1 = get(fk);
		if(result1 != null){
			// if a fuzzy match was found try to get a better match (decrease the threshold value)
			if(threshold > 0){
				fk = new FuzzyKey(key, threshold - 1, hashing_method);
				Object result2 = get(fk);
				if(result2 != null){
					return result2;
				}
			}
			return result1;
		}
		return get(fk);
	}

    /**
     * Returns the value to which an APPROXIMATE matching key is mapped,
     * or {@code null} if this map contains no mapping for an approximate matching key.
     * The level of approximation is the DEFAULT_THRESHOLD value; only approximate
     * matching words with distance lower then threshold are considered as valid results
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.approximateEquals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be multiple approximate mappings.)
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}
     * or the key was not found due to approximate imprecision.
     *
     * @see #put(Object, Object)
     */
	public Object getFuzzy(String key){
		// try to get a fuzzy match using the default threshold value
		return getFuzzy(key, DEFAULT_THRESHOLD);
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// Get values by using fuzzy search												^^^
	// //////////////////////////////////////////////////////////////////////////////////////
	// Check if key is contained by using fuzzy search								vvv
	// //////////////////////////////////////////////////////////////////////////////////////
	public boolean containsFuzzyKey(String key, int threshold){
		return getFuzzy(key, threshold) != null;
	}

	public boolean containsFuzzyKey(String key){
		return getFuzzy(key, DEFAULT_THRESHOLD) != null;
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// Check if key is contained by using fuzzy search								^^^
	// //////////////////////////////////////////////////////////////////////////////////////
}
