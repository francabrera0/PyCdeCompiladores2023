package compiladores;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class App {
    public static void main(String[] args) throws Exception {

        CharStream input = CharStreams.fromFileName("input/input.c");

        // create a lexer that feeds off of input CharStream
        compiladoresLexer lexer = new compiladoresLexer(input);
        
        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        // create a parser that feeds off the tokens buffer
        compiladoresParser parser = new compiladoresParser(tokens);
                
        // create Listener
        compiladoresBaseListener listener = new Listener();

        // Conecto el objeto con Listeners al parser
        parser.addParseListener(listener);

        // Solicito al parser que comience indicando una regla gramatical
        // En este caso la regla es el simbolo inicial
        ParseTree tree = null;
        try {
            tree = parser.program(); //Árbol con anotaciones
            // parser.program(); //Árbol con anotaciones
            
            // Conectamos el visitor
            Visitor visitor = new Visitor();
            String treeAddressCode = visitor.visit(tree); 

            CodeOptimizer codeOptimizer = new CodeOptimizer(treeAddressCode);
            String optimizedCode = codeOptimizer.optimize();

            System.out.println(optimizedCode);
            // System.out.println(visitor.getErrorNodes());
            // Imprime el arbol obtenido
            // System.out.println(tree.toStringTree(parser));
            // System.out.println(escucha);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            // e.printStackTrace();
            System.exit(1);
        }


        
    }
}
