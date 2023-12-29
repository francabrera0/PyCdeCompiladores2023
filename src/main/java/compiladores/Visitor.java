package compiladores;


import java.util.LinkedList;

import compiladores.compiladoresParser.AfContext;
import compiladores.compiladoresParser.ArithmeticExpressionContext;
import compiladores.compiladoresParser.ArithmeticTermContext;
import compiladores.compiladoresParser.AtContext;
import compiladores.compiladoresParser.FactorContext;
import compiladores.compiladoresParser.FunctionCallContext;
import compiladores.compiladoresParser.IncDecContext;
import compiladores.compiladoresParser.InstructionContext;
import compiladores.compiladoresParser.InstructionsContext;
import compiladores.compiladoresParser.LogicalArithmeticExpressionContext;
import compiladores.compiladoresParser.LogicalExpressionContext;
import compiladores.compiladoresParser.ProgramContext;

public class Visitor extends compiladoresBaseVisitor<String> {

    private String treeAddressCode;
    private String incDecInstruction; //Used to store increment or decrement instruction
    private int preOrPost; //0->None, 1->Pre, 2->Post
    private VariableGenerator variableGenerator;
    //private LabelGenerator labelGenerator;
    private LinkedList<String> operands;
    private LinkedList<String> incDecID;

    public Visitor() {
        treeAddressCode = "";
        incDecInstruction = "";
        preOrPost = 0;
        variableGenerator = VariableGenerator.getInstanceOf();
        // labelGenerator = LabelGenerator.getInstanceOf();
        operands = new LinkedList<>();
        incDecID = new LinkedList<>();
    }


    /**
     * Visita el programa, raiz del arbol completo
     */
    @Override
    public String visitProgram(ProgramContext ctx) {
        visitChildren(ctx);

        return treeAddressCode;
    }
    
    /**
     * Visita a los hijos, instruction e instructions(si mismo)
     */
    @Override
    public String visitInstructions(InstructionsContext ctx) {
        visitChildren(ctx);

        return treeAddressCode;
    }
    
    /**
     * Visita a los hijos (pueden ser compoundInstruction, statement, assignments,
     *  returnStatement, ifStatement, whileStatement, forStatement, functionCall, 
     *  logicalArithmeticExpression,functionStatement)
     */
    @Override
    public String visitInstruction(InstructionContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }

    /**
     * Visita a los hijos (logicalExpression)
     * 
     * logicalArithmeticExpression : logicalExpression;
     */
    @Override
    public String visitLogicalArithmeticExpression(LogicalArithmeticExpressionContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }

    /**
     * Visita a los hijos ()
     * Tengo que trabajar con esta regla ahora para agregar los and or o cmp    
     * 
     * logicalExpression : logicalExpression AND logicalExpression
     *                   | logicalExpression OR logicalExpression
     *                   | arithmeticExpression CMP arithmeticExpression
     *                   | arithmeticExpression
     *                   ; 
     */
    @Override
    public String visitLogicalExpression(LogicalExpressionContext ctx) {
                
        if(ctx.getChild(1) == null) { //En este caso es unicamente una opal. Creo una nuev var
            visitArithmeticExpression(ctx.arithmeticExpression(0)); 

            String newVariable = variableGenerator.getNewVariable();
            
            if(preOrPost == 1) //pre
                treeAddressCode += incDecInstruction;
            
            treeAddressCode += "\n" + newVariable + " = " + operands.pop(); 

            if(preOrPost == 2) //post
                treeAddressCode+= incDecInstruction;

            preOrPost = 0;
            incDecInstruction = "";

            operands.push(newVariable); 
        }
        else {
            if(ctx.getChild(1).getText().equals("&&") || ctx.getChild(1).getText().equals("||")) { //AND/OR
                /**
                 * Lo que hace es: - Busca el primer operando
                 *                 - Este seguramente sea o una cmp o una arithm
                 *                 - Luego busca el segundo ( el segundo puede ser otra && u ||) 
                 *                 - En caso que sea otra && u || cuando hace el visit logical "vuelve a empezar" dejando en la pila de operandos el primero
                 *                 - Cuando ya no hay && u || entonces empieza a armar el tac con todos los op que fue apilando
                 *                 - Es recursivo
                 */

                visitLogicalExpression(ctx.logicalExpression(0));
                visitLogicalExpression(ctx.logicalExpression(1));

                String newVariable = variableGenerator.getNewVariable();
                String firstOperand = operands.pop();
                String secondOperand = operands.pop();
                String operator = ctx.getChild(1).getText();

                treeAddressCode += "\n" + newVariable + " = " + firstOperand + operator + secondOperand;
                operands.push(newVariable);

                return treeAddressCode;

            }

            else { //CMP solo permite comparar 2 aritmeticas

                visitArithmeticExpression(ctx.arithmeticExpression(0));//Cuando retorna tiene en operands el resultado

                visitArithmeticExpression(ctx.arithmeticExpression(1)); //Cuando retorna vuelve con el resultado
        
                String newVariable = variableGenerator.getNewVariable(); //Variable en la que se guard la comp

                String secondOperand = operands.pop();
                String firstOperand = operands.pop();

                treeAddressCode += "\n" + newVariable + " = " +  firstOperand + ctx.getChild(1).getText() + secondOperand;
                operands.push(newVariable); //Pongo en operands la variable de la comp
            }
        }

        return treeAddressCode;
    }

    /**
     * Visita a los hijos de la expAritmetica, esots son aritmeticTerm y at.
     * Aca lo que hago es poner en una nueva var el resultado de la expresion.
     * 
     * arithmeticExpression : arithmeticTerm at;
     */
    @Override
    public String visitArithmeticExpression(ArithmeticExpressionContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }
    
    /**
     * Visita a los hijos de arithmeticTerm, estos son factor, af
     * 
     * arithmeticTerm : factor af;
     */
    @Override
    public String visitArithmeticTerm(ArithmeticTermContext ctx) {
        visitChildren(ctx);
        return treeAddressCode;
    }

    /**
     * Visitar un facto quiere decir que estamos en la hoja de esta rama. En caso de que el factor
     * sea directamente el operando (ID, NUMBER, CHAR), se coloca en la lista de operandos el valor.
     * En caso de ser un incDec, functionCall u otra expresion AL, se llama al visitor correspondiente.
     * 
     * factor : NUMBER
     *        | CHARACTER
     *        | ID
     *        | PARENTHESES_O logicalArithmeticExpression PARENTHESES_C
     *        | incDec
     *        | functionCall
     *        ;
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
     * Visita incDec, verifica si es post o pre y almacena esta información en preOrPost, luego 
     * en función de si es inc o dec arma una instrucción id = id +/- 1; y la almacena en incDecInstruction
     * Estas dos variables se usan luego para insertar el inc o dec en el TAC.
     * Por último, coloca en operands el ID de la variable incrementada.
     * 
     * incDec : INCDECOPERATORS ID
     *        | ID INCDECOPERATORS
     *        ;
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
     * No implementado todavia
     */
    @Override
    public String visitFunctionCall(FunctionCallContext ctx) {
        System.out.println("Function call");
        return treeAddressCode;
    }
   
    /**
     * Visitar Af es para encontrar si hay multiplicaciones o divisiones entre factores.
     * En caso de que el contexto no tenga mas factores se retorna. Si los tiene se hace una visita
     *  y en la lista de operands retornará el nuevo operando producto de la visita. Luego se arma el TAC.
     * Por último veerifica si hay mas anidaciones.
     * 
     * af : MUL factor af
     *    | DIV factor af
     *    |
     *    ;
     */
    @Override
    public String visitAf(AfContext ctx) {

        String firstOperand;

        if(ctx.factor() != null) {
            firstOperand = operands.pop();
            visitFactor(ctx.factor()); //Return with an operand in operands list
        }
        else {
            return treeAddressCode;
        }
        
        String secondOperand = operands.pop();
        String newVariable = variableGenerator.getNewVariable();
        String operator = ctx.getChild(0).getText();
        Boolean incDec = false;

        if(preOrPost!= 0){
            String s = incDecID.pop();
            if(s.equals(firstOperand) || s.equals(secondOperand))
                incDec = true;
            else
                incDecID.push(s);
        } 

        if(preOrPost == 1 && incDec) //pre
            treeAddressCode += incDecInstruction;
        
        treeAddressCode += "\n" + newVariable + " = " + firstOperand + operator + secondOperand; 

        if(preOrPost == 2 && incDec) //post
            treeAddressCode+= incDecInstruction;

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
     * at : ADD arithmeticTerm at
     *    | SUB arithmeticTerm at
     *    |
     *    ;
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

        if(preOrPost!= 0){
            String s = incDecID.pop();
            if(s.equals(firstOperand) || s.equals(secondOperand))
                incDec = true;
            else
                incDecID.push(s);
        } 

        if(preOrPost == 1 && incDec) //pre
            treeAddressCode += incDecInstruction;
        

        treeAddressCode += "\n" + newVariable + " = " + firstOperand + operator + secondOperand; 

        if(preOrPost == 2 && incDec) //post
            treeAddressCode+= incDecInstruction;

        if(incDec) {
            preOrPost = 0;
            incDecInstruction = "";
        }
        operands.push(newVariable);

        if(ctx.at().at() != null)
            visitAt(ctx.at());

        return treeAddressCode;    
    }
    
}