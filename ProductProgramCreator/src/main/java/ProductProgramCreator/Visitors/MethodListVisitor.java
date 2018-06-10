package ProductProgramCreator.Visitors;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.LinkedList;

/**
 * A visitor to accumulate all names of the classes in a given Compilation Unit.
 */
public class MethodListVisitor extends VoidVisitorAdapter {
    private LinkedList<String> allMethods = new LinkedList<String>();

    /**
     * Return a list of all methods in the visited Compilaton Unit.
     * Only call this method after you have visited a Compilation Unit.
     * @return the names of all Methods in the visited Compilation Unit
     */
    public LinkedList<String> getMethodList() {
        return this.allMethods;
    }
    @Override
    public void visit(MethodDeclaration n, Object arg) {
        allMethods.add(n.getNameAsString());
        super.visit(n, arg);
    }
}
