package ProductProgramCreator.WeavingRules;

import ProductProgramCreator.MainWeaver;
import ProductProgramCreator.Utilities.WeaveException;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.PrimitiveType;

import java.util.LinkedList;

public class WhileRule extends WeavingRule {
    private MainWeaver weaver;
    private int counter = 0;
    @Override
    public LinkedList<Statement> weave(Statement left, Statement right)
            throws WeaveException {
        LinkedList<Statement> retList = new LinkedList<Statement>();

        WhileStmt leftWhile = left.asWhileStmt().clone();
        WhileStmt rightWhile = right.asWhileStmt().clone();
        //Initialize the booleans to add in front of the whiles
        VariableDeclarator leftBool = new VariableDeclarator();
        leftBool.setName("lWhileB_" + counter);
        leftBool.setType(PrimitiveType.booleanType());
        leftBool.setInitializer(leftWhile.getCondition());
        final VariableDeclarationExpr vdeLeft =
                new VariableDeclarationExpr(leftBool);
        retList.add(new ExpressionStmt(vdeLeft));
        NameExpr leftBoolName = new NameExpr("lWhileB_" + counter);

        VariableDeclarator rightBool = new VariableDeclarator();
        rightBool.setName("rWhileB_" + counter);
        rightBool.setType(PrimitiveType.booleanType());
        rightBool.setInitializer(rightWhile.getCondition());
        final VariableDeclarationExpr vdeRight =
                new VariableDeclarationExpr(rightBool);
        retList.add(new ExpressionStmt(vdeRight));
        NameExpr rightBoolName = new NameExpr("rWhileB_" + counter);
        counter++;
        //initialize the return while stmt
        WhileStmt retWhile = new WhileStmt();
        //set condition
        BinaryExpr cond =
                new BinaryExpr(leftBoolName, rightBoolName,
                               BinaryExpr.Operator.OR);
        retWhile.setCondition(cond);
        //set body
        BlockStmt leftBlock = new BlockStmt();
        IfStmt leftIf = new IfStmt();
        leftIf.setCondition(leftBoolName);
        BlockStmt leftBody = leftWhile.getBody().asBlockStmt();
        AssignExpr condUpdaterLeft = new AssignExpr();
        condUpdaterLeft.setOperator(AssignExpr.Operator.ASSIGN);
        condUpdaterLeft.setTarget(leftBoolName);
        condUpdaterLeft.setValue(leftWhile.getCondition());
        leftBody.addStatement(condUpdaterLeft);
        leftIf.setThenStmt(leftBody);
        leftBlock.addStatement(leftIf);

        BlockStmt rightBlock = new BlockStmt();
        IfStmt rightIf = new IfStmt();
        rightIf.setCondition(rightBoolName);
        BlockStmt rightBody = rightWhile.getBody().asBlockStmt();
        AssignExpr condUpdaterRight = new AssignExpr();
        condUpdaterRight.setOperator(AssignExpr.Operator.ASSIGN);
        condUpdaterRight.setTarget(rightBoolName);
        condUpdaterRight.setValue(rightWhile.getCondition());
        rightBody.addStatement(condUpdaterRight);
        rightIf.setThenStmt(rightBody);
        rightBlock.addStatement(rightIf);

        retWhile.setBody(weaver.weave(leftBlock, rightBlock));
        retList.add(retWhile);


        return retList;
    }

    @Override
    public String getName() {
        return "WhileStmt";
    }

    @Override
    public void addMainWeaver(MainWeaver mainW) {
        this.weaver = mainW;
    }
}
