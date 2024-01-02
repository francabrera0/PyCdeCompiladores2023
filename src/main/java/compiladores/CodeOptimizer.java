package compiladores;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CodeOptimizer {
    
    private String initialCode;
    private String optimizedCode;
    private String filePath = "./optimizedIntermediateCode.log";

    public CodeOptimizer(String initialCode) {
        this.initialCode = initialCode;
        this.optimizedCode = "";
    }

    public String optimize() {
        System.out.println("\n-------------------\n<<Optimizer begin>>\n-------------------");

        optimizedCode = removeUnnecessaryAssignmets(initialCode);

        System.out.println("\n-------------------\n<<Optimizer end>>\n-------------------");

        File file = new File(filePath);
        if(file.exists())
            file.delete();
        
        try(FileWriter fileWriter = new FileWriter(filePath)){
            fileWriter.write(optimizedCode);
        } catch(IOException e) {
            e.printStackTrace();
        }

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
