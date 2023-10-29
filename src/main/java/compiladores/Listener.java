package compiladores;

import org.antlr.v4.runtime.tree.TerminalNode;

import compiladores.compiladoresParser.CompoudInstructionContext;
import compiladores.compiladoresParser.ProgramContext;

public class Listener extends compiladoresBaseListener{
    private Integer contexto = 0;

    @Override
    public void enterCompoudInstruction(CompoudInstructionContext ctx) {
        contexto++;
        System.out.println("Nuevo contexto " + contexto);
    }

    @Override
    public void exitCompoudInstruction(CompoudInstructionContext ctx) {
        contexto--;
        System.out.println("Fin de contexto " + contexto);
    }

    @Override
    public void enterProgram(ProgramContext ctx) {
        System.out.println("Comienza compilación |" + ctx.getText() + "|");
    }


    @Override
    public void exitProgram(ProgramContext ctx) {
        System.out.println("Fin compilación |" + ctx.getText() + "|");
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        System.out.println("-- TOKEN --> |" + node.getText() + "|");
    }
    
}
