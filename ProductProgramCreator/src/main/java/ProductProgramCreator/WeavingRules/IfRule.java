package ProductProgramCreator.WeavingRules;

import ProductProgramCreator.MainWeaver;
import ProductProgramCreator.Utilities.WeaveException;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.PrimitiveType;

import java.util.LinkedList;

public class IfRule extends WeavingRule {
    private MainWeaver weaver;
    private int counter = 0;
    @Override
    public LinkedList<Statement> weave(Statement left, Statement right) throws WeaveException {
        LinkedList<Statement> retList = new LinkedList();
        IfStmt leftIf = left.asIfStmt().clone();
        IfStmt rightIf = right.asIfStmt().clone();
        //Initialize the booleans to add in front of the ifs
        VariableDeclarator leftBool = new VariableDeclarator();
        leftBool.setName("lIfB_" + counter);
        leftBool.setType(PrimitiveType.booleanType());
        leftBool.setInitializer(leftIf.getCondition());
        retList.add(new ExpressionStmt(new VariableDeclarationExpr(leftBool)));
        NameExpr leftBoolName = new NameExpr("lIfB_" + counter);

        VariableDeclarator rightBool = new VariableDeclarator();
        rightBool.setName("rIfB_" + counter);
        rightBool.setType(PrimitiveType.booleanType());
        rightBool.setInitializer(rightIf.getCondition());
        retList.add(new ExpressionStmt(new VariableDeclarationExpr(rightBool)));
        NameExpr rightBoolName = new NameExpr("rIfB_" + counter);
        counter++;
        //Initialize the new If Stmt
        IfStmt retIf = new IfStmt();
        BinaryExpr newCond = new BinaryExpr(leftBoolName, rightBoolName, BinaryExpr.Operator.AND);
        retIf.setCondition(newCond);
        retIf.setThenStmt(weaver.weave(leftIf.getThenStmt().asBlockStmt(), rightIf.getThenStmt().asBlockStmt()));
        //Check for else branches
        //there is an else branch on the right side
        IfStmt elseIfOne = new IfStmt();
        elseIfOne.setCondition(leftBoolName);
        boolean rightHasElse = rightIf.hasElseBranch() || rightIf.hasElseBlock();
        if(rightHasElse) {
            if (rightIf.hasElseBlock()) {
                elseIfOne.setThenStmt(weaver.weave(leftIf.getThenStmt().asBlockStmt(), rightIf.getElseStmt().get().asBlockStmt()));
            } else {
                BlockStmt ifAsBlock = new BlockStmt();
                ifAsBlock.addStatement(rightIf.getElseStmt().get());
                elseIfOne.setThenStmt(weaver.weave(leftIf.getThenStmt().asBlockStmt(), ifAsBlock));
            }
        } else {
            elseIfOne.setThenStmt(leftIf.getThenStmt());
        }

        //there is an else branch on the left side
        IfStmt elseIfTwo = new IfStmt();
        elseIfTwo.setCondition(rightBoolName);
        boolean leftHasElse = leftIf.hasElseBlock() || leftIf.hasElseBranch();
        if(leftHasElse) {
            if(leftIf.hasElseBlock()) {
                elseIfTwo.setThenStmt(weaver.weave(leftIf.getElseStmt().get().asBlockStmt(), rightIf.getThenStmt().asBlockStmt()));
            } else {
                BlockStmt ifAsBlock = new BlockStmt();
                ifAsBlock.addStatement(leftIf.getElseStmt().get());
                elseIfTwo.setThenStmt(weaver.weave(ifAsBlock, rightIf.getThenStmt().asBlockStmt()));
            }
        } else {
            elseIfTwo.setThenStmt(rightIf.getThenStmt());
        }

        //there is an else branch on at least one of the sides
        if(leftHasElse && rightHasElse) {
            if(leftIf.hasElseBlock() && rightIf.hasElseBlock()) {
                elseIfTwo.setElseStmt(weaver.weave(leftIf.getElseStmt().get().asBlockStmt(), rightIf.getElseStmt().get().asBlockStmt()));
            } else if(leftIf.hasElseBlock()) {
                BlockStmt ifAsBlock = new BlockStmt();
                ifAsBlock.addStatement(rightIf.getElseStmt().get());
                elseIfTwo.setElseStmt(weaver.weave(leftIf.getElseStmt().get().asBlockStmt(), ifAsBlock));
            } else if(rightIf.hasElseBlock()) {
                BlockStmt ifAsBlock = new BlockStmt();
                ifAsBlock.addStatement(leftIf.getElseStmt().get());
                elseIfTwo.setElseStmt(weaver.weave(ifAsBlock, rightIf.getElseStmt().get().asBlockStmt()));
            } else {
                BlockStmt ifAsBlockR = new BlockStmt();
                ifAsBlockR.addStatement(rightIf.getElseStmt().get());
                BlockStmt ifAsBlockL = new BlockStmt();
                ifAsBlockL.addStatement(leftIf.getElseStmt().get());
                elseIfTwo.setElseStmt(weaver.weave(ifAsBlockL, ifAsBlockR));
            }
        } else if (leftHasElse) {
            elseIfTwo.setElseStmt(leftIf.getElseStmt().get());
        } else if (rightHasElse) {
            elseIfTwo.setElseStmt(rightIf.getElseStmt().get());
        }
        //add the ifstmts together
        elseIfOne.setElseStmt(elseIfTwo);
        retIf.setElseStmt(elseIfOne);
        retList.add(retIf);


        return retList;
    }

    @Override
    public String getName() {
        return "IfStmt";
    }

    @Override
    public void addMainWeaver(MainWeaver mainW) {
        this.weaver = mainW;
    }
}
