package project.src.java.dotTreeParser.treeStructure.Nodes;

import project.src.java.dotTreeParser.treeStructure.Comparison;

import java.util.ArrayList;

public class InnerNode extends Node {
    private Comparison comparison;
    private Node leftNode;
    private Node rightNode;

    public InnerNode(Comparison comparison, ArrayList<Integer> values){
        this.comparison = comparison;
        this.values = values;

        this.leftNode = null;
        this.rightNode = null;
    }

    public InnerNode() {
    }

    public boolean leftIsNull(){
        return this.leftNode == null;
    }

    public boolean rightIsNull(){
        return this.rightNode == null;
    }

    public Comparison getComparisson() {
        return comparison;
    }

    public Node getLeftNode() {
        return leftNode;
    }

    public Node getRightNode() {
        return rightNode;
    }

    public void setComparisson(Comparison comparison) {
        this.comparison = comparison;
    }

    public void setLeftNode(Node leftNode) {
        this.leftNode = leftNode;
    }

    public void setRightNode(Node rightNode) {
        this.rightNode = rightNode;
    }

    

}
