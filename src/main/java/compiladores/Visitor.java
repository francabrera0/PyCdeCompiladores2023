package compiladores;

import java.util.LinkedList;

import compiladores.compiladoresParser.AfContext;
import compiladores.compiladoresParser.ArithmeticExpressionContext;
import compiladores.compiladoresParser.ArithmeticTermContext;
import compiladores.compiladoresParser.AssignamentInStatementContext;
import compiladores.compiladoresParser.AssignmentContext;
import compiladores.compiladoresParser.AssignmentsContext;
import compiladores.compiladoresParser.AtContext;
import compiladores.compiladoresParser.ConditionContext;
import compiladores.compiladoresParser.FactorContext;
import compiladores.compiladoresParser.ForStatementContext;
import compiladores.compiladoresParser.FunctionCallContext;
import compiladores.compiladoresParser.FunctionDeclarationContext;
import compiladores.compiladoresParser.FunctionStatementContext;
import compiladores.compiladoresParser.IncDecContext;
import compiladores.compiladoresParser.InitContext;
import compiladores.compiladoresParser.InstructionContext;
import compiladores.compiladoresParser.InstructionsContext;
import compiladores.compiladoresParser.LogicalArithmeticExpressionContext;
import compiladores.compiladoresParser.LogicalExpressionContext;
import compiladores.compiladoresParser.ParametersContext;
import compiladores.compiladoresParser.ProgramContext;
import compiladores.compiladoresParser.ReturnStatementContext;
import compiladores.compiladoresParser.StatementContext;
import compiladores.compiladoresParser.StatementsTypesContext;
import compiladores.compiladoresParser.UpdateContext;
import compiladores.compiladoresParser.WhileStatementContext;

public class Visitor extends compiladoresBaseVisitor<String> {

    private String treeAddressCode;                 //Buffer to store tree address code
    private String incDecInstruction;               //Buffer to store increment or decrement instruction
    private int preOrPost;                          //Control variable for the inclusion of the incDecInstruction: 0->None, 1->Pre, 2->Post
    private LinkedList<String> incDecID;            //List used to store the ids that must be incremented or decremented
    private VariableGenerator variableGenerator;    //Variable generator
    private LabelGenerator labelGenerator;          //Label generator
    private LinkedList<String> operands;            //List used to store operands. It's useful to have the operands available from different functions


    /**
     * Class constructor
     */
    public Visitor() {
        treeAddressCode = "";
        incDecInstruction = "";
        preOrPost = 0;
        incDecID = new LinkedList<>();
        variableGenerator = VariableGenerator.getInstanceOf();
        labelGenerator = LabelGenerator.getInstanceOf();
        operands = new LinkedList<>();
    }

    /**
     * visitProgram()
     * 
     * @brief Starting point, begins to traverse the entire tree.
     * @rule program : instructions EOF;
     */
    @Override
    public String visitProgram(ProgramContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }
    
    /**
     * visitInstructions()
     * 
     * @brief It visits an instruction and then visits itself by nesting instructions.
     * @rule instructions : instruction instructions
     *                    |
     *                    ;
     */
    @Override
    public String visitInstructions(InstructionsContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }
    
    /**
     * visitInstruction()
     * 
     * @brief Visit each instruction.
     * @rule instruction : compoundInstruction
     *                   | statement
     *                   | assignments SEMICOLON                
     *                   | returnStatement  
     *                   | ifStatement
     *                   | whileStatement
     *                   | forStatement
     *                   | functionCall SEMICOLON
     *                   | logicalArithmeticExpression SEMICOLON
     *                   | functionStatement
     *                   ;  
     */
    @Override
    public String visitInstruction(InstructionContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }

    /**
     * visitLogicalArithmeticExpression()
     * 
     * @brief Visit your only child (logicalExpression)
     * @rule logicalArithmeticExpression : logicalExpression
     *                                   ;
     * 
     */
    @Override
    public String visitLogicalArithmeticExpression(LogicalArithmeticExpressionContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }

    /**
     * visitLogicalExpression()
     *    
     * @brief This can have three possible child
     *          - arithmeticExpression: call visitAritmeticExpression function and save 
     *                                   the resulting operand in a new variable (this 
     *                                   operand will be in the top of operands list).
     *          - comparison: call visitArithmeticExpression function twice to get both operands.
     *          - logic AND/OR: -> call visitLogicalExpression function to get the first operand
     *                          -> call visitLogicalExpression function again to get the second operand
     *                               (this operand can be another AND/OR)
     *                          -> in this case, when the visitLogicalExpression function is called ,
     *                               it is entered again in this case
     *                          -> recursive calls
     *                          -> This is done until all the operand are arithmeticExpressions or
     *                               comparisons and there the tac begins to be generated.
     * @rule logicalExpression : logicalExpression AND logicalExpression
     *                         | logicalExpression OR logicalExpression
     *                         | arithmeticExpression CMP arithmeticExpression
     *                         | arithmeticExpression
     *                         ; 
     */
    @Override
    public String visitLogicalExpression(LogicalExpressionContext ctx) {
                
        if(ctx.getChild(1) == null) { //arithmeticExpression
            visitArithmeticExpression(ctx.arithmeticExpression(0)); 

            String newVariable = variableGenerator.getNewVariable();
            
            if(preOrPost == 1) treeAddressCode += incDecInstruction;
            treeAddressCode += "\n" + newVariable + " = " + operands.pop(); 
            if(preOrPost == 2) treeAddressCode+= incDecInstruction;

            preOrPost = 0;
            incDecInstruction = "";

            operands.push(newVariable); 
        }
        else {
            if(ctx.getChild(1).getText().equals("&&") || ctx.getChild(1).getText().equals("||")) { //AND/OR

                visitLogicalExpression(ctx.logicalExpression(0));
                visitLogicalExpression(ctx.logicalExpression(1));

                String newVariable = variableGenerator.getNewVariable();
                String firstOperand = operands.pop();
                String secondOperand = operands.pop();
                String operator = ctx.getChild(1).getText();

                treeAddressCode += "\n" + newVariable + " = " + firstOperand + operator + secondOperand;
                operands.push(newVariable);
            }

            else { //Comparison (Only accepts two operands)

                visitArithmeticExpression(ctx.arithmeticExpression(0));
                visitArithmeticExpression(ctx.arithmeticExpression(1)); 

                String newVariable = variableGenerator.getNewVariable();
                String secondOperand = operands.pop();
                String firstOperand = operands.pop();

                treeAddressCode += "\n" + newVariable + " = " +  firstOperand + ctx.getChild(1).getText() + secondOperand;
                operands.push(newVariable); 
            }
        }

        return treeAddressCode;
    }

    /**
     * visitArithmeticExpression()
     *  
     * @brief Visit arithmetic terms and nested terms (at)
     * @rule arithmeticExpression : arithmeticTerm at;
     */
    @Override
    public String visitArithmeticExpression(ArithmeticExpressionContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }
    
    /**
     * visitArithmeticTerm()
     * 
     * @brief Visit factors and nested factors (af)
     * @rule arithmeticTerm : factor af;
     */
    @Override
    public String visitArithmeticTerm(ArithmeticTermContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }

    /**
     * visitFactor()
     * 
     * @brief When visiting a factor, it may be the case that is terminal (we already have the final
     *          value that we need) and therefore we only have to place this value in the operands list.
     *        If the factor is not terminal, the corresponding visit functions are called.
     * @rule factor : NUMBER
     *              | CHARACTER
     *              | ID
     *              | PARENTHESES_O logicalArithmeticExpression PARENTHESES_C
     *              | incDec
     *              | functionCall
     *              ;
     */
    @Override
    public String visitFactor(FactorContext ctx) {
        if(ctx.NUMBER() != null) {
            operands.push(ctx.NUMBER().getText());
        }
        else if(ctx.CHARACTER() != null) {
            char operand = ctx.CHARACTER().getText().charAt(1);
            operands.push(String.valueOf((int) operand));
        }
        else if(ctx.ID() != null) {
            operands.push(ctx.ID().getText());
        }
        else if(ctx.incDec() != null) {
            visitIncDec(ctx.incDec());
        }
        else if(ctx.logicalArithmeticExpression()!= null) {
            visitLogicalArithmeticExpression(ctx.logicalArithmeticExpression());
        }
        else if(ctx.functionCall() != null) {
            visitFunctionCall(ctx.functionCall());
        }

        return treeAddressCode;
    }

    /**
     * @brief Verifies if it is a pre or post operation and save this information in preOrPostVariable,
     *          then it assembles the instructions and stores it in the variable incDecInstruction,
     *          both variables will be used to insert the instruction into the TAC later.
     * @rule incDec : INCDECOPERATORS ID
     *              | ID INCDECOPERATORS
     *              ;
     */
    @Override
    public String visitIncDec(IncDecContext ctx) {
        if(ctx.getChild(0).getText().contains("+") || ctx.getChild(0).getText().contains("-") ) //pre
            preOrPost = 1;
        else //post
            preOrPost = 2;
        String id = ctx.ID().getText();

        incDecInstruction += "\n" + id + " = " + id;

        if(ctx.INCDECOPERATORS().getText().equals("++"))
            incDecInstruction += " + 1";
        else
            incDecInstruction += " - 1";

        operands.push(id);
        incDecID.push(id);
        return treeAddressCode;
    }

    /**
     * visitFunctionCall()
     * 
     * @brief Not implemented yet.
     * @rule 
     */
    @Override
    public String visitFunctionCall(FunctionCallContext ctx) {
        System.out.println("Function call");
        return treeAddressCode;
    }
   
    /**
     * visitAf()
     * 
     * @brief This node is visited in order to find multiplications or divisions between factors.
     *        In case this context does not have more factors the function returns.
     *        In case this context has more factors, the visitFactor function is called and return with 
     *          the factor in the operands list.
     *        Finally check if there are more nested operation, and if so, visit AF. 
     * @rule af : MUL factor af
     *          | DIV factor af
     *          |
     *          ;
     */
    @Override
    public String visitAf(AfContext ctx) {

        String firstOperand;

        if(ctx.factor() != null) {
            firstOperand = operands.pop();
            visitFactor(ctx.factor()); //Return with an operand in operands list
        }
        else 
            return treeAddressCode;
        
        
        String secondOperand = operands.pop();
        String newVariable = variableGenerator.getNewVariable();
        String operator = ctx.getChild(0).getText();
        Boolean incDec = false;

        if(preOrPost!= 0) { //Check if the id incremented(decremented) is used here
            String s = incDecID.pop();
            if(s.equals(firstOperand) || s.equals(secondOperand))
                incDec = true;
            else
                incDecID.push(s);
        } 

        if(preOrPost == 1 && incDec) treeAddressCode += incDecInstruction;
        treeAddressCode += "\n" + newVariable + " = " + firstOperand + operator + secondOperand; 
        if(preOrPost == 2 && incDec) treeAddressCode+= incDecInstruction;

        if(incDec) {
            preOrPost = 0;
            incDecInstruction = "";
        }

        operands.push(newVariable);

        if(ctx.af().af() != null) {
            visitAf(ctx.af());
        }

        return treeAddressCode;
    }

    /**
     * Visitar el At para encontrar si hay sumas o restas entre terminos aritméticos.
     * En caso de que no haya más terminos retorna. Si si hay terminos, saca el primer operandod e la 
     * lista y realiza una visita al otro termino. EN operands volverá el operando.
     * Arma el TAC.
     * Verifica si hay anidaciones. 
     * 
     * visitAt()
     * 
     * @brief This node is visited in order to find add or subs between terms.
     *        In case this context does not have more terms the function returns.
     *        In case this context has more terms, the visitArithmeticTerm function is called 
     *          and return with the term in the operands list.
     *        Finally check if there are more nested operation, and if so, visit AT. 
     * @rule at : ADD arithmeticTerm at
     *          | SUB arithmeticTerm at
     *          |
     *          ;
     */
    @Override
    public String visitAt(AtContext ctx) {
 
        String firstOperand;

        if(ctx.arithmeticTerm() != null) {
            firstOperand = operands.pop();
            visitArithmeticTerm(ctx.arithmeticTerm());
        }
        else {
            return treeAddressCode;
        }
        
        String secondOperand = operands.pop();
        String newVariable = variableGenerator.getNewVariable();
        String operator = ctx.getChild(0).getText();

        Boolean incDec = false;

        if(preOrPost!= 0){ //Check if the id incremented(decremented) is used here
            String s = incDecID.pop();
            if(s.equals(firstOperand) || s.equals(secondOperand))
                incDec = true;
            else
                incDecID.push(s);
        } 

        if(preOrPost == 1 && incDec) treeAddressCode += incDecInstruction;
        treeAddressCode += "\n" + newVariable + " = " + firstOperand + operator + secondOperand; 
        if(preOrPost == 2 && incDec) treeAddressCode+= incDecInstruction;

        if(incDec) {
            preOrPost = 0;
            incDecInstruction = "";
        }

        operands.push(newVariable);

        if(ctx.at().at() != null)
            visitAt(ctx.at());

        return treeAddressCode;    
    }    

    /**
     * visitAssignments()
     * 
     * @brief Visit all the assignments.
     * @rule assignments : assignment COMMA assignments
     *                   | assignment
     *                   ;
     */
    @Override
    public String visitAssignments(AssignmentsContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }
    
    /**
     * visitAssignment()
     * 
     * @brief calls the function visitLogicalArithmeticExpression to obtain the value of the expression
     *          that is assigned to the variable.
     * @rule assignment : ID EQUAL logicalArithmeticExpression
     *                  ;
     */
    @Override
    public String visitAssignment(AssignmentContext ctx) {
        visitLogicalArithmeticExpression(ctx.logicalArithmeticExpression());

        String id = ctx.ID().getText();
        String value = operands.pop();

        treeAddressCode += "\n" + id + " = " + value;
        //operands.push(id);
        return treeAddressCode;
    }

    /**
     * visitStatement()
     * 
     * @brief Visit statementsTypes.
     * @rule statement : TYPE statementsTypes SEMICOLON
     *                 ;
     */
    @Override
    public String visitStatement(StatementContext ctx) {
        visitStatementsTypes(ctx.statementsTypes());
        return treeAddressCode;
    }
    
    /**
     * visitStatementTypes()
     * 
     * @brief Visit children, if there is more than one declaration separated by comma, 
     *          it also visits.
     * @rule statementsTypes : ID COMMA statementsTypes
     *                       | ID
     *                       | assignamentInStatement COMMA statementsTypes
     *                       | assignamentInStatement
     *                       ;
     */
    @Override
    public String visitStatementsTypes(StatementsTypesContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }
    
    /**
     * visitAssignmentInsStatement()
     * 
     * @brief calls the function visitLogicalArithmeticExpression to obtain the value of the expression
     *          that is assigned to the variable.
     * @rule assignamentInStatement : ID EQUAL logicalArithmeticExpression
     *                              ;
     */
    @Override
    public String visitAssignamentInStatement(AssignamentInStatementContext ctx) {
        visitLogicalArithmeticExpression(ctx.logicalArithmeticExpression());

        String id = ctx.ID().getText();
        String value = operands.pop();

        treeAddressCode += "\n" + id + " = " + value;
        //operands.push(id);
        return treeAddressCode;
    }

    /**
     * visitReturnStatement()
     * 
     * @brief calls the function visitLogicalArithmeticExpression to get the value of the expression
     *          that is returned and this value is pushed onto the stack.
     * @rule returnStatement : RETURN logicalArithmeticExpression SEMICOLON
     *                       ;
     */
    @Override
    public String visitReturnStatement(ReturnStatementContext ctx) {
        visitLogicalArithmeticExpression(ctx.logicalArithmeticExpression());

        String value = operands.pop();

        treeAddressCode += "\npush " + value;

        return treeAddressCode;
    }

    /**
     * visitWhileStatement()
     * 
     * @brief First, generates the entry label. Then visits logicalArithmeticExpression node to get the condition;
     *          if the condition is 1 (true) skip the jump instruction to te output label. If the condition is 0
     *          (false) execute the jump instruction to the output label.
     *        After this the while instructions are added.
     *        Finally, a jump is added to the entry label (to make the loop) and followed by the output label (to exit
     *          the loop).
     * @rule whileStatement : WHILE PARENTHESES_O logicalArithmeticExpression PARENTHESES_C (instruction | SEMICOLON)
     *                      ;
     */
    @Override
    public String visitWhileStatement(WhileStatementContext ctx) {
        
        String entryLabel = labelGenerator.getNewLabel();
        treeAddressCode += "\n" + entryLabel;

        visitLogicalArithmeticExpression(ctx.logicalArithmeticExpression());
        String condition = operands.pop();
        treeAddressCode += "\nsnz " + condition; 

        String outLabel = labelGenerator.getNewLabel();
        treeAddressCode += "\njmp " + outLabel;

        visitInstruction(ctx.instruction());

        treeAddressCode += "\njmp " + entryLabel;
        treeAddressCode += "\n" + outLabel;

        return treeAddressCode;
    }
    

    /**
     * visitForStatement()
     * 
     * @brief This function, performs the following steps:
     *          - visits the init node in order to get all the initialization instructions
     *          - generates the entry label
     *          - Then visits condition node to get the condition; if the condition is 1 (true) 
     *             skip the jump instruction to te output label. If the condition is 0
     *             (false) execute the jump instruction to the output label.
     *          - The for instructions are added by visiting the instruction node.
     *          - visits the update node in order to get all the update instructions
     *          - Finally, a jump is added to the entry label (to make the loop) and followed 
     *             by the output label (to exit the loop).
     * @rule forStatement : FOR PARENTHESES_O init condition update PARENTHESES_C (instruction | SEMICOLON)
     *                    ;
     */
    @Override
    public String visitForStatement(ForStatementContext ctx) {
        
        visitInit(ctx.init());
        
        String entryLabel = labelGenerator.getNewLabel();
        treeAddressCode += "\n" + entryLabel;

        visitCondition(ctx.condition());
        
        String condition = operands.pop();
        treeAddressCode += "\nsnz " + condition; 

        String outLabel = labelGenerator.getNewLabel();
        treeAddressCode += "\njmp " + outLabel;

        visitInstruction(ctx.instruction());

        visitUpdate(ctx.update());

        treeAddressCode += "\njmp " + entryLabel;
        treeAddressCode += "\n" + outLabel;

        return treeAddressCode;
    }

    /**
     * visitInit()
     * 
     * @brief visit children
     * @rule init : statement
     *            | assignments SEMICOLON
     *            | SEMICOLON
     *            ;
     */
    @Override
    public String visitInit(InitContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }

    /**
     * visitCondition()
     * 
     * @brief visit children
     * @rule condition : logicalArithmeticExpression SEMICOLON
     *                 | SEMICOLON
     *                 ;
     */
    @Override
    public String visitCondition(ConditionContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }

    /**
     * visitUpdate()
     * 
     * @brief visit children
     * @rule update : logicalArithmeticExpression COMMA update
     *              | logicalArithmeticExpression
     *              | assignments
     *              |
     *              ;
     */
    @Override
    public String visitUpdate(UpdateContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }
    
    /**
     * visitFunctionStatement()
     * 
     * @brief Visit functionDeclaration node to insert the input label, return address and parameters.
     *        Then it visits the instructions node to add the function's instructions.
     *        Finally adds a jump to the return address.
     * @rule functionStatement : functionDeclaration compoundInstruction
     *                         | functionPrototype
     *                         ;
     */
    @Override
    public String visitFunctionStatement(FunctionStatementContext ctx) {
        if(ctx.getChild(0) instanceof FunctionDeclarationContext) {
            visitFunctionDeclaration(ctx.functionDeclaration());
            visitInstructions(ctx.compoundInstruction().instructions());
            treeAddressCode += "\njmp returnAddress\n";
        }
        return treeAddressCode;
    }

    /**
     * visitFunctionDeclaration()
     * 
     * @brief This function performs the following steps:
     *          - Adds the entry label (same as function ID)
     *          - Add the returnAddress
     *          - If the function has parameters, visits the corresponding nodes.
     * @rule functionDeclaration : TYPE ID PARENTHESES_O parameters PARENTHESES_C
     *                           ;
     */
    @Override
    public String visitFunctionDeclaration(FunctionDeclarationContext ctx) {

        String entryLabel = ctx.ID().getText();
        treeAddressCode += "\n" + entryLabel;
        treeAddressCode += "\nreturnAddress = pop";

        if(ctx.parameters().ID() != null)
            visitParameters(ctx.parameters());

        return treeAddressCode;
    }

    /**
     * visitParameters()
     * 
     * @brief Adds all function parameters.
     * @rule parameters : TYPE ID
     *                  | TYPE ID COMMA parameters
     *                  |
     *                  ;
     */
    @Override
    public String visitParameters(ParametersContext ctx) {
        treeAddressCode += "\n" + ctx.ID().getText() + " = pop" ;
        visitChildren(ctx);
        return treeAddressCode;
    }
    
    

}