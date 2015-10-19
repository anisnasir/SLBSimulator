package slb;

import java.util.HashSet;
import java.util.Set;

/**
 * Statistics for a server.
 * 
 */
public class ServerStats implements Comparable<ServerStats> {
	public static final ServerStats EMPTY_STATS = new ServerStats();
	public static final double RELATIVE_STANDARD_DEVIATION = 0.01; // 1% of
		
	private int wordCount = 0;
	private int transitions = 0;
	private Set<String> dictionary = new HashSet<String>();

	public void accumulate(ServerStats other) {
		wordCount += other.wordCount;
		dictionary.addAll(other.dictionary);
		transitions += other.transitions;
	}

	public void addTransition() {
		transitions++;
	}

	public int compareTo(ServerStats other) {
		return this.wordCount - other.wordCount;
	}

	public long dictionarySize() {
		return dictionary.size();
	}

	@Override
	public String toString() {
		return (wordCount + " " + dictionary.size() + " " + transitions + "\t");
	}

	public void update(long timestamp, String word) {
		wordCount++;
		dictionary.add(word);
	}
}
