package edu.usc.sql.analyses;

import edu.usc.sql.AndroidApp;
import soot.*;
import soot.jimple.*;
import soot.options.Options;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by mianwan on 6/9/16.
 */
public class RDAnalysisMain {
    private static String CLASS_NAME = "testers.Aa";
    private static String METHOD_NAME = "main";

    public static void main(String[] args) {
        String cp = "/home/mianwan/AppSet";
        String apk = cp + File.separator + "com.liveleak.liveleak.apk";
        String clsList = cp + File.separator + "com.liveleak.liveleak.txt";

        try {
//            JavaAppRDAnalysis(cp);
            AndroidAppRDAnalysis(cp, apk, clsList);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void JavaAppRDAnalysis(String classPath) throws FileNotFoundException {
        String sootClsPath = Scene.v().getSootClassPath() + File.pathSeparator + classPath;
        Scene.v().setSootClassPath(sootClsPath);

        Options.v().set_keep_line_number(true);
//        Options.v().set_keep_offset(true);

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
        ReachingDefinitions an = new ReachingDefinitions(g);

        // Iterate over the results
        Iterator<Unit> i = g.iterator();
        PrintWriter pw = new PrintWriter(new File("/home/mianwan/RD/RDResult.txt"));

        while (i.hasNext()) {
            Unit u = i.next();
            /*for (Iterator j = u.getTags().iterator(); j.hasNext(); ) {
                Tag tag = (Tag) j.next();
                if (tag instanceof LineNumberTag) {
                    byte[] value = tag.getValue();
                    int lineNumber = ((value[0] & 0xff) << 8)
                            | (value[1] & 0xff);
                    System.out.println("Line " + lineNumber + ":");
                }
            }*/

            List<Definition> IN = an.getReachingDefinitionsBefore(u);
//            G.v().out.println("\nUnit: " + u.toString());

            dumpDefWithinInvocation(u, IN, pw);

            /**
             G.v().out.println("IN contains:");
             for (Definition d : IN) {
             G.v().out.println("\t" + d.toString());
             }*/

            /**
            List<ValueBox> uses = u.getUseBoxes();
            for (ValueBox vb : uses) {
                Value v = vb.getValue();
                if (v instanceof Local) {
                    int defs = getDefinitionNumber(v, IN);
                    G.v().out.println("The DEF number for " + v + ": " + defs);
                } else if (v instanceof Expr) {
                    G.v().out.println(v + "@" + v.getClass());
                    if(v instanceof InstanceInvokeExpr) {
                        Value target = ((InstanceInvokeExpr) v).getBase();
                        System.out.println("target: " + target);
                    }
                }
            }*/

        }
        pw.close();
    }

    public static void AndroidAppRDAnalysis(String androidJarPath, String androidApkPath, String classListPath) throws FileNotFoundException {
        AndroidApp app = new AndroidApp(androidJarPath, androidApkPath, classListPath);
        Set<SootClass> allClasses = app.getAllClasses();
        for (SootClass sc : allClasses) {
            if (sc.getName().contains("com.google.ads")) {
                continue;
            }

            if (!sc.getName().equals("com.actionbarsherlock.internal.widget.ActionBarContextView"))
                continue;

            PrintWriter pw = new PrintWriter(new FileOutputStream("/home/mianwan/RD/"+ sc.getName() +".txt"));

            for (SootMethod sm : sc.getMethods()) {
                if (sm.isConcrete()) {
                    if (!(sm.getName().equals("initTitle")))
                        continue;
                    Body body = sm.retrieveActiveBody();

                    // Build the CFG and run the analysis
                    UnitGraph g = new ExceptionalUnitGraph(body);
                    ReachingDefinitions an = new ReachingDefinitions(g);

                    // Iterate over the results
                    Iterator<Unit> i = g.iterator();

                    while (i.hasNext()) {
                        Unit u = i.next();
                        List<Definition> IN = an.getReachingDefinitionsBefore(u);
                        dumpDefWithinInvocation(u, IN, pw);

                        /**List<ValueBox> uses = u.getUseBoxes();
                        fos.println("\nUnit: " + u.toString());
                        for (ValueBox vb : uses) {
                            Value v = vb.getValue();
                            if (v instanceof Constant || v instanceof Expr)
                                continue;
                            if (v instanceof Value) {
                                int defs = getDefinitionNumber(v, IN);
                                fos.println("The DEF number for " + v + ": " + defs);
                            }
                        }*/

                    }
                }
            }
            pw.close();
        }
    }

    public static int getDefinitionNumber(Value value, List<Definition> in) {
        int defNum = 0;
        for (Definition d : in) {
            if (d.getLeft().equivTo(value)) {
                defNum++;
            }
        }
        return defNum;
    }

    public static void dumpDefWithinInvocation(Unit u, List<Definition> in, PrintWriter pw) {
        if (u instanceof InvokeStmt) {
            InvokeExpr ie = ((InvokeStmt) u).getInvokeExpr();
            pw.println("\n" + u.toString());
            pw.println("Method invoked: " + ie.getMethod().getSignature());
            List<Value> args = ie.getArgs();
            int t = 0;
            for (Value a : args) {
                if (!(a instanceof Constant)) {
                    int defArg = getDefinitionNumber(a, in);
                    pw.print("Arg " + t);
                    pw.println(":" + a + "--->" + defArg);
                }
                t++;
            }
            if (ie instanceof InstanceInvokeExpr) {
                Value base = ((InstanceInvokeExpr) ie).getBase();
                int defs = getDefinitionNumber(base, in);
                pw.println("Base: " + base + "--->" + defs);
            }

        } else if (u instanceof AssignStmt) {
            Value right = ((AssignStmt) u).getRightOp();
            if (right instanceof InvokeExpr) {
                pw.println("\n" + u.toString());
                pw.println("Method invoked: " + ((InvokeExpr) right).getMethod().getSignature());
                List<Value> args = ((InvokeExpr) right).getArgs();
                int t = 0;
                for (Value a : args) {
                    if (!(a instanceof Constant)) {
                        int defArg = getDefinitionNumber(a, in);
                        pw.print("Arg " + t);
                        pw.println(":" + a + "--->" + defArg);
                    }
                    t++;
                }

                if(right instanceof InstanceInvokeExpr) {
                    Value base = ((InstanceInvokeExpr) right).getBase();
                    int defs = getDefinitionNumber(base, in);
                    pw.println("Base: " + base + "--->" + defs);
                }
            }
        }
    }
}
