package compiladores;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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
        list.add(new HashMap<String, ID>());
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



}
