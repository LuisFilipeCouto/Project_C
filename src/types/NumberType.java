package types;

public class NumberType extends Type{
    public NumberType(){
        super("number");
    }
    
    public boolean isNumeric() {
        return true;
     }
}