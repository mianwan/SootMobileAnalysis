package edu.usc.sql.analyses.demo;

import java.util.List;

import soot.Unit;
import soot.jimple.Expr;
import soot.toolkits.graph.UnitGraph;

/**
 * Created by mianwan on 6/7/16.
 * refer to https://github.com/domainexpert/sootexamples/tree/master/src/org/domainexpert/survivor
 */
public class SimpleVeryBusyExpressions implements VeryBusyExpressions {
    private final VeryBusyExpressionAnalysis analysis;

    public SimpleVeryBusyExpressions(UnitGraph graph) {
        analysis = new VeryBusyExpressionAnalysis(graph);
    }

    @Override
    public List<Expr> getBusyExpressionsBefore(Unit s) {
        return analysis.getFlowBefore(s).toList();
    }

    @Override
    public List<Expr> getBusyExpressionsAfter(Unit s) {
        return analysis.getFlowAfter(s).toList();
    }
}


