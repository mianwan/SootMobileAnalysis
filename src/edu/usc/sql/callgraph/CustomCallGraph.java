package edu.usc.sql.callgraph;

import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;

import java.util.*;

/**
 * Created by mian on 8/14/16.
 */
public class CustomCallGraph {
    private Set<Node> nodes;
    private Set<Node> heads;
    private Set<Node> nodesInScc;
    private List<Node> RTOList;

    public CustomCallGraph(CallGraph cg, Set<SootMethod> allMethods) {
        nodes = new HashSet<Node>();
        heads = new HashSet<Node>();
        nodesInScc = new HashSet<Node>();
        RTOList = new LinkedList<Node>();

        // Build a lookup table for each node
        Map<SootMethod, Node> methodToNodeMap = new HashMap<SootMethod, Node>();
        for (SootMethod sm : allMethods) {
            Node node = new Node(sm);
            nodes.add(node);
            methodToNodeMap.put(sm, node);
        }

        // Fill in the parent child relationship
        for (Node node : nodes) {
            SootMethod sm = node.getMethod();
            Sources srcIt = new Sources(cg.edgesInto(sm));

            while (srcIt.hasNext()) {
                SootMethod src = (SootMethod) srcIt.next();
                if (allMethods.contains(src)) {
                    Node parent = methodToNodeMap.get(src);
                    // If not a recursive method
                    if (!parent.getMethod().getSignature().equals(node.getMethod().getSignature())) {
                        node.addParent(parent);
                        parent.addChild(node);
                    }
                }
            }
        }

        for (Node node : nodes) {
            if (node.getInDegree() == 0) {
                heads.add(node);
            }
        }



    }

    public Node DFS (String entry, String target) {

        Node caller = null;
        for (Node node : heads) {
            if (node.getMethod().getSignature().equals(entry)) {
                System.out.println(node.getMethod().getSignature());
                Set<Node> visited = new HashSet<Node>();
                LinkedList<Node> stack = new LinkedList<Node>();
                stack.push(node);
                Node prev = null;
                while (!stack.isEmpty()) {
                    Node curr = stack.pop();
                    if (visited.contains(curr))
                        continue;
                    else
                        visited.add(curr);
                    if (curr.getMethod().getSubSignature().equals(target)) {
                        caller = prev;
                        break;
                    }
                    for (Node child : curr.getChildren()) {
                        stack.push(child);
                    }
                    prev = curr;
                }
            }
        }
        return caller;
    }

    public void RTOSorting() {

    }

    public int size() {
        return nodes.size();
    }

    public Set<Node> getHeads() {
        return heads;
    }
}
