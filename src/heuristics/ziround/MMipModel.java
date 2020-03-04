package heuristics.ziround;

import org.jetbrains.annotations.NotNull;

import javax.management.InvalidAttributeValueException;

/**
 * Defines a mixed-Mip model
 *
 * @author Turcato
 */
public class MMipModel {
    private NumVariable[] variables;

    private double[][] multiplierMatrix;
    private double[] expressionValue;
    private ExprType[] constraintsExprTypes;

    private double[] objMultipliers;
    private ObjType objType;

    /**
     * Constructor that initializes an empty Model
     */
    public MMipModel() {
    }

    /**
     * Sets the model variables
     *
     * @param variables The model's variables
     * @return {@code true} if the variables have been accepted, if the model's variables were already set this method is
     * accepted only if the input of this method contains the same number of variables, so they will be replaced
     */
    public boolean setVariables(@NotNull NumVariable[] variables) {
        if ((this.variables == null && (objMultipliers == null || variables.length == objMultipliers.length)) ||
                (countNumVariables() == variables.length)) {
            this.variables = variables;
            return true;
        }
        return false;
    }

    /**
     * Relaxes all the integer variable constraints
     */
    public void setRelaxed() {
        for (NumVariable var : variables) {
            try {
                var.setType(VarType.REAL);
            } catch (InvalidAttributeValueException e) {
                //Won't happen
            }
        }
    }

    /**
     * Sets the integer constraints for each variable indicated in the array of indexes
     *
     * @param index The indexes of the model's variables
     * @return {@code True} if it was possible to impose the constraints, {@code False} otherwise
     */
    public boolean setIntegerConstraints(int[] index) {
        for (int i = 0; i < index.length; i++)
            try {
                variables[index[i]].setType(VarType.INT);
            } catch (InvalidAttributeValueException e) {
                return false;
            }
        return true;
    }


    /**
     * Changes all MORE_THAN and MORE_OR_EQUAL constraints to LESS_THAN and LESS_OR_EQUAL respectively
     */
    public void changeConstraintsToLessThan() {
        int i = 0; //index for the current constraint
        for (double[] constraintMultipliers : multiplierMatrix) {
            switch (constraintsExprTypes[i]) {
                case EQUAL:
                case LESS_THAN:
                case LESS_OR_EQUAL:
                    break;

                case MORE_THAN:
                    for (int j = 0; j < constraintMultipliers.length; j++) {
                        constraintMultipliers[j] = -constraintMultipliers[j];
                    }
                    expressionValue[i] = -expressionValue[i];
                    constraintsExprTypes[i] = ExprType.LESS_THAN;
                    break;

                case MORE_OR_EQUAL:
                    for (int j = 0; j < constraintMultipliers.length; j++) {
                        constraintMultipliers[j] = -constraintMultipliers[j];
                    }
                    expressionValue[i] = -expressionValue[i];
                    constraintsExprTypes[i] = ExprType.LESS_OR_EQUAL;
                    break;
            }
            i++;
        }
    }

    /**
     * Changes all LESS_THAN and LESS_OR_EQUAL constraints to MORE_THAN and MORE_OR_EQUAL respectively
     */
    public void changeConstraintsToMoreThan() {
        int i = 0; //index for the current constraint
        for (double[] constraintMultipliers : multiplierMatrix) {
            switch (constraintsExprTypes[i]) {
                case EQUAL:
                case MORE_THAN:
                case MORE_OR_EQUAL:
                    break;

                case LESS_THAN:
                    for (int j = 0; j < constraintMultipliers.length; j++) {
                        constraintMultipliers[j] = -constraintMultipliers[j];
                    }
                    expressionValue[i] = -expressionValue[i];
                    constraintsExprTypes[i] = ExprType.MORE_THAN;
                    break;

                case LESS_OR_EQUAL:
                    for (int j = 0; j < constraintMultipliers.length; j++) {
                        constraintMultipliers[j] = -constraintMultipliers[j];
                    }
                    expressionValue[i] = -expressionValue[i];
                    constraintsExprTypes[i] = ExprType.MORE_OR_EQUAL;
                    break;
            }
            i++;
        }
    }

    /**
     * Returns the slacks for the given constraints, if the constraint is a = expression, it will always return 0
     *
     * @param i Index of a constraint
     * @return Current slack of i° constraint
     */
    public double getConstraintSlack(int i) {
        if (constraintsExprTypes[i] == ExprType.EQUAL)
            return 0;
        //value of the left member of the constraint's expression
        double leftValue = 0;
        for (int col = 0; col < multiplierMatrix[i].length; col++) {
            leftValue += multiplierMatrix[i][col] * variables[col].getValue();
        }
        switch (constraintsExprTypes[i]) {
            case LESS_THAN:
            case LESS_OR_EQUAL:
                return expressionValue[i] - leftValue;

            case MORE_THAN:
            case MORE_OR_EQUAL:
                return leftValue - expressionValue[i];
        }
        return 0;
    }

    /**
     * @return The number of variables of this model
     * @throws NullPointerException If there aren't any variables
     */
    public int countNumVariables() throws NullPointerException {
        return this.variables.length;
    }

    /**
     * @return The number of constraints of thi model
     */
    public int countConstraints() {
        return constraintsExprTypes.length;
    }

    /**
     * Sets the objective of this model
     *
     * @param objType     Type of object, defined with enum {@link ObjType}
     * @param multipliers Multipliers of the obj function, including zeros
     * @return True if the obj function has been accepted depending by the number of variables
     */
    public boolean setObjective(ObjType objType, @NotNull double[] multipliers) {
        if (this.variables == null || countNumVariables() == multipliers.length) {
            this.objMultipliers = multipliers;
            this.objType = objType;
            return true;
        }
        return false;
    }

    /**
     * @return The type of objective function {max, min}
     */
    public ObjType getObjType() {
        return objType;
    }

    /**
     * @param multiplierMatrix The constraints' multipliers Matrix
     * @param expressionValue  The value of the expression
     * @param exprType         Type of expression (>, >=, =, <=, <)
     * @return {@code Type} if the constraints have been accepted depending by the number of variables
     */
    public boolean setConstraints(double[][] multiplierMatrix, double[] expressionValue, ExprType[] exprType) {
        if (multiplierMatrix.length > 0 && multiplierMatrix[0].length == variables.length &&
                expressionValue.length == multiplierMatrix.length && exprType.length == expressionValue.length) {
            this.multiplierMatrix = multiplierMatrix;
            this.expressionValue = expressionValue;
            this.constraintsExprTypes = exprType;
            return true;
        }
        return false;
    }

    public double[] getExpressionsValues() {
        return expressionValue;
    }

    public double getExpressionValue(int i) {
        return expressionValue[i];
    }

    public double[] getObjMultipliers() {
        return objMultipliers;
    }

    public double getObjMultiplier(int i) {
        return objMultipliers[i];
    }

    public double[][] getMultiplierMatrix() {
        return multiplierMatrix;
    }

    /**
     * @param i #Row
     * @param j #Column
     * @return The multiplier a row i and column j of the constraints matrix
     */
    public double getConstraintsMultiplier(int i, int j) {
        return multiplierMatrix[i][j];
    }

    public ExprType[] getConstraintsExprTypes() {
        return constraintsExprTypes;
    }

    public ExprType getExprType(int i) {
        return constraintsExprTypes[i];
    }

    public NumVariable[] getVariables() {
        return variables;
    }

    public NumVariable getVariable(int i) {
        return variables[i];
    }


    public enum VarType {
        INT,
        REAL
    }

    public enum ExprType {
        MORE_THAN,
        MORE_OR_EQUAL,
        EQUAL,
        LESS_OR_EQUAL,
        LESS_THAN
    }

    public enum ObjType {
        MIN,
        MAX
    }

}


