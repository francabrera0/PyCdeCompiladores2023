package compiladores;

import java.util.LinkedList;

public class Function extends ID {

    private LinkedList<DataType> args;
    private Boolean isPrototype;

    public Function (String name, DataType dataType, Boolean used, Boolean initialized) {
        super.name = name;
        super.dataType = dataType;
        super.used = used;
        super.initialized = initialized;
        this.args = new LinkedList<DataType>();
        this.isPrototype = false;
    }

    public void setArgs(LinkedList<DataType> args) {
        this.args = args;
    }

    public LinkedList<DataType> getArgs() {
        return this.args;
    }

    public void addArg(DataType arg) {
        args.add(arg);
    }

    public void setIsPrototype(Boolean isPrototype) {
        this.isPrototype = isPrototype;
    }

    public Boolean getIsPrototype() {
        return this.isPrototype;
    }
    
}
