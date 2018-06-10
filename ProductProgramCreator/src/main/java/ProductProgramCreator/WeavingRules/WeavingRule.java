package ProductProgramCreator.WeavingRules;

import ProductProgramCreator.MainWeaver;
import ProductProgramCreator.Utilities.WeaveException;
import com.github.javaparser.ast.stmt.Statement;

import java.util.LinkedList;

public abstract class WeavingRule {
    private MainWeaver weaver;
    public abstract LinkedList<Statement> weave(Statement left, Statement right) throws WeaveException;

    public abstract String getName();

    public void addMainWeaver(MainWeaver mainW){
        this.weaver = mainW;
    }
}
