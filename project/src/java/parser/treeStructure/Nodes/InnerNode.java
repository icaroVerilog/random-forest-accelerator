package project.src.java.parser.treeStructure.Nodes;

import project.src.java.parser.treeStructure.Feature;

public class InnerNode extends Node {
    private Feature feature;

    private Node innerLeftNode;
    private Node innerRightNode;

    public InnerNode(Feature feature){
        this.feature = feature;

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
