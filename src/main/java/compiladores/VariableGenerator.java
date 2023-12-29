package compiladores;

import java.util.LinkedList;

public class VariableGenerator {
    private static VariableGenerator instance;
    private int numberOfVariables;
    private LinkedList<String> variables;

    private VariableGenerator() {
        this.numberOfVariables = 0;
        this.variables = new LinkedList<String>();
    }

    public static VariableGenerator getInstanceOf() {
        if(instance == null)
            instance = new VariableGenerator();
        
            return instance;
    }

    public int getNumberOfVariables() {
        return numberOfVariables;
    }

    public LinkedList<String> getVariables() {
        return variables;
    }    

    public String getLastVariable() {
        return variables.pop();
    }

    public String getNewVariable() {
        String newVariable = "t" + numberOfVariables++;
        variables.add(newVariable);

        return newVariable;
    }
}
