package compiladores;

import java.util.LinkedList;


import compiladores.compiladoresParser.AssignamentInStatementContext;
import compiladores.compiladoresParser.AssignmentContext;
import compiladores.compiladoresParser.CompoudInstructionContext;
import compiladores.compiladoresParser.FunctionDeclarationContext;
import compiladores.compiladoresParser.FunctionStatementContext;
import compiladores.compiladoresParser.ParametersContext;
import compiladores.compiladoresParser.ProgramContext;
import compiladores.compiladoresParser.StatementContext;
import compiladores.compiladoresParser.StatementsTypesContext;


public class Listener extends compiladoresBaseListener{
    private SymbolTable symbolTable = SymbolTable.getInstanceOf();
    
    @Override
    public void enterProgram(ProgramContext ctx) {
        System.out.println("------------>Compilation begins<------------");
        symbolTable.addContext(); //Global context
    }
    
    @Override
    public void exitProgram(ProgramContext ctx) {
        System.out.println("------------->Compilation ends<-------------");
        symbolTable.delContext(); //Global context
    }

    @Override
    public void enterCompoudInstruction(CompoudInstructionContext ctx) {
        symbolTable.addContext(); 

        if(ctx.getParent() instanceof FunctionStatementContext) { //Viene de una declaración de función
            Function function = (Function) symbolTable.searchSymbol(ctx.getParent().getChild(0).getChild(1).getText());
            LinkedList<Parameter> parameters = function.getArgs();

            for (Parameter parameter : parameters) { //Agrego los parámetros de la función al contexto
                Variable variable = new Variable(parameter.getName(), parameter.getDataType(), false, true);
                symbolTable.addSymbol(variable);
            }
        }

    }

    @Override
    public void exitCompoudInstruction(CompoudInstructionContext ctx) {
        symbolTable.printSymbolTable();
        //Al salir debería verificar si hay variables o funciones sin usar
        symbolTable.delContext();
    }


    /**
     * Agrego una nueva función al contexto. Se agrega el ID de la función.
     * Por ejemplo:
     * int sum(int a, int b) {
     *  return a+b;
     * }
     * Se agrega la función sum (con su tipo y parámetros) al contexto actual.
     * Luego cuando se encuentra el {} se crea un nuevo contexto local de la función.
     */
    @Override
    public void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
        Function prototype = (Function) symbolTable.searchSymbol(ctx.ID().getText());

        if (prototype == null) {
            DataType dataType = DataType.getDataTypeFromString(ctx.TYPE().getText());
            Function function = new Function(ctx.ID().getText(), dataType, false, true);
        
            ParametersContext parameters = ctx.parameters();

            while(parameters.getChildCount() != 0){
                function.addArg(DataType.getDataTypeFromString(parameters.TYPE().getText()),
                                parameters.ID().getText());
                if(parameters.getChildCount() == 4) 
                    parameters = (ParametersContext) parameters.parameters();
                else
                    break;
            }
            symbolTable.addSymbol(function);
        }

        else {
            System.out.println("Function with prototype");
        }
            
    }

    /**
     * Cada variable declarada se agrega al contexto actual. Contempla las inicializadas y 
     * no inicializadas. Verifica la re definición local de variables.
     * Caso de ejemplo: int a, b=12, c;
     */
    @Override
    public void exitStatement(StatementContext ctx) {

        DataType statementDataType = DataType.getDataTypeFromString(ctx.TYPE().getText()); //Todas tendrán el mismo tipo
        StatementsTypesContext statementsTypes = ctx.statementsTypes();

        while(true) {

            Variable variable = new Variable(null,
                                             statementDataType, 
                                            false,
                                            null);
            String variableName;
            Boolean initialized;

            if(statementsTypes.getChild(0) instanceof AssignamentInStatementContext) {
                variableName = statementsTypes.getChild(0).getChild(0).getText();
                initialized = true;
            }
            else {
                variableName = statementsTypes.getChild(0).getText();
                initialized = false;    
            }
            
            if(symbolTable.searchLocalSymbol(variableName) != null)
                throw new RuntimeException("error: redefinition of '" + variableName + "'");
            
            variable.setName(variableName);
            variable.setInitialized(initialized);
            symbolTable.addSymbol(variable);

            if(statementsTypes.getChildCount() == 3)
                statementsTypes = (StatementsTypesContext)statementsTypes.getChild(2);
            else
                break;
        }        
    }

    /**
     * Asignaciones, se debe chequear que la variable exista en algun contexto (local o superior).
     * Si existe, se debe cambiar el inicializado a true.
     */
    @Override
    public void exitAssignment(AssignmentContext ctx) {
        String variableName = ctx.ID().getText();

        ID variable = symbolTable.searchSymbol(variableName);

        if(variable == null)
            throw new RuntimeException("error: '" + variableName + "' undeclared");
        else {
            if(!variable.getInitialized())
                variable.setInitialized(true);
        }
    }
}
