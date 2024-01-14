package project.src.java.dotTreeParser.treeStructure.Nodes;

import project.src.java.dotTreeParser.treeStructure.Comparisson;

import java.util.ArrayList;

public class InnerNode extends Node {
    private Comparisson comparisson;
    private Node leftNode;
    private Node rightNode;

    public InnerNode(Comparisson comparisson, ArrayList<Integer> values){
        this.comparisson = comparisson;
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

    public Comparisson getComparisson() {
        return comparisson;
    }

    public Node getLeftNode() {
        return leftNode;
    }

    public Node getRightNode() {
        return rightNode;
    }

    public void setComparisson(Comparisson comparisson) {
        this.comparisson = comparisson;
    }

    public void setLeftNode(Node leftNode) {
        this.leftNode = leftNode;
    }

    public void setRightNode(Node rightNode) {
        this.rightNode = rightNode;
    }

    

}
