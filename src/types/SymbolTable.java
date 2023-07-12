package types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SymbolTable {
    private HashMap<String,Type> symbolTable = new HashMap<>();
    private SymbolTable parent;
    private HashMap<String,SymbolTable> children = new HashMap<>();

    public SymbolTable(){
        this(null);
    }

    public SymbolTable(SymbolTable p){
        this.parent = p;
    }

    public void addSymbol(String key, Type type) {
        symbolTable.put(key, type);
    }

    public String getType(String key){
        Type type = symbolTable.get(key);
        if (type != null) {
            String strType = type.name();
            return strType;                // Found in current symbol table
        } else if (parent != null) {
            return parent.getType(key); // Check parent symbol table
        } else {
            return null;                // Not found in any symbol table
        }
    }

    public boolean contains(String key) {
        return symbolTable.containsKey(key) || (parent != null && parent.contains(key));
    }

    public boolean containsLocally(String key) {
        return symbolTable.containsKey(key);
    }

    public SymbolTable getParent() {
        return parent;
    }

    public void addChild(String key, SymbolTable child){
        children.put(key, child);
    }

    public HashMap<String,SymbolTable> getChildren() {
        return children;
    }

    public SymbolTable getChild(String key) {
        return children.get(key);
    }
}
