package project.src.java.util.relatory;

import project.src.java.util.FileBuilder;

import java.util.ArrayList;

public class ReportGenerator {

	private static final ArrayList<Entry> entries = new ArrayList<>();

	public void createEntry(String dataset, String approach, String maxDepth, ArrayList<Integer> nodeQntByTree){
		entries.add(new Entry(dataset, approach, maxDepth, nodeQntByTree));
	}

	public void generateReport(){

		String src = "";

		for (int index1 = 0; index1 < entries.size(); index1++) {
			Entry entry = entries.get(index1);
			String entryFileLine = "";

			entryFileLine += String.format("dataset: %s\n", entry.dataset);
			entryFileLine += String.format("approach: %s\n", entry.approach);
			entryFileLine += String.format("tree max depth: %s\n", entry.maxDepth);
			entryFileLine += String.format("total node quantity: %d\n", entry.totalNodesQnt);

			if (entry.nodeQntByTree != null){
				for (int index2 = 0; index2 < entry.nodeQntByTree.size(); index2++) {
					entryFileLine += String.format("tree%d node quantity: %d\n", index2, entry.nodeQntByTree.get(index2));
				}
			}
			entryFileLine += "=================================================\n";
			src += entryFileLine;
		}

		FileBuilder.execute(src, "report.txt");
	}

//	private static final ArrayList<Entry>   relatoryEntries = new ArrayList<>();
//	private static final ArrayList<String> 	datasets 	    = new ArrayList<>();
//
//	private static final HashMap<String, Integer> totalNodes = new HashMap<>();
//	private static final HashMap<String, Integer> maxTreesDepth = new HashMap<>();
//
//	public void newEntry(String dataset, String approach, int treeIndex, int nodeQnt){
//		Entry newEntry = new Entry(dataset, approach, treeIndex, nodeQnt);
//		relatoryEntries.add(newEntry);
//
//		if (!datasets.contains(dataset)){
//			datasets.add(dataset);
//		}
//	}
//
//	public void nodesQntByDataset(String dataset, int nodesQnt){
//		totalNodes.putIfAbsent(dataset, nodesQnt);
//	}
//
//	public void treeDepthByDataset(String dataset, int treeDepth){
//		maxTreesDepth.putIfAbsent(dataset, treeDepth);
//	}
//
//	public void GenerateRelatory(){
//
//		HashMap<String, String> relatoryFileEntries = new HashMap<>();
//
//		for (int index = 0; index < relatoryEntries.size(); index++) {
//			if (relatoryFileEntries.containsKey(relatoryEntries.get(index).dataset)){
//				String relatoryEntryString = relatoryFileEntries.get(relatoryEntries.get(index).dataset);
//				relatoryEntryString += String.format(
//					"tree%d: %d\n",
//					relatoryEntries.get(index).treeIndex,
//					relatoryEntries.get(index).nodeQnt
//				);
//				relatoryFileEntries.put(relatoryEntries.get(index).dataset, relatoryEntryString);
//			} else {
//				String relatoryEntryString = "";
//				relatoryEntryString += String.format("dataset: %s\n", relatoryEntries.get(index).dataset);
//				relatoryEntryString += String.format("approach: %s\n", relatoryEntries.get(index).approach);
//				relatoryEntryString += String.format("max tree depth: %s\n", maxTreesDepth.get(relatoryEntries.get(index).dataset));
//				relatoryEntryString += "nodes by tree\n";
//				relatoryEntryString += String.format(
//						"tree%d: %d\n",
//						relatoryEntries.get(index).treeIndex,
//						relatoryEntries.get(index).nodeQnt
//				);
//				relatoryFileEntries.put(relatoryEntries.get(index).dataset, relatoryEntryString);
//			}
//		}
//
//		for (String key: relatoryFileEntries.keySet()){
//			int totalNodesQnt = totalNodes.get(key);
//
//			String relatoryEntryString = relatoryFileEntries.get(key);
//			relatoryEntryString += String.format("total nodes: %d\n", totalNodesQnt);
//			relatoryFileEntries.put(key, relatoryEntryString);
//		}
//
//		String relatory = "";
//
//		for (String key: relatoryFileEntries.keySet()){
//			relatory += (relatoryFileEntries.get(key) + "\n");
//		}
//
//		FileBuilder.execute(relatory, "relatory.txt");
//	}
}
