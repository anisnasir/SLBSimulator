package slb;

import java.util.HashMap;
import java.util.List;

import com.clearspring.analytics.stream.Counter;
import com.clearspring.analytics.stream.StreamSummary;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class LBGreedyChoice implements LoadBalancer {
	public HashMap<String,Long> getTopK(StreamSummary<String> topk, float probability, Long totalItems) {
		HashMap<String,Long> returnList = new HashMap<String,Long>();
		List<Counter<String>> counters = topk.topK(topk.getCapacity()); 
		
		for(Counter<String> counter : counters)  {
			float freq = counter.getCount();
			float error = counter.getError();
			float itemProb = (freq+error)/totalItems;
			if (itemProb > probability) {
				returnList.put(counter.getItem(),counter.getCount());
			}
		}
		return returnList;
		
	}
	private final List<Server> nodes;
	private final int serversNo;
	private final long loadSamplingGranularity;
	private final int numSources;
	private long[] localworkload[];
	private int sourceCount;
	private HashMap<Integer,StreamSummary<String> > map;
	private Seed seeds;
	private HashFunction[] hash;
	private long[] messageCount;

	private int threshold;

	public LBGreedyChoice(List<Server> nodes, int numSources, int threshold) {
		this.nodes = nodes;
		this.numSources = numSources;
		this.serversNo = nodes.size();
		this.loadSamplingGranularity = nodes.get(0).getGranularity();
		for (int i = 1; i < this.serversNo; i++) {
			assert (this.loadSamplingGranularity == nodes.get(i)
					.getGranularity());
		}
		this.localworkload = new long[numSources][];
		for (int i = 0; i < numSources; i++)
			localworkload[i] = new long[nodes.size()];
		this.sourceCount = 0;
		
		map = new HashMap<Integer,StreamSummary<String>> ();
		for (int i = 0 ;i<numSources;i++) {
			map.put(i, new StreamSummary<String>(Constants.STREAM_SUMMARY_CAPACITY));
		}
		seeds = new Seed(serversNo);
		hash = new HashFunction[this.serversNo];
		for (int i=0;i<hash.length;i++) {
			hash[i] = Hashing.murmur3_128(seeds.SEEDS[i]);
		}
		this.threshold = threshold;
		messageCount = new long[this.numSources];
		
	}
	public Server getSever(long timestamp, Object key) {

		int source = (this.sourceCount++) % this.numSources;
		this.sourceCount %= this.numSources;
		this.messageCount[source]++;
		
		//retrieve the source HashMap
		StreamSummary<String> topk = map.get(source);
		
		//update the key in the source HashMap
		String keyStr = key.toString();
		topk.offer(keyStr);
		
		int Choice = 2;
		float probability = Choice/(float)(this.serversNo*this.threshold);
		HashMap<String,Long> freqList = getTopK(topk,probability,this.messageCount[source]);
		
		if(freqList.containsKey(keyStr)) {
			Choice = (int)Math.ceil(freqList.get(keyStr)*this.serversNo/(double)this.messageCount[source]);
		}
		
		//Hash the key accordingly
		int counter = 0;
		int[] choice = new int[Choice];
		byte[] b = key.toString().getBytes();
		
		while(counter < Choice) {
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
