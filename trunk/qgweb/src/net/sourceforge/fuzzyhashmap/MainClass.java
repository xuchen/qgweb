package net.sourceforge.fuzzyhashmap;





public class MainClass {

	public static void main(String [ ] args){
		// an example of how FuzzyHashMap works
		// here we'll use it as a phone book;
		// for English names the best hashing method is Soundex
		FuzzyHashMap phoneBook = new FuzzyHashMap(FuzzyHashMap.PRE_HASHING_METHOD.SOUNDEX);
		//populate dictionary
		//for keys that have to be fuzzy search enabled "putFuzzy" method should be used to populate the map
		phoneBook.putFuzzy("Ralph Smith", "888-123-1234");
		phoneBook.putFuzzy("Tom Smith", "867-562-1238");
		phoneBook.putFuzzy("John Doe", "822-156-3768");
		phoneBook.putFuzzy("Todd Hall", "834-153-5305");
		phoneBook.putFuzzy("Jane Baker", "859-843-4862");


		// 1.
		// now say we're searching for "Todd Hall", but we're miss typing it
		// "Tod Hal"
		String searchedNumber = (String)phoneBook.getFuzzy("Tod Hal");
		// NOTE: the search done behind is not an iteration trough all elements
		// this should print the phone number of "Todd Hall": 834-153-5305
		System.out.println("Searched number is:" + searchedNumber);

		// 2.
		// we can also decide the threshold value of fuzzyness allowed in the search
		// let's set the allowed fuzzyness as 1;
		// this means that the (Levenshtein) distance between an existing key and the searched key can be maximum 1
		// in our case the distance between "Todd Hall and "Tod Hal" is 2 (the minimum number of operations is 2 deletions)
		searchedNumber = (String)phoneBook.getFuzzy("Tod Hal", 1);
		//this should print null, because no entry passed the conditions
		System.out.println("Searched number is:" + searchedNumber);

		// this is a banal example;
		// you can use FuzzyHashMap for spell checking, NLP (natural language processing), dictionaries...
		// to find more about fuzzyHashMap visit the http://fuzzyhashmap.sourceforge.net/

	}


}


