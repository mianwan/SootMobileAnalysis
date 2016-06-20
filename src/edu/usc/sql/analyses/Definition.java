package edu.usc.sql.analyses;

import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;

/**
 * Created by mianwan on 6/9/16.
 */
public class Definition {
    private int offset;
    private Value left;
    private Value right;

    public Definition(Unit unit, int offset){
        if (unit instanceof DefinitionStmt) {
            this.offset = offset;
            left = ((DefinitionStmt) unit).getLeftOp();
            right = ((DefinitionStmt) unit).getRightOp();
        }
    }

    public Value getLeft() {
        return left;
    }

    @Override
    public String toString() {
        return left.toString() + "->" + right.toString();
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + offset;
        result = 31 * result + left.hashCode();
        result = 31 * result + right.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Definition) {
            Definition d = (Definition) o;
            if (this.offset == d.offset && this.right.equivTo(d.right) && this.left.equivTo(d.left) ) {
                return true;
            }
        }

        return false;
    }
}
