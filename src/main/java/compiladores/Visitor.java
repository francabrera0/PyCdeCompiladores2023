package compiladores;

import compiladores.compiladoresParser.InstructionContext;
import compiladores.compiladoresParser.InstructionsContext;
import compiladores.compiladoresParser.ProgramContext;

public class Visitor extends compiladoresBaseVisitor<String> {

    @Override
    public String visitProgram(ProgramContext ctx) {
        System.out.println("-->Visit Program<--");
        return super.visitProgram(ctx);
    }
    
    @Override
    public String visitInstructions(InstructionsContext ctx) {
        System.out.println("-- Visit instructions --");
        System.out.println("-- Childrens: " + ctx.getChildCount() + " --");
        System.out.println("-- Text: " + ctx.getText() + " --");
        return super.visitInstructions(ctx);
    }
    
    @Override
    public String visitInstruction(InstructionContext ctx) {
        System.out.println("## Visit instruction ##");
        System.out.println("## Childrens: " + ctx.getChildCount() + " ##");
        System.out.println("## Text: " + ctx.getText() + " ##");
        return super.visitInstruction(ctx);
    }

    
    
}
