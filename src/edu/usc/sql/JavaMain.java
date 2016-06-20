package edu.usc.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.util.Chain;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by mianwan on 2/28/16.
 */
public class JavaMain {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws IOException {
        // write your code here
        args = new String[]{"/home/mianwan/IdeaProjects/SootMobileAnalysis/libs/rt.jar",
                "/home/mianwan/AppSet/java/classes", "/home/mianwan/AppSet/java/classList.txt"};
        if (args.length != 3) {
            System.out.println("Usage: jre_jar_path class_path class_list_path");
            return;
        }
        JavaApp app = new JavaApp(args[0], args[1], args[2]);
        Set<SootClass> allClasses = app.getAllClasses();

        for (SootClass sc : allClasses) {
            for (SootMethod sm : sc.getMethods()) {
                if (sm.isConcrete() && sm.getName().equals("ssa")) {
                    Body body = sm.retrieveActiveBody();
                    Chain<Unit> unitChain = body.getUnits();
                    Iterator<Unit> unitIt = unitChain.iterator();

                    while (unitIt.hasNext()) {
                        Stmt stmt = (Stmt) unitIt.next();
                        System.out.println(stmt);
//                        logger.debug(stmt);
//                        if (stmt instanceof DefinitionStmt)
//                            logger.debug(((DefinitionStmt) stmt).getLeftOp() + "--->" + ((DefinitionStmt) stmt).getRightOp());
                    }
                }
            }
        }
    }
}
