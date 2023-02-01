package project.src.java.model;

import java.util.HashMap;
import java.util.Objects;

import project.src.java.model.Nodes.InnerNode;
import project.src.java.model.Nodes.Node;
import project.src.java.model.Nodes.OuterNode;

public class Tree {

    private HashMap<Integer, InnerNode> innerNodes = new HashMap<>();
    private HashMap<Integer, OuterNode> outerNodes = new HashMap<>();

    private Node root;

    public void newInnerNode(InnerNode node){
        if (Objects.isNull(root)){
            this.root = node;
        }
        innerNodes.put(node.getId(), node);
    }

    public void newOuterNode(OuterNode node){
        if (Objects.isNull(root)){
            this.root = node;
        }
        outerNodes.put(node.getId(), node);
    }
    public void linkNodes(Integer fatherId, Integer sonId){
        InnerNode father = innerNodes.get(fatherId);
        Node son = innerNodes.get(sonId);
        if(Objects.isNull(son)){
            son = outerNodes.get(sonId);
        }
        
        if (father.leftIsNull()) {
            father.setLeftNode(son);
        } else {
            father.setRightNode(son);
        }
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return "Tree [root=" + root + "]";
    }

    public int getClassQuantity() {
        return outerNodes.size();
    }
  
}
