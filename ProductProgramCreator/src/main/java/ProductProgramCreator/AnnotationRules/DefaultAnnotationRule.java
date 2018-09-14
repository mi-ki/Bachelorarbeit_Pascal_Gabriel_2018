package ProductProgramCreator.AnnotationRules;

import ProductProgramCreator.MainWeaver;
import ProductProgramCreator.Utilities.WeaveException;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.util.LinkedList;

/**
 * This annotation rule calls the standard weave function of MainWeaver.
 * Use this rule for annotating parts in the code you want to explicitly weave
 * that do not need a special rule.
 * The getName() will return 'default'
 */
public class DefaultAnnotationRule extends AnnotationRule {
    private MainWeaver weaver;

    @Override
    public LinkedList<Statement> weave(BlockStmt left, BlockStmt right)
                throws WeaveException {
        BlockStmt retBlock = weaver.weave(left, right);
        return weaver.blockToList(retBlock);
    }

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public void addMainWeaver(MainWeaver mainW) {
        this.weaver = mainW;
    }
}
