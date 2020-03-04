package heuristics.test;

import heuristics.ziround.MMipModel;
import heuristics.ziround.MMipModel.ExprType;
import heuristics.ziround.NumVariable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.management.InvalidAttributeValueException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * min 2 x1 − x2
 * x1 + 2 x2 ≥ 7
 * 2 x1 − x2 ≥ −6
 * −3x1 + 2x2 ≥ 8
 * x1, x2 ≥ 0
 * <p>
 * x1, x2 <= 100
 * <p>
 * A:   <>  b
 * 1   2   >= 7
 * 2  -1   >= -6
 * -3   2   >= 8
 *
 * @author Turcato
 */
@RunWith(Parameterized.class)
public class MMipModelTest {
    @Parameterized.Parameters

    public static Collection<Object[]> data() throws NumVariable.ValueOutOfBoundsException, InvalidAttributeValueException {
        return Arrays.asList(new Object[][]{{
                new NumVariable[]{
                        new NumVariable(MMipModel.VarType.INT, 0, 100, 0),
                        new NumVariable(MMipModel.VarType.INT, 0, 100, 0),
                }
                ,
                new double[][]{
                        {1, 2},
                        {2, -1},
                        {-3, 2}
                },
                new MMipModel.ExprType[]{
                        MMipModel.ExprType.MORE_OR_EQUAL,
                        MMipModel.ExprType.MORE_OR_EQUAL,
                        MMipModel.ExprType.MORE_OR_EQUAL
                },
                new double[]{7, -6, 8},
                new double[]{2, -1},
                MMipModel.ObjType.MIN
        }});
        //Add models to this method
    }


    MMipModel testModel;
    NumVariable[] vars;
    MMipModel.ExprType[] constraintsExpr;
    double[] b;
    double[][] A;
    double[] c;
    MMipModel.ObjType objType;

    public MMipModelTest(NumVariable[] vars, double[][] multiplierMatrix, ExprType[] constraintsExpr, double[] b, double[] c, MMipModel.ObjType objType) {
        this.vars = vars;
        this.A = multiplierMatrix;
        this.constraintsExpr = constraintsExpr;
        this.b = b;
        this.c = c;
        this.objType = objType;
        testModel = new MMipModel();
        testModel.setVariables(vars);
        testModel.setConstraints(A, b, constraintsExpr);
        testModel.setObjective(objType, c);
    }

    @Test
    public void setVariables() {
        MMipModel constructionTestModel = new MMipModel();
        assertTrue(constructionTestModel.setVariables(vars));
    }

    @Test
    public void setConstraints() {
        MMipModel constructionTestModel = new MMipModel();
        constructionTestModel.setVariables(vars);
        //Must have variables defined
        assertTrue(constructionTestModel.setConstraints(A, b, constraintsExpr));
    }

    @Test
    public void setObjective() {
        MMipModel constructionTestModel = new MMipModel();
        constructionTestModel.setVariables(vars);
        //Must have variables defined
        assertTrue(constructionTestModel.setObjective(MMipModel.ObjType.MIN, c));
    }

    @Test
    public void getObjType() {
        assertEquals(objType, testModel.getObjType());
    }

    @Test
    public void getNumVariables() {
        assertEquals(vars.length, testModel.countNumVariables());
    }

    @Test
    public void setRelaxed() {
        testModel.setRelaxed();
        for (NumVariable var : testModel.getVariables()) {
            assertEquals(MMipModel.VarType.REAL, var.getType());
        }
    }

    @Test
    public void setIntegerConstraints() {
        int[] intConstraints = new int[]{1};
        testModel.setRelaxed();
        testModel.setIntegerConstraints(intConstraints);

        List<Integer> intConstraintsIndex = new ArrayList<>();
        for (int index : intConstraints) {
            intConstraintsIndex.add(index);
        }
        for (int j = 0; j < testModel.countNumVariables(); j++) {
            if (intConstraintsIndex.contains(j))
                assertEquals(MMipModel.VarType.INT, testModel.getVariable(j).getType());
            else
                assertEquals(MMipModel.VarType.REAL, testModel.getVariable(j).getType());
        }

    }

    @Test
    public void changeConstraintsToLessThan() {
        testModel.changeConstraintsToLessThan();
        for (MMipModel.ExprType type : testModel.getConstraintsExprTypes()) {
            assertTrue(type.equals(MMipModel.ExprType.LESS_OR_EQUAL) || type.equals(MMipModel.ExprType.LESS_THAN));
        }
    }

    @Test
    public void changeConstraintsToMoreThan() {
        testModel.changeConstraintsToMoreThan();
        for (MMipModel.ExprType type : testModel.getConstraintsExprTypes()) {
            assertTrue(type.equals(MMipModel.ExprType.MORE_OR_EQUAL) || type.equals(MMipModel.ExprType.MORE_THAN));
        }
    }

    @Test
    public void getConstraintSlack() {
        try {
            testModel.getVariable(0).setValue(5);
            testModel.getVariable(1).setValue(15);
        } catch (InvalidAttributeValueException e) {
            // values are integer
        } catch (NumVariable.ValueOutOfBoundsException e) {
            //Values are out of bounds
            fail();
        }
        double[] slacks = new double[testModel.countConstraints()];

        for (int i = 0; i < testModel.countConstraints(); i++) {
            for (int j = 0; j < testModel.countNumVariables(); j++) {
                slacks[i] += testModel.getVariable(j).getValue() * testModel.getConstraintsMultiplier(i, j);
            }
            if (testModel.getExprType(i).equals(MMipModel.ExprType.MORE_OR_EQUAL) || testModel.getExprType(i).equals(MMipModel.ExprType.MORE_THAN))
                slacks[i] = slacks[i] - testModel.getExpressionValue(i);
            else
                slacks[i] = testModel.getExpressionValue(i) - slacks[i];
        }

        int i = 0;
        for (double slack : slacks) {
            assertEquals(slack, testModel.getConstraintSlack(i++), 0);
        }
    }

    @Test
    public void countNumVariables() {
        assertEquals(vars.length, testModel.countNumVariables());
    }

    @Test
    public void countConstraints() {
        assertEquals(constraintsExpr.length, testModel.countConstraints());
    }

    @Test
    public void getExpressionsValues() {
        assertEquals(b, testModel.getExpressionsValues());
    }

    @Test
    public void getExpressionValue() {
        for (int i = 0; i < testModel.countConstraints(); i++) {
            assertEquals(b[i], testModel.getExpressionValue(i), 0);
        }
    }

    @Test
    public void getObjMultipliers() {
        assertEquals(c, testModel.getObjMultipliers());
    }

    @Test
    public void getObjMultiplier() {
        for (int j = 0; j < testModel.countNumVariables(); j++) {
            assertEquals(c[j], testModel.getObjMultiplier(j), 0);
        }
    }

    @Test
    public void getMultiplierMatrix() {
        double[][] matrix = testModel.getMultiplierMatrix();
        for (int i = 0; i < testModel.countConstraints(); i++) {
            assertEquals(A[i], matrix[i]);
        }
    }

    @Test
    public void getConstraintsMultiplier() {
        double[][] matrix = testModel.getMultiplierMatrix();
        for (int i = 0; i < testModel.countConstraints(); i++) {
            for (int j = 0; j < testModel.countNumVariables(); j++) {
                assertEquals(A[i][j], matrix[i][j], 0);
            }
        }
    }

    @Test
    public void getConstraintsExprTypes() {
        ExprType[] exprTypes = testModel.getConstraintsExprTypes();
        for (int i = 0; i < testModel.countConstraints(); i++) {
            assertEquals(constraintsExpr[i], exprTypes[i]);
        }
    }

    @Test
    public void getExprType() {
        for (int i = 0; i < testModel.countConstraints(); i++) {
            assertEquals(constraintsExpr[i], testModel.getExprType(i));
        }
    }

    @Test
    public void getVariables() {
        NumVariable[] variables = testModel.getVariables();
        for (int j = 0; j < testModel.countNumVariables(); j++) {
            assertEquals(vars[j], variables[j]);
        }
    }

    @Test
    public void getVariable() {
        for (int j = 0; j < testModel.countNumVariables(); j++) {
            assertEquals(vars[j], testModel.getVariable(j));
        }
    }

}