package project.src.java.model.Nodes;

import java.util.ArrayList;

public abstract class Node {
    protected Integer id;
    protected ArrayList<Integer> values;

  

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

    
}
