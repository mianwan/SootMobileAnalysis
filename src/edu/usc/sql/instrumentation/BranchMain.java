package edu.usc.sql.instrumentation;

import soot.*;

import java.io.File;

/**
 * Created by mianwan on 6/23/16.
 */
public class BranchMain {

    public static void main(String[] args) {
         /* check the arguments */
        if (args.length == 0) {
            System.err.println("Usage: java MainDriver [options] classname");
            System.exit(0);
        }


        /* add a phase to transformer pack by call Pack.add */
        Pack jtp = PackManager.v().getPack("jtp");
        jtp.add(new Transform("jtp.instrumenter",
                new BranchInstrumenter()));


        Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        /* Give control to Soot to process all options,
         * InvokeStaticInstrumenter.internalTransform will get called.
         */
        soot.Main.main(args);
    }
}
