package edu.jhu.cs.xuchen;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class Utils {

	// http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
	public static <K, V extends Comparable<? super V>> Map<K, V>
	sortByValue( Map<K, V> map )
	{
		List<Map.Entry<K, V>> list =
			new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
				{
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return (o1.getValue()).compareTo( o2.getValue() );
			}
				} );

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
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


}
