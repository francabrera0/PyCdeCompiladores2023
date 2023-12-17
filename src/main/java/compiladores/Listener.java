package compiladores;

import java.util.LinkedList;


import compiladores.compiladoresParser.AssignamentInStatementContext;
import compiladores.compiladoresParser.AssignmentContext;
import compiladores.compiladoresParser.CallParametersContext;
import compiladores.compiladoresParser.CompoundInstructionContext;
import compiladores.compiladoresParser.ElseIfStatementContext;
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
import compiladores.compiladoresParser.IfStatementContext;
import compiladores.compiladoresParser.InstructionContext;
import compiladores.compiladoresParser.ParametersPrototypeContext;


public class Listener extends compiladoresBaseListener{

    private SymbolTable symbolTable = SymbolTable.getInstanceOf();
    String filePath = "./symbolTable.log";
    

    /**
     * Enter initial rule. Global context is added.
     * 
     */
    @Override
    public void enterProgram(ProgramContext ctx) {
        symbolTable.deleteFile(filePath);
        
        //System.out.println("------------>Compilation begins<------------");
        symbolTable.addContext();
    }
    
    /**
     * Exit initial rule. Global context is deleted.
     *  It checks for unused variables or uninitialized used functions (prototype without definition).
     */
    @Override
    public void exitProgram(ProgramContext ctx) {
        
        deleteContext();
        //System.out.println("------------->Compilation ends<-------------");
    }


    /**
     * Enter compound instruction rule. 
     *  In case this rule is entered from a function statement, a new context is added and all
     *  function parameters are added to the function's local context.
     */
    @Override
    public void enterCompoundInstruction(CompoundInstructionContext ctx) {
        
        if(ctx.getParent() instanceof FunctionStatementContext) {
            symbolTable.addContext(); 
            Function function = (Function) symbolTable.searchSymbol(ctx.getParent().getChild(0).getChild(1).getText());
            LinkedList<Parameter> parameters = function.getArgs();

            for (Parameter parameter : parameters) {
                Variable variable = new Variable(parameter.getName(), parameter.getDataType(), false, true);
                symbolTable.addSymbol(variable);
            }
        }
    }


    /**
     * Exit compound instruction rule. 
     *  - In case this rule is entered from a function statement, checks the return type of the function
     *  - It checks for unused variables or uninitialized used functions (prototype without definition)
     *  - Delete local context
     *  
     */
    @Override
    public void exitCompoundInstruction(CompoundInstructionContext ctx) {
        
        
        if(ctx.getParent() instanceof FunctionStatementContext) {
            //Function return type
            DataType returnType =  DataType.getDataTypeFromString(ctx.getParent().getChild(0).getChild(0).getText());
            Boolean returnFlag = false;

            InstructionsContext instructions = ctx.instructions();

            while(instructions.getChildCount() != 0) {
                //Look for the return statement
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

        deleteContext();
    }


    /**
     * Exit function declaration rule. A new function is created.
     *  - If the function does not have a prototype, the function is added to the current context.
     *  - If the function has a prototype, it is verified that there are no inconsistencies. If 
     *       everything is ok, function becomes initialized.
     */
    @Override
    public void exitFunctionDeclaration(FunctionDeclarationContext ctx) {
        
        String functionName = ctx.ID().getText();
        ParametersContext parameters = ctx.parameters();
        DataType dataType = DataType.getDataTypeFromString(ctx.TYPE().getText());

        Function function = new Function(functionName, dataType, false, true);
    
        //Add function parameters
        while(parameters.getChildCount() != 0){
            function.addArg(DataType.getDataTypeFromString(parameters.TYPE().getText()),
                            parameters.ID().getText());
            if(parameters.getChildCount() == 4) 
                parameters = (ParametersContext) parameters.parameters();
            else
                break;
        }

        Function prototype = (Function) symbolTable.searchLocalSymbol(functionName);
        
        if(prototype == null) {
            if(functionName.equals("main"))
                function.setUsed(true);
            
            symbolTable.addSymbol(function);
        }
        
        else {
            //Different type
            if(function.getDataType() != prototype.getDataType())
                throw new RuntimeException("error: conflicting types for ' " + functionName + "'");
            //Different parameters
            if(!function.compareArgs(prototype.getArgs()))
                throw new RuntimeException("error: conflicting types for ' " + functionName + "'");
            
            prototype.setArgs(function.getArgs());
            prototype.setInitialized(true);
        }
    }

    
    /**
     * Exit function prototype rule. 
     *  - If it is not a redefinition, add the prototype to the current context.
     * 
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
     * Exit statement rule (i.e. int a, b=12, c=2;)
     *  - Each declared variable is added to the current context. Consider whether they are initialized or not.
     *  - Checks that is not a redefinition
     */
    @Override
    public void exitStatement(StatementContext ctx) {

        DataType statementDataType = DataType.getDataTypeFromString(ctx.TYPE().getText()); //All variables have the same type
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
     * Exit assignment rule.
     *  - Checks if the variable exists in some context (local o higher)
     *  - Variable becomes initialized
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
     * Exit factor rule. Factors are the operands of logic arithmetic expressions.
     *  - If a factor is a variable or increment/decrement, then it should be considered as used.
     *  - Checks if the variable is used uninitialized.
     */
    @Override
    public void exitFactor(FactorContext ctx) {
        if(ctx.ID() != null) {
            ID id = symbolTable.searchSymbol(ctx.ID().getText());

            if(id != null) {
                if(!id.getInitialized())
                    System.out.println("warning: '" + ctx.ID().getText() + "' is used uninitialized");
                id.setUsed(true);
            }
            else
                throw new RuntimeException("error: '" + ctx.ID().getText() + "' undeclared (first use in this function)");
        }
        else if(ctx.incDec() != null) {
            ID id = symbolTable.searchSymbol(ctx.incDec().ID().getText());

            if(id != null) {
                if(!id.getInitialized())
                    System.out.println("warning: '" + ctx.incDec().ID().getText() + "' is used uninitialized");
                id.setUsed(true);
            }
            else
                throw new RuntimeException("error: '" + ctx.incDec().ID().getText() + "' undeclared (first use in this function)");
        }
    }


    /**
     * Exit function call rule. 
     *  - Checks if the function already exists
     *  - If the call function is a factor (appears on the right side of an assignment), it is checked to make sure it is not void.
     *  - Checks if the parameters are correct
     */
    @Override
    public void exitFunctionCall(FunctionCallContext ctx) {
        Function function = (Function) symbolTable.searchSymbol(ctx.ID().getText());

        if(function != null) { //Function exists
            if (ctx.getParent() instanceof FactorContext && function.getDataType() == DataType.VOID) //Function void as a factor
                throw new RuntimeException("error: void value not ignored as it ought to be");
            
            LinkedList <DataType> expectedParameters = function.getDataTypeArgs();
            
            LinkedList <DataType> parameters = new LinkedList<>();

            CallParametersContext callParameters = ctx.callParameters();

            while(callParameters.getChildCount() != 0){
                
                if (callParameters.parameter().NUMBER() != null) //Parameter is a number
                    parameters.add(DataType.INT);
                
                else if (callParameters.parameter().CHARACTER() != null) //Parameter is a character
                    parameters.add(DataType.CHAR);
                

                else if(callParameters.parameter().ID() != null) { //Parameter is a variable
                    Variable variable = (Variable) symbolTable.searchSymbol(callParameters.parameter().ID().getText());
                    if(variable != null) { //Variable exists
                        if(!variable.getInitialized())
                            System.out.println("warning: '" + callParameters.parameter().ID().getText() + "' is used uninitialized");
                        variable.setUsed(true);
                    } 
                    else //Variable does not exist
                        throw new RuntimeException("error: '" + callParameters.parameter().ID().getText() + "' undeclared (first use in this function)");
                    
                    parameters.add(variable.getDataType());
                }

                else if(callParameters.parameter().incDec() != null) { //Parameter is a incDec
                    Variable variable = (Variable) symbolTable.searchSymbol(callParameters.parameter().incDec().ID().getText());

                    if(variable != null) { //Variable exists
                        if(!variable.getInitialized())
                            System.out.println("warning: '" + callParameters.parameter().incDec().ID().getText() + "' is used uninitialized");
                        variable.setUsed(true);
                    }
                    else //Variable does not exist
                        throw new RuntimeException("error: '" + callParameters.parameter().incDec().ID().getText() + "' undeclared (first use in this function)");

                    parameters.add(DataType.INT);
                }

                else if(callParameters.parameter().functionCall() != null){ //Parameter is a function call
                    Function functionParameter = (Function) symbolTable.searchSymbol(callParameters.parameter().functionCall().ID().getText()); //It does not check if the function exist because it is another function call 
                    parameters.add(functionParameter.getDataType());
                }

                else if(callParameters.parameter().logicalArithmeticExpression() != null)  //Parameter is an opal
                    parameters.add(DataType.INT);
                
                else if(callParameters.parameter().assignment() != null) { //Parameter is an assignment
                    Variable variable = (Variable) symbolTable.searchSymbol(callParameters.parameter().assignment().ID().getText());

                    parameters.add(variable.getDataType());
                }

                if (callParameters.getChildCount() == 3)
                    callParameters = (CallParametersContext) callParameters.getChild(2);
                else 
                    break;
                
            }

            if(!compareLists(parameters, expectedParameters)){
                System.out.println("Error in parameters in function " + function.getName());
                System.out.println("Obtained: " + parameters);
                System.out.println("Expected: " + expectedParameters);
            }

            function.setUsed(true);
        }
        else //Function does not exist
            throw new RuntimeException("error: implicit declaration of function " + ctx.ID().getText());
    }

    /**
     * Enter for rule. Add new local context.
     */
    @Override
    public void enterForStatement(ForStatementContext ctx) {
        symbolTable.addContext();
    }
    
    /**
     * Enter while rule. Add new local context.
     */
    @Override
    public void enterWhileStatement(WhileStatementContext ctx) {
        symbolTable.addContext();
    }
    
    /**
     * Enter if rule. Add new local context.
     */
    @Override
    public void enterIfStatement(IfStatementContext ctx) {
        symbolTable.addContext();
    }

    /**
     * Enter else if rule. Add new local context.
     */
    @Override
    public void enterElseIfStatement(ElseIfStatementContext ctx) {
        symbolTable.addContext();
    }

    /**
     * Exit instruction rule. If the instruction is a compound instruction it does nothing (it already has its particular rule).
     *  If it is not and it is a statement coming from an if, else, for or while then it eliminates the context.
     *  
     */
    @Override
    public void exitInstruction(InstructionContext ctx) {

        if(!(ctx.getChild(0) instanceof CompoundInstructionContext)) {

            if (ctx.getParent() instanceof ForStatementContext | ctx.getParent() instanceof IfStatementContext  |
                ctx.getParent() instanceof ElseIfStatementContext  | ctx.getParent() instanceof WhileStatementContext)  
                deleteContext();
            
        }
    }

    /**
     * Delete context. Logs the symbol table, checks for unused variables and uninitialized used functions.
     */
    public void deleteContext() {

        symbolTable.printSymbolTableToFile(filePath);
        //Unused variables and functions
        if(!symbolTable.getUnusedID().isEmpty())
            System.out.println("Warning: Unused " + symbolTable.getUnusedID()); 
 
        //The prototypes defined in this context lose their scope, therefore it is verified if they were
        // used and not initialized
        if(!symbolTable.getUsedUninitialized().isEmpty()) 
            throw new RuntimeException("error: undefined reference to '" + symbolTable.getUsedUninitialized().get(0) + "'");

        symbolTable.delContext();
    }

    /** 
     * Compare two lists.
     */
    public <T> Boolean compareLists(LinkedList <T> list1, LinkedList<T> list2) {
        if(list1.size()!= list2.size())
            return false;

        for (int dataType = 0; dataType<list1.size(); dataType++)
            if(list1.get(dataType) != list2.get(dataType)) 
                return false;

        return true;
    }

    
}

