package project.src.java.approaches.fpga.tableGenerator.tableBuilder;

import project.src.java.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.Node;
import project.src.java.dotTreeParser.treeStructure.Nodes.OuterNode;
import project.src.java.dotTreeParser.treeStructure.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TableBuilder {

    private ArrayList<RawTableEntry> rawTableEntries = new ArrayList<>();

    public void execute(List<Tree> treeList){
        Node teste;

        teste = treeList.get(0).getRoot();

        busca(teste);
        aa();

    }

    private void busca(Node node){
        if (node instanceof OuterNode){
            OuterNode newNode = (OuterNode) node;
            this.rawTableEntries.add(
                new RawTableEntryOuterNode(
                    newNode.getId(),
                    newNode.getClassNumber()
                )
            );
        }
        else {
            InnerNode newNode = (InnerNode) node;

            this.rawTableEntries.add(
                new RawTableEntryInnerNode(
                    newNode.getId(),
                    newNode.getComparisson().getThreshold().toString(),
                    newNode.getComparisson().getColumn()
                )
            );
            busca(newNode.getLeftNode());

            this.rawTableEntries.add(
                    new RawTableEntryInnerNode(
                            newNode.getId(),
                            newNode.getComparisson().getThreshold().toString(),
                            newNode.getComparisson().getColumn()
                    )
            );
            busca(newNode.getRightNode());
        }
    }

    private void aa(){
        for (int i = 0; i < this.rawTableEntries.size(); i++){

            String node = "node "+i+" | ";

            for (int index = 0; index < this.rawTableEntries.size(); index++){
                if (this.rawTableEntries.get(index) instanceof RawTableEntryInnerNode){
                    if (this.rawTableEntries.get(index).getId() == i){
                        node += this.rawTableEntries.get(index+1).getId()+ " | ";
                    }
                }
                if (this.rawTableEntries.get(index) instanceof RawTableEntryOuterNode) {
                    if (this.rawTableEntries.get(index).getId() == i){
                        node += "0 | " + ((RawTableEntryOuterNode) this.rawTableEntries.get(index)).getNodeClass();
                        this.rawTableEntries.remove(index);
                    }
                }
            }

            System.out.println(node);
        }
    }
}
