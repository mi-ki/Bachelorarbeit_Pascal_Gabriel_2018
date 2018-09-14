package ProductProgramCreator.Utilities;

import java.util.LinkedList;

public class WeavingPriorityList {
    private LinkedList<String> rulePriority;
    /**
     * Initiates the priority list with the order returned from
     * the lists iterator.
     * @param rules The rule list
     */
    public WeavingPriorityList (LinkedList<String> rules) {
        this.rulePriority = new LinkedList<String>();
        this.rulePriority.addAll(rules);
    }

    /**
     * Initiates the priority list without components.
     */
    public WeavingPriorityList (){
        this.rulePriority = new LinkedList<String>();
    }

    /**
     * Adds the given rule name to the end of the list.
     * @param rule
     */
    public void addRule(String rule) {
        this.rulePriority.add(rule);
    }
    /**
     * Checks whether the rule in the first argument has higher priority
     * than the rule in the second parameter.
     * Rules that are not part of this list will be treated as, the same,
     * lowest priority.
     * @param ruleToCheck   The rule to check for.
     * @param ruleToCompareTo   The rule which serves as comparison
     * @return true, if it has strictly higher priority, false, otherwise
     */
    public boolean hasHigherPriority(String ruleToCheck,
                                     String ruleToCompareTo) {
        int leftIndex = rulePriority.indexOf(ruleToCheck);
        int rightIndex = rulePriority.indexOf(ruleToCompareTo);
        if(!(leftIndex == -1)&&!(rightIndex == -1)) {
            return leftIndex > rightIndex;
        } else if (rightIndex == -1) {
            return true;
        } else {
            //Case: if left is not in the list or both aren't in the list
            return false;
        }
    }
}
