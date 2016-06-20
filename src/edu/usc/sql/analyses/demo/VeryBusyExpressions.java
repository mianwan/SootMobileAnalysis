package edu.usc.sql.analyses.demo;

/**
 * Created by mianwan on 6/7/16.
 * Example of Soot guide
 */

import soot.Unit;
import soot.jimple.Expr;

import java.util.List;

/**
 * Provides an interface for querying the expressions that are very busy
 * before and after a unit in a method.
 */
public interface VeryBusyExpressions {
    /**
     *   Returns the list of expressions that are very busy before the specified
     *   Unit.
     *   @param s the Unit that defines this query.
     *   @return a list of expressions that are busy before the specified unit in the method.
     */
    public List<Expr> getBusyExpressionsBefore(Unit s);

    /**
     *   Returns the list of expressions that are very busy after the specified
     *   Unit.
     *   @param s the Unit that defines this query.
     *   @return a list of expressions that are very busy after the specified unit in the method.
     */
    public List<Expr> getBusyExpressionsAfter(Unit s);
}
