/**
 *
 */
package edu.jhu.cs.xuchen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.code.bing.search.client.BingSearchClient;
import com.google.code.bing.search.client.BingSearchServiceClientFactory;
import com.google.code.bing.search.client.BingSearchClient.SearchRequestBuilder;
import com.google.code.bing.search.schema.AdultOption;
import com.google.code.bing.search.schema.SearchOption;
import com.google.code.bing.search.schema.SearchRequest;
import com.google.code.bing.search.schema.SearchResponse;
import com.google.code.bing.search.schema.SourceType;
import com.google.code.bing.search.schema.web.WebResult;
import com.google.code.bing.search.schema.web.WebSearchOption;

/**
 * @author Xuchen Yao
 *
 */
public class BingDisambiguator {

    /** The Constant APPLICATION_KEY_OPTION. */
    private static final String APPLICATION_KEY_OPTION = "75FF0CF6DF5B44B4EBC5B64E792FA3314F6F3672";

    protected static SearchRequestBuilder builder;
    protected static BingSearchClient client;
    protected static BingSearchServiceClientFactory factory;

    private static BingDisambiguator instance = null;

    public static BingDisambiguator getInstance () {
    	if (instance == null) {
    		instance = new BingDisambiguator();
    	}
    	return instance;
    }

    public BingDisambiguator () {
		factory = BingSearchServiceClientFactory.newInstance();
		client = factory.createBingSearchClient();
    }

    public List<String> disambiguate(HashSet<String> hypernymSet, String sentence, String answer) {
    	long cHypernymContext, cAll;
    	Double p;
    	String context = sentence.replaceAll(answer, "").replaceAll(" and ", "").replaceAll(" or ", "");
    	sentence = sentence.replaceAll(" and ", "");
    	sentence = sentence.replaceAll(" or ", "");
    	HashMap<String, Double> pmi= new HashMap<String, Double>();
    	HashMap<String, Double> pmi1= new HashMap<String, Double>();
    	HashMap<String, Double> pmi2= new HashMap<String, Double>();
    	if (hypernymSet.size() == 0) return new ArrayList<String>();
    	System.out.println("=====Disambiguate Start=====");
    	System.out.println("Sentence: " + sentence);
    	System.out.println("Context: " + context);
    	System.out.println("Answer: " + answer);
    	System.out.println("Hypernyms: " + hypernymSet);
		answer = "\"" + answer + "\"";
    	for (String h:hypernymSet) {
    		h = "\"" + h + "\"";
    		cHypernymContext = getTotalResults(h+" "+context);
    		cAll = getTotalResults(answer+" near:10 "+h+ " " + context);
    		p = cHypernymContext*1.0/cAll;
    		pmi.put(h, p);
    		System.out.println(String.format("[near:10] Hypernym/cHypernym/cAll/pmi: %s/%d/%d/%.2f", h, cHypernymContext, cAll,  p));
    		cAll = getTotalResults(answer+" near:20 "+h+ " " + context);
    		p = cHypernymContext*1.0/cAll;
    		pmi1.put(h, p);
    		System.out.println(String.format("[near:20] Hypernym/cHypernym/cAll/pmi: %s/%d/%d/%.2f", h, cHypernymContext, cAll,  p));
    		cAll = getTotalResults(answer+" near:30 "+h+ " " + context);
    		p = cHypernymContext*1.0/cAll;
    		pmi2.put(h, p);
    		System.out.println(String.format("[near:30] Hypernym/cHypernym/cAll/pmi: %s/%d/%d/%.2f", h, cHypernymContext, cAll,  p));
    	}
    	System.out.println("");
    	List<String> sortedHypernyms = sortByValue(pmi);
    	System.out.println("[Hypernym+Answer]");
    	for (String s:sortedHypernyms) {
	    	System.out.print(s+": "+pmi.get(s)+"\t");
    	}
    	List<String> sortedHypernyms1 = sortByValue(pmi1);
    	System.out.println("[Hypernym   Only]");
    	for (String s:sortedHypernyms1) {
	    	System.out.print(s+": "+pmi1.get(s)+"\t");
    	}
    	List<String> sortedHypernyms2 = sortByValue(pmi2);
    	System.out.println("[Hypernym   Only]");
    	for (String s:sortedHypernyms2) {
	    	System.out.print(s+": "+pmi2.get(s)+"\t");
    	}
    	System.out.println("\n=====Disambiguate End=====");
    	return sortedHypernyms1;
    }

    private static long getTotalResults (String query) {

    	SearchRequest request = createSearchRequest(client, APPLICATION_KEY_OPTION, query);
		SearchResponse response = client.search(request);
		return response.getWeb().getTotal();
    }

	/**
	 * Creates the search request.
	 *
	 * @param client the client
	 * @param applicationId the application id
	 * @param query the query
	 *
	 * @return the search request
	 */
	private static SearchRequest createSearchRequest(BingSearchClient client, String applicationId, String query) {
		SearchRequestBuilder builder = client.newSearchRequestBuilder();
		builder.withAppId(applicationId);
		builder.withQuery(query);
		builder.withSourceType(SourceType.WEB);
		builder.withVersion("2.0");
		builder.withMarket("en-us");
		builder.withAdultOption(AdultOption.MODERATE);
		//builder.withSearchOption(SearchOption.ENABLE_HIGHLIGHTING);

		builder.withWebRequestCount(1L);
		builder.withWebRequestOffset(0L);
		builder.withWebRequestSearchOption(WebSearchOption.DISABLE_HOST_COLLAPSING);
		builder.withWebRequestSearchOption(WebSearchOption.DISABLE_QUERY_ALTERATIONS);

		return builder.getResult();
	}

	/**
	 * Prints the response.
	 *
	 * @param response the response
	 */
	private static void printResponse(SearchResponse response) {
		System.out.println("Bing API Version " + response.getVersion());
		System.out.println("Web results for " + response.getQuery().getSearchTerms());
		for (WebResult result : response.getWeb().getResults()) {
			System.out.println(result.getTitle());
			System.out.println(result.getDescription());
			System.out.println(result.getUrl());
			System.out.println(result.getDateTime());
		}
		System.out.println("Total: "+response.getWeb().getTotal());
	}

	@SuppressWarnings("unchecked")
    public static List sortByValue(final Map m) {
        List keys = new ArrayList();
        keys.addAll(m.keySet());
        Collections.sort(keys, new Comparator() {
            public int compare(Object o1, Object o2) {
                Object v1 = m.get(o1);
                Object v2 = m.get(o2);
                if (v1 == null) {
                    return (v2 == null) ? 0 : 1;
                }
                else if (v1 instanceof Comparable) {
                    return ((Comparable) v1).compareTo(v2);
                }
                else {
                    return 0;
                }
            }
        });
        return keys;
    }

}
