package compiladores;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


public class SymbolTable {
    private LinkedList<Map<String, ID>> list;    
    private static SymbolTable instance;


    private SymbolTable() {
        this.list = new LinkedList<Map<String, ID>>();
    }

    public static SymbolTable getInstanceOf() {
        if(instance == null) 
            instance = new SymbolTable();

        return instance;
    }

    public void addContext() {
        list.add(new LinkedHashMap<String, ID>());
    }

    public void delContext() {
        list.removeLast();
    }

    public void addSymbol(ID id) {
        list.getLast().put(id.getName(), id);
    }

    public ID searchSymbol(String name) {
        Iterator<Map<String, ID>> iterator = list.descendingIterator();

        while(iterator.hasNext()) {
            Map<String,ID> context = iterator.next();
            
            if(context.containsKey(name))
                return context.get(name);
        }
        return null;
    }

    public ID searchLocalSymbol(String name) {
        if (list.getLast().containsKey(name))
            return list.getLast().get(name);
        return null;
    }

    public void printSymbolTable() {
        System.out.println("--------------------->Symbol Table<---------------------");
        int contextNumber = 1;
        for (Map<String, ID> context : list) {
            System.out.println("Context " + contextNumber + ":");
            for (Map.Entry<String, ID> entry : context.entrySet()) {
                ID id = entry.getValue();

                if (id instanceof Variable) {
                    System.out.println("    Variable: " + entry.getKey() + " : type ->" + id.getDataType() + ", used ->" + id.getUsed() + ", initialized ->" + id.getInitialized());
                } 
                
                else if (id instanceof Function) {
                    Function function = (Function) id;
                    System.out.print("    Function: " + entry.getKey() + " : type ->" + function.getDataType()+ ", used ->" + function.getUsed() + ", initialized ->" + function.getInitialized() + ", args ->(");
                    LinkedList <Parameter> args = function.getArgs();
                    for(Parameter arg : args) {
                        System.out.print(arg.getName() + " -> " +  arg.getDataType().toString()+ ", ");
                    }
                    System.out.println(")");
                }
            }
            contextNumber++;
        }
    }

    public List<String> getUnusedID(){
        List<String> unusedList = new ArrayList<String>();

        for(Map.Entry<String, ID> entry: list.getLast().entrySet()) {
            if(!entry.getValue().getUsed()) {
                unusedList.add(entry.getKey());
            }
        }

        return unusedList;
    }

}
