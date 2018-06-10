package ProductProgramCreator;

import ProductProgramCreator.Utilities.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.util.List;

public class ParserMain {
    //Used to print all inputs to a file
    private static LinkedList<String> validInputs = new LinkedList<>();
    private static String configFileOutput;
    private static BufferedReader standardIn;
    private static boolean moreWeaving;
    /**
     * Initializes the JFrame and its contents.
     * Redirects System.in and System.out
     * Starts mainInteraction
     * @param args
     */
    public static void main(String[] args){
        //Create and set up the window.
        JFrame frame = new JFrame("Product Program Creator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Initialize and add its contents
        JTextField textField = new JTextField(80);
        JTextArea textArea = new JTextArea(20, 80);
        textArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.PAGE_START);
        panel.add(textField, BorderLayout.PAGE_END);
        frame.add(panel);
        //Display the window.
        frame.pack();
        frame.setVisible(true);

        //Redirect the System.in and System.out
        PrintStream printStream = new PrintStream(new TextAreaOutputStream(textArea));
        System.setOut(printStream);
        System.setErr(printStream);
        TextFieldInputStream readStream = new TextFieldInputStream(textField);
        //maybe this next line should be done in the TextFieldStreamer ctor
        //but that would cause a "leak a this from the ctor" warning
        textField.addActionListener(readStream);
        System.setIn(readStream);
        //Call the interaction method
        mainInteraction(args);
        //if there are more to weave
        while(moreWeaving) {
            mainInteraction(new String[0]);
        }
        System.exit(0);
    }

    /**
     * Interacts with the user to get all the data needed for the weaving process.
     * Starts the weaving.
     * @param args
     */
    public static void mainInteraction(String[] args){
        try {
            //new reader for the console & new file handler
            FileHandler fh = new FileHandler();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            standardIn = new BufferedReader(new InputStreamReader(System.in));
            if(args.length == 0) {
                //check if a configuration file should be printed
                print("Do you want to save the following configuration in a file?(Y/N)");
                boolean printOut = yesOrNoRead(br);
                //Remove this, as it should not appear in the file
                validInputs.removeLast();
                if(printOut) {
                    print("Please input the path for the file.");
                    String in = br.readLine();
                    boolean pathValid = false;
                    while(!pathValid) {
                        pathValid = fh.isCorrectPath(in);
                        if(pathValid) {
                            configFileOutput = in;
                        } else {
                            print("The File could not be found, please input again:");
                            in = br.readLine();
                        }
                    }
                } else {
                    //check if the input will go per file or per console
                    print("Do you want to use a file as input?(Y/N)");
                    boolean fileInput = yesOrNoRead(br);
                    if(fileInput) {
                        print("Please input the path for the file.");
                        br = new PrintingBufferedReader(new InputStreamReader(readFile(br)));
                    }
                }
            } else {
                br = new PrintingBufferedReader(new InputStreamReader(fh.fileStream(args[0])));
            }

            //Get all of the left files
            print("How many files do you want to use for your left file?");
            int leftNr = readInteger(br);
            print("Please input the path(s) for the left file(s).");
            if (leftNr > 1) {
                print("The first file will be treated as main file with the " +
                        "equally named main class.");
            }
            LinkedList<CompilationUnit> leftFiles = readFiles(br, leftNr);
            //Get the name of the left main method
            print("Please input the name of the left main method:");
            String leftMethodName = readMethod(br, leftFiles.get(0));

            //Get all of the right files
            print("How many files for your right file?");
            int rightNr = readInteger(br);
            print("Please input the path(s) for the right file(s).");
            if (rightNr > 1) {
                print("The first file will be treated as main file with the " +
                        "equally named main class.");
            }
            LinkedList<CompilationUnit> rightFiles = readFiles(br, rightNr);

            //Get the name of the right main method
            print("Please input the name of the right main method:");
            String rightMethodName = readMethod(br, rightFiles.get(0));

            //Get the suffixes for the renaming
            print("Please input the suffixes for the renaming.");
            print("Left suffix:");
            String leftSuffix = br.readLine();
            validInputs.add(leftSuffix);
            print("Right suffix:");
            String rightSuffix = br.readLine();
            //check if they are different
            while(leftSuffix.equals(rightSuffix)) {
                print("Suffixes must be different. Input right suffix again:");
                rightSuffix = standardIn.readLine();
            }
            validInputs.add(rightSuffix);

            //get the folder on where to save the output
            print("Do you want to specify the location for the output?(Y/N)");
            boolean askOutputBool = yesOrNoRead(br);
            String outputPath = "";
            if (askOutputBool) {
                print("Please input the folder for the output:");
                //make sure the input is a valid path
                outputPath = isValidPath(br);
            }
            print("Do you want to specify a file for the priority list?(Y/N)");
            boolean customPrio = yesOrNoRead(br);
            String prioPath = "";
            if(customPrio) {
                print("Please input the path to the file:");
                prioPath = isValidPath(br);
            }
            print("Do you want to weave methods automatically(Y) or only if annotated(N)?");
            boolean weaveMethodsAuto = yesOrNoRead(br);
            //combine the units into one compilation unit per side
            //set the storage component for later use
            CompilationUnit left = fh.combineUnits(leftFiles);
            left.setStorage(leftFiles.get(0).getStorage().get().getPath());
            CompilationUnit right = fh.combineUnits(rightFiles);
            right.setStorage(rightFiles.get(0).getStorage().get().getPath());

            //rename the contents
            fh.renameAllInFile(left, leftSuffix);
            fh.renameAllInFile(right, rightSuffix);
            //initiate the weaving process, check if custom priority list is given
            MainWeaver weaver;
            if(customPrio) {
                weaver = new MainWeaver(leftSuffix, rightSuffix, leftMethodName,
                        rightMethodName, prioPath, weaveMethodsAuto);
            } else {
                weaver = new MainWeaver(leftSuffix, rightSuffix, leftMethodName,
                                                                rightMethodName);
            }
            weaver.setMethodWeaving(weaveMethodsAuto);
            CompilationUnit out = weaver.initiateWeave(left, right);
            String leftName = left.getStorage().get().getFileName().toString();
            leftName = leftName.substring(0, leftName.length() - 5);
            String rightName = right.getStorage().get().getFileName().toString();
            rightName = rightName.substring(0, rightName.length() - 5);
            String outputName = leftName + leftSuffix + "_x_" + rightName
                                                                + rightSuffix;

            //write the output file
            if(askOutputBool) {
                fh.writeFile(out, outputName, outputPath);
                print("Weaving complete.");
            } else {
                fh.writeFile(out, outputName);
                print("The output can be found in the directory of this .jar");
            }
            if(configFileOutput != null) {
                fh.printStringToFile(validInputs, configFileOutput);
            }
            print("Do you want to do another weaving process?(Y/N)");
            moreWeaving = yesOrNoRead(standardIn);
        } catch (IOException e) {
            print("An I/O-Exception has occurred:\n" + e.toString());
        } catch (WeaveException e) {
            print(e.toString());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            print("The instantiation of a rule has failed:");
            e.printStackTrace();
        }
    }

    /**
     * Calls System.out.println
     * @param toPrint
     */
    private static void print (String toPrint) {
        System.out.println(toPrint);
    }

    /**
     * Reads in an integer, retries until successfull.
     * @param br A buffered Reader to be used
     * @return
     * @throws IOException
     */
    private static int readInteger(BufferedReader br) throws IOException{
        //Indicates if a reading process has been successfull
        BufferedReader b = br;
        boolean readComplete = false;
        int retNr = 0;
        while (!readComplete) {
            try {
                retNr = Integer.parseInt(b.readLine());
                if(retNr > 0) {
                    readComplete = true;
                } else {
                    print("Number has to be > 0! Input again:");
                    //use standardinput for error handling
                    b = standardIn;
                }
            } catch (NumberFormatException e){
                print("Invalid Format! Input again:");
                b = standardIn;
            }
        }
        validInputs.add(retNr +"");
        return retNr;
    }
    /**
     * Reads in a file that will be used for the input informations.
     * @param b
     * @return  An InputStream to the input file
     * @throws IOException
     */
    private static InputStream readFile
    (BufferedReader b) throws IOException{
        InputStream ret = System.in;
        FileHandler fh = new FileHandler();
        boolean readComplete = false;
        while (!readComplete) {
            String input = b.readLine();
            try {
                ret = fh.fileStream(input);
                readComplete = true;
            } catch(FileNotFoundException e ) {
                print("File could not be found! Input again:");
            }
        }
        return ret;
    }
    /**
     * Reads in a given number of files and converts them to a compilationUnit
     * @param br
     * @param amount
     * @return  A list of the files as Compilation Units
     * @throws IOException
     */
    private static LinkedList<CompilationUnit> readFiles
            (BufferedReader br, int amount) throws IOException{
        BufferedReader b = br;
        LinkedList<CompilationUnit> retList = new LinkedList();
        FileHandler fh = new FileHandler();
        String[] inputsToWrite = new String[amount];
        for (int i = 0; i < amount; i++) {
            print("File Nr." + (i+1) + ":");
            String input = b.readLine();
            inputsToWrite[i] = input;
            //add the file as compUnit and set its internal storage path
            //exceptions caught will let the user input the path again
            try {
                retList.add(fh.readFile(input));
                Path filepath = Paths.get(input);
                retList.get(i).setStorage(filepath);
                //tests if there is a main class with the corresponding name
                if (i == 0) {
                    String className = filepath.getFileName().toString();
                    className = className.substring(0, className.length() - 5);
                    if(!retList.get(0).getClassByName(className).isPresent()) {
                        print("There is no equally named class in this file." +
                                " Input again!");
                        i = i-1;
                        b = standardIn;
                    } else {
                        b = br;
                    }
                }
                if (i > 0) {
                    b = br;
                }
            } catch(FileNotFoundException e ) {
                print("File could not be found! Input again:");
                i = i-1;
                b = standardIn;
            } catch(ParseProblemException e) {
                print("The given file could not be parsed! Input again:");
                i = i-1;
                b = standardIn;
            }
        }
        //Add all valid inputs to the list
        for(String current: inputsToWrite) {
            validInputs.add(current);
        }
        return retList;
    }

    /**
     * Reads in the name of the main method and checks if there is exactly one.
     * @param br
     * @param main  The compUnit representing the main file
     * @return
     * @throws IOException
     */
    private static String readMethod (BufferedReader br, CompilationUnit main)
                                                            throws IOException {
        BufferedReader b = br;
        String ret = "";
        boolean readComplete = false;
        while(!readComplete) {
            ret = b.readLine();

            //get the main class of the main file
            String className = main.getStorage().get().getFileName().toString();
            className = className.substring(0, className.length() - 5);
            ClassOrInterfaceDeclaration c = main.getClassByName(className).get();

            //Count the methods with the given name
            int amountMethods = 0;
            for(BodyDeclaration<?> current : c.getMembers()) {
                if (current.isMethodDeclaration()) {
                    String name = current.asMethodDeclaration().getName().toString();
                    if (name.equals(ret)) {
                        amountMethods++;
                    }
                }
            }

            //Restart if there is not exactly one method with this name
            if (amountMethods == 0) {
                print("The given method is not in this class. Input again!");
                b = standardIn;
            } else if (amountMethods == 1) {
                readComplete = true;
            } else {
                print("Only one method with this name may exist in this class." +
                        " Input again!");
                b = standardIn;
            }
        }
        validInputs.add(ret);
        return ret;
    }

    /**
     * Checks wether the input was Y or N.
     * Reasks for input if it was neither.
     * @param br    The current BufferedReader
     * @return true if Y, false if N
     * @throws IOException  if an I/O-Exception has occurred
     */
    private static boolean yesOrNoRead (BufferedReader br) throws IOException {
        String askOutput = br.readLine();
        boolean askOutputBool = askOutput.equalsIgnoreCase("Y");
        //Assure the answer was either y or n
        while(!askOutputBool && !askOutput.equalsIgnoreCase("N")) {
            print("Please answer either 'Y' or 'N' :");
            askOutput = standardIn.readLine();
            askOutputBool = askOutput.equalsIgnoreCase("Y");
        }
        if(askOutputBool) {
            validInputs.add("Y");
        } else {
            validInputs.add("N");
        }
        return askOutputBool;
    }

    /**
     * Reads in a path and checks if it is valid.
     * Reasks if input was invalid
     * @return the resulting valid path
     * @throws IOException if an I/O-Exception has occurred
     */
    private static String isValidPath(BufferedReader br) throws IOException {
        BufferedReader b = br;
        String retPath = "";
        boolean isValid = false;
        while(!isValid) {
            retPath = b.readLine();
            try {
                Paths.get(retPath);
                isValid = true;
            } catch (InvalidPathException e) {
                print("Not a valid path. Input again:");
                b = standardIn;
            }
        }
        validInputs.add(retPath);
        return retPath;
    }
}
