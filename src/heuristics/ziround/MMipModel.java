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
                (getNumVariables() == variables.length)) {
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
     * Changes the expression of constraints to the given type
     * <p>
     * Currently supported: {@link ExprType#LESS_OR_EQUAL}
     *
     * @param exprType The new type of expression to impose to all constraints
     * @return {@code True} if converting was possible
     */
    public boolean changeConstraintsTo(ExprType exprType) {
        int i = 0; //index for the current constraint
        for (double[] constraintMultipliers : multiplierMatrix) {
            if (!(constraintsExprTypes[i] == exprType)) {
                switch (exprType) {
                    //type of expression to which convert
                    case LESS_OR_EQUAL:
                        //type of expression to be converted
                        switch (constraintsExprTypes[i]) {
                            case LESS_OR_EQUAL:
                                break;
                            case MORE_OR_EQUAL:
                                for (double constraintMultiplier : constraintMultipliers) {
                                    constraintMultiplier = -constraintMultiplier;
                                }
                                expressionValue[i] = expressionValue[i];
                                constraintsExprTypes[i] = ExprType.LESS_OR_EQUAL;
                                break;


                        }
                        break;

                    default:
                        return false;
                }
            }
        }

        return true;
    }

    /**
     * @return The number of variables of this model
     * @throws NullPointerException If there aren't any variables
     */
    public int getNumVariables() throws NullPointerException {
        return this.variables.length;
    }

    /**
     * Sets the objective of this model
     *
     * @param objType     Type of object, defined with enum {@link ObjType}
     * @param multipliers Multipliers of the obj function, including zeros
     * @return True if the obj function has been accepted depending by the number of variables
     */
    public boolean setObjective(ObjType objType, @NotNull double[] multipliers) {
        if (this.variables == null || getNumVariables() == multipliers.length) {
            this.objMultipliers = multipliers;
            this.objType = objType;
        }
        return false;
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

    public double[] getExpressionValue() {
        return expressionValue;
    }

    public double getExpressionValue(int i) {
        return expressionValue[i];
    }

    public double[] getObjMultipliers() {
        return objMultipliers;
    }

    public double getObjMultipliers(int i) {
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
    public double getMultiplierMatrix(int i, int j) {
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

    public NumVariable getVariables(int i) {
        return variables[i];
    }

    /**
     * Simple class representing a Variable, can be of 2 types:
     * -Integer
     * -Real
     */
    public class NumVariable {
        private VarType type;
        private double value;
        private double upBound;
        private double lowBound;

        private static final String VALUE_NOT_INT_ERROR = "The var's type is Integer but the value is not";

        /**
         * Constructor, builds the object variable starting from the Type, the value, the UPPER and LOWER bound
         *
         * @param type     Type of Variable
         * @param value    Value of the Variable
         * @param upBound  UpperBound
         * @param lowBound LowerBound
         * @throws InvalidAttributeValueException if the variable's type is integer and the value is not
         */
        public NumVariable(VarType type, double value, double upBound, double lowBound) throws InvalidAttributeValueException {
            if (type == VarType.INT && !isInt(value))
                throw new InvalidAttributeValueException(VALUE_NOT_INT_ERROR);
            this.type = type;
            this.value = value;
            this.lowBound = lowBound;
            this.upBound = upBound;
        }

        /**
         * @return {@code True} if the value is integer, {@code False} otherwise
         */
        public boolean isInt() {
            return isInt(value);
        }

        /**
         * @param value A numerical value
         * @return {@code True} if the value is integer
         */
        private boolean isInt(double value) {
            return value % 2 == 0;
        }

        /**
         * @return The variable's type
         */
        public VarType getType() {
            return type;
        }

        /**
         * @param type The new type for the variable
         * @throws InvalidAttributeValueException if the variable's type is integer and the value is not
         */
        public void setType(VarType type) throws InvalidAttributeValueException {
            if (type == VarType.INT && !isInt(value))
                throw new InvalidAttributeValueException(VALUE_NOT_INT_ERROR);
            this.type = type;
        }

        /**
         * @return The variable's current value
         */
        public double getValue() {
            return value;
        }

        /**
         * @param value The new value for the variable
         * @throws InvalidAttributeValueException if the variable's type is integer and the value is not
         */
        public void setValue(double value) throws InvalidAttributeValueException {
            if (type == VarType.INT && !isInt(value))
                throw new InvalidAttributeValueException(VALUE_NOT_INT_ERROR);
            this.value = value;
        }

        /**
         * @return The variable's current lower bound
         */
        public double getLowBound() {
            return lowBound;
        }

        /**
         * @param lowBound The new lower bound
         */
        public void setLowBound(double lowBound) {
            this.lowBound = lowBound;
        }

        /**
         * @return The variable's current upper bound
         */
        public double getUpBound() {
            return upBound;
        }

        /**
         * @param upBound The new upper bound
         */
        public void setUpBound(double upBound) {
            this.upBound = upBound;
        }
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
