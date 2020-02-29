package heuristics.ziround;

import ilog.concert.IloIntVar;
import ilog.concert.IloNumVar;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a mixed-Mip model
 *
 * @author Turcato
 */
public class MMpipModel {
    IloNumVar[] variables;
    IloIntVar[] intVariables;

    double[] values;
    int[] intValues;

    public MMpipModel() {

    }

    /**
     * Sets the model variables
     *
     * @param variables The model's variables
     * @param values    The variable's values
     * @return {@code true} if the variables have been accepted, if the model's variables were already set this method is
     * accepted only if the input of this method contains the same number of variables, so they will be replaced
     */
    public boolean setVariables(@NotNull IloNumVar[] variables, @NotNull double[] values) {
        if (this.variables == null || (this.variables.length == variables.length && this.values.length == values.length
                && variables.length == values.length)) {
            this.variables = variables;
            this.values = values;
            return true;
        }
        return false;
    }

}
