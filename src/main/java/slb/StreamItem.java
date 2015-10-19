package slb;

import java.util.Iterator;
import java.util.List;

/**
 * An atomic item in the stream. An item is composed by a timestamp and a line
 * of text. The timestamp represents the position in the stream, the text is the
 * payload and is represented as list of strings.
 */
public class StreamItem implements Iterable<String> {
	private long timestamp;
	private List<String> words;

	StreamItem(long timestamp, List<String> words) {
		this.timestamp = timestamp;
		this.words = words;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getWord(int pos) {
		return words.get(pos);
	}

	public int getWordsSize() {
		return words.size();
	}

	public Iterator<String> iterator() {
		return words.iterator();
	}

	@Override
	public String toString() {
		return words.toString();
	}
}
