package compiladores;

import java.util.LinkedList;

public class LabelGenerator {
    private static LabelGenerator instance;
    private int numberOfLabels;
    private LinkedList<String> labels;

    private LabelGenerator() {
        this.numberOfLabels = 0;
        this.labels = new LinkedList<String>();
    }

    public static LabelGenerator getInstanceOf() {
        if(instance == null)
            instance = new LabelGenerator();
        
            return instance;
    }

    public int getNumberOfLabels() {
        return numberOfLabels;
    }

    public LinkedList<String> getLabels() {
        return labels;
    }    

    public String getLastLabel() {
        return labels.getLast();
    }

    public String getNewLabel(String labelType) {
        String newLabel = "l" + labelType+ numberOfLabels++;
        labels.add(newLabel);

        return newLabel;
    }
    
}
