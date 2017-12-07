/* Type Map */
import java.util. *;

public class TypeMap extends HashMap<Variable, Type>{
    public void display(){
        System.out.println(this.entrySet());    //get all <Variable, Type>
    }

    //TypeMap is implemented Java HashMap.
    //'display' method to facilitate experimentation.
}
