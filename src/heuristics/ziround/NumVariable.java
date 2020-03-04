package heuristics.ziround;

import javax.management.InvalidAttributeValueException;

/**
 * Simple class representing a Variable, can be of 2 types:
 * -Integer
 * -Real
 */
public class NumVariable {
    private MMipModel.VarType type;
    private double value;
    private double upBound;
    private double lowBound;

    private static final String VALUE_NOT_INT_ERROR = "The var's type is Integer but the value is not";
    private static final String VALUE_OUT_OF_BOUNDS_ERROR = "The var's value can't be out of the given bounds";

    /**
     * Constructor, builds the object variable starting from the Type, the value, the UPPER and LOWER bound
     *
     * @param type     Type of Variable
     * @param value    Value of the Variable
     * @param upBound  UpperBound
     * @param lowBound LowerBound
     * @throws InvalidAttributeValueException If the variable's type is integer and the value is not
     *                                        OR the value is out of the given bounds
     */
    public NumVariable(MMipModel.VarType type, double value, double upBound, double lowBound) throws InvalidAttributeValueException, ValueOutOfBoundsException {
        if (type == MMipModel.VarType.INT && !isInt(value))
            throw new InvalidAttributeValueException(VALUE_NOT_INT_ERROR);
        if (value < lowBound || value > upBound)
            throw new ValueOutOfBoundsException(VALUE_OUT_OF_BOUNDS_ERROR);
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
        return Math.min(value - Math.floor(value), Math.ceil(value) - value) == 0;
    }

    /**
     * @return The variable's type
     */
    public MMipModel.VarType getType() {
        return type;
    }

    /**
     * @param type The new type for the variable
     * @throws InvalidAttributeValueException if the variable's type is integer and the value is not
     */
    public void setType(MMipModel.VarType type) throws InvalidAttributeValueException {
        if (type == MMipModel.VarType.INT && !isInt(value))
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
     * @throws InvalidAttributeValueException If the variable's type is integer and the value is not
     *                                        OR the value is out of the given bounds
     */
    public void setValue(double value) throws InvalidAttributeValueException, ValueOutOfBoundsException {
        if (type == MMipModel.VarType.INT && !isInt(value))
            throw new InvalidAttributeValueException(VALUE_NOT_INT_ERROR);
        if (value < lowBound || value > upBound)
            throw new ValueOutOfBoundsException(VALUE_OUT_OF_BOUNDS_ERROR);
        this.value = value;
    }

    /**
     * @return The variable's current lower bound
     */
    public double getLowBound() {
        return lowBound;
    }

    /**
     * @return The variable's current upper bound
     */
    public double getUpBound() {
        return upBound;
    }


    public class ValueOutOfBoundsException extends Exception {
        public ValueOutOfBoundsException(String message) {
            super(message);
        }

        public ValueOutOfBoundsException() {
            super();
        }
    }
}
