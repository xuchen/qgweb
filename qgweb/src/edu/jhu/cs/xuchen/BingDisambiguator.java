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
import com.google.code.bing.search.schema.SearchRequest;
import com.google.code.bing.search.schema.SearchResponse;
import com.google.code.bing.search.schema.SourceType;
import com.google.code.bing.search.schema.web.WebResult;
import com.google.code.bing.search.schema.web.WebSearchOption;

import edu.cmu.ark.AnalysisUtilities;

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
    	long cHypernymContext, cAll, cAllNear;
    	StringBuffer sb = new StringBuffer();
    	String origSentence = sentence, origAnswer = answer;
    	Double p;
    	answer = AnalysisUtilities.getInstance().getContentWords(answer);
    	sentence = AnalysisUtilities.getInstance().getContentWords(sentence);
    	String context = sentence.replaceAll(answer, "").replaceAll(" and ", " ").replaceAll(" or ", " ");
    	sentence = sentence.replaceAll(" and ", " ");
    	sentence = sentence.replaceAll(" or ", " ");
    	HashMap<String, Double> pmi1= new HashMap<String, Double>();
    	HashMap<String, Long> pmi3= new HashMap<String, Long>();
    	HashMap<String, Long> pmi4= new HashMap<String, Long>();
    	HashMap<String, Double> pmi2= new HashMap<String, Double>();
    	if (hypernymSet.size() == 0) return new ArrayList<String>();
    	System.out.println("=====Disambiguate Start=====");
    	System.out.println("## Original Sentence: " + origSentence);
    	System.out.println("## Modified Sentence: " + sentence);
    	System.out.println("## Context: " + context);
    	System.out.println("## Original Answer: " + origAnswer);
    	System.out.println("## Modified Answer: " + answer);
    	System.out.println("## Hypernyms: " + hypernymSet);
		sb.append("\n#CVS#,");
		sb.append("\""+origSentence.replaceAll("\"","'")+"\"");
		sb.append(",");
		sb.append("\""+sentence.replaceAll("\"","'")+"\"");
		sb.append(",");
		sb.append("\""+context.replaceAll("\"","'")+"\"");
		sb.append(",");
		sb.append("\""+origAnswer.replaceAll("\"","'")+"\"");
		sb.append(",");
		sb.append("\""+answer.replaceAll("\"","'")+"\"");
		sb.append(",");
		sb.append("\""+hypernymSet.toString().replaceAll("\"","'")+"\"");
		sb.append(",");

		answer = "\"" + answer + "\"";
		String origh;
    	for (String h:hypernymSet) {

    		origh = h;
    		// pmi1: (hypernym + context) / (hypernym + answer + context)
    		// smaller is better
    		cHypernymContext = getTotalResults(h+" "+context);
    		// cAll should be less than cHypernymContext!
    		cAll = getTotalResults(answer+ " " + h+ " " + context);
    		p = cHypernymContext*1.0/cAll;
    		pmi1.put(origh, p);
    		System.out.println(String.format("## pmi1 Hypernym/cHypernymContext/cAll/pmi: %s/%d/%d/%.2f", h, cHypernymContext, cAll,  p));


    		// pmi2: (hypernym + context) / (answer NEAR hypernym + context)
    		// smaller is better
    		h = "\"" + h + "\"";
    		cAllNear = getTotalResults(answer+" near:10 "+h+ " " + context);
    		p = cHypernymContext*1.0/cAllNear;
    		pmi2.put(origh, p);
    		System.out.println(String.format("## pmi2 Hypernym/cHypernymContext/cAllNear/pmi: %s/%d/%d/%.2f", h, cHypernymContext, cAllNear,  p));

    		// pmi3: hypernym + context
    		// larger is better
    		pmi3.put(origh, cHypernymContext);
    		System.out.println(String.format("## pmi3 Hypernym/cHypernymContext: %s/%d", h, cHypernymContext));

    		// pmi4: hypernym + answer + context
    		// larger is better
    		pmi4.put(origh, cAll);
    		System.out.println(String.format("## pmi4 Hypernym/cAll: %s/%d", h, cAll));

    	}
    	System.out.println("");
    	List<String> sortedHypernyms1 = sortByValue(pmi1);
    	System.out.println("## pmi1:");
    	for (String s:sortedHypernyms1) {
	    	System.out.print(s+": "+pmi1.get(s)+"\t");
    	}
    	sb.append("\""+sortedHypernyms1.get(0)+"\"");
    	sb.append(",");
    	sb.append("\""+sortedHypernyms1+"\"");
    	sb.append(",,");

    	List<String> sortedHypernyms2 = sortByValue(pmi2);
    	System.out.println("\n## pmi2:");
    	for (String s:sortedHypernyms2) {
	    	System.out.print(s+": "+pmi2.get(s)+"\t");
    	}
    	sb.append("\""+sortedHypernyms2.get(0)+"\"");
    	sb.append(",");
    	sb.append("\""+sortedHypernyms2+"\"");
    	sb.append(",,");

    	List<String> sortedHypernyms3 = sortByValueReversed(pmi3);
    	System.out.println("\n## pmi3:");
    	for (String s:sortedHypernyms3) {
	    	System.out.print(s+": "+pmi3.get(s)+"\t");
    	}
    	sb.append("\""+sortedHypernyms3.get(0)+"\"");
    	sb.append(",");
    	sb.append("\""+sortedHypernyms3+"\"");
    	sb.append(",,");

    	List<String> sortedHypernyms4 = sortByValueReversed(pmi4);
    	System.out.println("\n## pmi4:");
    	for (String s:sortedHypernyms4) {
	    	System.out.print(s+": "+pmi4.get(s)+"\t");
    	}
    	sb.append("\""+sortedHypernyms4.get(0)+"\"");
    	sb.append(",");
    	sb.append("\""+sortedHypernyms4+"\"");
    	sb.append(",,");
    	sb.append("\n");
    	System.out.println(sb.toString());
    	System.out.println("\n=====Disambiguate End=====");
    	return sortedHypernyms1;
    }

    private static long getTotalResults (String query) {

    	SearchRequest request = createSearchRequest(client, APPLICATION_KEY_OPTION, query);
		SearchResponse response = client.search(request);
		try {
			return response.getWeb().getTotal();
		} catch (NullPointerException e) {
			return 0;
		}
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

	@SuppressWarnings("unchecked")
    public static List sortByValueReversed(final Map m) {
        List keys = new ArrayList();
        keys.addAll(m.keySet());
        Collections.sort(keys, new Comparator() {
            public int compare(Object o1, Object o2) {
                Object v2 = m.get(o1);
                Object v1 = m.get(o2);
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
