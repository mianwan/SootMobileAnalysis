package edu.usc.sql;

import soot.*;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Created by mianwan on 2/28/16.
 */
public class JavaApp {
    private Set<SootClass> allClasses = new HashSet<SootClass>();

    public JavaApp(String jrePath, String appPath, String classListPath){
        Options.v().set_soot_classpath(jrePath + File.pathSeparator + appPath);
        Options.v().set_whole_program(true);
        Options.v().set_verbose(false);
        Options.v().set_keep_line_number(true);
        Options.v().set_keep_offset(true);
        Options.v().set_allow_phantom_refs(true);

        Options.v().set_process_dir(Collections.singletonList(appPath));
        ArrayList<SootMethod> entryPoints = new ArrayList<SootMethod>();
        Set<SootMethod> allMethods = new HashSet<SootMethod>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(classListPath));
            String className;
            while (null != (className = br.readLine())) {
                SootClass sc = Scene.v().loadClassAndSupport(className);

                sc.setApplicationClass();
                allClasses.add(sc);
                allMethods.addAll(sc.getMethods());

                for (SootMethod sm : sc.getMethods()) {
                    if (sm.isStatic() && sm.getSubSignature().equals("void main(java.lang.String[])")) {
                        entryPoints.add(sm);
                    }
                }

            }
            br.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        Scene.v().loadNecessaryClasses();
//        Scene.v().setEntryPoints(entryPoints);
//        CHATransformer.v().transform();
//        CallGraph cg = Scene.v().getCallGraph();
    }

    public Set<SootClass> getAllClasses() {
        return allClasses;
    }
}
