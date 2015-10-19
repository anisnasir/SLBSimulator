package slb;

public interface LoadBalancer {
	public Server getSever(long timestamp, Object key);
}
