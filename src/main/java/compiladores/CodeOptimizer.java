package compiladores;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stringtemplate.v4.compiler.STParser.mapExpr_return;

public class CodeOptimizer {
    
    private String initialCode;
    private String optimizedCode;
    private String filePathOut = "./optimizedIntermediateCode.log";

    public CodeOptimizer(String initialCode) {
        this.initialCode = initialCode;
        this.optimizedCode = "";
    }

    public String optimize() {
        System.out.println("\n-------------------\n<<Optimizer begin>>\n-------------------");

        optimizedCode = removeUnnecessaryAssignmets(initialCode);

        System.out.println("\n-------------------\n<<Optimizer end>>\n-------------------");
        //Write code into a file
        return optimizedCode;
    }

    public String removeUnnecessaryAssignmets(String code) {
        String optCode = "";

        String[] lines = code.split("\n");

        Pattern pattern = Pattern.compile("\\b([a-zA-Z0-9]+)\\s?=\\s?t(\\d+)\\b");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if(matcher.find()) {
                String left = matcher.group(1);
                String right = "t"+matcher.group(2);
                optCode = optCode.replace(right, left);
            }
            else {
                optCode += line + "\n";
            }
        }

        return optCode;
    }
}
