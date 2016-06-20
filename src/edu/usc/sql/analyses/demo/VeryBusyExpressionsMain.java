package edu.usc.sql.analyses.demo;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.*;
import soot.jimple.AssignStmt;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * Sample very busy expressions dataflow analysis implemented using Soot.
 * This is part of the complete code for Section 5 of "A Survivor's Guide
 * to Java Program Analysis in Soot"
 *
 * A very busy expression at a control point is an expression that will
 * be evaluated later without definition of its arguments from the current
 * control point to the point of its evaluation. This is useful, e.g., for
 * code motion to move expressions to before a loop such that they need not
 * be evaluated at every iteration.
 *
 * A very busy expressions analysis is a backward analysis collecting
 * expressions encountered and remove them from the flow whenever any of
 * its arguments is defined.
 *
 * @author andrew
 *
 */
public class VeryBusyExpressionsMain {
    private static String CLASS_NAME =
            "testers.VeryBusyClass";
    private static String METHOD_NAME = "main";

    /**
     * This executes the analysis and outputs the IN/OUT set
     * for every statement (Jimple unit). Note that very busy
     * expressions is a backward analysis.
     *
     * @param args the command-line arguments for Soot
     */
    public static void main(String[] args) {

//        soot.options.Options.v().parse(args);

        String sootClsPath = Scene.v().getSootClassPath() + File.pathSeparator + "/home/mianwan/AppSet";
        Scene.v().setSootClassPath(sootClsPath);

        Options.v().set_keep_line_number(true);
        Options.v().set_keep_offset(true);

        // Set up the class we�re working with
        SootClass c = Scene.v().loadClassAndSupport(CLASS_NAME);
        c.setApplicationClass();

        // Load all the necessary classes based on the options
        Scene.v().loadNecessaryClasses();

        // Retrieve the method and its body
        SootMethod m = c.getMethodByName(METHOD_NAME);
        Body b = m.retrieveActiveBody();

        // Build the CFG and run the analysis
        UnitGraph g = new ExceptionalUnitGraph(b);
        VeryBusyExpressions an = new SimpleVeryBusyExpressions(g);

        // Iterate over the results
        Iterator<Unit> i = g.iterator();

        Set<Value> valueSet = new HashSet<Value>();

        while (i.hasNext()) {
            Unit u = i.next();
            /*List<Expr> OUT = an.getBusyExpressionsBefore(u);
            List<Expr> IN = an.getBusyExpressionsAfter(u);

            // Do something clever with the results
            G.v().out.println("\nUnit: " + u.toString());
            G.v().out.println("IN contains:");
            for (Object item: IN) {
                G.v().out.println("\t" + item.toString());
            }
            G.v().out.println("OUT contains:");
            for (Object item: OUT) {
                G.v().out.println("\t" + item.toString());
            }*/

            G.v().out.println("\nUnit: " + u.toString());
            /********************************************
            G.v().out.println("USE:");
            List<ValueBox> useBoxes = u.getUseBoxes();
            for(ValueBox vb : useBoxes) {
                G.v().out.println(vb);
            }
            G.v().out.println("DEF:");
            List<ValueBox> defBoxes = u.getDefBoxes();
            for(ValueBox vb : defBoxes) {
                G.v().out.println(vb.getValue().getType().getClass());
            }*/

            if (u instanceof AssignStmt) {
                Value v = ((AssignStmt) u).getLeftOp();
                G.v().out.println(v.getClass());
                G.v().out.println(((AssignStmt) u).getRightOp().getClass());
                boolean toBeAdded = true;
                for (Value vEle : valueSet) {
                    if (v.equivTo(vEle)) {
                        toBeAdded = false;
                        break;
                    }
                }
                if (toBeAdded) {
                    G.v().out.println(v);
                    valueSet.add(v);
                }

            }
        }
    }
}
