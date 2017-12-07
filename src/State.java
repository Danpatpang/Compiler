/*TypeMap state*/
import java.util.*;

public class State extends HashMap<Variable, Value>{
    public State() {}

    public State(Variable key, Value value){
        put(key, value);
    }

    public State onion(Variable key, Value value){
        put(key, value);
        return this;
    }

    public State onion(State t){
        for (Variable key : t.keySet()){
            put(key, t.get(key));
        }
        return this;
    }
}
//Defines the set of variables and their associated values
//that are active during interpretation