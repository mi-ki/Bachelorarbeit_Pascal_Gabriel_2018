package ProductProgramCreator.Utilities;

import ProductProgramCreator.ParserMain;
import ProductProgramCreator.Visitors.AddingToNameVisitor;
import ProductProgramCreator.Visitors.ClassListVisitor;
import ProductProgramCreator.Visitors.MethodListVisitor;
import ProductProgramCreator.Visitors.VariableListVisitor;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

public class FileHandler {
    /**
     * Parses the java File at the given path and returns a CompilationUnit
     * @param filepath The path of the file to parse
     * @return CompilationUnit representing the Java source code
     * @throws FileNotFoundException if the file was not found
     * @throws ParseProblemException if the source code has parser errors
     */
    public CompilationUnit readFile(String filepath)
            throws FileNotFoundException, ParseProblemException {
        FileInputStream in = new FileInputStream(filepath);
        CompilationUnit retCU = JavaParser.parse(in);
        return retCU;
    }

    /**
     * Returns an Inputstream to a given file so that it can be parsed.
     * @param filepath
     * @return
     * @throws FileNotFoundException if the file could not be found
     */
    public InputStream fileStream(String filepath)
            throws FileNotFoundException {
        InputStream in = new FileInputStream(filepath);
        return in;
    }

    /**
     * Creates a file with the given name in the location given
     * and writes Java code to it.
     * @param code  The AST representing the java code
     * @param fileName  The name of the file, without extension.
     *                  Should match the class name.
     * @param location  The path to the directory in which to store the output
     *                  (No file separator at the end)
     * @throws IOException  if an I/O Error occurs writing to
     *                      or creating the file
     */
    public void writeFile(CompilationUnit code, String fileName,
                          String location) throws IOException {
        String property = location + File.separator + fileName + ".java";
        Files.write(new File(property).toPath(),
                    Collections.singleton(code.toString()),
                    StandardCharsets.UTF_8);
    }
    /**
     * Creates a file with the given name in the location of this class
     * or the respective .jar and writes the Java code to it.
     * @param code  The AST representing the Java code
     * @param fileName  The name of the file, without extension.
     *                  Should match the class name.
     * @throws IOException  if an I/O Error occurs writing to
     *                      or creating the file
     */
    public void writeFile(CompilationUnit code, String fileName)
            throws IOException {
        final CodeSource cs =
                ParserMain.class.getProtectionDomain().getCodeSource();
        final URL loc = cs.getLocation();
        try {
            File directory = new File(loc.toURI().getPath()).getParentFile();
            String property = directory + File.separator + fileName + ".java";
            Files.write(new File(property).toPath(),
                        Collections.singleton(code.toString()),
                        StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            //Do nothing as this exception will not happen since
            // the return value of "getLocation()" matches the needed standard.
        }
    }

    /**
     * Adds the given suffix to all names defined in the Compilation Unit.
     * @param code  the compilation unit to work on
     * @param addition  the suffix to add to the names
     */
    public void renameAllInFile(CompilationUnit code, String addition) {
        //only renames method calls of which an equally named method
        // was defined in the Comp Unit (can lead to errors)
        MethodListVisitor mLister = new MethodListVisitor();
        ClassListVisitor cLister = new ClassListVisitor();
        VariableListVisitor vLister = new VariableListVisitor();
        code.accept(mLister, new Object());
        code.accept(cLister, new Object());
        code.accept(vLister, new Object());
        LinkedList<String> allMethods = mLister.getMethodList();
        LinkedList<String> allClasses = cLister.getClassList();
        LinkedList<String> allVariables = vLister.getVariablesList();
        AddingToNameVisitor add =
                new AddingToNameVisitor(addition, allMethods,
                                        allClasses, allVariables);
        code.accept(add,new Object());
    }

    /**
     * Adds all the contents of the given compilation unit to the first unit in
     * the list.
     * It is expected to have no collisions.
     * @param unitsIn The compilation Units of multiple files
     * @return A single unit combined of all input units
     */
    public CompilationUnit combineUnits (LinkedList<CompilationUnit> unitsIn) {
        LinkedList<CompilationUnit> units = new LinkedList<CompilationUnit>();
        units.addAll(unitsIn);
        CompilationUnit retUnit = units.get(0).clone();
        units.removeFirst();
        for(CompilationUnit current: units) {
            //Add the imports
            for(ImportDeclaration currentImport: current.getImports()) {
                retUnit.addImport(currentImport.getNameAsString(),
                          currentImport.isStatic(),
                          currentImport.isAsterisk());
            }
            //Add all classes
            for(TypeDeclaration<?> currentClass : current.getTypes()) {
                retUnit.addType(currentClass.clone());
            }
        }
        return retUnit;
    }

    /**
     * Returns the Priority list of weaving rules in the given path
     * @param path The .txt-file containing the list
     * @return The files content as priorityList for weaving use
     * @throws FileNotFoundException if the file could not be found
     */
    public WeavingPriorityList getPriorityList(String path)
            throws FileNotFoundException {
        FileInputStream input = new FileInputStream(path);
        //TODO
        try { input.close(); } catch (IOException e) {}
        return null;
    }

    /**
     * Returns the priority list of the weaving rules defined in
     * the provided .txt file
     * @return The priorityList
     * @throws FileNotFoundException if the provided .txt could not be found
     */
    public WeavingPriorityList getPriorityList() throws IOException {
        ClassLoader classloader =
                Thread.currentThread().getContextClassLoader();
        InputStream input =
                classloader.getResourceAsStream("PriorityList.txt");
        if (input == null) {
            throw new FileNotFoundException();
        }
        LinkedList<String> retList = new LinkedList<String>();
        try {
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(input, "UTF-8"));
            //Read all lines in
            while(br.ready()) {
                retList.add(br.readLine());
            }
        } catch (UnsupportedEncodingException e) {
            //will never be thrown
            e.printStackTrace();
        }
        //Convert to a WeavingPriorityList
        WeavingPriorityList retPrioList = new WeavingPriorityList();
        Iterator<String> iterator = retList.iterator();
        while (iterator.hasNext()) {
            retPrioList.addRule(iterator.next());
        }
        return retPrioList;
    }

    /**
     * Prints a given array of Strings to a file. One member per line.
     * @param input The list of Strings
     * @param path The path to the file as String
     * @throws FileNotFoundException if the path was not correct
     */
    public void printStringToFile(LinkedList<String> input,
                                  String path) throws FileNotFoundException {
        PrintWriter printOut = new PrintWriter(new FileOutputStream(path));
        for(String current:input) {
            printOut.println(current);
        }
        printOut.close();
    }

    /**
     * Checks whether a given path is correct.
     * @param path The path as String
     * @return true if it is correct, false otherwise
     */
    public boolean isCorrectPath(String path) {
        try{
            Paths.get(path);
            return true;
        } catch (InvalidPathException e) {
            return false;
        }
    }
}
