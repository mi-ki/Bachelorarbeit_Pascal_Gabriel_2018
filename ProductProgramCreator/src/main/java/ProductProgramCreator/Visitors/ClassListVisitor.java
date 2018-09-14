package ProductProgramCreator.Visitors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.LinkedList;

/**
 * A visitor to accumulate all names of the classes
 * in a given Compilation Unit.
 */
public class ClassListVisitor extends VoidVisitorAdapter<Object> {
    private LinkedList<String> allClasses = new LinkedList<String>();

    /**
     * Return a list of all classes in the visited Compilaton Unit.
     * Only call this method after you have visited a Compilation Unit.
     * @return the names of all classes in the visited Compilation Unit
     */
    public LinkedList<String> getClassList() {
        return this.allClasses;
    }
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
        allClasses.add(n.getNameAsString());
        super.visit(n, arg);
    }
}