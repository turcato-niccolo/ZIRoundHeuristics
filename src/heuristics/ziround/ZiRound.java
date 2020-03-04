package heuristics.ziround;

//import heuristics.interfaces.Heuristic;

import heuristics.interfaces.HeuristicListener;
import org.jetbrains.annotations.NotNull;

import javax.management.InvalidAttributeValueException;

/**
 * This class uses the library {@link ilog.cplex}
 * https://www.ibm.com/support/knowledgecenter/SSSA5P_12.7.0/ilog.odms.cplex.help/refjavacplex/html/ilog/cplex/IloCplex.html
 *
 * @author Turcato
 */
public class ZiRound // implements Heuristic
{
    private MMipModel model;
    private NumVariable[] solutions;
    private int[] integerSolutions;
    private double threshold;
    private HeuristicListener solutionListener;

    /**
     * Constructor that initializes an instance of {@link #ZiRound}
     * Takes a solved Model
     *
     * @param model     A relaxed solved Model
     * @param threshold threshold param for the ZiRound algorithm
     */
    public ZiRound(@NotNull MMipModel model, double threshold) {
        this.model = model;
        solutions = model.getVariables();
        this.threshold = threshold;
    }

    /**
     * @param integerSolutions The indexes of the variables to round to their integer value
     */
    public void setIntegerSolutions(int[] integerSolutions) {
        this.integerSolutions = integerSolutions;
    }

    /**
     * The algorithm is designed to work on models that have only <, <=, = constraints
     * <p>
     * call {@link #setIntegerSolutions(int[])} before of this method to select solutions to round to integer
     */
    public void applyHeuristic() throws NumVariable.ValueOutOfBoundsException {
        //To get <, <=, = constraints
        model.changeConstraintsToLessThan();
        //The model is solved, we retrieve the solutions
        solutions = model.getVariables();

        NumVariable[] toBeRounded = new NumVariable[integerSolutions.length];
        boolean[] rounded = new boolean[toBeRounded.length];
        //select solutions to be rounded
        for (int i = 0; i < integerSolutions.length; i++) {
            toBeRounded[i] = solutions[integerSolutions[i]];
        }

        double ZI = 0;
        double[] zis = getZis(toBeRounded);

        double slacksLB = 0;
        double slacksUB = 0;
        do {
            for (int i = 0; i < integerSolutions.length; i++) {
                if (!rounded[i]) {
                    double UB = Math.min(toBeRounded[i].getUpBound() - toBeRounded[i].getValue(), getSlackUB(i));

                    /// TODO: added to the original algorithm, to be verified
                    UB = Math.min(UB, Math.ceil(toBeRounded[i].getValue()) - toBeRounded[i].getValue());
                    ///

                    double LB = Math.min(toBeRounded[i].getValue() - toBeRounded[i].getLowBound(), getSlackLB(i));

                    /// TODO: added to the original algorithm, to be verified
                    LB = Math.min(LB, toBeRounded[i].getValue() - Math.floor(toBeRounded[i].getValue()));
                    ///

                    //UB, LB, threshold available
                    if (computeZI(toBeRounded[i].getValue() + UB) == computeZI(toBeRounded[i].getValue() - LB)
                            && computeZI(toBeRounded[i].getValue() + UB) < zis[i]) {
                        try {
                            //Rounding based on the objective function
                            if (model.getObjType() == MMipModel.ObjType.MIN && model.getObjMultiplier(integerSolutions[i]) > 0
                                    || model.getObjType() == MMipModel.ObjType.MAX && model.getObjMultiplier(integerSolutions[i]) < 0)
                                toBeRounded[i].setValue(toBeRounded[i].getValue() - LB);
                            else
                                toBeRounded[i].setValue(toBeRounded[i].getValue() + UB);

                        } catch (InvalidAttributeValueException e) {
                            //won't happen as long as the given problem was relaxed
                        }
                    } else if (computeZI(toBeRounded[i].getValue() + UB) < computeZI(toBeRounded[i].getValue() - LB)
                            && computeZI(toBeRounded[i].getValue() + UB) < zis[i]) {
                        try {
                            toBeRounded[i].setValue(toBeRounded[i].getValue() + UB);
                        } catch (InvalidAttributeValueException e) {
                            //won't happen as long as the given problem was relaxed
                        }
                    } else if (computeZI(toBeRounded[i].getValue() - LB) < computeZI(toBeRounded[i].getValue() + UB)
                            && computeZI(toBeRounded[i].getValue() - LB) < zis[i]) {
                        try {
                            toBeRounded[i].setValue(toBeRounded[i].getValue() - LB);
                        } catch (InvalidAttributeValueException e) {
                            //won't happen as long as the given problem was relaxed
                        }
                    }

                    if (computeZI(toBeRounded[i]) == 0) {
                        rounded[i] = true;
                    }
                }

            }

            slacksLB = 0;
            slacksUB = 0;
            //Computing the exit condition
            for (int i = 0; i < model.countConstraints(); i++) {

                slacksLB += getSlackLB(i);
                slacksUB += getSlackUB(i);
            }
            //slacks stay 0 if all variables were rounded
        }
        while (slacksLB != 0 || slacksUB != 0); //no updates can be found


    }

    /**
     * @param j Index of a variable in the model
     * @return The ub of xj = min(i) {si/aij: aij > 0}, 0 if there's no aij > 0
     */
    private double getSlackUB(int j) {
        double min = -1;
        for (int i = 0; i < model.countConstraints(); i++) {
            if (model.getConstraintsMultiplier(i, j) > 0) {
                if (min == -1)
                    min = model.getConstraintsMultiplier(i, j) / model.getConstraintsMultiplier(i, j);
                if (model.getConstraintSlack(i) / model.getConstraintsMultiplier(i, j) < min)
                    min = model.getConstraintSlack(i) / model.getConstraintsMultiplier(i, j);
            }
        }
        return (min == -1 ? 0 : min);
    }

    /**
     * @param j Index of a variable in the model
     * @return The lb of xj = min(i) {-si/aij: aij < 0}, 0 if there's no aij < 0
     */
    private double getSlackLB(int j) {
        double min = -1;
        for (int i = 0; i < model.countConstraints(); i++) {
            if (model.getConstraintsMultiplier(i, j) < 0) {
                if (min == -1)
                    min = model.getConstraintsMultiplier(i, j) / model.getConstraintsMultiplier(i, j);
                if (-model.getConstraintSlack(i) / model.getConstraintsMultiplier(i, j) < min)
                    min = -model.getConstraintSlack(i) / model.getConstraintsMultiplier(i, j);
            }
        }
        return (min == -1 ? 0 : min);
    }

    private static double[] getZis(NumVariable[] vars) {
        double[] zis = new double[vars.length];
        for (int i = 0; i < zis.length; i++) {
            zis[i] = computeZI(vars[i]);
        }
        return zis;
    }

    private static double sumZis(double[] zis) {
        double ZI = 0;
        for (double zi : zis)
            ZI += zi;
        return ZI;
    }

    /**
     * @param var A numerical variable
     * @return ZI(var)
     */
    private static double computeZI(@NotNull NumVariable var) {
        return computeZI(var.getValue());
    }

    /**
     * @param value A numerical variable's value
     * @return ZI(value)
     */
    private static double computeZI(double value) {
        return Math.min(value - Math.floor(value), Math.ceil(value) - value);
    }

    //    @Override
    public void setHeuristicListener(HeuristicListener listener) {
        solutionListener = listener;
    }
}
