package compiladores;


import java.util.LinkedList;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import compiladores.compiladoresParser.AfContext;
import compiladores.compiladoresParser.ArithmeticExpressionContext;
import compiladores.compiladoresParser.ArithmeticTermContext;
import compiladores.compiladoresParser.AssignamentInStatementContext;
import compiladores.compiladoresParser.AssignmentContext;
import compiladores.compiladoresParser.AssignmentsContext;
import compiladores.compiladoresParser.AtContext;
import compiladores.compiladoresParser.CallParametersContext;
import compiladores.compiladoresParser.CompoundInstructionContext;
import compiladores.compiladoresParser.ConditionContext;
import compiladores.compiladoresParser.ElseIfStatementContext;
import compiladores.compiladoresParser.FactorContext;
import compiladores.compiladoresParser.ForStatementContext;
import compiladores.compiladoresParser.FunctionCallContext;
import compiladores.compiladoresParser.FunctionDeclarationContext;
import compiladores.compiladoresParser.FunctionPrototypeContext;
import compiladores.compiladoresParser.FunctionStatementContext;
import compiladores.compiladoresParser.IfStatementContext;
import compiladores.compiladoresParser.IncDecContext;
import compiladores.compiladoresParser.InitContext;
import compiladores.compiladoresParser.InstructionContext;
import compiladores.compiladoresParser.InstructionsContext;
import compiladores.compiladoresParser.LogicalArithmeticExpressionContext;
import compiladores.compiladoresParser.LogicalExpressionContext;
import compiladores.compiladoresParser.ParameterContext;
import compiladores.compiladoresParser.ParametersContext;
import compiladores.compiladoresParser.ParametersPrototypeContext;
import compiladores.compiladoresParser.ProgramContext;
import compiladores.compiladoresParser.ReturnStatementContext;
import compiladores.compiladoresParser.StatementContext;
import compiladores.compiladoresParser.StatementsTypesContext;
import compiladores.compiladoresParser.UpdateContext;
import compiladores.compiladoresParser.WhileStatementContext;

public class Visitor extends compiladoresBaseVisitor<String> {

    private String treeAddressCode;
    private String incDecInstruction; //Used to store increment or decrement instruction
    private int preOrPost; //0->None, 1->Pre, 2->Post
    private VariableGenerator variableGenerator;
    private LabelGenerator labelGenerator;
    private LinkedList<String> operands;

    public Visitor() {
        treeAddressCode = "";
        incDecInstruction = "";
        preOrPost = 0;
        variableGenerator = VariableGenerator.getInstanceOf();
        labelGenerator = LabelGenerator.getInstanceOf();
        operands = new LinkedList<>();
    }


    @Override
    public String visitProgram(ProgramContext ctx) {
        visitChildren(ctx);

        return treeAddressCode;
    }
    
    @Override
    public String visitInstructions(InstructionsContext ctx) {
        visitChildren(ctx);

        return treeAddressCode;
    }
    
    @Override
    public String visitInstruction(InstructionContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }

    @Override
    public String visitLogicalArithmeticExpression(LogicalArithmeticExpressionContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }
    @Override
    public String visitLogicalExpression(LogicalExpressionContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }

    @Override
    public String visitArithmeticExpression(ArithmeticExpressionContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }
    
    @Override
    public String visitArithmeticTerm(ArithmeticTermContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }

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

    @Override
    public String visitIncDec(IncDecContext ctx) {
        if(ctx.getChild(0).getText().contains("+") || ctx.getChild(0).getText().contains("-") ) //pre
            preOrPost = 1;
        else //post
            preOrPost = 2;
        System.out.println(ctx.getChild(0).getText());
        String id = ctx.ID().getText();

        incDecInstruction += "\n" + id + " = " + id;

        if(ctx.INCDECOPERATORS().getText().equals("++"))
            incDecInstruction += " + 1";
        else
            incDecInstruction += " - 1";

        operands.push(id);
        return treeAddressCode;
    }

    @Override
    public String visitFunctionCall(FunctionCallContext ctx) {
        System.out.println("Function call");
        return treeAddressCode;
    }
   

    @Override
    public String visitAf(AfContext ctx) {

        String firstOperand = operands.pop();

        if(ctx.factor() != null) {
            visitFactor(ctx.factor());
        }
        
        if(preOrPost == 1) //pre
            treeAddressCode += incDecInstruction;
        
        String newVariable = variableGenerator.getNewVariable();

        treeAddressCode += "\n" + newVariable + " = " + firstOperand; 

        if(ctx.getChild(0) != null){
            treeAddressCode += ctx.getChild(0).getText();
        }

        treeAddressCode += operands.pop();

        if(preOrPost == 2) //post
            treeAddressCode+= incDecInstruction;

        preOrPost = 0;
        incDecInstruction = "";

        operands.push(newVariable);

        if(ctx.af().af() != null) {
            visitAf(ctx.af());
        }

        return treeAddressCode;
    }


    @Override
    public String visitAt(AtContext ctx) {
        return treeAddressCode;    
    }



    /* @Override
    public String visitAssignamentInStatement(AssignamentInStatementContext ctx) {
        // TODO Auto-generated method stub
        return super.visitAssignamentInStatement(ctx);
    }
 */

    /* @Override
    public String visitAssignment(AssignmentContext ctx) {
        // TODO Auto-generated method stub
        return super.visitAssignment(ctx);
    }
 */

    /* @Override
    public String visitAssignments(AssignmentsContext ctx) {
        // TODO Auto-generated method stub
        return super.visitAssignments(ctx);
    }
 */

    /* @Override
    public String visitCallParameters(CallParametersContext ctx) {
        // TODO Auto-generated method stub
        return super.visitCallParameters(ctx);
    }
 */

    /* @Override
    public String visitCompoundInstruction(CompoundInstructionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitCompoundInstruction(ctx);
    }
 */

    /* @Override
    public String visitCondition(ConditionContext ctx) {
        // TODO Auto-generated method stub
        return super.visitCondition(ctx);
    }
 */

    /* @Override
    public String visitElseIfStatement(ElseIfStatementContext ctx) {
        // TODO Auto-generated method stub
        return super.visitElseIfStatement(ctx);
    }
 */

    /* @Override
    public String visitForStatement(ForStatementContext ctx) {
        // TODO Auto-generated method stub
        return super.visitForStatement(ctx);
    }
 */



    /* @Override
    public String visitFunctionDeclaration(FunctionDeclarationContext ctx) {
        // TODO Auto-generated method stub
        return super.visitFunctionDeclaration(ctx);
    }
 */

    /* @Override
    public String visitFunctionPrototype(FunctionPrototypeContext ctx) {
        // TODO Auto-generated method stub
        return super.visitFunctionPrototype(ctx);
    }
 */

    /* @Override
    public String visitFunctionStatement(FunctionStatementContext ctx) {
        // TODO Auto-generated method stub
        return super.visitFunctionStatement(ctx);
    }
 */

    /* @Override
    public String visitIfStatement(IfStatementContext ctx) {
        // TODO Auto-generated method stub
        return super.visitIfStatement(ctx);
    }
 */



    /* @Override
    public String visitInit(InitContext ctx) {
        // TODO Auto-generated method stub
        return super.visitInit(ctx);
    }
 */

    /* @Override
    public String visitParameter(ParameterContext ctx) {
        // TODO Auto-generated method stub
        return super.visitParameter(ctx);
    }
 */

    /* @Override
    public String visitParameters(ParametersContext ctx) {
        // TODO Auto-generated method stub
        return super.visitParameters(ctx);
    }
 */

    /* @Override
    public String visitParametersPrototype(ParametersPrototypeContext ctx) {
        // TODO Auto-generated method stub
        return super.visitParametersPrototype(ctx);
    }
 */

    /* @Override
    public String visitReturnStatement(ReturnStatementContext ctx) {
        // TODO Auto-generated method stub
        return super.visitReturnStatement(ctx);
    }
 */

    /* @Override
    public String visitStatement(StatementContext ctx) {
        // TODO Auto-generated method stub
        return super.visitStatement(ctx);
    }
 */

    /* @Override
    public String visitStatementsTypes(StatementsTypesContext ctx) {
        // TODO Auto-generated method stub
        return super.visitStatementsTypes(ctx);
    }
 */

    /* @Override
    public String visitUpdate(UpdateContext ctx) {
        // TODO Auto-generated method stub
        return super.visitUpdate(ctx);
    } */


    /* @Override
    public String visitWhileStatement(WhileStatementContext ctx) {
        // TODO Auto-generated method stub
        return super.visitWhileStatement(ctx);
    } */


    /* @Override
    protected String aggregateResult(String aggregate, String nextResult) {
        // TODO Auto-generated method stub
        return super.aggregateResult(aggregate, nextResult);
    } */


    /* @Override
    protected String defaultResult() {
        // TODO Auto-generated method stub
        return super.defaultResult();
    } */


    /* @Override
    protected boolean shouldVisitNextChild(RuleNode node, String currentResult) {
        // TODO Auto-generated method stub
        return super.shouldVisitNextChild(node, currentResult);
    } */


    /* @Override
    public String visit(ParseTree tree) {
        // TODO Auto-generated method stub
        return super.visit(tree);
    } */


    /* @Override
    public String visitChildren(RuleNode node) {
        // TODO Auto-generated method stub
        return super.visitChildren(node);
    } */


    /* @Override
    public String visitErrorNode(ErrorNode node) {
        // TODO Auto-generated method stub
        return super.visitErrorNode(node);
    } */


    /* @Override
    public String visitTerminal(TerminalNode node) {
        // TODO Auto-generated method stub
        return super.visitTerminal(node);
    } */


    /* @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    } */


    /* @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return super.equals(obj);
    } */


    /* @Override
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        super.finalize();
    } */


    /* @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    } */


    /* @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    } */
    
}