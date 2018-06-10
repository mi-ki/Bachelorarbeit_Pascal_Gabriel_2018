package ProductProgramCreator.Visitors;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.LinkedList;

public class VariableListVisitor extends VoidVisitorAdapter {
    private LinkedList<String> allVariables = new LinkedList<String>();

    /**
     * Return a list of all methods in the visited Compilaton Unit.
     * Only call this method after you have visited a Compilation Unit.
     * @return the names of all Methods in the visited Compilation Unit
     */
    public LinkedList<String> getVariablesList() {
        return this.allVariables;
    }
    @Override
    public void visit(VariableDeclarator n, Object arg) {
        allVariables.add(n.getNameAsString());
        super.visit(n, arg);
    }
}
