package slb;

import java.util.List;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class LBPartialKeyGrouping implements LoadBalancer {
	private final List<Server> nodes;
	private final int serversNo;
	private final long loadSamplingGranularity;
	private final int numSources;
	private long[] localworkload[];
	private int sourceCount;
	private int choices; 
	private Seed seeds;
	private HashFunction[] hash;

	public LBPartialKeyGrouping(List<Server> nodes, int numSources) {
		this.nodes = nodes;
		this.numSources = numSources;
		this.serversNo = nodes.size();
		this.choices = 2;
		this.loadSamplingGranularity = nodes.get(0).getGranularity();
		for (int i = 1; i < this.serversNo; i++) {
			assert (this.loadSamplingGranularity == nodes.get(i)
					.getGranularity());
		}
		this.localworkload = new long[numSources][];
		for (int i = 0; i < numSources; i++)
			localworkload[i] = new long[nodes.size()];
		this.sourceCount = 0;
		seeds = new Seed(serversNo);
		hash = new HashFunction[this.serversNo];
		for (int i=0;i<hash.length;i++) {
			hash[i] = Hashing.murmur3_128(seeds.SEEDS[i]);
		}
	}

	public Server getSever(long timestamp, Object key) {

		int source = (this.sourceCount++) % this.numSources;
		this.sourceCount %= this.numSources;

		int counter = 0;
		int choice[] = new int[this.choices];
		byte b[] = key.toString().getBytes();
		
		while(counter < this.choices) {
			//HashFunction h1 = Hashing.murmur3_128(seeds.SEEDS[counter]);
			choice[counter] =  Math.abs(hash[counter].hashBytes(b).asInt()%serversNo); 
			counter++;
		}
	
		int selected = selectMinChoice(localworkload[source],choice);
		localworkload[source][selected]++;
		
		Server selectedNode = nodes.get(selected);
		return selectedNode;
	}
		int selectMinChoice(long loadVector[], int choice[]) {
			int index = choice[0];
			for(int i = 0; i< choice.length; i++) {
				if (loadVector[choice[i]]<loadVector[index])
					index = choice[i];
			}
			return index;
		}
}
