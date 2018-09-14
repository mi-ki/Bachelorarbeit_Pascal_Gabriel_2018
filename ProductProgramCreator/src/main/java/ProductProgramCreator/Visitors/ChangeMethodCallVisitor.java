package ProductProgramCreator.Visitors;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

/**
 * Its purpose is to be called on an assignexpr to change its first method call
 */
public class ChangeMethodCallVisitor extends ModifierVisitor<Object> {
    private boolean hasChanged;
    private String methodToChange;
    private NodeList<Expression> arguments;

    @Override
    public Visitable visit(MethodCallExpr n, Object arg) {
        if(!hasChanged && n.getName().asString().equals(methodToChange)) {
            arguments = n.getArguments();
            n.replace(new NameExpr("retValueOf" + n.getNameAsString()));
        }
        super.visit(n, arg);
        return new NameExpr("retValueOf" + n.getNameAsString());
    }
    public NodeList<Expression> getArguments() {
        return arguments;
    }
    /**
     * Initiate the visitor.
     * @param name the name of the method to change the call
     */
    public ChangeMethodCallVisitor(String name) {
        this.hasChanged = false;
        this.methodToChange = name;
        this.arguments = new NodeList<Expression>();
    }
}
