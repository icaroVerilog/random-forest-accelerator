package project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes;

import java.util.ArrayList;

public abstract class Node {
    protected Integer id;
    protected ArrayList<Integer> values;
    protected InnerNode father;
    protected Integer level;

    public InnerNode getFather() {
        return father;
    }

    public void setFather(InnerNode father) {
        this.father = father;
    }

    @Override
    public String toString() {
        return "Node [id=" + id + ", values=" + values + "]";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ArrayList<Integer> getValues() {
        return values;
    }

    public void setValues(ArrayList<Integer> values) {
        this.values = values;
    }

    public void setLevel(Integer level){
        this.level = level;
    }

    public Integer getLevel(){
        return this.level;
    }
}
