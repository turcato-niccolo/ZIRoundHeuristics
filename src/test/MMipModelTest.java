package test;

import heuristics.ziround.MMipModel;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MMipModelTest {
    MMipModel testModel;
    IloNumVar[] vars;
    IloNumVar var = new IloNumVar() {

    }

    @BeforeEach
    void setUp() throws IloException {
        vars = new IloNumVar[]{}
        testModel = new MMipModel();
    }

    @Test
    void setVariables() {
    }

    @Test
    void getNumVariables() {
    }

    @Test
    void setObjective() {
    }
}