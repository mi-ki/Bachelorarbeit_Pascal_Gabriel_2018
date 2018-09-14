package ProductProgramCreator.Visitors;

import ProductProgramCreator.MainWeaver;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.LinkedList;

/**
 * Is used to find out if there is at least one method call of a method
 * that was defined inside this compilation Unit.
 */
public class MethodCallExprVisitor extends VoidVisitorAdapter<Object> {
    // private MainWeaver weaver;
    private boolean thereIsAMethodCall;
    private String nameOfTheFirstMethodCalled;
    private LinkedList<String> allMethods;
    private LinkedList<String> allMethodCalls;
    @Override
    public void visit(MethodCallExpr n, Object arg) {
        //Checks for the first occurrence of a call of a method that got
        // declared in this unit and collect all called method names
        String name = n.getName().asString();
        if(allMethods.contains(name)) {
            thereIsAMethodCall = true;
            nameOfTheFirstMethodCalled = name;
            allMethodCalls.add(name);
        }
        if(!allMethodCalls.isEmpty()) {
            nameOfTheFirstMethodCalled = allMethodCalls.getFirst();
            thereIsAMethodCall = true;
        }
        super.visit(n, arg);
    }

    /**
     * Checks if there has been a method call in the visited expression
     * @return  true if there was, false otherwise
     */
    public boolean isThereAMethodCall() {
        return thereIsAMethodCall;
    }

    /**
     * Returns the name of the first called method
     * (that is defined in this unit)
     * @return the name if isThereAMethodCall() yields true,
     *         otherwise an empty string.
     */
    public String methodName() {
        return nameOfTheFirstMethodCalled;
    }
    public LinkedList<String> getMethodCalls() {
        return allMethodCalls;
    }

    /**
     * Initialize this visitor. It will not work with another constructor
     * @param mainW The active MainWeaver
     */
    public MethodCallExprVisitor (MainWeaver mainW) {
        // this.weaver = mainW;
        this.thereIsAMethodCall = false;
        this.nameOfTheFirstMethodCalled = "";
        this.allMethods = mainW.getAllDeclaredMethods();
        this.allMethodCalls = new LinkedList<String>();
    }
}
