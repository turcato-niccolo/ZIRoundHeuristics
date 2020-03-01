package heuristics.ziround;

import heuristics.ziround.interfaces.Heuristic;
import org.jetbrains.annotations.NotNull;

/**
 * This class uses the library {@link ilog.cplex}
 * https://www.ibm.com/support/knowledgecenter/SSSA5P_12.7.0/ilog.odms.cplex.help/refjavacplex/html/ilog/cplex/IloCplex.html
 *
 * @author Turcato
 */
public class ZiRound implements Heuristic {
    private MMipModel model;
    private MMipModel.NumVariable[] solutions;

    /**
     * Constructor that initializes an instance of {@link #ZiRound}
     * Takes a solved Model
     *
     * @param model A solved Model
     */
    public ZiRound(@NotNull MMipModel model) {
        this.model = model;
        solutions = model.getVariables();
    }

    public void applyHeuristics() {

    }
}
