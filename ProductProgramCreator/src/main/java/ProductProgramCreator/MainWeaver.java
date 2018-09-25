package ProductProgramCreator;

import ProductProgramCreator.Utilities.FileHandler;
import ProductProgramCreator.Utilities.WeaveException;
import ProductProgramCreator.Utilities.WeavingPriorityList;
import ProductProgramCreator.Visitors.FindMethodVisitor;
import ProductProgramCreator.Visitors.MethodListVisitor;
import ProductProgramCreator.WeavingRules.MethodRule;
import ProductProgramCreator.WeavingRules.WeavingRule;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.VoidType;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * The main class for handling and distributing the weaving tasks.
 */
public class MainWeaver {
    private String additionL;
    private String additionR;
    private String mainMethodLName;
    private String mainMethodRName;
    private HashMap<String, WeavingRule> weavingRules;
    private WeavingPriorityList priorityList;
    private boolean methodWeavingAutomated;
    private LinkedList<String> allDeclaredMethods;
    private CompilationUnit leftBackup;
    private CompilationUnit rightBackup;
    private CompilationUnit retCu;
    private String retMainClassName;

    /**
     * The to be used constructor, defines all given variables.
     * Collects all weaving rules present.
     * @param addL  The suffix for the left program
     * @param addR  The suffix for the right program
     * @param mainMethodLeft    The name of the main class of the left program
     * @param mainMethodRight   The name of the main class of the right program
     * @param path  The path to the file containing the priority list
     * @param weaveAuto true means automated method weaving,
     *                  false only if annotated.
     * @throws InstantiationException When a rule instantiation has failed.
     * @throws IllegalAccessException if a rule or its nullary constructor
     *                                is not available.
     * @throws FileNotFoundException if the given file could not be found.
     */
    public MainWeaver(String addL, String addR,
                      String mainMethodLeft,
                      String mainMethodRight,
                      String path, boolean weaveAuto)
            throws InstantiationException, IllegalAccessException,
                   FileNotFoundException {
        this.additionL = addL;
        this.additionR = addR;
        this.mainMethodLName = mainMethodLeft;
        this.mainMethodRName = mainMethodRight;
        try { this.weavingRules = getRules(); }
        catch (IllegalArgumentException e) { }
        catch (InvocationTargetException e) { }
        catch (NoSuchMethodException e) { }
        catch (SecurityException e) { }
        FileHandler fh = new FileHandler();
        this.priorityList = fh.getPriorityList(path);
        this.methodWeavingAutomated = weaveAuto;
    }
    /**
     * Defines all given variables.
     * Defaults the priority list.
     * Collects all weaving rules present.
     * @param addL  The suffix for the left program
     * @param addR  The suffix for the right program
     * @param mainMethodLeft    The name of the main class of the left program
     * @param mainMethodRight   The name of the main class of the right program
     * @throws InstantiationException When a rule instantiation has failed.
     * @throws IllegalAccessException if a rule or its nullary constructor
     *                                is not available.
     * @throws IOException if an I/O-Exception has occurred or the resource
     *                     could not be found.
     */
    public MainWeaver(String addL, String addR,
                      String mainMethodLeft,
                      String mainMethodRight)
            throws InstantiationException, IllegalAccessException,
                   IOException {
        this.additionL = addL;
        this.additionR = addR;
        this.mainMethodLName = mainMethodLeft;
        this.mainMethodRName = mainMethodRight;
        try { this.weavingRules = getRules(); }
        catch (IllegalArgumentException e) { }
        catch (InvocationTargetException e) { }
        catch (NoSuchMethodException e) { }
        catch (SecurityException e) { }
        FileHandler fh = new FileHandler();
        this.priorityList = fh.getPriorityList();
        this.methodWeavingAutomated = false;
        this.allDeclaredMethods = new LinkedList<String>();
    }
    /**
     * A constructor which defines only the suffixes for the input programs.
     * Defaults the main method names to 'main'.
     * Defaults the priority list.
     * Collects all weaving rules present.
     * @param addL  The suffix for the left program
     * @param addR  The suffix for the right program
     * @throws InstantiationException When a rule instantiation has failed.
     * @throws IllegalAccessException if a rule or its nullary constructor
     *                                is not available.
     * @throws IOException if an I/O-Exception has occurred or the resource
     *                     could not be found.
     */
    public MainWeaver(String addL, String addR) throws InstantiationException,
            IllegalAccessException, IOException {
        this.additionL = addL;
        this.additionR = addR;
        this.mainMethodLName = "main";
        this.mainMethodRName = "main";
        try { this.weavingRules = getRules(); }
        catch (IllegalArgumentException e) { }
        catch (InvocationTargetException e) { }
        catch (NoSuchMethodException e) { }
        catch (SecurityException e) { }
        FileHandler fh = new FileHandler();
        this.priorityList = fh.getPriorityList();
        this.methodWeavingAutomated = false;
    }

    /**
     * The default constructor, assumes the left suffix is '_1' and
     * the right suffix is '_2'.
     * Defaults the main method names to 'main'.
     * Defaults the priority list.
     * Collects all weaving rules present
     * @throws InstantiationException When a rule instantiation has failed
     * @throws IllegalAccessException if a rule or its nullary constructor is
     *                                not available
     * @throws IOException if an I/O-Exception has occurred or the resource
     *                     could not be found.
     */
    public MainWeaver() throws InstantiationException, IllegalAccessException,
                               IOException {
        this.additionL = "_1";
        this.additionR = "_2";
        this.mainMethodLName = "main";
        this.mainMethodRName = "main";
        try { this.weavingRules = getRules(); }
        catch (IllegalArgumentException e) { }
        catch (InvocationTargetException e) { }
        catch (NoSuchMethodException e) { }
        catch (SecurityException e) { }
        FileHandler fh = new FileHandler();
        this.priorityList = fh.getPriorityList();
        this.methodWeavingAutomated = false;
    }
    /**
     * Inititates the weaving process by creating the surrounding (main) class
     * and calling the main weave method on its content.
     * @param leftIn  The left program as CompilationUnit
     * @param rightIn The right program as CompilationUnit
     * @return  The Product Program as CompilationUnit
     */
    public CompilationUnit initiateWeave(CompilationUnit leftIn,
                                         CompilationUnit rightIn)
            throws WeaveException {
        //Clone input
        CompilationUnit left = leftIn.clone();
        left.setStorage(leftIn.getStorage().get().getPath());
        CompilationUnit right = rightIn.clone();
        right.setStorage(rightIn.getStorage().get().getPath());
        //Clone again, this time for later use, outside of this method
        leftBackup = leftIn.clone();
        leftBackup.setStorage(leftIn.getStorage().get().getPath());
        rightBackup = rightIn.clone();
        rightBackup.setStorage(rightIn.getStorage().get().getPath());

        //Extract the names of all methods defined in both compilation units
        //(needed for method weaving)
        this.allDeclaredMethods = new LinkedList<String>();
        this.allDeclaredMethods.addAll(getDeclaredMethods(left));
        this.allDeclaredMethods.addAll(getDeclaredMethods(right));
        //Extract the names of the main classes and remove ".java",
        // add the suffix from the renaming process
        String leftMainClassName =
                left.getStorage().get().getFileName().toString();
        leftMainClassName =
                leftMainClassName.substring(0,
                                            leftMainClassName.length() - 5)
                                            + this.additionL;
        String rightMainClassName =
                right.getStorage().get().getFileName().toString();
        rightMainClassName =
                rightMainClassName.substring(0,
                                             rightMainClassName.length() - 5)
                                             + this.additionR;

        retCu = new CompilationUnit();
        //Add all imports
        addImports(retCu, left);
        addImports(retCu, right);

        //Add all classes (including main classes,
        //                 since self instantiation is possible)
        addAllClasses(retCu, left);
        addAllClasses(retCu, right);

        //find the main classes and process them
        retMainClassName = leftMainClassName + "_x_" + rightMainClassName;
        retCu.addClass(retMainClassName);
        ClassOrInterfaceDeclaration retMainClass =
                retCu.getClassByName(leftMainClassName + "_x_" +
                                     rightMainClassName).get();
        ClassOrInterfaceDeclaration leftMainClass =
                left.getClassByName(leftMainClassName).get();
        ClassOrInterfaceDeclaration rightMainClass =
                right.getClassByName(rightMainClassName).get();

        //Check if all modifiers are supported
        //if one is strict, the output will be strict
        //The output class will have a public modifier
        EnumSet<Modifier> modLeft = checkModifiers(leftMainClass);
        EnumSet<Modifier> modRight = checkModifiers(rightMainClass);
        if(leftMainClass.isStrictfp()) {
            retMainClass.setModifiers(modLeft);
        } else if(rightMainClass.isStrictfp()){
            retMainClass.setModifiers(modRight);
        }
        retMainClass.addModifier(Modifier.PUBLIC);

        //Add all the type parameters to the return class
        retMainClass.setTypeParameters(leftMainClass.getTypeParameters());
        for(TypeParameter current : rightMainClass.getTypeParameters()) {
            retMainClass.addTypeParameter(current);
        }

        //get the and call the weave function to generate its body
        //adds all members except and the main method to the
        // product classes body, and extract the main methods.
        MethodDeclaration mainMethodL =
                addMembersAndReturnMainMethod(retMainClass, leftMainClass,
                                              mainMethodLName + additionL);
        MethodDeclaration mainMethodR =
                addMembersAndReturnMainMethod(retMainClass, rightMainClass,
                                              mainMethodRName + additionR);
        MethodDeclaration retMainMethod = new MethodDeclaration();

        //Throw exception if mainMethods could not be found
        //this should never be the case, as the presence of
        //the methods are checked when their name is given by the user
        if((mainMethodL == null) && (mainMethodR == null)) {
            String exMessage = "Neither main method could be found.";
            exMessage = exMessage + "\nWeaving aborted.";
            throw new WeaveException(exMessage);
        } else if(mainMethodL == null) {
            String exMessage = "The left main method could not be found.";
            exMessage = exMessage + "\nWeaving aborted.";
            throw new WeaveException(exMessage);
        } else if(mainMethodR == null) {
            String exMessage = "The right main method could not be found.";
            exMessage = exMessage + "\nWeaving aborted.";
            throw new WeaveException(exMessage);
        }

        //Adds the modifiers of the main methods if they match
        if(mainMethodL.isStatic() && !mainMethodR.isStatic()) {
            String exMessage =
                    "Either both classes or no class can be static.";
            exMessage = exMessage + "\nWeaving aborted.";
            throw new WeaveException(exMessage);
        } else if (!mainMethodL.isStatic() && mainMethodR.isStatic()) {
            String exMessage =
                    "Either both classes or no class can be static.";
            exMessage = exMessage + "\nWeaving aborted.";
            throw new WeaveException(exMessage);
        }
        EnumSet<Modifier> methodLModifiers = checkModifiers(mainMethodL);
        EnumSet<Modifier> methodRModifiers = checkModifiers(mainMethodR);
        if(mainMethodL.isStrictfp()) {
            retMainMethod.setModifiers(methodLModifiers);
        } else if(mainMethodR.isStrictfp()){
            retMainMethod.setModifiers(methodRModifiers);
        }
        retMainMethod.addModifier(Modifier.PUBLIC);

        //Checks if the main methods have return types,
        // transforms it to a class without return type
        //Return values will be saved in a field
        // String nameOfReturnFieldLeft =
        //                "retValueOf" + mainMethodLName + additionL;
        //FieldDeclaration retFieldLeft =
        //                transformToVoid(leftMainClass, mainMethodL,
        //                                nameOfReturnFieldLeft);
        MethodRule helper = new MethodRule();
        FieldDeclaration retFieldLeft = helper.removeReturn(mainMethodL);
        if(retFieldLeft != null) {
            //retMainClass.addField(retFieldLeft.getElementType(),
            //                      nameOfReturnFieldLeft, Modifier.PUBLIC);
            addDeclarationToMainClass(retFieldLeft);
        }
        // String nameOfReturnFieldRight =
        //            "retValueOf" + mainMethodRName + additionR;
        //FieldDeclaration retFieldRight =
        //            transformToVoid(rightMainClass, mainMethodR,
        //                            nameOfReturnFieldRight);
        FieldDeclaration retFieldRight = helper.removeReturn(mainMethodR);
        if(retFieldRight != null) {
            //retMainClass.addField(retFieldRight.getElementType(),
            //                      nameOfReturnFieldRight, Modifier.PUBLIC);
            addDeclarationToMainClass(retFieldRight);
        }

        //set the type to void, since all return values will be
        // redirected to fields
        retMainMethod.setType(new VoidType());
        //Sets the name
        retMainMethod.setName(mainMethodLName + "_x_" + mainMethodRName);
        //Sets the parameters
        addParameters(retMainMethod, mainMethodL);
        addParameters(retMainMethod, mainMethodR);
        //gets the bodies
        BlockStmt bodyLeft = getBody(mainMethodL);
        BlockStmt bodyRight = getBody(mainMethodR);
        //Call the weave function and add the main method to the return class
        retMainMethod.setBody(weave(bodyLeft.clone(), bodyRight.clone()));
        retMainClass.addMember(retMainMethod);
        return retCu;
    }

    /**
     * Weaves BlockStmts of the given mainMethods and calls weaveMethods
     * @param left  The left BlockStmt to weave
     * @param right The right BlockStmt to weave
     * @return  The result product code as BlockStmt
     * @throws WeaveException if there has been a problem with the annotations
     */
    public BlockStmt weave(BlockStmt left, BlockStmt right)
            throws WeaveException {
        //The Block Stmt that will be modified and returned
        BlockStmt retBlock = new BlockStmt();

        NodeList<Statement> leftStatements = left.getStatements();
        NodeList<Statement> rightStatements = right.getStatements();
        Iterator<Statement> leftIterator = leftStatements.iterator();
        Iterator<Statement> rightIterator = rightStatements.iterator();
        //Booleans to check if, and then which side blocks
        boolean stopLeft = false;
        boolean stopRight = false;

        //Statements where it is currently, and their names
        Statement currentLeft = null;
        Statement currentRight = null;
        String currentLName = "";
        String currentRName = "";
        //Current state of the iterator
        boolean leftHasNext = leftIterator.hasNext();
        boolean rightHasNext = rightIterator.hasNext();
        //Needed fields for the annotation handling
        boolean leftAtAnnotation = false;
        boolean rightAtAnnotation = false;
        boolean annoJustFinished = false;
        //Iterate over all of the statements
        while(leftHasNext || rightHasNext) {
            //Choose behaviour depending on iterator state
            if(leftHasNext && rightHasNext) {
                //Skip if there has just finished an annotation weaving
                //Needed because else the last node would be skipped
                if(stopLeft && !annoJustFinished) {
                    currentRight = rightIterator.next();
                    currentRName = checkExpressionStmt(currentRight);
                } else if (stopRight && !annoJustFinished) {
                    currentLeft = leftIterator.next();
                    currentLName = checkExpressionStmt(currentLeft);
                } else if(!annoJustFinished){
                    currentLeft = leftIterator.next();
                    currentLName = checkExpressionStmt(currentLeft);
                    currentRight = rightIterator.next();
                    currentRName = checkExpressionStmt(currentRight);
                } else {
                    //Only happens if there was just an annotation weaving
                    //reset the corresponding booleans
                    annoJustFinished = false;
                }
                //check if one of the current nodes has a weave annotation
                // in its comment
                leftAtAnnotation = hasWeaveAnnotation(currentLeft);
                rightAtAnnotation = hasWeaveAnnotation(currentRight);
                //ensure if there is one side at the start of the annotation,
                //the second will be too
                boolean canCollectStatements = false;

                if(leftAtAnnotation && rightAtAnnotation) {
                    final String leftAnnoLabel =
                            getLabelOfAnno(currentLeft);
                    final String leftAnnoType =
                            getTypeOfAnno(currentLeft);
                    final String rightAnnoType =
                            getTypeOfAnno(currentRight);
                    final String rightAnnoLabel =
                            getLabelOfAnno(currentRight);
                    boolean sameLabel = leftAnnoLabel.equals(rightAnnoLabel);
                    boolean sameType = leftAnnoType.equals(rightAnnoType);
                    canCollectStatements = sameLabel && sameType;
                } else if(leftAtAnnotation) {
                    while(!rightAtAnnotation && rightHasNext) {
                        //Add all statements in front of the
                        // annotation to the block
                        retBlock.addStatement(currentRight);
                        currentRight = rightIterator.next();
                        rightHasNext = rightIterator.hasNext();
                        //set it to true ONLY if both have the
                        // exact same annotation
                        if(hasWeaveAnnotation(currentRight)) {
                            final String leftAnnoLabel =
                                    getLabelOfAnno(currentLeft);
                            final String leftAnnoType =
                                    getTypeOfAnno(currentLeft);
                            final String rightAnnoType =
                                    getTypeOfAnno(currentRight);
                            final String rightAnnoLabel =
                                    getLabelOfAnno(currentRight);
                            boolean sameLabel =
                                    leftAnnoLabel.equals(rightAnnoLabel);
                            boolean sameType =
                                    leftAnnoType.equals(rightAnnoType);
                            rightAtAnnotation = sameLabel && sameType;
                        }
                    }
                    if(rightAtAnnotation) {
                        canCollectStatements = true;
                    } else {
                        final String leftAnnoLabel =
                                getLabelOfAnno(currentLeft);
                        final String leftAnnoType =
                                getTypeOfAnno(currentLeft);
                        String anno = leftAnnoType + " " + leftAnnoLabel;
                        String exMessage =
                                "2nd Annotation for " + anno
                                + " not found.\nWeaving aborted.";
                        throw new WeaveException(exMessage);
                    }

                } else if (rightAtAnnotation) {
                    while(!leftAtAnnotation && leftHasNext) {
                        //Add all statements in front of the annotation
                        // to the block
                        retBlock.addStatement(currentLeft);
                        currentLeft = leftIterator.next();
                        leftHasNext = leftIterator.hasNext();
                        if(hasWeaveAnnotation(currentRight)) {
                            final String leftAnnoLabel =
                                    getLabelOfAnno(currentLeft);
                            final String leftAnnoType =
                                    getTypeOfAnno(currentLeft);
                            final String rightAnnoType =
                                    getTypeOfAnno(currentRight);
                            final String rightAnnoLabel =
                                    getLabelOfAnno(currentRight);
                            boolean sameLabel =
                                    leftAnnoLabel.equals(rightAnnoLabel);
                            boolean sameType =
                                    leftAnnoType.equals(rightAnnoType);
                            leftAtAnnotation = sameLabel && sameType;
                        }
                    }
                    if (leftAtAnnotation) {
                        canCollectStatements = true;
                    } else {
                        final String rightAnnoLabel =
                                getLabelOfAnno(currentRight);
                        final String rightAnnoType =
                                getTypeOfAnno(currentRight);
                        String anno =
                                rightAnnoType + " " + rightAnnoLabel;
                        String exMessage =
                                "2nd Annotation for " + anno
                                + " not found.\nWeaving aborted.";
                        throw new WeaveException(exMessage);
                    }

                }
                if(canCollectStatements) {
                    //At the start of both annotated areas,
                    //now collect all statements
                    //Add start statements
                    String label = getLabelOfAnno(currentLeft);
                    removeAnnotation(currentLeft);
                    removeAnnotation(currentRight);
                    BlockStmt leftArea = new BlockStmt();
                    leftArea.addStatement(currentLeft);
                    BlockStmt rightArea = new BlockStmt();
                    rightArea.addStatement(currentRight);
                    //Add all elements until the next annotation (excluding it)
                    //or the end of the code
                    leftHasNext = leftIterator.hasNext();
                    rightHasNext = rightIterator.hasNext();
                    boolean leftFinished = false;
                    while(leftHasNext && !leftFinished) {
                        currentLeft = leftIterator.next();
                        boolean isCorrectLabel = false;
                        if(hasWeaveAnnotation(currentLeft)) {
                            isCorrectLabel =
                                    getLabelOfAnno(currentLeft).equals(label);
                        } else {
                            leftArea.addStatement(currentLeft);
                            leftHasNext = leftIterator.hasNext();
                        }
                        if(isCorrectLabel) {
                            removeAnnotation(currentLeft);
                            leftFinished = true;
                        }
                    }
                    boolean rightFinished = false;
                    while(rightHasNext && !rightFinished) {
                        currentRight = rightIterator.next();
                        boolean isCorrectLabel = false;
                        if(hasWeaveAnnotation(currentRight)) {
                            isCorrectLabel =
                                    getLabelOfAnno(currentRight).equals(label);
                        } else {
                            rightArea.addStatement(currentRight);
                            rightHasNext = rightIterator.hasNext();
                        }
                        if(isCorrectLabel) {
                            removeAnnotation(currentRight);
                            rightFinished = true;
                        }
                    }
                    //weave them and add the result
                    WeavingRule currentRule =
                            weavingRules.get("AnnotationRule");
                    for (Statement current:
                            currentRule.weave(leftArea, rightArea)) {
                        retBlock.addStatement(current);
                    }
                    annoJustFinished = true;
                }
                //Only execute if the annotation handling did not just finish
                //Because else it would cause unwanted behaviour
                if(!annoJustFinished) {
                    //Check for which statements there is a weaving rule
                    boolean leftHasRule =
                            weavingRules.containsKey(currentLName);
                    boolean rightHasRule =
                            weavingRules.containsKey(currentRName);
                    if (leftHasRule && rightHasRule) {
                        boolean leftHigher =
                                priorityList.hasHigherPriority(currentLName,
                                                               currentRName);
                        //Check if the right statement has the same type
                        if(currentLName.equals(currentRName)) {
                            stopLeft = false;
                            stopRight = false;
                            //weave them and add the result
                            WeavingRule currentRule =
                                    weavingRules.get(currentLName);
                            LinkedList<Statement> allStmts =
                                    currentRule.weave(currentLeft,
                                                      currentRight);
                            for (Statement current : allStmts) {
                                retBlock.addStatement(current);
                            }
                        } else if(leftHigher) {
                            //add the right rule and stop searching on the
                            //left side
                            stopLeft = true;
                            stopRight = false;
                            retBlock.addStatement(currentRight);
                        } else if(!leftHigher) {
                            //add the left rule and stop searching on the
                            //right side
                            stopLeft = false;
                            stopRight = true;
                            retBlock.addStatement(currentLeft);
                        } else {
                            //This case will happen if both rules are not
                            //in the priority list.
                            //In this case the first rule to be found will be
                            //moved on with or if none was first, the left one
                            if(stopLeft) {
                                retBlock.addStatement(currentRight);
                            } else if (stopRight) {
                                retBlock.addStatement(currentLeft);
                            } else {
                                retBlock.addStatement(currentRight);
                            }
                        }
                    } else if (leftHasRule) {
                        //initiate search on the right for a match
                        stopLeft = true;
                        stopRight = false;
                        retBlock.addStatement(currentRight);
                    } else if (rightHasRule) {
                        //initiate search on the left for a match
                        stopLeft = false;
                        stopRight = true;
                        retBlock.addStatement(currentLeft);
                    } else {
                        //Both have no rule so will be added
                        stopLeft = false;
                        stopRight = false;
                        retBlock.addStatement(currentLeft);
                        retBlock.addStatement(currentRight);
                    }
                }
            } else if (leftHasNext) {
                //No need for annotationhandling, since annotations will only
                //block inside the first if-branch
                //If left blocks but no more right elements,
                //then remove block and add all
                if(stopLeft) {
                    stopLeft = false;
                    retBlock.addStatement(currentLeft);
                }
                currentLeft = leftIterator.next().clone();
                currentLName = checkExpressionStmt(currentLeft);
                //check for the case, that the right one still searches
                // for a match
                if (stopRight) {
                    //if they are the same type, weave them
                    if (currentLName.equals(currentRName)) {
                        stopLeft = false;
                        stopRight = false;
                        //weave them and add the result
                        WeavingRule currentRule =
                                weavingRules.get(currentLName);
                        for (Statement current:
                                currentRule.weave(currentLeft, currentRight)) {
                            retBlock.addStatement(current);
                        }
                    } else {
                        retBlock.addStatement(currentLeft);
                    }
                } else {
                    //no search ongoing, no more elements right,
                    //so add all remaining left
                    retBlock.addStatement(currentLeft);
                }
            } else if (rightHasNext) {
                //No need for annotationhandling, since annotations will only
                //block inside the first if-branch
                //If right blocks but no more left elements,
                //then remove block and add all
                if(stopRight) {
                    stopRight = false;
                    retBlock.addStatement(currentRight);
                }
                currentRight = rightIterator.next().clone();
                currentRName = checkExpressionStmt(currentRight);
                //check for the case that the left one still
                //searches for a match
                if(stopLeft) {
                    //if they are the same type, weave them
                    if(currentLName.equals(currentRName)) {
                        stopLeft = false;
                        stopRight = false;
                        //weave them and add the result
                        WeavingRule currentRule =
                                weavingRules.get(currentLName);
                        for (Statement current:
                                currentRule.weave(currentLeft, currentRight)) {
                            retBlock.addStatement(current);
                        }
                    } else {
                        retBlock.addStatement(currentRight);
                    }
                } else {
                    //no search ongoing, no more elements on the left,
                    //so add all remaining right
                    retBlock.addStatement(currentRight);
                }
            }
            //Update state booleans
            leftHasNext = leftIterator.hasNext();
            rightHasNext = rightIterator.hasNext();
        }
        //Add if last element of either side waits for a match but there are no
        //more candidates
        if(stopLeft) {
            stopLeft = false;
            retBlock.addStatement(currentLeft);
        }
        if(stopRight) {
            stopRight = false;
            retBlock.addStatement(currentRight);
        }
        return retBlock;
    }

    /**
     * Sets the type of method weaving to be used.
     * true means automated weaving(as with any other weaving rule,
     * but has lowest priority)
     * false means only weave if annotated as such
     * @param in
     */
    public void setMethodWeaving (boolean in) {
        this.methodWeavingAutomated = in;
    }

    /**
     * Returns if automated weaving is on
     * @return true if it is, false otherwise
     */
    public boolean getMethodWeaving () {
        return this.methodWeavingAutomated;
    }

    /**
     * Adds the imports of source to the target, checks for duplicates.
     * @param target
     * @param source
     */
    private void addImports(CompilationUnit target, CompilationUnit source) {
        for(ImportDeclaration current : source.getImports()) {
            target.addImport(current.getNameAsString(), current.isStatic(),
                             current.isAsterisk());
        }
    }

    /**
     * Adds all classes from source to target
     * @param target
     * @param source
     */
    private void addAllClasses(CompilationUnit target,
                               CompilationUnit source) {
        for(TypeDeclaration<?> current : source.getTypes()) {
            target.addType(current.clone());
        }
    }

    /**
     * Returns the modifiers of a given class without
     * public, private, protected. Checks for unsupported modifiers.
     * @param inputClass
     * @return  the modifiers
     */
    private EnumSet<Modifier>
                checkModifiers(ClassOrInterfaceDeclaration inputClass)
                    throws WeaveException {
        EnumSet<Modifier> modifiers = inputClass.getModifiers().clone();
        if(inputClass.isPublic()) {
            modifiers.remove(Modifier.PUBLIC);
        } else {
            modifiers.remove(Modifier.PRIVATE);
            modifiers.remove(Modifier.PROTECTED);
            System.out.println("The class " + inputClass.getNameAsString() +
                               " is not public. The product class will be" +
                               " set to public.");
        }
        if(inputClass.isFinal()) {
            modifiers.remove(Modifier.FINAL);
            System.out.println("The class " + inputClass.getNameAsString() +
                               " is final. Final modifier will be removed.");
        }
        if(modifiers.contains(Modifier.NATIVE)) {
            modifiers.remove(Modifier.NATIVE);
            System.out.println("The class " + inputClass.getNameAsString() +
                               " is native. Native modifier will be removed.");
        }
        //Gather all the found unsupported modifiers, then return these in the
        //exception message and throw it
        boolean throwException = false;
        LinkedList<String> unsupportedModifiers = new LinkedList<String>();
        if(inputClass.isAbstract()) {
            throwException = true;
            unsupportedModifiers.add("abstract");
        }
        if(inputClass.isInterface()) {
            throwException = true;
            unsupportedModifiers.add("interface");
        }
        if(inputClass.isStatic()) {
            throwException = true;
            unsupportedModifiers.add("static");
        }
        if(modifiers.contains(Modifier.VOLATILE)) {
            throwException = true;
            unsupportedModifiers.add("volatile");
        }
        if(modifiers.contains(Modifier.SYNCHRONIZED)) {
            throwException = true;
            unsupportedModifiers.add("synchronized");
        }
        if(modifiers.contains(Modifier.TRANSIENT)) {
            throwException = true;
            unsupportedModifiers.add("transient");
        }
        if(throwException) {
            String exMessage = "The class " + inputClass.getNameAsString();
            exMessage = exMessage + " has unsupported modifiers: ";
            int i = 0;
            for(String current : unsupportedModifiers) {
                if (i == 0) {
                    exMessage = exMessage + current;
                } else {
                    exMessage = exMessage + ", " + current;
                }
                i++;
            }
            exMessage = exMessage + "\nWeaving aborted.";
            throw new WeaveException(exMessage);
        }
        return modifiers;
    }

    /**
     * Returns the modifiers of a given method without
     * public, private, protected. Checks for unsupported modifiers.
     * @param inputMethod
     * @return  the modifiers
     */
    private EnumSet<Modifier>
                checkModifiers(MethodDeclaration inputMethod)
                    throws WeaveException {
        EnumSet<Modifier> modifiers = inputMethod.getModifiers().clone();
        if(inputMethod.isPublic()) {
            modifiers.remove(Modifier.PUBLIC);
        } else {
            modifiers.remove(Modifier.PRIVATE);
            modifiers.remove(Modifier.PROTECTED);
            System.out.println("The method " + inputMethod.getNameAsString() +
                               " is not public. The product method will be" +
                               " set to public.");
        }
        if(inputMethod.isStrictfp()) {
            modifiers.remove(Modifier.STRICTFP);
        }
        if(inputMethod.isFinal()) {
            modifiers.remove(Modifier.FINAL);
            System.out.println("The method " + inputMethod.getNameAsString() +
                    " is final. Final modifier will be removed.");
        }
        if(modifiers.contains(Modifier.NATIVE)) {
            modifiers.remove(Modifier.NATIVE);
            System.out.println("The method " + inputMethod.getNameAsString() +
                    " is native. Native modifier will be removed.");
        }
        //Gather all the found unsupported modifiers, then return these in the
        //exception message and throw it
        boolean throwException = false;
        LinkedList<String> unsupportedModifiers = new LinkedList<String>();
        if(inputMethod.isAbstract()) {
            throwException = true;
            unsupportedModifiers.add("abstract");
        }
        if(modifiers.contains(Modifier.VOLATILE)) {
            throwException = true;
            unsupportedModifiers.add("volatile");
        }
        if(modifiers.contains(Modifier.SYNCHRONIZED)) {
            throwException = true;
            unsupportedModifiers.add("synchronized");
        }
        if(modifiers.contains(Modifier.TRANSIENT)) {
            throwException = true;
            unsupportedModifiers.add("transient");
        }
        if(throwException) {
            String exMessage = "The method " + inputMethod.getNameAsString();
            exMessage = exMessage + " has unsupported modifiers: ";
            int i = 0;
            for(String current : unsupportedModifiers) {
                if (i == 0) {
                    exMessage = exMessage + current;
                } else {
                    exMessage = exMessage + ", " + current;
                }
                i++;
            }
            exMessage = exMessage + "\nWeaving aborted.";
            throw new WeaveException(exMessage);
        }
        return modifiers;
    }

    /**
     * Adds all members of the source class to the target except
     * the specified method.
     * @param target
     * @param source
     * @param nameWithAddition
     *          The method to not add, with the correct modifier
     *          (that got added in the renaming process).
     * @return  The main Method, null if the main method could not be found.
     */
    private MethodDeclaration
            addMembersAndReturnMainMethod(ClassOrInterfaceDeclaration target,
                                          ClassOrInterfaceDeclaration source,
                                          String nameWithAddition) {
        NodeList<BodyDeclaration<?>> nodes = source.getMembers();
        MethodDeclaration mainMethod = null;
        for(BodyDeclaration<?> current : nodes) {
            if(!current.isMethodDeclaration()) {
                target.addMember(current.clone());
            } else if(!current.asMethodDeclaration().getName().toString()
                        .equals(nameWithAddition)) {
                target.addMember(current.clone());
            } else {
                mainMethod = current.asMethodDeclaration().clone();
                target.addMember(current.clone());
            }
        }
        return mainMethod;
    }

    /**
     * Transforms the given method into a method without return value,
     * adds a field to the given class.
     * @param inputMethod
     * @param nameOfField The name the returned field should have
     * @return the declaration of the field that got added,
     *         null if method was already void
     */
    @SuppressWarnings("unused")
    private FieldDeclaration
                transformToVoid(ClassOrInterfaceDeclaration correspondingClass,
                                MethodDeclaration inputMethod,
                                String nameOfField) {
        FieldDeclaration retField = null;
        Type type = inputMethod.getType();
        if (!type.isVoidType()) {
            //add return value to corresponding class
            retField =
                    correspondingClass.addField(type, nameOfField,
                                                Modifier.PUBLIC);
            for(Statement current:
                    inputMethod.getBody().get().getStatements()) {
                if (current.isReturnStmt()) {
                    AssignExpr replacement = new AssignExpr();
                    replacement.setOperator(AssignExpr.Operator.ASSIGN);
                    final NameExpr target =
                            retField.getVariable(0).getNameAsExpression();
                    replacement.setTarget(target);
                    Optional<Expression> returnExpr =
                            current.asReturnStmt().getExpression();
                    if(returnExpr.isPresent()) {
                        replacement.setValue(returnExpr.get());
                    } else {
                        //TODO Ausgabe von Exception, da vorausgesetzt wurde,
                        //     dass nur EIN return das ETWAS zurückgibt.
                    }
                    current.replace(new ExpressionStmt(replacement));
                }
            }
        }
        return retField;
    }

    /**
     * Adds all parameters of a source method to the target method
     * @param target
     * @param source
     */
    private void addParameters(MethodDeclaration target,
                               MethodDeclaration source) {
        //Sets the parameters
        for(Parameter current : source.getParameters()) {
            target.addParameter(current.clone());
        }
    }

    /**
     * Returns the body of a given method
     * @param input
     * @return
     */
    private BlockStmt getBody(MethodDeclaration input) {
        if(input.getBody().isPresent()) {
            return input.getBody().get();
        } else {
            return new BlockStmt();
        }
    }
    /**
     * Collects all implemented WeavingRules for use in weave
     * @return A Map of all rules, where keys are the names and values the rule
     * @throws IllegalAccessException   if a rule or its nullary constructor is
     *                                  not available
     * @throws InstantiationException   if instantiation failed for a rule
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     */
    private HashMap<String, WeavingRule> getRules ()
            throws IllegalAccessException, InstantiationException,
                   IllegalArgumentException, InvocationTargetException,
                   NoSuchMethodException, SecurityException {
        HashMap<String, WeavingRule> retMap =
                new HashMap<String, WeavingRule>();
        Reflections reflections = new Reflections(
                ClasspathHelper.forPackage("ProductProgramCreator"),
                new SubTypesScanner());
        Set<Class<? extends WeavingRule>> implementingClasses =
                reflections.getSubTypesOf(WeavingRule.class);
        // LinkedList<WeavingRule> rules = new LinkedList<WeavingRule>();
        for(Class<?> current : implementingClasses) {
            WeavingRule currentRule =
                    (WeavingRule)current.getConstructor().newInstance();
            currentRule.addMainWeaver(this);
            retMap.put(currentRule.getName(), currentRule);
        }
        return retMap;
    }

    /**
     * This method is used for determining which name the given statement
     * should be given (for weaving purpose).
     * It is also used to determine targets for method weaving.
     * @param stmt  The statement to check on
     * @return  'MethodCallExpr' if it is a method call expr, or does contain
     *          a method call to a method defined in this compUnit
     *          its correct name, otherwise
     *          Will always return correct name if automated weaving is off,
     *          except for MethodCallExpr without annotation it will return
     *          'noWeave'
     */
    private String checkExpressionStmt(Statement stmt) {
        //check if it is an assign expr with a method call inside
        //set its name to the corresponding result
        if(stmt.isExpressionStmt() && methodWeavingAutomated) {
            Expression expr = stmt.asExpressionStmt().getExpression();
            if(expr.isAssignExpr()) {
                AssignExpr assExpr = expr.asAssignExpr();
                MethodRule mr = new MethodRule();
                mr.addMainWeaver(this);
                return mr.hasMethodCall(assExpr);
            } else {
                return expr.getMetaModel().toString();
            }
        } else if (stmt.isExpressionStmt()){
            //only weave annotated methods
            Expression expr = stmt.asExpressionStmt().getExpression();
            boolean weave = hasMethodWeaveAnnotation(stmt);
            if(expr.isAssignExpr() && weave) {
                //check even if its already annotated, to prevent bugs
                AssignExpr assExpr = expr.asAssignExpr();
                MethodRule mr = new MethodRule();
                mr.addMainWeaver(this);
                return mr.hasMethodCall(assExpr);
            } else if(expr.isMethodCallExpr() && !weave) {
                //rename methodcallexpr if it has no annotation, so it will not
                //interfere with other method calls that  have it
                return "noWeave";
            } else {
                //return regular name
                final Expression stmtExpr =
                        stmt.asExpressionStmt().getExpression();
                return stmtExpr.getMetaModel().toString();
            }
        } else {
            return stmt.getMetaModel().toString();
        }
    }

    /**
     * Checks whether the comments of a given statement start with '@weave'
     * @param in
     * @return  true if it starts with '@weave', false otherwise
     */
    public boolean hasWeaveAnnotation (Statement in) {
        Optional<Comment> comment = in.getComment();
        if(comment.isPresent()) {
            return comment.get().getContent().trim().startsWith("@weave");
        } else {
            return false;
        }
    }

    /**
     * Removes the first symbol of the annotation so that it will not be used
     * again by the weave function
     * @param in
     */
    private void removeAnnotation (Statement in) {
        Optional<Comment> comment = in.getComment();
        if(comment.isPresent()) {
            Comment c = comment.get();
            c.setContent(" $" + c.getContent().trim().substring(1));
            in.setComment(c);
        }
    }

    /**
     * Returns the type of annotation of a given statement
     * @param in    A statement with a '@weave<type>' annotation
     * @return 'type', null if no annotation was found
     */
    @SuppressWarnings("unused")
    private String getAnnotationType(Statement in) {
        Optional<Comment> comment = in.getComment();
        if(comment.isPresent()) {
            String comString = comment.get().getContent();
            comString = comString.trim();
            comString = comString.substring(6, comString.indexOf(">"));
            return comString;
        } else {
            //it had no annotation, although expected
            return null;
        }
    }

    /**
     * Returns a list of the names of all methods that got declared inside this
     * unit.
     * @param cu
     * @return
     */
    private LinkedList<String> getDeclaredMethods(CompilationUnit cu) {
        MethodListVisitor lister = new MethodListVisitor();
        cu.accept(lister, new Object());
        return lister.getMethodList();
    }

    /**
     * Converts a given BlockStmt to a LinkedList of the statements in the same
     * order.
     * @param input A BlockStmt
     * @return  The list of statements
     */
    public LinkedList<Statement> blockToList (BlockStmt input) {
        NodeList<Statement> nList = input.getStatements();
        LinkedList<Statement> retList = new LinkedList<Statement>();
        retList.addAll(nList);
        return retList;
    }

    /**
     * Wether or not the methods are weaved automatically
     * @return true, if automatically, false otherwise
     */
    public boolean weaveMethodsAutomatically() {
        return this.methodWeavingAutomated;
    }

    /**
     * Returns a list of names of all in either compilation unit
     * declared methods.
     * @return
     */
    public LinkedList<String> getAllDeclaredMethods() {
        //add all additions since the last call
        this.allDeclaredMethods.addAll(getDeclaredMethods(this.retCu));
        return this.allDeclaredMethods;
    }

    /**
     * Checks if the given statements has a '@methodWeave' annotation at
     * the start of its comments.
     * @param in    The statement to check on
     * @return  'true' if it has, 'false' otherwise
     */
    private boolean hasMethodWeaveAnnotation(Statement in) {
        Optional<Comment> comment = in.getComment();
        if(comment.isPresent()) {
            String comAsString = comment.get().getContent().trim();
            return comAsString.startsWith("@methodWeave");
        } else {
            return false;
        }
    }

    /**
     * Checks for the given method in the compilation units.
     * If it finds it it will return it with its surrounding class' name
     * @param methodName the method to search for
     * @return  A one element HashMap with the name of the surrounding class as
     *          key and the MethodDeclaration as value.
     * @throws WeaveException if the method could not be found
     */
    public HashMap<String, MethodDeclaration>
            getMethodAndClass (String methodName) throws WeaveException {
        FindMethodVisitor visitor = new FindMethodVisitor (methodName);
        leftBackup.accept(visitor, new Object());
        if(!visitor.methodFound()) {
            rightBackup.accept(visitor, new Object());
        }
        if(visitor.methodFound()) {
            return visitor.getResults();
        } else {
            throw new WeaveException("The method declaration of: " +
                                     methodName +
                                     " could not be found.\nWeaving aborted");
        }
    }

    /**
     * Adds a given field declaration to the body of the main class of
     * the product.
     * @param toAdd
     */
    public void addDeclarationToMainClass(FieldDeclaration toAdd) {
        if(toAdd != null) {
            retCu.getClassByName(retMainClassName).get().addMember(toAdd);
        }
    }

    /**
     * Adds a given method to the body of the main class of the product
     * @param toAdd
     */
    public void addMethodToMainClass(MethodDeclaration toAdd) {
        if(toAdd != null) {
            retCu.getClassByName(retMainClassName).get().addMember(toAdd);
        }
    }

    /**
     * Checks wether a given method is already defined in the class
     * @param name
     * @return true if it is, false otherwise
     */
    public boolean methodIsDefined(String name) {
        LinkedList<String> list = getAllDeclaredMethods();
        return list.contains(name);
    }

    /**
     * Adds a given name to the list of methods that are declared inside
     * this CU. Is used for ensuring that a recursive method weaving does not
     * loop endlessly.
     * @param name
     */
    public void addNameToMethodList(String name) {
        this.allDeclaredMethods.add(name);
    }

    /**
     * Gets the type of an annotation on the given statement.
     * Expects there to be an annotation. Will return an empty
     * string otherwise.
     * @param in the statement to check on
     * @return the type, an empty String if none could be found
     */
    private String getTypeOfAnno(Statement in) {
        String retType = "";
        if(hasWeaveAnnotation(in)) {
            retType = in.getComment().get().getContent().trim().substring(6);
            retType = retType.substring(0, retType.indexOf(">"));
        }
        return retType;
    }

    /**
     * Gets the label of an annotation on the given statement.
     * Expects there to be an annotation. Will return an empty
     * string otherwise.
     * @param in the statement to check on
     * @return the label, an empty String if none could be found
     */
    private String getLabelOfAnno(Statement in) {
        String retLabel = "";
        if(hasWeaveAnnotation(in)) {
            retLabel = in.getComment().get().getContent().trim();
            retLabel = retLabel.substring(retLabel.indexOf("["),
                                          retLabel.indexOf("]"));
        }
        return retLabel;
    }
}
