/*Type check*/
import java.util. *;

public class StaticTypeCheck {
    //put <Variable, Type> in TypeMap
    public static TypeMap typing (Declarations d){
        TypeMap map = new TypeMap();
        for(Declaration i : d){
            //Variable v, Type t
            map.put(i.v, i.t);
        }
        return map;
    }

    //check error
    public static void check(boolean test, String msg){
        if(test){
            return;
        }
        System.out.println(msg);
        System.exit(1);
    }

    //check duplicate declaration
    //i, char / i, int를 다른 것으로 보는가??
    //d = (v, t)
    public static void V(Declarations d){
        for (int i=0; i<d.size()-1; i++){
            for (int j=i+1; j<d.size(); j++){
                Declaration di = d.get(i);
                Declaration dj = d.get(j);
                check(!(di.v.equals(dj.v)), "duplicate declaration :" + dj.v);
            }
        }
    }

    //valid Expression
    public static void V(Expression e, TypeMap map) {
        if (e instanceof Value) {
            return;
        }
        if (e instanceof Variable){
            Variable v = (Variable)e;
            check(map.containsKey(v), "undeclared variable : "+v);
            return;
        }
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Type type1 = typeOf(b.term1, map);
            Type type2 = typeOf(b.term2, map);
            V(b.term1, map);
            V(b.term2, map);
            //Arithmetic +,-,*,/
            if (b.op.ArithmeticOp()) {
                //type equality
                check(type1 == type2 && (type1 == Type.INT || type1 == Type.FLOAT), "type error for " + b.op);
            }
            else if (b.op.RelationalOp()) {
                //type equal
                check(type1 == type2, "type error for" + b.op);
            }
            else if (b.op.BooleanOp()) {
                check(type1 == Type.BOOL && type2 == Type.BOOL,
                        "type error for" + b.op);
            } else{
              throw new IllegalArgumentException("should never reach here BinaryOp error");
            }
            return;
        }
        if(e instanceof  Unary){
            Unary u = (Unary) e;
            Type type = typeOf(u.term,map);
            V(u.term,map);
            if(u.op.NegateOp()){
                check((type == Type.INT) || (type == Type.FLOAT), "type error for NegateOp" + u.op);
            }else if(u.op.NotOp()){
                check((type==Type.BOOL), "type error for NotOp" + u.op);
            }else {
                throw new IllegalArgumentException("should never reach here UnaryOp error");
            }
            return;
        }
        throw new IllegalArgumentException("should never reach here");
    }

    //valid Statement
    public static void V(Statement s, TypeMap map){
        if(s == null){
            throw new IllegalArgumentException("AST error:null statement");
        }
        else if(s instanceof Skip) {
            return;
        }
        else if(s instanceof Assignment) {
            Assignment a = (Assignment) s;
            //check exist variable
            check(map.containsKey(a.target), "undefined target in assignment : " + a.target);
            V(a.source, map);
            Type targetType = (Type)map.get(a.target);
            Type sourceType = typeOf(a.source, map);
            if(targetType != sourceType){
                if(targetType == Type.FLOAT){
                    check(sourceType == Type.INT, "mixed mode assigned to " + a.target);
                }else if(targetType == Type.INT){
                    check(sourceType == Type.CHAR, "mixed mode assigned to" + a.target);
                }else{
                    check(false, "mixed mode assignment to" + a.target);
                }
            }
            return;
        }
        else if(s instanceof Conditional) {
            Conditional c = (Conditional) s;
            V(c.test,map);
            Type testType = typeOf(c.test, map);
            if(testType == Type.BOOL){
                V(c.thenbranch, map);
                V(c.elsebranch, map);
                return;
            }else{
                check(false, "poorly typed if in Conditional : " + c.test);
            }
        }
        else if(s instanceof Loop){
            Loop l = (Loop) s;
            V(l.test, map);
            Type testType = typeOf(l.test,map);
            if(testType == Type.BOOL){
                V(l.body, map);
                return;
            }else {
                check(false, "poorly typed test in while Loop in Conditional :" + l.test);
            }
        }
        else if(s instanceof Block){
            Block b = (Block) s;
            for(Statement i : b.members){
                V(i,map);
            }
        }
        else{
            throw new IllegalArgumentException("should never reach here");
        }
    }

    //check defined variable
    public static Type typeOf(Expression e, TypeMap map){
        if(e instanceof Value){
            return ((Value)e).type;
        }
        if(e instanceof Variable) {
            Variable v = (Variable) e;
            check (map.containsKey(v), "undefined variable : " + v);
            return(Type) map.get(v);
        }
        if(e instanceof Binary){
            Binary b = (Binary) e;
            if(b.op.ArithmeticOp()){
                if(typeOf(b.term1, map) == Type.FLOAT){
                    return Type.FLOAT;
                }
                else {
                    return Type.INT;
                }
            }
            if(b.op.BooleanOp() || b.op.RelationalOp()){
                return (Type.BOOL);
            }
        }
        if(e instanceof  Unary){
            Unary u = (Unary) e;
            if(u.op.NotOp()){
                return Type.BOOL;
            }
            else if(u.op.NegateOp()){
                return typeOf(u.term,map);
            }
            else if(u.op.intOp()){
                return Type.INT;
            }
            else if(u.op.floatOp()){
                return  Type.FLOAT;
            }
            else if(u.op.charOp()){
                return Type.CHAR;
            }
        }
        throw new IllegalArgumentException("should never reach here");
    }

    //valid Programs
    public static void V(Program p){
        V(p.decpart);
        //check duplicate variance
        V(p.body, typing(p.decpart));   //body, declaration map
        //V(statement, map)
    }

    //main
    public static void main(String args[]){
        Parser parser  = new Parser(new Lexer("test.txt"));
        Program program = parser.program();
        program.display(1);

        System.out.println("Beginning type checking...");
        System.out.print("TypeMap : ");
        //Declarations decpart
        TypeMap map = typing(program.decpart);
        map.display();
        V(program);
        System.out.println(map);
    }
}
