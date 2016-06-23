package edu.usc.sql.instrumentation;

/* Usage: java MainDriver [soot-options] appClass
 */

/* import necessary soot packages */
import soot.*;
import soot.options.Options;

import java.io.File;

/**
 * Created by mian on 6/22/16.
 * https://www.sable.mcgill.ca/soot/tutorial/   More on profiling
 */
public class MainDriver {
    public static void main(String[] args) {

        /* check the arguments */
        if (args.length == 0) {
            System.err.println("Usage: java MainDriver [options] classname");
            System.exit(0);
        }


        String s = Scene.v().getSootClassPath() + File.pathSeparator + "/Users/mian/Documents";
        System.out.println(s.contains("mian"));
        Options.v().set_soot_classpath(s);
        Options.v().set_output_format(Options.output_format_class);

        Scene.v().loadClassAndSupport("MyCounter");
        /* add a phase to transformer pack by call Pack.add */
        Pack jtp = PackManager.v().getPack("jtp");
        jtp.add(new Transform("jtp.instrumenter",
            new InvokeStaticInstrumenter()));


        Scene.v().addBasicClass("MyCounter");
        /* Give control to Soot to process all options,
         * InvokeStaticInstrumenter.internalTransform will get called.
         */
        soot.Main.main(args);
    }
}
