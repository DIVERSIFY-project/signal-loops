package fr.inria.diverse.signalloops.codegenerators;

/**
 * Created by marodrig on 29/09/2015.
 */
public class TemplateInputVariable {

    String variableName;
    String loadMethodName;
    String variableType;
    private boolean initialized;
    private boolean isArray;

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setLoadMethodName(String loadMethodName) {
        this.loadMethodName = loadMethodName;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getLoadMethodName() {
        return loadMethodName;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean getInitialized() {
        return initialized;
    }

    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }

    public boolean getIsArray() {
        return isArray;
    }

    public void setArray(boolean isArray) {
        this.isArray = isArray;
    }
}
