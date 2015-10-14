package fr.inria.diverse.signalloops.dataflow;

import spoon.reflect.declaration.CtVariable;

/**
 * Value traveling the data-flow
 *
 * Created by marodrig on 13/10/2015.
 */
public class Value {

    public CtVariable getVariable() {
        return variable;
    }

    public void setVariable(CtVariable variable) {
        this.variable = variable;
    }

    /**
     * Variable holding this value
     */
    CtVariable variable;


}
