package slb;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class LBHashing implements LoadBalancer {
	private final SortedMap<Integer, Server> circle = new TreeMap<Integer, Server>();
	private int numServers;

	public LBHashing(Collection<Server> nodes) {
		this.numServers = nodes.size();
		int i = 0;
		for (Server node : nodes) {
			circle.put(i, node);
			i++;
		}
	}

	public Server getSever(long timestamp, Object key) {
		//int serverID = Math.abs(key.toString().hashCode()) % this.numServers;
		//Seed seeds = new Seed(this.numServers);
		HashFunction h1 = Hashing.murmur3_128(13);
		int serverID = Math.abs(h1.hashBytes(key.toString().getBytes()).asInt()%numServers); 
		
		return this.circle.get(serverID);
	}

}
