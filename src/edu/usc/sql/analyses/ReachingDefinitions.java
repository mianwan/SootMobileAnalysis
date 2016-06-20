package edu.usc.sql.analyses;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.util.List;

/**
 * Created by mianwan on 6/9/16.
 */
public class ReachingDefinitions {
    private final ReachingDefinitionAnalysis analysis;

    public ReachingDefinitions(UnitGraph graph) {
        analysis = new ReachingDefinitionAnalysis(graph);
    }

    public List<Definition> getReachingDefinitionsBefore(Unit s) {
        return analysis.getFlowBefore(s).toList();
    }
}
