package edu.usc.sql;

import soot.*;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.util.Chain;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class AndroidMain {

    public static void main(String[] args) throws IOException {
	    // write your code here
        if (args.length != 3) {
            System.out.println("Usage: android_jar_path apk_path class_list_path");
            return;
        }
        AndroidApp app = new AndroidApp(args[0], args[1], args[2]);
        Set<SootClass> allClasses = app.getAllClasses();
        int i = 0;
        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/mianwan/appCalls/target.txt"));
        for (SootClass sc : allClasses) {
            if (sc.getName().contains("com.google.ads")) {
                continue;
            }
            for (SootMethod sm : sc.getMethods()) {
                if (sm.isConcrete()) {
                    Body body = sm.retrieveActiveBody();
                    Chain<Unit> unitChain = body.getUnits();
                    Iterator<Unit> unitIt = unitChain.iterator();

                    while (unitIt.hasNext()) {
                        Stmt stmt = (Stmt) unitIt.next();
                        if (stmt instanceof InvokeStmt) {
                            InvokeExpr ie = stmt.getInvokeExpr();
                            SootMethod callee = ie.getMethod();
                            if (callee.getName().equals("setBackgroundColor") ||
                                    callee.getName().equals("setBackgroundDrawable") ||
                                    callee.getName().equals("setBackground") ||
                                    callee.getName().equals("setTextColor")) {
                                Value value = ie.getArg(0);
                                // For IntConstant, toString() can get its value

                                bw.write("" + ++i);
                                bw.write("\n");
                                bw.write("Class:" + sc.getName());
                                bw.write("\n");
                                bw.write("Method:" + sm.getName());
                                bw.write("\n");
                                bw.write("Code:" + stmt);
                                bw.write("\n");
                                bw.write("Value:" + value);
                                bw.write("\n");
                                bw.write("===================");
                                bw.write("\n");
                                /**if (!(value instanceof Constant)) {
                                    bw.write("" + ++i);
                                    bw.write("\n");
                                    bw.write("Class:" + sc.getName());
                                    bw.write("\n");
                                    bw.write("Method:" + sm.getName());
                                    bw.write("\n");
                                    bw.write("Code:" + stmt);
                                    bw.write("\n");
                                    bw.write("Value:" + value.getClass());
                                    bw.write("\n");
                                    bw.write("===================");
                                    bw.write("\n");
                                }*/
                            }
                            /*if (callee.getName().equals("addView")) {
                                bw.write("" + ++i);
                                bw.write("\n");
                                bw.write("Class:" + sc.getName());
                                bw.write("\n");
                                bw.write("Method:" + sm.getName());
                                bw.write("\n");
                                bw.write("Code:" + stmt);
                                bw.write("\n");
                                bw.write("===================");
                                bw.write("\n");
                            }*/
                        }
                    }
                }
            }
        }
        bw.close();
    }
}
