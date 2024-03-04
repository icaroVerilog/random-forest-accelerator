package project.src.java.util.relatory;

import java.util.ArrayList;

public class Entry {

	public String dataset;
	public String approach;
	public String maxDepth;
	public int totalNodesQnt;
	public ArrayList<Integer> nodeQntByTree;

	public Entry(String dataset, String approach, String maxDepth, ArrayList<Integer> nodeQntByTree){
		this.dataset       = dataset;
		this.approach      = approach;
		this.maxDepth      = maxDepth;
		this.nodeQntByTree = nodeQntByTree;
		this.totalNodesQnt = 0;

		for (int index = 0; index < nodeQntByTree.size(); index++) {
			this.totalNodesQnt += nodeQntByTree.get(index);
		}
	}
}
