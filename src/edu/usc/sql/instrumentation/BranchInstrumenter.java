package edu.usc.sql.instrumentation;

import soot.*;
import soot.jimple.*;
import soot.util.Chain;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by mianwan on 6/23/16.
 * command argument: -cp /home/mianwan/AppSet:/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/rt.jar testers.Aa
 */
public class BranchInstrumenter extends BodyTransformer {


    private Local addTmpRef(Body body) {
        Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
        body.getLocals().add(tmpRef);
        return tmpRef;
    }

    private static Local addTmpString(Body body)
    {
        Local tmpString = Jimple.v().newLocal("tmpString", RefType.v("java.lang.String"));
        body.getLocals().add(tmpString);
        return tmpString;
    }

    /**
     * Here, the body has been optimized, such as dead code elimination and constant propagation
     * thus, the statement number can be less than that of the original body
     * @param body
     * @param phaseName
     * @param options
     */
    protected void internalTransform(Body body, String phaseName, Map options) {
        // body's method
        SootMethod method = body.getMethod();

        if (!(method.getSubSignature().equals("void main(java.lang.String[])"))) {
            return;
        }

        // debugging
        System.out.println("instrumenting method : " + method.getSignature());

        // get body's unit as a chain
        Chain units = body.getUnits();

        // get a snapshot iterator of the unit since we are going to
        // mutate the chain when iterating over it.
        //
        Iterator stmtIt = units.snapshotIterator();

        while (stmtIt.hasNext()) {
            // cast back to a statement.
            Stmt stmt = (Stmt)stmtIt.next();

            if (stmt instanceof IfStmt) {

                Local tmpRef = addTmpRef(body);
                Local tmpString = addTmpString(body);

                AssignStmt toAdded1 = Jimple.v().newAssignStmt(tmpRef, Jimple.v().
                        newStaticFieldRef(Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef()));
                AssignStmt toAdded2 = Jimple.v().newAssignStmt(tmpString, StringConstant.v("HEY"));

                SootMethod toCall = Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>");
                InvokeStmt toAdded3 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), tmpString));

                // Insert a statement before the target of if branch
                /*
                Stmt target = ((IfStmt) stmt).getTarget();

                units.insertBefore(toAdded1, target);
                units.insertBefore(toAdded2, target);
                units.insertBefore(toAdded3, target);

                ((IfStmt) stmt).setTarget(toAdded1);
                */

                // Instrument the else branch (1)
//                units.insertAfter(toAdded3, stmt);
//                units.insertAfter(toAdded2, stmt);
//                units.insertAfter(toAdded1, stmt);

                // Instrument the else branch (2)
                units.insertAfter(toAdded1, stmt);
                units.insertAfter(toAdded2, toAdded1);
                units.insertAfter(toAdded3, toAdded2);


            }
        }
    }
}
