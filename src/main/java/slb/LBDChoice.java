package slb;

import java.util.HashMap;
import java.util.List;

import com.clearspring.analytics.stream.Counter;
import com.clearspring.analytics.stream.StreamSummary;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class LBDChoice implements LoadBalancer {
	private final List<Server> nodes;
	private final int serversNo;
	private final long loadSamplingGranularity;
	private final int numSources;
	private long localworkload[][];
	private int sourceCount;
	private HashMap<Integer,StreamSummary<String> > map;
	private Seed seeds;
	private HashFunction[] hash;
	private long[] messageCount;
	private int threshold;
	private float epsilon;

	public LBDChoice(List<Server> nodes, int numSources, int threshold, float epsilon) {
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
		this.epsilon = epsilon;	
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
			double pTop = getPTop(topk,this.messageCount[source]);
			PHeadCount pHead = getPHead(topk,probability,this.messageCount[source]);
			double pTail = 1-pHead.probability;
			double n = (double)this.serversNo;
			double val1 = (n-1)/n;
			int d = (int)Math.round(pTop*n);
			double val2,val3,val4,sum1;
			double sum2,value1,value2,value3,value4;
			do{
				//finding sum Head
				val2 = Math.pow(val1, pHead.numberOfElements*d);
				val3 = 1-val2;
				val4 = Math.pow(val3, 2);
				sum1 = pHead.probability + pTail*val4;
				
				//finding sum1
				value1 = Math.pow(val1, d);
				value2 = 1-value1;
				value3 = Math.pow(value2, d);
				value4 = Math.pow(value2, 2);
				sum2 = pTop+((pHead.probability-pTop)*value3)+(pTail*value4);
				d++;
			}while((d<=this.serversNo) && ((sum1 > (val3+epsilon)) || (sum2 > (value2+epsilon))));			
			Choice = d-1;
		}
		
		//Hash the key accordingly
		int counter = 0;
		int[] choice = new int[Choice];
		byte[] b = key.toString().getBytes();
		if(Choice < this.serversNo) {
			while(counter < Choice) {
				choice[counter] =  Math.abs(hash[counter].hashBytes(b).asInt()%serversNo); 
				counter++;
			}
		}else {
			while(counter < Choice) {
				choice[counter] =  counter; 
				counter++;
			}
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
	public PHeadCount getPHead(StreamSummary<String> topk, double probability, Long totalItems) {
		PHeadCount returnValue = new PHeadCount();
		returnValue.probability=0;
		
		List<Counter<String>> counters = topk.topK(topk.getCapacity()); 
		
		for(Counter<String> counter : counters)  {
			float freq = counter.getCount();
			float error = counter.getError();
			float itemProb = (freq+error)/totalItems;
			if (itemProb > probability) {
				returnValue.probability+=itemProb;
				returnValue.numberOfElements++;
			}
		}
		return returnValue;	
	}
	public float getPTop(StreamSummary<String> topk, Long totalItems) {
		List<Counter<String>> counters = topk.topK(1);
		for(Counter<String> counter : counters)  {
			float freq = counter.getCount();
			float error = counter.getError();
			float itemProb = (freq+error)/totalItems;
			return itemProb;
		}
		return 0f;
	}
}
