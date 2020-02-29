package heuristics.ziround;

import ilog.concert.IloConstraint;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;

/**
 * This class uses the library {@link ilog.cplex}
 * https://www.ibm.com/support/knowledgecenter/SSSA5P_12.7.0/ilog.odms.cplex.help/refjavacplex/html/ilog/cplex/IloCplex.html
 *
 * @author Turcato
 */
public class ZiRound {
    IloCplex cplex;

    /**
     * Constructor that initializes an instance of {@link #ZiRound}
     */
    public ZiRound(IloNumVar[] variables, IloConstraint[] constraints, IloObjective objective) {
        IloNumExpr expr;

    }


}
