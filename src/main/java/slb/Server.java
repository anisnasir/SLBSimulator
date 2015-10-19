package slb;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.io.Writer;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents a single server
 */
public class Server {
	private long currentTimestamp;
	private final long granularity;
	private final int minSize;
	private final Deque<ServerStats> timeSerie = new LinkedList<ServerStats>();

	public Server(long initialTimestamp, TimeGranularity granularity,
			int minSize) {
		this.currentTimestamp = initialTimestamp;
		this.timeSerie.add(new ServerStats());
		this.granularity = granularity.getNumberOfSeconds();
		this.minSize = (minSize > 0) ? minSize : 1; // minimum size = 1
	}

	public boolean addTransition(long timestamp) {
		// CONTRACT: updates have monotonically increasing timestamps
		try {
			checkArgument(timestamp >= currentTimestamp);
			synch(timestamp);
			this.timeSerie.peekLast().addTransition();
			return timeSerie.size() > minSize; // 2 or more
		} catch (Exception ex) {
			return false;
		}

	}

	public boolean flushNext(Writer out) {
		checkArgument(!timeSerie.isEmpty());
		if (timeSerie.isEmpty())
			return false;
		try {
			out.write(timeSerie.pollFirst().toString());
			return true;
		} catch (IOException e) {
			System.err.println("Problem writing time serie to output file");
			e.printStackTrace();
			return false;
		}
	}

	public long getGranularity() {
		return granularity;
	}

	public ServerStats getStats(long timestamp, int numTimeSlots) {

	checkArgument(timestamp >= currentTimestamp);
	checkArgument(numTimeSlots > 0);
	synch(timestamp);

	Iterator<ServerStats> iter = timeSerie.descendingIterator();
	ServerStats accStats = new ServerStats();
	for (int i = 0; i <= numTimeSlots; i++) {
		if (!iter.hasNext())
			break;
		accStats.accumulate(iter.next());
	}
	return accStats;
}

	public boolean printNextUnused(Writer out) {
		if (timeSerie.size() <= minSize)
			return false;
		try {
			checkArgument(!timeSerie.isEmpty());
			out.write(timeSerie.pollFirst().toString());
			return true;
		} catch (IOException e) {
			System.err.println("Problem writing time serie to output file");
			e.printStackTrace();
			return false;
		}
	}

		public void synch(long newTimestamp) {
			// add new elements to the time serie until we get one for the right
			// windows
			while (this.currentTimestamp + this.granularity - 1 < newTimestamp) {
				ServerStats newStats = new ServerStats();
				this.timeSerie.addLast(newStats);
				this.currentTimestamp += this.granularity;
				// this.timeSlot++; // see if the timeslots coincide among
				// timeseries
			}
		}

	public boolean updateStats(long timestamp, String word) {
		try {
			checkArgument(timestamp >= currentTimestamp);
			synch(timestamp);
			this.timeSerie.peekLast().update(timestamp, word);
			return timeSerie.size() > minSize; // 2 or more
		} catch (Exception ex) {
			return false;
		}
	}
}
