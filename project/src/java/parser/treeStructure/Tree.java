package project.src.java.parser.treeStructure;

import project.src.java.parser.treeStructure.Nodes.InnerNode;
import project.src.java.parser.treeStructure.Nodes.Node;

public class Tree {

    private Node root;

    public void newInnerNode(Node node){
        if (((InnerNode) root).leftIsNull() && ((InnerNode) root).rightIsNull()){
            this.root = node;
        }

    }
}
