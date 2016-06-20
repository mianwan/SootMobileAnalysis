package edu.usc.sql.analyses;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by mianwan on 6/8/16.
 */
public class ReachingDefinitionAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<Definition>> {
    private SootMethod method;
    private Map<Unit, Integer> offsetMap = new HashMap<Unit, Integer>();

    public ReachingDefinitionAnalysis(UnitGraph graph) {
        super(graph);
        method = graph.getBody().getMethod();
        // Manually assign offset to each Unit
        int offset = 0;
        for (Iterator<Unit> unitIt = graph.iterator(); unitIt.hasNext();) {
            Unit unit = unitIt.next();
            offsetMap.put(unit, offset++);
        }

        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet<Definition> in, Unit node, FlowSet<Definition> out) {
        Unit u = node;
        kill(in, u, out);
        gen(out, u);
    }

    private void kill(FlowSet<Definition> inSet, Unit u, FlowSet<Definition> outSet) {
        //System.out.println("before"+inSet.size()+" "+outSet.size());
        inSet.copy(outSet);
        if (u instanceof DefinitionStmt) {
            Value v = ((DefinitionStmt) u).getLeftOp();

            for (Definition d : inSet) {
                if (d.getLeft().equivTo(v)) {
                    outSet.remove(d);
                }
            }
        }

    }

    private void gen(FlowSet<Definition> out, Unit u) {
        if (u instanceof DefinitionStmt) {
            int offset = offsetMap.get(u);
            Definition def = new Definition(u, offset);
            out.add(def);
        }
    }

    @Override
    protected FlowSet<Definition> newInitialFlow() {
        return new ArraySparseSet<Definition>();
    }

    @Override
    protected FlowSet<Definition> entryInitialFlow() {
        return new ArraySparseSet<Definition>();
    }

    @Override
    protected void merge(FlowSet<Definition> in1, FlowSet<Definition> in2, FlowSet<Definition> out) {
        in1.union(in2, out);
    }

    @Override
    protected void copy(FlowSet<Definition> source, FlowSet<Definition> dest) {
        source.copy(dest);
    }
}
