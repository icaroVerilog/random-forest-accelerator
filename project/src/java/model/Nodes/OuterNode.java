package project.src.java.model.Nodes;

import java.util.ArrayList;

public class OuterNode extends Node {

    private String className;
    private Integer classNumber;

    public OuterNode(ArrayList<Integer> values){
        this.values = values;
    }

    public OuterNode() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getClassNumber() {
        return classNumber;
    }

    public void setClassNumber(Integer classNumber) {
        this.classNumber = classNumber;
    }

    
}
