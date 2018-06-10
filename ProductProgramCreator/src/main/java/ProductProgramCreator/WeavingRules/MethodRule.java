package ProductProgramCreator.WeavingRules;

import ProductProgramCreator.MainWeaver;
import ProductProgramCreator.Utilities.WeaveException;
import ProductProgramCreator.Visitors.ChangeMethodCallVisitor;
import ProductProgramCreator.Visitors.MethodCallExprVisitor;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;

import java.util.*;

import static java.util.EnumSet.copyOf;

public class MethodRule extends WeavingRule {
    private MainWeaver weaver;
    private boolean weaveAutomatically;
    @Override
    public LinkedList<Statement> weave(Statement left, Statement right) throws WeaveException {
        /*
         * This method will only be called if two stmts with method calls
         * have to be weaved. That is because in the case of only annotated
         * method getting weaved all non applicable stmts that contain method
         * calls will be renamed to noWeave and so will never trigger this
         * weave method. See MainWeaver.checkExpression(Stmt) for Details
         */
        /*
         * Recursive methods that are called to be weaved will only be weaved
         * if their declaration is part of the main class. Else nothing will happen.
         */
        Expression leftEx = left.toExpressionStmt().get().getExpression().clone();
        Expression rightEx = right.toExpressionStmt().get().getExpression().clone();
        boolean leftIsCall = leftEx.isMethodCallExpr();
        boolean rightIsCall = rightEx.isMethodCallExpr();
        String leftMethodName;
        //is either methodcall or assign expression
        if(leftIsCall) {
            leftMethodName = leftEx.asMethodCallExpr().getName().asString();
        } else {
            MethodCallExprVisitor visitor = new MethodCallExprVisitor(weaver);
            leftEx.accept(visitor, new Object());
            leftMethodName = visitor.methodName();
        }
        String rightMethodName;

        if(rightIsCall) {
            rightMethodName = rightEx.asMethodCallExpr().getName().asString();
        } else {
            MethodCallExprVisitor visitor = new MethodCallExprVisitor(weaver);
            rightEx.accept(visitor, new Object());
            rightMethodName = visitor.methodName();
        }

        //Get the method declarations
        HashMap<String, MethodDeclaration> methods = new HashMap();
        methods.putAll(weaver.getMethodAndClass(leftMethodName));
        methods.putAll(weaver.getMethodAndClass(rightMethodName));
        Iterator<MethodDeclaration> iterator = methods.values().iterator();
        MethodDeclaration leftMethod = iterator.next();
        MethodDeclaration rightMethod = iterator.next();
        //Change methods to have void type
        if(!leftMethod.getType().isVoidType()) {
            weaver.addDeclarationToMainClass(removeReturn(leftMethod));
        }
        if(!rightMethod.getType().isVoidType()) {
            weaver.addDeclarationToMainClass(removeReturn(rightMethod));
        }
        //Change the method call on an AssignExpr and collect the arguments
        NodeList<Expression> leftArgs = new NodeList();
        NodeList<Expression> rightArgs = new NodeList();
        if(!leftIsCall) {
            ChangeMethodCallVisitor vis = new ChangeMethodCallVisitor(leftMethodName);
            leftEx.accept(vis, new Object());
            leftArgs = vis.getArguments();
        } else {
            leftArgs = leftEx.asMethodCallExpr().getArguments();
        }
        if(!rightIsCall) {
            ChangeMethodCallVisitor vis = new ChangeMethodCallVisitor(rightMethodName);
            rightEx.accept(vis, new Object());
            rightArgs = vis.getArguments();
        } else {
            rightArgs = rightEx.asMethodCallExpr().getArguments();
        }
        NodeList<Expression> collectiveArgs = new NodeList();
        collectiveArgs.addAll(leftArgs);
        collectiveArgs.addAll(rightArgs);
        //create the call of the new product method
        String productName = leftMethodName + "_x_" + rightMethodName;
        MethodCallExpr retCall = new MethodCallExpr();
        retCall.setName(new SimpleName(productName));
        retCall.setArguments(collectiveArgs);

        //Create product method
        boolean isAlreadyDefined = weaver.methodIsDefined(productName);
        if(!isAlreadyDefined) {
            //make sure a recursive call knows this method is already in progress
            weaver.addNameToMethodList(productName);
            MethodDeclaration retDecl = new MethodDeclaration();
            //add modifiers
            LinkedList<Modifier> declMods = new LinkedList();
            declMods.add(Modifier.PUBLIC);
            EnumSet<Modifier> declModsSet = copyOf(declMods);
            retDecl.setModifiers(declModsSet);
            //set name
            retDecl.setName(new SimpleName(productName));
            //set return type
            retDecl.setType(new VoidType());
            //set Parameters
            NodeList<Parameter> declPar = leftMethod.getParameters();
            declPar.addAll(rightMethod.getParameters());
            retDecl.setParameters(declPar);
            //set Body, initiate weave
            //set method weaving to true, so that a recursive method will be correctly
            //weaved without needing to add annotations to it
            boolean backup = weaver.getMethodWeaving();
            weaver.setMethodWeaving(true);
            retDecl.setBody(weaver.weave(leftMethod.getBody().get(), rightMethod.getBody().get()));
            weaver.setMethodWeaving(backup);
            weaver.addMethodToMainClass(retDecl);
        }

        //The list of statement to be added to the surrounding body
        LinkedList<Statement> retList = new LinkedList();
        //Add all the statements
        retList.add(new ExpressionStmt(retCall));
        if(!leftIsCall) {
            retList.add(new ExpressionStmt(leftEx));
        }
        if(!rightIsCall) {
            retList.add(new ExpressionStmt(rightEx));
        }
        return retList;
    }

    @Override
    public String getName() {
        return "MethodCallExpr";
    }

    @Override
    public void addMainWeaver(MainWeaver mainW) {
        this.weaver = mainW;
        this.weaveAutomatically = weaver.weaveMethodsAutomatically();
    }

    /**
     * Checks wether the given expression has a method call of a method that is
     * part of this compilation unit.
     * Intended for use to precheck for method weaving.
     * It is advised to save the return value and reuse it, as the method may
     * get very expensive depending on the size of the AssignExpr.
     * @param expression    An assignexpr to check on
     * @return 'MethodCallExpr' if there is one, 'AssignExpr' otherwise
     */
    public String hasMethodCall(AssignExpr expression) {
        Expression value = expression.getValue();
        MethodCallExprVisitor visitor = new MethodCallExprVisitor(weaver);
        value.accept(visitor, new Object());
        if(visitor.isThereAMethodCall()) {
            return "MethodCallExpr";
        } else {
            return "AssignExpr";
        }
    }

    /**
     * Checks wether the associated method is a recursive method.
     * REMEMBER: methodnames in the whole input programs are demanded to
     * be UNIQUE, else this will not work.
     * @param expr  The method call
     * @return true, if the method is recursive, false otherwise
     * @throws WeaveException if the method declaration could not be found
     */
    private boolean isRecursiveMethod(MethodCallExpr expr) throws WeaveException{
        //Get the method declaration to check for recursion in it
        String name = expr.getName().asString();
        HashMap<String, MethodDeclaration> method = weaver.getMethodAndClass(name);
        if(!(method == null)) {
            MethodCallExprVisitor visitor = new MethodCallExprVisitor(weaver);
            MethodDeclaration m = method.values().iterator().next();
            m.accept(visitor, new Object());
            LinkedList<String> methodCalls = visitor.getMethodCalls();
            return methodCalls.contains(name);
        } else {
            throw new WeaveException("The method declaration of: " + name +
            " could not be found.\nWeaving aborted");
        }
    }

    /**
     * Changes all return statements in a method to assign to 'retValueOf' + MethodName.
     * Only call this method if it is sure to have a return type.
     * @param in    The methoddeclaration to change
     * @return  The FieldDeclaration that needs to be added to the classes body
     *          null if it had no return stmts
     */
    public FieldDeclaration removeReturn(MethodDeclaration in) {
        boolean didSomething = false;
        NameExpr name = new NameExpr("retValueOf" + in.getName().asString());
        for(Statement current : in.getBody().get().getStatements()) {
            if(current.isReturnStmt()) {
                Expression rightSide = current.asReturnStmt().getExpression().get();
                AssignExpr ret = new AssignExpr(name, rightSide, AssignExpr.Operator.ASSIGN );
                current.replace(new ExpressionStmt(ret));
                didSomething = true;
            }
        }
        if(didSomething) {
            Type type = in.getType();
            in.setType(new VoidType());
            LinkedList<Modifier> list = new LinkedList();
            list.add(Modifier.PUBLIC);
            EnumSet<Modifier> set = EnumSet.copyOf(list);
            return new FieldDeclaration(set, type, name.getNameAsString());
        } else {
            return null;
        }
    }



}
