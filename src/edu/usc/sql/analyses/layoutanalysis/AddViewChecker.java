package edu.usc.sql.analyses.layoutanalysis;

import edu.usc.sql.AndroidApp;
import soot.*;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by mian on 7/6/16.
 * First, use Floyd Washall algorithm to check two "addView" invocation is reachable or not?
 * Then, check the definition of the parameters
 */
public class AddViewChecker {
    private static String className = "com.financial.calculator.APRAdvancedAmortization";
    private static String methodName = "fillScheduleData";
    public static void main(String[] args) {
        String cp = "/Users/mian/Documents/Input/AppSet";
        String apk = cp + File.separator + "com.mix.muxicdownload.apk";
        String clsList = cp + File.separator + "com.mix.muxicdownload.txt";

        try {
            AndroidAppRDAnalysis(cp, apk, clsList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void AndroidAppRDAnalysis(String androidJarPath, String androidApkPath, String classListPath) throws FileNotFoundException {
        AndroidApp app = new AndroidApp(androidJarPath, androidApkPath, classListPath);
        Set<SootClass> allClasses = app.getAllClasses();

        int counter = 0;
        for (SootClass sc : allClasses) {
            if (sc.getName().contains("com.google.ads")) {
                continue;
            }

//            if (!sc.getName().equals(className))
//                continue;

//            PrintWriter pw = new PrintWriter(new FileOutputStream("/Users/mian/Documents/Output/"+ sc.getName() +".txt"));

            for (SootMethod sm : sc.getMethods()) {
                if (sm.isConcrete()) {
//                    if (!(sm.getName().equals(methodName)))
//                        continue;

                    List<InvokeExpr> addViewList = new ArrayList<InvokeExpr>();

                    Body body = sm.retrieveActiveBody();

                    // Build the CFG and run the analysis
                    UnitGraph g = new ExceptionalUnitGraph(body);

                    // Iterate over the results
                    Iterator<Unit> it = g.iterator();

                    while (it.hasNext()) {
                        Unit u = it.next();
                        if (u instanceof InvokeStmt) {
                            InvokeExpr expr = ((InvokeStmt) u).getInvokeExpr();
                            if (expr.getMethod().getName().equals("addView")) {
                                addViewList.add(expr);
                            }
                        }

                    }




                    if (!addViewList.isEmpty())
                        System.out.println("\n" + sc.getName() + " | " + sm.getName());

                    Set<Value> layoutSet = new HashSet<Value>();
                    for (int i = 0; i < addViewList.size(); i++) {
                        InvokeExpr e1 = addViewList.get(i);
                        for (int j = i + 1; j < addViewList.size(); j++) {
                            InvokeExpr e2 = addViewList.get(j);
                            if (e1.equivTo(e2)) {
                                continue;
                            }
                            Value base = ((InstanceInvokeExpr) e1).getBase();
                            Value arg = e2.getArg(0);
                            if (base.equivTo(arg) && ((RefType)base.getType()).getClassName().endsWith("Layout")) {
                                System.out.println(++counter);
                                System.out.println(e1);
                                System.out.println(e2);
                                layoutSet.add(arg);
                            }
                        }
                    }
//                    if (!layoutSet.isEmpty())
//                        System.out.println("Layout Number: " + layoutSet.size());

                }
            }


//            pw.close();
        }
    }
}
