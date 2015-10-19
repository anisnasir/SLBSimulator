package slb;

public enum TimeGranularity {
	MINUTE(60),
	HALFHOUR(30 * 60), HOUR(60 * 60);
	
	private int seconds;

	private TimeGranularity(int seconds) {
		this.seconds = seconds;
	}

	public int getNumberOfSeconds() {
		return seconds;
	}
}
