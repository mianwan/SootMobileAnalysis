package edu.usc.sql.callgraph;

import soot.SootMethod;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mian on 8/14/16.
 */
public class Node {
    private SootMethod method;
    private Set<Node> children;
    private Set<Node> parents;

    public Node(SootMethod method) {
        this.method = method;
        children = new HashSet<Node>();
        parents = new HashSet<Node>();
    }

    public void addParent(Node parent) {
        parents.add(parent);
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public int getInDegree() {
        return parents.size();
    }

    public SootMethod getMethod() {
        return method;
    }

    public Set<Node> getChildren() {
        return children;
    }

    public Set<Node> getParents() {
        return parents;
    }
}
