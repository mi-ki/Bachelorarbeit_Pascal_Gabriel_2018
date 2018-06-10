package ProductProgramCreator.AnnotationRules;

import ProductProgramCreator.MainWeaver;
import ProductProgramCreator.Utilities.WeaveException;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.LinkedList;

public abstract class AnnotationRule {
    private MainWeaver weaver;

    public abstract LinkedList<Statement> weave(BlockStmt left, BlockStmt right) throws WeaveException;

    public abstract String getName();

    public void addMainWeaver (MainWeaver mainW){
        this.weaver = mainW;
    }

}
