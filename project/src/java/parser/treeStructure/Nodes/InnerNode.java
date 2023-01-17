package project.src.java.parser.treeStructure.Nodes;

import project.src.java.parser.treeStructure.Feature;

import java.util.ArrayList;

public class InnerNode extends Node {
    private Feature feature;

    private Node innerLeftNode;
    private Node innerRightNode;

    public InnerNode(Feature feature, ArrayList<Integer> values){
        this.feature = feature;
        this.values = values;

        this.innerLeftNode = null;
        this.innerRightNode = null;
    }

    public boolean leftIsNull(){
        return this.innerLeftNode == null;
    }

    public boolean rightIsNull(){
        return this.innerRightNode == null;
    }
}
