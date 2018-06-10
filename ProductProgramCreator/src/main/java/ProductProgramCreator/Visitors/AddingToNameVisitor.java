package ProductProgramCreator.Visitors;

import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import sun.awt.image.ImageWatched;

import java.util.LinkedList;

/**
 * A visitor used for adding a certain String to all names in a compilation unit.
 */
public class AddingToNameVisitor extends ModifierVisitor {
    private String addition;
    private LinkedList<String> allMethods;
    private LinkedList<String> allClasses;
    private LinkedList<String> allVariables;

    /**
     * A constructor used for initially setting the addition.
     * @param addition The String to be added to a visited name.
     * @param methodsToInclude  a List of methods to include in the renaming
     * @param classesToInclude  a list of classes to include in the renaming
     * @param varsToInclude     a list of variables to include int the renaming
     */
    public AddingToNameVisitor(String addition,LinkedList<String> methodsToInclude, LinkedList<String> classesToInclude, LinkedList<String> varsToInclude) {
        super();
        setAddition(addition);
        this.allMethods = methodsToInclude;
        this.allClasses = classesToInclude;
        this.allVariables = varsToInclude;
    }

    /**
     * Get the current addition
     * @return A String representing the addition
     */
    public String getAddition(){
        return this.addition;
    }

    /**
     * Set the addition.
     * @param addition The String to be added to a visited name.
     */
    public void setAddition(String addition) {
        this.addition = addition;
    }

    @Override
    public Visitable visit(SimpleName n, Object arg) {
        super.visit(n, arg);
        n.setIdentifier(n.asString() + addition);
        return n;
    }
    @Override
    public Visitable visit(FieldAccessExpr n, Object arg) {
        String name = n.getNameAsString();
        super.visit(n,arg);
        if(!allVariables.contains(name)) {
            n.setName(name);
        }
        return n;
    }
    @Override
    public Visitable visit(ClassOrInterfaceType n, Object arg){
        String name = n.getNameAsString();
        super.visit(n, arg);
        if(!allClasses.contains(name)) {
            n.setName(name);
        }
        return n;
    }

    //TODO diese Methode sollte beachten auf welchem Objekt aufgerufen wird
    @Override
    public Visitable visit(MethodCallExpr n, Object arg) {
        String name = n.getNameAsString();
        super.visit(n, arg);
        if(!allMethods.contains(name)) {
            n.setName(name);
        }
        return n;
    }
}
