package ProductProgramCreator.WeavingRules;

import ProductProgramCreator.AnnotationRules.AnnotationRule;
import ProductProgramCreator.MainWeaver;
import ProductProgramCreator.Utilities.WeaveException;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * This class initiates all annotation rules and delegates a call to an
 * annotation weave to the respective annotationRule.
 */
public class AnnotationRuleHandler extends WeavingRule {
    private HashMap<String, AnnotationRule> rules;
    private MainWeaver weaver;

    @Override
    public LinkedList<Statement> weave(Statement left, Statement right)
                                                    throws WeaveException {
        //It is expected, that the input statements are always BlockStmts
        //When this method is called both statements are sure to have the same
        //annotation.
        //Because it is expected that annotations do not overlap.
        BlockStmt leftBlock = left.asBlockStmt().clone();
        BlockStmt rightBlock = right.asBlockStmt().clone();
        Statement leftFirst = leftBlock.getStatement(0);
        String annotation = getType(leftFirst.getComment().get());
        if (rules.containsKey(annotation)) {
            AnnotationRule currentRule = rules.get(annotation);
            return currentRule.weave(leftBlock, rightBlock);
        } else {
            //use default handling
            AnnotationRule defaultRule = rules.get("default");
            return defaultRule.weave(leftBlock, rightBlock);
        }
    }

    @Override
    public String getName() {
        return "AnnotationRule";
    }

    @Override
    public void addMainWeaver(MainWeaver mainW) {
        this.weaver = mainW;
        try {
            this.rules = getRules();
        } catch (InstantiationException e) {
            System.out.println("An error has occurred during annotation instantiation." +
                    "Annotations may not work correctly.");
        } catch (IllegalAccessException e) {
            System.out.println("An error has occurred during annotation instantiation." +
                    "Annotations may not work correctly.");
        }
    }

    /**
     * Collects all annotation rules and instantiates them
     * @return  A Hashmap containing the rules, where key is they name and value the rule itself
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private HashMap<String, AnnotationRule> getRules ()
            throws IllegalAccessException, InstantiationException {
        HashMap<String, AnnotationRule> retMap = new HashMap();
        Reflections reflections = new Reflections(
                ClasspathHelper.forPackage("ProductProgramCreator"),
                new SubTypesScanner());
        Set<Class<? extends AnnotationRule>> extendingClasses =
                reflections.getSubTypesOf(AnnotationRule.class);
        LinkedList<WeavingRule> rules = new LinkedList();
        for(Class current : extendingClasses) {
            AnnotationRule currentRule = (AnnotationRule)current.newInstance();
            currentRule.addMainWeaver(weaver);
            retMap.put(currentRule.getName(), currentRule);
        }
        return retMap;
    }

    /**
     * Extracts the information what kind of weave annotation it was:
     * The comment has to start with: @weave<'the_kind'>
     * @param comment
     * @return 'the_kind'
     */
    private String getType(Comment comment) {
        String comString = comment.getContent().trim();
        comString = comString.substring(6, comString.indexOf(">"));
        return comString;
    }
}
