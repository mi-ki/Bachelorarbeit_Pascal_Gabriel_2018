package ProductProgramCreator.Visitors;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.HashMap;
import java.util.Optional;

/**
 * Is used to find a certain method.
 */
public class FindMethodVisitor extends VoidVisitorAdapter {
    private String methodName;
    private MethodDeclaration retMethod;
    private String className;
    private boolean methodFound;
    @Override
    public void visit(MethodDeclaration n, Object arg) {
        //Collect the method with this name as well as the class in which it
        //is defined
        if (n.getName().asString().equals(methodName) && !methodFound) {
            retMethod = n.clone();
            Optional<Node> optParent = n.getParentNode();
            boolean stop = false;
            while (optParent.isPresent() && !stop) {
                String type = optParent.get().getMetaModel().toString();
                if (type.equals("ClassOrInterfaceDeclaration")) {
                    ClassOrInterfaceDeclaration decl = (ClassOrInterfaceDeclaration) optParent.get();
                    className = decl.getNameAsString();
                    stop = true;
                } else {
                    optParent = optParent.get().getParentNode();
                }
            }
            methodFound = true;
        }
        super.visit(n, arg);
    }

    /**
     * Returns the result of the search.
     * @return A one element HashMap with the name of the surrounding class as key
     *         and the MethodDeclaration as value
     */
    public HashMap<String, MethodDeclaration> getResults() {
        HashMap<String, MethodDeclaration> retMap = new HashMap();
        retMap.put(className, retMethod);
        return retMap;
    }

    /**
     * Wether or not the method has been found.
     * @return true if it has been found, false otherwise
     */
    public boolean methodFound() {
        return methodFound;
    }
    /**
     * Instantiates the visitor.
     * @param name The name of the method to search for.
     */
    public FindMethodVisitor(String name) {
        this.methodName = name;
        this.methodFound = false;
    }
}
