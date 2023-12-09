package compiladores;

import java.util.LinkedList;

import compiladores.compiladoresParser.AssignamentInStatementContext;
import compiladores.compiladoresParser.AssignmentContext;
import compiladores.compiladoresParser.CompoundInstructionContext;
import compiladores.compiladoresParser.FactorContext;
import compiladores.compiladoresParser.ForStatementContext;
import compiladores.compiladoresParser.FunctionCallContext;
import compiladores.compiladoresParser.FunctionDeclarationContext;
import compiladores.compiladoresParser.FunctionPrototypeContext;
import compiladores.compiladoresParser.FunctionStatementContext;
import compiladores.compiladoresParser.InstructionsContext;
import compiladores.compiladoresParser.ParametersContext;
import compiladores.compiladoresParser.ProgramContext;
import compiladores.compiladoresParser.ReturnStatementContext;
import compiladores.compiladoresParser.StatementContext;
import compiladores.compiladoresParser.StatementsTypesContext;
import compiladores.compiladoresParser.WhileStatementContext;
import compiladores.compiladoresParser.ParametersPrototypeContext;


public class Listener extends compiladoresBaseListener{
    private SymbolTable symbolTable = SymbolTable.getInstanceOf();
    String filePath = "./symbolTable.log";
    

    /**
     * Esta es la regla inicial, al ingresar debo crear un nuevo contexto, este corresponde 
     * al contexto global.
     */
    @Override
    public void enterProgram(ProgramContext ctx) {
        symbolTable.deleteFile(filePath);
        System.out.println("------------>Compilation begins<------------");
        symbolTable.addContext();
    }
    
    /**
     * Al salir del programa elimino el contexto global.
     */
    @Override
    public void exitProgram(ProgramContext ctx) {
        
        symbolTable.printSymbolTable();
        symbolTable.printSymbolTableToFile(filePath);
        
        //Verificar si hay funciones usadas no inicializadas.
        
        System.out.println("Unused: " + symbolTable.getUnusedID());
        //Si desde acá llamo a una función que me diga si hay funciones usadas que no fueron inicializadas
        //Debería funcionar ya que se termina el scope de la función acá.
        System.out.println("Used uninitialized: " + symbolTable.getUsedUninitialized());
        symbolTable.delContext();
        System.out.println("------------->Compilation ends<-------------");
    }

    /**
     * Las instrucciones compuestas son las que se encuentran entre {}
     * Al ingresar a esta regla, se crea un nuevo contexto. Se pueden dar diferentes casos:
     *  - Si se entra a una instrucción compuesta desde una declaración de función, se deben
     *      agregar los parámetros de la función al contexto local.
     *  - Si viene de un for, debo agregar las definiciones que se hicieron en la declaración
     *      del mismo.
     */
    @Override
    public void enterCompoundInstruction(CompoundInstructionContext ctx) {
        
        //Verifico si viene de una declaración de función
        if(ctx.getParent() instanceof FunctionStatementContext) {
            symbolTable.addContext(); 
            Function function = (Function) symbolTable.searchSymbol(ctx.getParent().getChild(0).getChild(1).getText());
            LinkedList<Parameter> parameters = function.getArgs();

            for (Parameter parameter : parameters) { //Agrego los parámetros de la función al contexto
                Variable variable = new Variable(parameter.getName(), parameter.getDataType(), false, true);
                symbolTable.addSymbol(variable);
            }
        }

    }


    /**
     * Al salir de una compoundInstruction:
     *  -Si viene de una definición de función chequeo el return
     *  -Debo verificar si quedaron variables o funciones sin usar
     *  -Elimino el contexto
     */
    @Override
    public void exitCompoundInstruction(CompoundInstructionContext ctx) {
        
        
        if(ctx.getParent() instanceof FunctionStatementContext) {
            //Obtengo el tipo de dato de retorno de la función
            DataType returnType =  DataType.getDataTypeFromString(ctx.getParent().getChild(0).getChild(0).getText());
            Boolean returnFlag = false;

            InstructionsContext instructions = ctx.instructions();

            while(instructions.getChildCount() != 0) {
                
                //Busco la instrucción de return
                if(instructions.instruction().getChild(0) instanceof ReturnStatementContext) {
                    returnFlag = true;
                    //System.out.println(instructions.instruction().getChild(0).getChild(1));
                    break;
                }
                instructions = (InstructionsContext) instructions.instructions();
            }

            if(returnType.toString() == "VOID" && returnFlag) 
                throw new RuntimeException("error: 'return' with a value in function returning void");

            if(returnType.toString() != "VOID" && !returnFlag)
                throw new RuntimeException("error: control reaches end of non-void function [-Wreturn-type]");
        }

        symbolTable.printSymbolTable();
        symbolTable.printSymbolTableToFile(filePath);
        //Ver como manejo los warnings de las variables o funciones sin usar.
        System.out.println("Unused: " + symbolTable.getUnusedID());

        //Si desde acá llamo a una función que me diga si hay funciones usadas que no fueron inicializadas
        //Debería funcionar ya que se termina el scope de la función acá.
        System.out.println("Used uninitialized: " + symbolTable.getUsedUninitialized());
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
        
        String functionName = ctx.ID().getText();
        ParametersContext parameters = ctx.parameters();
        DataType dataType = DataType.getDataTypeFromString(ctx.TYPE().getText());

        Function function = new Function(functionName, dataType, false, true);
    
        //Agrego todos los parámetros de la función
        while(parameters.getChildCount() != 0){
            function.addArg(DataType.getDataTypeFromString(parameters.TYPE().getText()),
                            parameters.ID().getText());
            if(parameters.getChildCount() == 4) 
                parameters = (ParametersContext) parameters.parameters();
            else
                break;
        }

        Function prototype = (Function) symbolTable.searchLocalSymbol(functionName);
        //Si no tiene prototipo, entonces agrego la función al contexto actual
        if(prototype == null) {
            if(functionName.equals("main"))
                function.setUsed(true);
            
            symbolTable.addSymbol(function);
        }
        
        else {
            //Declaración con distinto tipo que el prototipo
            if(function.getDataType() != prototype.getDataType())
                throw new RuntimeException("error: conflicting types for ' " + functionName + "'");
            //Diferentes argumentos en los argumentos
            if(!function.compareArgs(prototype.getArgs()))
                throw new RuntimeException("error: conflicting types for ' " + functionName + "'");
            
            //Le agrega los argumentos al prototipo y lo pone como inicializado
            prototype.setArgs(function.getArgs());
            prototype.setInitialized(true);
        }
    }

    
    /**
     * Agrega la el prototipo con sus argumentos de la función al contexto actual.
     */
    @Override
    public void exitFunctionPrototype(FunctionPrototypeContext ctx) {
        
        String functionName = ctx.ID().getText();

        if(symbolTable.searchLocalSymbol(functionName) == null) {
            DataType dataType = DataType.getDataTypeFromString(ctx.TYPE().getText());
            Function function = new Function(functionName, dataType, false, false);

            ParametersPrototypeContext parameters = ctx.parametersPrototype();

            while(parameters.getChildCount() != 0) {
                function.addArg(DataType.getDataTypeFromString(parameters.TYPE().getText()), null);

                if(parameters.getChildCount() >= 3)
                    parameters = parameters.parametersPrototype();
                else break;
            }
            symbolTable.addSymbol(function);
        }

        else
            throw new RuntimeException("error: redefinition of prototype ' " + functionName + "'");

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

    /**
     * Los factores son los "operandos" de las expresiones aritmético lógicas. Si hay una variable
     * como factor, entonces esta debe considerarse como usada. Antes debe verificarse que exista
     * en cualquier contexto superior al actual (o el actual).
     */
    @Override
    public void exitFactor(FactorContext ctx) {
        if(ctx.ID() != null){
            ID id = symbolTable.searchSymbol(ctx.ID().getText());

            if(id != null) {
                if(!id.getInitialized())
                    System.out.println("warning: '" + ctx.ID().getText() + "' is used uninitialized");
                id.setUsed(true);
            }
            else
                throw new RuntimeException("error: '" + ctx.ID().getText() + "' undeclared (first use in this function)");
        }
    }

    /**
     * Salgo de una llamada a función. Debo verificar primero que exista.
     *  Luego, si es un factor (está en una asignación del lado derecho) debo corroborar que no sea void
     *  En este punto se le pone la función como usada, al terminar todo (exit program) debería ver si hay funciones
     *  usadas pero no inicializadas.
     */
    @Override
    public void exitFunctionCall(FunctionCallContext ctx) {
        ID id = symbolTable.searchSymbol(ctx.ID().getText());

        if(id != null){
            if (ctx.getParent() instanceof FactorContext && id.getDataType() == DataType.VOID)
                throw new RuntimeException("error: void value not ignored as it ought to be");
                
            id.setUsed(true);
        }
        else 
            throw new RuntimeException("error: implicit declaration of function " + ctx.ID().getText());
    }

    @Override
    public void enterForStatement(ForStatementContext ctx) {
        symbolTable.addContext();
    }
    
    @Override
    public void enterWhileStatement(WhileStatementContext ctx) {
        symbolTable.addContext();
    }    

    
}

