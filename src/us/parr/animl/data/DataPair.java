package us.parr.animl.data;

import java.util.List;

public class DataPair {
	public List<int[]> region1;
	public List<int[]> region2;

	public DataPair(List<int[]> region1, List<int[]> b) {
		this.region1 = region1;
		this.region2 = b;
	}
}
