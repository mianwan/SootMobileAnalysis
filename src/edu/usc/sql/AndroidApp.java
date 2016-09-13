package edu.usc.sql;

import edu.usc.sql.callgraph.CustomCallGraph;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mianwan on 11/16/15.
 */
public class AndroidApp {
    // All classes in the app
    private Set<SootClass> allClasses = new HashSet<SootClass>();
    private Set<SootMethod> allMethods = new HashSet<SootMethod>();
    private CustomCallGraph callGraph;

    public AndroidApp(String androidJarPath, String apkPath, String classListPath) {
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_android_jars(androidJarPath);
        Options.v().set_whole_program(true);
        Options.v().set_verbose(false);
        Options.v().set_keep_line_number(true);
        Options.v().set_keep_offset(true);
        Options.v().set_allow_phantom_refs(true);
        List<String> dirList = new ArrayList<String>();
        dirList.add(apkPath);
        Options.v().set_process_dir(dirList);

        ArrayList<SootMethod> entryPoints = new ArrayList<SootMethod>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(classListPath));
            String className;
            while (null != (className = br.readLine())) {
                SootClass sc = Scene.v().loadClassAndSupport(className);

                // Ignore android support classes
                if (!sc.getName().startsWith("android.support") && !sc.getName().startsWith("com.google")) {
                    sc.setApplicationClass();
                    allClasses.add(sc);
                    allMethods.addAll(sc.getMethods());
                    try {
                        SootMethod onCreate = sc.getMethod("void onCreate(android.os.Bundle)");
//                        SootMethod doInBackground = sc.getMethodByName("doInBackground");

                        if (onCreate.isConcrete()) {
                            entryPoints.add(onCreate);
                            System.out.println(onCreate.getSubSignature());
                        }

//                        if (doInBackground.isConcrete()) {
//                            entryPoints.add(doInBackground);
//                            System.out.println(onCreate);
//                        }

                    } catch (RuntimeException e) {
//                       System.out.println(className + "doesn't have the target entry method!");
                    }

                }

            }
            br.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        Scene.v().loadNecessaryClasses();
        Scene.v().setEntryPoints(entryPoints);
        CHATransformer.v().transform();
        callGraph = new CustomCallGraph(Scene.v().getCallGraph(), allMethods);
    }

    public Set<SootClass> getAllClasses() {
        return allClasses;
    }

    public CustomCallGraph getCallGraph() {
        return callGraph;
    }
}
