import java.util.*;
public class TypeTransformer {
    //Expression
    public static Expression T(Expression e, TypeMap map){
        if(e instanceof Value){     //value
            return e;
        }

        if(e instanceof Variable){     //variable
            return e;
        }

        if(e instanceof Binary){        //binary
            Binary b = (Binary)e;
            Type type1 = StaticTypeCheck.typeOf(b.term1, map);
            Type type2 = StaticTypeCheck.typeOf(b.term2, map);
            Expression t1 = T(b.term1, map);
            Expression t2 = T(b.term2, map);

            if(type1 == Type.INT){
                return new Binary(b.op.intMap(b.op.val),t1,t2);
            }
            else if(type1 == Type.FLOAT){
                return new Binary(b.op.floatMap(b.op.val),t1,t2);
            }
            else if(type1 == Type.CHAR){
                return new Binary(b.op.charMap(b.op.val),t1,t2);
            }
            else if(type1 == Type.BOOL){
                return new Binary(b.op.boolMap(b.op.val),t1,t2);
            }
            throw new IllegalArgumentException("should never reach here");
        }
        if(e instanceof Unary){
            Unary u = (Unary) e;
            Type type = StaticTypeCheck.typeOf(u.term, map);
            Expression t = T(u.term, map);

            if(type == Type.BOOL && u.op.NotOp()){
                return new Unary(u.op.boolMap(u.op.val), t);
            }
            else if(type == Type.INT && u.op.NegateOp()){
                return new Unary(u.op.intMap(u.op.val), t);
            }
            else if(type == Type.FLOAT && u.op.NegateOp()){
                return new Unary(u.op.floatMap(u.op.val), t);
            }
            /*
            else if ((u.op.I2F) || (u.op.I2C)) {
                if (type == Type.INT)
                    return new Unary(u.op.intMap(u.op.val), t);}
            else if ((type == Type.FLOAT) && ((u.op.F2I) == TRUE))
                return new Unary(u.op.floatMap(u.op.val), t);
            else if ((type == Type.CHAR) && (u.op.C2I) == TRUE)
                return new Unary(u.op.charMap(u.op.val), t);
             */
        }

        throw new IllegalArgumentException("Malformed Unary expression");
    }

    //Statement
    public static Statement T(Statement s, TypeMap map){
        if(s instanceof Skip){
            return s;
        }

        if(s instanceof Assignment){
            Assignment a = (Assignment) s;
            Variable target = a.target;
            Expression source = a.source;
            Type targetType = StaticTypeCheck.typeOf(a.target, map);
            Type sourceType = StaticTypeCheck.typeOf(a.source, map);

            if(targetType == Type.INT){
                if(sourceType == Type.CHAR){
                    source = new Unary(new Operator(Operator.C2I), source);
                    sourceType = Type.INT;
                }
            }
            else if(targetType == Type.FLOAT){
                if(sourceType == Type.INT){
                    source = new Unary(new Operator(Operator.I2F), source);
                    sourceType = Type.FLOAT;
                }
            }
            StaticTypeCheck.check(targetType == sourceType, "bug in assignment to "+target);
            return new Assignment(target, source);
        }

        if(s instanceof Conditional){
            Conditional c = (Conditional)s;
            Expression test = T(c.test, map);
            Statement thenbarnch = T (c.thenbranch, map);
            Statement elsebranch = T (c.elsebranch, map);
            return new Conditional(test, thenbarnch, elsebranch);
        }
        if(s instanceof Loop){
            Loop l = (Loop) s;
            Expression test = T(l.test, map);
            Statement body = T(l.body, map);
            return new Loop(test, body);
        }
        if(s instanceof Block){
            Block b = (Block) s;
            Block out = new Block();
            for(Statement statement : b.members){
                out.members.add(T(statement, map));
            }
            return out;
        }
        throw new IllegalArgumentException("should never reach here");
    }

    //Program
    public static Program T(Program p, TypeMap map){
        Block body = (Block) T(p.body,map);
        return new Program(p.decpart, body);
    }

    public static void main(String args[]){
        Parser parser = new Parser(new Lexer("test.txt"));
        Program program = parser.program();
        program.display(1);
        System.out.println("\nBegin type checking..");
        System.out.println("Type map : ");
        TypeMap map = StaticTypeCheck.typing(program.decpart);  //put declarations
        map.display();
        //StaticTypeCheck.V(program);
        Program out = T(program, map);
        System.out.println("Output ASt");
        out.display(1);
    }
}
