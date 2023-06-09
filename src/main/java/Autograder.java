import java.util.List;
import java.util.ArrayList;
//import java.util.Scanner; 
import java.util.Random;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.tools.ToolProvider;
import javax.tools.JavaCompiler;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.ProcessBuilder.Redirect;
import org.junit.runner.notification.RunListener;
import org.junit.runner.JUnitCore;

import java.util.concurrent.FutureTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

import java.lang.SecurityManager;
import java.lang.SecurityException;
import java.security.Permission;

import com.puppycrawl.tools.checkstyle.Main;

import jh61b.grader.TestResult;
import jh61b.grader.TestResultList; 

import brandon.math.Math;
import brandon.convert.ClassConverter;
import brandon.convert.ClassConverterList;
//import brandon.util.Scanner;

/**
   Classs representing an autograder.
   It's main method is the running of the autograder
   and instances can be made to store all important information.
   @author Brandon Lax
*/
public class Autograder {

   /**A checksum to ensure all additions of tests.*/
   private long checksum;
   
   /** The value of each test.*/
   protected double maxScore;
   
   /**The current test number we are on.*/
   protected int diffNum;

   /**The visibilty for the current gradescope test.*/
   protected String visibility;

   /**The list of all tests performed.*/
   private TestResultList allTestResults;
   
   /**The current junit test.*/
   private TestResult currentJunitTestResult;

   private static ClassConverterList conversions = new ClassConverterList();
   
   /**The location of the checkstyle Jar.*/
   public static final String CHECKSTYLE_JAR = "/autograder/source/checkstyle/checkstyle-8.28-all.jar";

   /**The location of the checkstyle xml.*/
   public static final String CHECKSTYLE_XML = "/autograder/source/checkstyle/check112.xml";

   public static final String CHECKSTYLE_LISTEN_XML = "/autograder/source/checkstyle/check112listen.xml";
   //public static final String CHECKSTYLE_LISTEN_XML = "checkstyle/check112listen.xml";
   
   /**The amount of time to wait before timing out a test that runs student code.*/
   private long waitTime = 1;


   /**
      The Autograder class constructor.
      Initializes the list of all tests.
      @param visible The visibility of the result to students see {@link #setVisibility(int) setvisibility}
      @param score The amount of points a test is worth
   */
   public Autograder(int visible, double score) {
      Random r = new Random();
      this.checksum = r.nextLong();
      this.allTestResults = new TestResultList(this.checksum);
      this.diffNum = 1;
      this.setVisibility(visible);
      this.setScore(score);
      this.disableSystemExit();
   }

   private void disableSystemExit() {
      SecurityManager securityManager = new StopExitSecurityManager();
      System.setSecurityManager(securityManager) ;
   }

   private void enableSystemExit() {
      SecurityManager mgr = System.getSecurityManager();
      if ((mgr != null) && (mgr instanceof StopExitSecurityManager)) {
         StopExitSecurityManager smgr = (StopExitSecurityManager)mgr;
         System.setSecurityManager(smgr.getPreviousMgr());
      }
      else {
         System.setSecurityManager(null);
      }
   }

   /**
      The Autograder class constructor.
      Initializes the list of all tests.
      Also sets the visibility to hidden and 
      the score to 0.1
   */
   public Autograder() {
      this(1, 0.1);
   }
   
   /** Method to add a seperately made test to the results.
       This allows for people to make child classes of the autograder
       if they need tests that dont currently exist that they would 
       prefer to avoid adding to this class. For an example see
       {@link PictureAutograder}
       @param t the test to be added to the output
    */
   public void addTestResult(TestResult t) {
      this.allTestResults.add(t, this.checksum);
   }
   
   /** Method to add a user written converter to the autograder.
       Converters are used for the comp tests. This allows you 
       to make comp tests that work with parameters and returns
       other than the base primitive types and string. A converter
       takes a string of text and turns it into the desired object
       and performs the reverse operation as well. See 
       {@link brandon.convert.ClassConverter}.
       @param c The converter to add to the autograder
    */
   public static void addConverter(ClassConverter c) {
      Autograder.conversions.add(c);
   }
   
   /** This is the wrap-up code of the autograder.
       <b>Must be the last line of the main method.</b> It
       prints all of the results in a JSON format to 
       standard out.
       @throws Exception fails to create json for a test 
    */
   public void testRunFinished() throws Exception {  
      this.enableSystemExit();
      /* Dump allTestResults to StdOut in JSON format. */
      ArrayList<String> objects = new ArrayList<String>();
      for (TestResult tr : this.allTestResults.toArray(this.checksum)) {
         objects.add(tr.toJSON());
      }
      String testsJSON = String.join(",", objects);
      
      System.out.println("{" + String.join(",", new String[] {
               String.format("\"tests\": [%s]", testsJSON)}) + "}");
      //System.exit(0);
   }

   /** This is the wrap-up code of the autograder.
       <b>Must be the last line of the main method.</b> It
       prints all of the results in a JSON format to 
       a specified file.
       @param filename the file to write the output to
       @throws Exception fails to create json for a test 
    */
   public void testRunFinished(String filename) throws Exception {  
      this.enableSystemExit();
      /* Dump allTestResults to StdOut in JSON format. */
      PrintWriter pw = new PrintWriter(filename);
      ArrayList<String> objects = new ArrayList<String>();
      for (TestResult tr : this.allTestResults.toArray(this.checksum)) {
         objects.add(tr.toJSON());
      }
      String testsJSON = String.join(",", objects);
      
      pw.println("{" + String.join(",", new String[] {
               String.format("\"tests\": [%s]", testsJSON)}) + "}");
      pw.close();
      System.exit(0);
   }

   
   /**
    * Test to check if source file exists.
    * Will output whether the file exists as well as 
    * add a junit test for it.
    * @param programName the program name (can include or not include the .java)
    * @return whether or not the source exists
    */
   public boolean testSourceExists(String programName) {
      boolean sourceExists = false;
   
      File source;
      if (programName.indexOf(".") == -1) {
         source = new File(programName + ".java");
      } else {
         source = new File(programName);
      }
      TestResult trSourceFile = new TestResult(programName +
                                               " Source File Exists", 
                                               "Pre-Test",
                                               this.maxScore, 
                                               this.visibility);
      
      if (!source.exists() || source.isDirectory()) { // source not present
         trSourceFile.setScore(0);
         trSourceFile.addOutput("ERROR: file " + programName +
                                 ".java is not present!\n");
         trSourceFile.addOutput("\tCheck the spelling of your file name.\n");
      } else { // source present
         trSourceFile.setScore(this.maxScore);
         trSourceFile.addOutput("SUCCESS: file " + programName +
                                 ".java is present!\n");
         sourceExists = true;
      }
      this.allTestResults.add(trSourceFile, this.checksum);
      return sourceExists;
   }

   /**
      Method to compile a java file for you.
      NOTE: <b> This is not a test </b>
      This just compiles a file. It is useful
      for when you need a test file but dont want
      to compile it until you know with certainty
      that the base file compiles.
      @param fileName The filename of the file to compile
      @return the int result of the java compiler 0 for success, nonzero otherwise 
    */
   public int compile(String fileName) {
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      return compiler.run(null, null, null, fileName);
   }


   /** Function to test if a class compiles.
       Outputs whether the file compiles as well as 
       adds an additional gradescope test for it.
       If the program fails due to unmappable characters,
       the test will automatically be made visible as this is 
       a problem created on gradescope and might not be a problem
       that a student will see on their own device.
       @param programName the name of the java file to test (without the .java) 
       @return whether the class compiled
    */
   public boolean testCompiles(String programName) {
      boolean passed = false;
      TestResult trCompilation = new TestResult(programName + " Compiles",
                                                 "Pre-Test", this.maxScore,
                                                this.visibility);
      String fileName = programName + ".java";
      JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayOutputStream err = new ByteArrayOutputStream();
      int compilationResult = compiler.run(null, out, err, fileName);
      if (compilationResult != 0) {
         String output = new String(out.toByteArray());
         String error = new String(err.toByteArray());
         if (error.contains("unmappable character")) {
            trCompilation = new TestResult(programName + " Compiles",
                                           "Pre-Test", this.maxScore,
                                           "visible");
         } 
         trCompilation.setScore(0);
         trCompilation.addOutput("ERROR: " + programName + 
                                 ".java did not compile!\n");
         trCompilation.addOutput("Output: " + output + "\n"
                                 + "Error: " + error);
      } 
      else {
         trCompilation.setScore(this.maxScore);
         trCompilation.addOutput("SUCCESS: " + programName + 
                                  ".java compiled successfully!\n");
         passed = true;
      }
      this.allTestResults.add(trCompilation, this.checksum);
      return passed;
   }
   
   
   /**
    * Checks if checkstyle passed.
    * Creates a gradescope test with the
    * results from checkstyle and gives the 
    * output to the grader. Relies on having 
    * the checkstyle files (the jar and xml)
    * in the location of CHECKSTYLE_JAR and 
    * CHECKSTYLE_XML. Please change those to 
    * match the location for your class.
    * This also assumes that the java files 
    * is in the source folder of the 
    * autograder when uploaded to gradescope.
    * @param programName the java class name (without the .java)
    */
   public void testCheckstyle(String programName) {
      TestResult trCheck = new TestResult(programName + "Checkstyle Compliant",
                                          "Pre-Test",
                                           this.maxScore, this.visibility);      
      String result;
      try {
         String proc = "java -jar " + CHECKSTYLE_JAR +
            " -c " + CHECKSTYLE_XML + " /autograder/source/" +
            programName + ".java";
         Process check = Runtime.getRuntime().exec(proc);
         check.waitFor();  
         java.util.Scanner s = new java.util.Scanner(check.getInputStream()).useDelimiter("\\A");
         result = s.hasNext() ? s.next() : "";
         //no problems reported in checkstylefile; it passed checkstyle
         if (result.equals("Starting audit...\nAudit done.\n")) {
            trCheck.setScore(this.maxScore);
            trCheck.addOutput("SUCCESS: " + programName +
                               " passed checkstyle with no warnings\n");
         }
         else {  //something in checkstylefile; it failed checkstyle
            trCheck.setScore(0);
            trCheck.addOutput("ERROR: " + programName +
                               " did not pass checkstyle." + 
                              " Results are below:\n" + result);
         }
         
      }  catch (IOException e) {
         return;
      } catch (InterruptedException e) {
         return;
      }
      this.allTestResults.add(trCheck, this.checksum);
   }

   
   /**
      A test that runs a checkstyle test sorting the output.
      This test takes off for either each type of mistake or 
      each mistake that a student has. 
      It also formats the output for easy grading by hand. It 
      shows each type of error and all the lines on which that error occurs.
      To use this test you need the CHECKSTYLE_LISTEN_XML to match the location 
      of the xml file you use and this xml file has to have the listener configured
      as specified in the README.
      @param programName the classname of the java file to run the test one
      @param errValue the number of points lost per type of checkstyle error
      @param perType true if taking off per type of mistake, false if per mistake
    */
   public void testSortedCheckstyle(String programName, double errValue, boolean perType) {
      PrintStream originalOut = System.out;
      try {
         GatewayCheckstyleListener.setDefaultValues(this.maxScore, errValue, this.visibility, perType);
         System.setOut(new PrintStream(
         new OutputStream() {
            public void write(int b) {
            
            }
         }));
         Main.main("-c", CHECKSTYLE_LISTEN_XML, programName + ".java");
      } catch(ExitTrappedException e) {
         //Ignore the exception
      } catch (Exception e) {
         System.err.println("Failed to run checkstyle on file: " + programName + "\n");
      }
      System.setOut(originalOut);
      List<TestResult> all = GatewayCheckstyleListener.getResults();
      this.allTestResults.addAll(all, this.checksum);
   }

   
   /**
      Runs all the diff tests for a specific file.
      This runs count diff tests each one using the naming 
      convetion on the next line for the name of the input file.
      All input files are named: {Program_Name}_Diff_#.in
      The diff test have the option of not using a sample program
      and instead using already made output files. This approach
      is good if there is a sample run created.
      The expected output should be named:
      {program_Name}_#.expected
      @param name the name of the program to do diff tests on
      @param count the number of diffs to perform
      @param sampleFile true if using a sample program false if just comparing to a file.
      @param ignoreWhitespace true if want to ignore whitespace false otherwise
   */
   public void stdOutDiffTests(String name, int count, boolean sampleFile, boolean ignoreWhitespace) {
      this.stdOutDiffTests(name, count, sampleFile, ignoreWhitespace, 0);
   }

   /**
      Runs all the diff tests for a specific file.
      This runs count diff tests each one using the naming 
      convetion on the next line for the name of the input file.
      All input files are named: {Program_Name}_Diff_#.in
      The diff test have the option of not using a sample program
      and instead using already made output files. This approach
      is good if there is a sample run created.
      The expected output should be named:
      {program_Name}_#.expected
      This version allows for a certain amount to be forced to be
      hidden. 
      @param name the name of the program to do diff tests on
      @param count the number of diffs to perform
      @param sampleFile true if using a sample program false if just comparing to a file.
      @param ignoreWhitespace true if want to ignore whitespace false otherwise
      @param numVisible The number of tests that should be visible instead of what visibility is set to (count - numVisible) = numHidden
   */
   public void stdOutDiffTests(String name, int count, boolean sampleFile, boolean ignoreWhitespace, int numVisible) {
      PrintStream originalOut = System.out;
      InputStream originalIn = System.in;
      if (sampleFile) {
         this.compile(name+"Sample.java");
      }
      String visible = this.visibility;
      this.setVisibility(0);
      for (int i = 0; i < count; i++) {
         Math.resetRandom();
         if (i >= numVisible) {
            this.visibility = visible;
         }
         TestResult trDiff = new TestResult(name + " Standard Output Diff Test #" + i,
                                            "" + this.diffNum,
                                            this.maxScore, this.visibility);
         this.diffNum++;
         String input = name + "_Diff_" + i + ".in";
         String exOut = name + i + ".expected";
         String acOut = name + "_" + i + ".out";
         try {
            File exfile = new File(exOut);
            File infile = new File(input);
            File acfile = new File(acOut);
            File sample = new File(name + "Sample.java");
            if (sampleFile && sample.exists() && !sample.isDirectory()) {
               String[] procSample = {"java", name + "Sample"};
               ProcessBuilder pbSample = new ProcessBuilder(procSample);
               pbSample.redirectOutput(Redirect.to(exfile));
               pbSample.redirectInput(Redirect.from(infile));
               Process sampleProcess = pbSample.start();
               sampleProcess.waitFor();
            }
            PrintStream out = new PrintStream(new FileOutputStream(acOut));
            System.setOut(out);
            System.setIn(new FileInputStream(input));
            Class<?> act = Class.forName(name);
            if (act == null) {
               throw new ClassNotFoundException();
            }
            Method main = act.getMethod("main", String[].class);
            if (main == null) {
               throw new NoSuchMethodException();
            }
            String[] strings = new String[0];
            this.runMethodWithTimeout(main, null, ((Object)strings));
            out.flush();
            out.close();
            diffFiles(trDiff, name, exOut, acOut, ignoreWhitespace);
         } catch (IOException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: " + name + 
                          " could not be found to run Diff Test");
         } catch (InterruptedException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: " +  name +
                          " got interrupted");
         } catch (ClassNotFoundException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: " + name + 
                             " could not be found to run Diff Test");
         } catch (NoSuchMethodException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students " +
                             name + " Main method not found");
         } catch (IllegalAccessException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students code not accessible");
         } catch (InvocationTargetException e) {
            Throwable et = e.getCause();
            Exception es;
            if(et instanceof Exception) {
               es = (Exception) et;
            } else {
               es = e;
            }
            if (es instanceof ExitTrappedException) {
               trDiff.setScore(0);
               trDiff.addOutput("ERROR: Do not use System.exit() in your code" 
                                + " its bad practice and can cause the autograder" + 
                                " to crash.");
            } else {
            String sStackTrace = stackTraceToString(es);
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students code threw " + 
                             es + "\n Stack Trace: " +
                             sStackTrace);
            }
         } catch (TimeoutException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Main Method Timed out after "+ waitTime + " Seconds");
         } catch (ExecutionException e) {
            Throwable et = e.getCause();
            Exception es;
            if(et instanceof Exception) {
               es = (Exception) et;
            } else {
               es = e;
            }
            if (es instanceof InvocationTargetException) {
               et = es.getCause();
               if(et instanceof Exception) {
                  es = (Exception) et;
               } else {
                  es = e;
               }
            }
            if (es instanceof ExitTrappedException) {
               diffFiles(trDiff, name, exOut, acOut, ignoreWhitespace);
            } else {
            String sStackTrace = stackTraceToString(es);
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students code threw " + 
                             es + "\n Stack Trace: " +
                             sStackTrace);
            }

         }
         this.allTestResults.add(trDiff, this.checksum);
         System.setOut(originalOut);
      }
      this.visibility = visible;
   }

   /**
      Runs a diff test on the std output of a specific java program.
      This test runs one diff test comparing the output of the java program
      against either a sampole implementation or a hand written file.
      These tests rely on redirecting input from a file to standard 
      input. These tests will not work as expected if the student 
      opens multiple Scanners to read from standard input. inFile
      should be a filename without an extension. Thsi test auto-appends
      extensions onto the filename. The input file should end in .in, 
      the expected output file should end in .expected and the program
      will always create the students output file which will end in 
      .out.
      @param name the name of the java program to do diff tests on
      @param inFile the name of the test input file to use without an extension
      @param sampleFile true if using a sample program false if just comparing to a file.
      @param ignoreWhitespace true if want to ignore whitespace false otherwise
   */
   public void stdOutDiffTest(String name, String inFile, boolean sampleFile, boolean ignoreWhitespace) {
      PrintStream originalOut = System.out;
      InputStream originalIn = System.in;
      if (sampleFile) {
         this.compile(name+"Sample.java");
      }
      Math.resetRandom();
      TestResult trDiff = new TestResult(name + " Standard Output Diff Test For " + inFile,
                                         "" + this.diffNum,
                                         this.maxScore, this.visibility);
      this.diffNum++;
      String input = inFile + ".in";
      String exOut = inFile + ".expected";
      String acOut = inFile + ".out";
      try {
         File exfile = new File(exOut);
         File infile = new File(input);
         File acfile = new File(acOut);
         File sample = new File(name + "Sample.java");
         if (sampleFile && sample.exists() && !sample.isDirectory()) {
            String[] procSample = {"java", name + "Sample"};
            ProcessBuilder pbSample = new ProcessBuilder(procSample);
            pbSample.redirectOutput(Redirect.to(exfile));
            pbSample.redirectInput(Redirect.from(infile));
            Process sampleProcess = pbSample.start();
            sampleProcess.waitFor();
         }
         PrintStream out = new PrintStream(new FileOutputStream(acOut));
         System.setOut(out);
         System.setIn(new FileInputStream(input));
         Class<?> act = Class.forName(name);
         if (act == null) {
            throw new ClassNotFoundException();
         }
         Method main = act.getMethod("main", String[].class);
         if (main == null) {
            throw new NoSuchMethodException();
         }
         String[] strings = new String[0];
         this.runMethodWithTimeout(main, null, ((Object)strings));
         out.flush();
         out.close();
         diffFiles(trDiff, name, exOut, acOut, ignoreWhitespace);
      } catch (IOException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: " + name + 
                          " could not be found to run Diff Test");
      } catch (InterruptedException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: " +  name +
                          " got interrupted");
      } catch (ClassNotFoundException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: " + name + 
                          " could not be found to run Diff Test");
      } catch (NoSuchMethodException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: Students " +
                          name + " Main method not found");
      } catch (IllegalAccessException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: Students code not accessible");
      } catch (InvocationTargetException e) {
         Throwable et = e.getCause();
         Exception es;
         if(et instanceof Exception) {
            es = (Exception) et;
         } else {
            es = e;
         }
         if (es instanceof ExitTrappedException) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Do not use System.exit() in your code" 
                             + " its bad practice and can cause the autograder" + 
                             " to crash.");
         } else {
            String sStackTrace = stackTraceToString(es);
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students code threw " + 
                             es + "\n Stack Trace: " +
                             sStackTrace);
         }
      } catch (TimeoutException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: Main Method Timed out after "+ waitTime + " Seconds");
      } catch (ExecutionException e) {
         Throwable et = e.getCause();
         Exception es;
         if(et instanceof Exception) {
            es = (Exception) et;
         } else {
            es = e;
         }
         if (es instanceof InvocationTargetException) {
            et = es.getCause();
            if(et instanceof Exception) {
               es = (Exception) et;
            } else {
               es = e;
            }
         }
         if (es instanceof ExitTrappedException) {
            diffFiles(trDiff, name, exOut, acOut, ignoreWhitespace);
         } else {
            String sStackTrace = stackTraceToString(es);
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students code threw " + 
                             es + "\n Stack Trace: " +
                             sStackTrace);
         }

      }
      this.allTestResults.add(trDiff, this.checksum);
      System.setOut(originalOut);
   }


   /**
      Runs all the diff tests for a specific java program comparing resulting file output.
      This runs count diff tests each one using the naming 
      convetion on the next line for the name of the input file.
      All input files are named: {Program_Name}_Diff_#.in
      For this method to work correctly, the sample implementation
      and the student submission should always write to the same file.
      If you need different filenames for each run, you should use
      {@link #logFileDiffTest(String, String, String, String, boolean) logFileDiffTest}
      with a loop to modify the filename before calling each run.
      @param name the name of the program to do diff tests on
      @param count the number of diffs to perform
      @param logFile the filename of the file the students code writes to and should be compared
      @param sampleLogFile the filename that the sample code writes to and should be used for comparison
      @param ignoreWhitespace true if want to ignore whitespace false otherwise
   */
   public void logFileDiffTests(String name, int count, String logFile,
                                String sampleLogFile, boolean ignoreWhitespace) {
      this.logFileDiffTests(name, count, 0, logFile, sampleLogFile, ignoreWhitespace);
   }

   
   /**
      Runs all the diff tests for a specific java program comparing resulting file output.
      This runs count diff tests each one using the naming 
      convetion on the next line for the name of the input file.
      All input files are named: {Program_Name}_Diff_#.in
      For this method to work correctly, the sample implementation
      and the student submission should always write to the same file.
      If you need different filenames for each run, you should use
      {@link #logFileDiffTest(String, String, String, String, boolean) logFileDiffTest}
      with a loop to modify the filename before calling each run.
      @param name the name of the program to do diff tests on
      @param count the number of diffs to perform
      @param numVisible The number of tests that should be shown to students (count - numVisible) = numHidden
      @param logFile the filename of the file the students code writes to and should be compared
      @param sampleLogFile the filename that the sample code writes to and should be used for comparison
      @param ignoreWhitespace true if want to ignore whitespace false otherwise
   */
   public void logFileDiffTests(String name, int count, int numVisible, String logFile,
                                String sampleLogFile, boolean ignoreWhitespace) {
      PrintStream originalOut = System.out;
      InputStream originalIn = System.in;
      this.compile(name+"Sample.java");
      String visible = this.visibility;
      this.setVisibility(0);
      for (int i = 0; i < count; i++) {
         Math.resetRandom();
         if (i >= numVisible) {
            this.visibility = visible;
         }
         TestResult trDiff = new TestResult(name + " Log File Diff Test #" + i,
                                            "" + this.diffNum,
                                            this.maxScore, this.visibility);
         this.diffNum++;
         String input = name + "_Diff_" + i + ".in";
         String exOut = name + i + ".expected";
         String acOut = name + "_" + i + ".out";
         try {
            File exfile = new File(exOut);
            File infile = new File(input);
            File acfile = new File(acOut);
            File sample = new File(name + "Sample.java");
            if (sample.exists() && !sample.isDirectory()) {
               String[] procSample = {"java", name + "Sample"};
               ProcessBuilder pbSample = new ProcessBuilder(procSample);
               pbSample.redirectOutput(Redirect.to(exfile));
               pbSample.redirectInput(Redirect.from(infile));
               Process sampleProcess = pbSample.start();
               sampleProcess.waitFor();
            }
            PrintStream out = new PrintStream(new FileOutputStream(acOut));
            System.setOut(out);
            System.setIn(new FileInputStream(input));
            Class<?> act = Class.forName(name);
            if (act == null) {
               throw new ClassNotFoundException();
            }
            Method main = act.getMethod("main", String[].class);
            if (main == null) {
               throw new NoSuchMethodException();
            }
            String[] strings = new String[0];
            this.runMethodWithTimeout(main, null, ((Object)strings));
            out.flush();
            out.close();
            diffFiles(trDiff, name, sampleLogFile, logFile, ignoreWhitespace);
         } catch (IOException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: " + name + 
                          " could not be found to run Diff Test");
         } catch (InterruptedException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: " +  name +
                          " got interrupted");
         } catch (ClassNotFoundException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: " + name + 
                             " could not be found to run Diff Test");
         } catch (NoSuchMethodException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students " +
                             name + " Main method not found");
         } catch (IllegalAccessException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students code not accessible");
         } catch (InvocationTargetException e) {
            Throwable et = e.getCause();
            Exception es;
            if(et instanceof Exception) {
               es = (Exception) et;
            } else {
               es = e;
            }
            if (es instanceof ExitTrappedException) {
               trDiff.setScore(0);
               trDiff.addOutput("ERROR: Do not use System.exit() in your code" 
                                + " its bad practice and can cause the autograder" + 
                                " to crash.");
            } else {
            String sStackTrace = stackTraceToString(es);
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students code threw " + 
                             es + "\n Stack Trace: " +
                             sStackTrace);
            }
         } catch (TimeoutException e) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Main Method Timed out after "+ waitTime + " Seconds");
         } catch (ExecutionException e) {
            Throwable et = e.getCause();
            Exception es;
            if(et instanceof Exception) {
               es = (Exception) et;
            } else {
               es = e;
            }
            if (es instanceof InvocationTargetException) {
               et = es.getCause();
               if(et instanceof Exception) {
                  es = (Exception) et;
               } else {
                  es = e;
               }
            }
            if (es instanceof ExitTrappedException) {
               diffFiles(trDiff, name, sampleLogFile, logFile, ignoreWhitespace);
            } else {
            String sStackTrace = stackTraceToString(es);
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students code threw " + 
                             es + "\n Stack Trace: " +
                             sStackTrace);
            }

         }
         this.allTestResults.add(trDiff, this.checksum);
         System.setOut(originalOut);
      }
      this.visibility = visible;
   }


   /**
      Runs a diff test for a specific java program comparing resulting file output.
      This test runs a student and sample implementation then compares the results left 
      in specified output files. The parameter inFile should be the name of a file contianing 
      all the std input that needs to be passed to the program. This file name should be passed 
      without an extension. The program autoappends .in to the filename so all input files should be
      named {inFile}.in.
      @param name the name of the program to do diff tests on
      @param logFile the filename of the file the students code writes to and should be compared
      @param sampleLogFile the filename that the sample code writes to and should be used for comparison
      @param inFile the name of the test input file to use without an extension
      @param ignoreWhitespace true if want to ignore whitespace false otherwise
   */
   public void logFileDiffTest(String name, String logFile, String sampleLogFile,
                               String inFile, boolean ignoreWhitespace) {
      PrintStream originalOut = System.out;
      InputStream originalIn = System.in;
      this.compile(name+"Sample.java");
      Math.resetRandom();
      TestResult trDiff = new TestResult(name + " Log File Diff Test For " + inFile,
                                         "" + this.diffNum,
                                         this.maxScore, this.visibility);
      this.diffNum++;
      String input = inFile + ".in";
      String exOut = inFile + ".expected";
      String acOut = inFile + ".out";
      try {
         File exfile = new File(exOut);
         File infile = new File(input);
         File acfile = new File(acOut);
         File sample = new File(name + "Sample.java");
         if (sample.exists() && !sample.isDirectory()) {
            String[] procSample = {"java", name + "Sample"};
            ProcessBuilder pbSample = new ProcessBuilder(procSample);
            pbSample.redirectOutput(Redirect.to(exfile));
            pbSample.redirectInput(Redirect.from(infile));
            Process sampleProcess = pbSample.start();
            sampleProcess.waitFor();
         }
         PrintStream out = new PrintStream(new FileOutputStream(acOut));
         System.setOut(out);
         System.setIn(new FileInputStream(input));
         Class<?> act = Class.forName(name);
         if (act == null) {
            throw new ClassNotFoundException();
         }
         Method main = act.getMethod("main", String[].class);
         if (main == null) {
            throw new NoSuchMethodException();
         }
         String[] strings = new String[0];
         this.runMethodWithTimeout(main, null, ((Object)strings));
         out.flush();
         out.close();
         diffFiles(trDiff, name, sampleLogFile, logFile, ignoreWhitespace);
      } catch (IOException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: " + name + 
                          " could not be found to run Diff Test");
      } catch (InterruptedException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: " +  name +
                          " got interrupted");
      } catch (ClassNotFoundException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: " + name + 
                          " could not be found to run Diff Test");
      } catch (NoSuchMethodException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: Students " +
                          name + " Main method not found");
      } catch (IllegalAccessException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: Students code not accessible");
      } catch (InvocationTargetException e) {
         Throwable et = e.getCause();
         Exception es;
         if(et instanceof Exception) {
            es = (Exception) et;
         } else {
            es = e;
         }
         if (es instanceof ExitTrappedException) {
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Do not use System.exit() in your code" 
                             + " its bad practice and can cause the autograder" + 
                             " to crash.");
         } else {
            String sStackTrace = stackTraceToString(es);
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students code threw " + 
                             es + "\n Stack Trace: " +
                             sStackTrace);
         }
      } catch (TimeoutException e) {
         trDiff.setScore(0);
         trDiff.addOutput("ERROR: Main Method Timed out after "+ waitTime + " Seconds");
      } catch (ExecutionException e) {
         Throwable et = e.getCause();
         Exception es;
         if(et instanceof Exception) {
            es = (Exception) et;
         } else {
            es = e;
         }
         if (es instanceof InvocationTargetException) {
            et = es.getCause();
            if(et instanceof Exception) {
               es = (Exception) et;
            } else {
               es = e;
            }
         }
         if (es instanceof ExitTrappedException) {
            diffFiles(trDiff, name, sampleLogFile, logFile, ignoreWhitespace);
         } else {
            String sStackTrace = stackTraceToString(es);
            trDiff.setScore(0);
            trDiff.addOutput("ERROR: Students code threw " + 
                             es + "\n Stack Trace: " +
                             sStackTrace);
         }

      }
      this.allTestResults.add(trDiff, this.checksum);
      System.setOut(originalOut);
   }


   

   private void diffFiles(TestResult test, String name, String exOut, String acOut, boolean ignoreWhitespace) {
      try {

         String[] procDiff; 
                              //"-t" };
         if (ignoreWhitespace) {
            procDiff = new String[]{"diff", exOut, acOut, "-y", "-w", 
                               "--width=175", "-t" };
         } else {
            procDiff = new String[]{"diff", exOut, acOut, "-y", 
                               "--width=175", "-t" };
         }
         ProcessBuilder pbDiff = new ProcessBuilder(procDiff);
         Process diffProcess = pbDiff.start();
         StringBuilder sb = new StringBuilder();
         BufferedReader reader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
         String line;
         while ((line = reader.readLine()) != null) { 
            sb.append(line.replace(" \\ ", " \\\\ "));
            sb.append("\n");
         }
         String result = sb.toString();
         diffProcess.waitFor();
         if (diffProcess.exitValue() == 0) {
            test.setScore(this.maxScore);
            test.addOutput("SUCCESS: " + name +
                             " passed this diff test");
         }
         else { 
            test.setScore(0);
            test.addOutput("ERROR: " + name +
                             " differed from expected output." +
                             " Results are below:\n"
                             +"Left: Expected \n"
                             +"Right: Yours \n"
                             + result);
         } 
      } catch (IOException e) {
         test.setScore(0);
         test.addOutput("ERROR: " + name + 
                          " could not be found to run Diff Test");
      } catch (InterruptedException e) {
         test.setScore(0);
         test.addOutput("ERROR: " +  name +
                          " got interrupted");
      }
   }


   /**
      Test to compare two files.
      This will run a diff test on two files. It compares the 
      files and displays the difference between the two files
      as a test. Will give no credit if either file did not exist.
      @param firstFile the filename of the first file to compare
      @param secondFile the filename of the other file to compare
      @return true if files match, false otherwise
    */
   public boolean diffFiles(String firstFile, String secondFile) {
      TestResult test = new TestResult("File Diff Test Between " + firstFile
                                       + " and " + secondFile,
                                            "" + this.diffNum,
                                            this.maxScore, this.visibility);
      this.diffNum++;
      try {

         String[] procDiff = {"diff", firstFile, secondFile, "-y", 
                                    "--width=175", "-t" };
         ProcessBuilder pbDiff = new ProcessBuilder(procDiff);
         Process diffProcess = pbDiff.start();
         diffProcess.waitFor();
         java.util.Scanner s = new java.util.Scanner(diffProcess.getInputStream())
            .useDelimiter("\\A");
         String result = s.hasNext() ? s.next() : "";
         if (diffProcess.exitValue() == 0) {
            test.setScore(this.maxScore);
            test.addOutput("SUCCESS: The Two files Matched");
            return true;
         }
         else { 
            test.setScore(0);
            test.addOutput("ERROR: The two files did not match" +
                             " Results are below:\n"
                             +"Left: " + firstFile + " \n"
                             +"Right: " + secondFile +" \n"
                             + result);
         } 
      } catch (IOException e) {
         test.setScore(0);
         test.addOutput("ERROR: A file could not be found to run this test");
      } catch (InterruptedException e) {
         test.setScore(0);
         test.addOutput("ERROR: Diff process got interrupted");
      }
      this.allTestResults.add(test, this.checksum);
      return false;
   }


   private static ClassConverter findConverterForReturn(Object object) {
      for(ClassConverter c: Autograder.conversions) {
         if (object.getClass().equals(c.getClassType())) {
            return c;
         }
      }
      return null;
   }

   
   private static ClassConverter findConverterForObject(Object object) {
      for(ClassConverter c: Autograder.conversions) {
         if (c.equals(object)) {
            return c;
         }
      }
      return null;
   }
   /**
      Method to do a test comparing the ouptut of the students method against an expected value.
      Basically this method runs the students code and calls 
      .equals() method of the return to compare it to the expected value.
      It also will work if both methods are supposed to return null.
      If the method fails or gives the wrong output, the test result
      will include the information about the method call and potentially 
      the stack trace if the method crashed.
      @param programName the name of the java class to test
      @param m The method to test use {@link #getMethod(String, String, String[]) getMethod} to get the method
      @param ret The expect output from the method
      @param caller An object the method should be called on (null if a static method)
      @param stdinput the file to treat as standard input. If null use the default.
      @param args Any arguments that need to be passed into the method, can be an array of objects
    */
   public void compTest(String programName, Method m, Object ret, Object caller, String stdinput, Object... args) {
      TestResult trComp = new TestResult(programName + " Unit Test # " + this.diffNum,
                                         "" + this.diffNum,
                                         this.maxScore, this.visibility);
      this.diffNum++;
      Math.resetRandom();
      if (m != null) {
         try {
            Object t = null;
            if (stdinput != null) {
               InputStream in = System.in;
               FileInputStream newIo= new FileInputStream(stdinput);
               System.setIn(newIo);
               t = this.runMethodWithTimeout(m, caller, args);
               System.setIn(in);
               newIo.close();
            } else {
               t = this.runMethodWithTimeout(m, caller, args);
            }
            if ((t != null && t.equals(ret)) || (t == ret)) {
               ClassConverter c = findConverterForReturn(ret);
               String output = c.toString(ret);
               trComp.setScore(this.maxScore);
               trComp.addOutput("SUCCESS: Method - "
                                + m.getName() + 
                                " \n" + "Returned the correct output of: " + 
                                output + "\n" + "On Inputs: \n");
            
            } else {
               ClassConverter c = findConverterForReturn(ret);
               String retout = c.toString(ret);
               ClassConverter d = findConverterForReturn(t);
               String tout = d.toString(t);
               trComp.setScore(0);
               trComp.addOutput("FALIURE: Method - "
                                + m.getName() + 
                                "\n" + "Returned the incorrect output of: " + 
                                tout + " \n" + "Instead of: " + retout + 
                                "\n" + "On Inputs: \n");
            }
            for (Object arg: args) {
               ClassConverter c = findConverterForReturn(arg);
               trComp.addOutput(c.toString(arg));
            }
         } catch (IllegalAccessException e) {
            trComp.setScore(0);
            trComp.addOutput("ERROR: Method - " + 
                             m.getName() + "Is not Accessible");
         } catch (InvocationTargetException e) {
            Throwable et = e.getCause();
            Exception es;
            if(et instanceof Exception) {
               es = (Exception) et;
            } else {
               es = e;
            }
            String sStackTrace = stackTraceToString(e);
            trComp.setScore(0);
            trComp.addOutput("ERROR: Method = " +
                             m.getName() +
                             " threw " + 
                             es + "On Inputs: \n");
            for (Object arg: args) {
               ClassConverter c = findConverterForReturn(arg);
               trComp.addOutput(c.toString(arg));
            }
            trComp.addOutput("\n Stack Trace: " +
                             sStackTrace);
         } catch (ExecutionException e) {
            Throwable et = e.getCause();
            Exception es;
            if(et instanceof Exception) {
               es = (Exception) et;
            } else {
               es = e;
            }
            String sStackTrace = stackTraceToString(e);
            trComp.setScore(0);
            trComp.addOutput("ERROR: Method = " +
                             m.getName() +
                             " threw " + 
                             es + "On Inputs: \n");
            for (Object arg: args) {
               ClassConverter c = findConverterForReturn(arg);
               trComp.addOutput(c.toString(arg));
            }
            trComp.addOutput("\n Stack Trace: " +
                             sStackTrace);
         } catch(InterruptedException e) {
            trComp.setScore(0);
            trComp.addOutput("ERROR: Method call got interrupted!");
               
         } catch (TimeoutException e) {
            trComp.setScore(0);
            trComp.addOutput("ERROR: Method " 
                             + m.getName() 
                             + " Timed out!"); 
         } catch (IOException e) {
            trComp.setScore(0);
            trComp.addOutput("ERROR: Failed to find Standard" 
                             + " input file for test");

         }
      } else {
         trComp.setScore(0);
         trComp.addOutput("ERROR: " + programName + " is missing the expected method.");
      }
      this.allTestResults.add(trComp, this.checksum);
   }

   public static String stackTraceToString(Exception es) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            es.printStackTrace(pw);
            return sw.toString().replace("/", "//"); // stack trace as a string
   }


   /**
      Method to do a test comparing the ouptut of the students method against an expected value.
      Will wrap the int in its object type (Integer) and then call the object version
      @see #compTest(String, Method, Object, Object, String, Object...)
      @param programName the name of the java class to test
      @param m The method to test use {@link #getMethod(String, String, String[]) getMethod} to get the method
      @param ret The expect output from the method
      @param caller An object the method should be called on (null if a static method)
      @param args Any arguments that need to be passed into the method, can be an array of objects
    */
   public void compTest(String programName,  Method m, int ret, Object caller, Object... args) {
      Integer i = ret;
      this.compTest(programName, m, i, caller, args);
   }

   /**
      Method to do a test comparing the ouptut of the students method against an expected value.
      Will wrap the boolean in its object type (Boolean) and then call the object version
      @see #compTest(String, Method, Object, Object, String, Object...)
      @param programName the name of the java class to test
      @param m The method to test use {@link #getMethod(String, String, String[]) getMethod} to get the method
      @param ret The expect output from the method
      @param caller An object the method should be called on (null if a static method)
      @param args Any arguments that need to be passed into the method, can be an array of objects
    */
   public void compTest(String programName, Method m, boolean ret, Object caller, Object... args) {
      Boolean i = ret;
      this.compTest(programName, m, i, caller, args);
   }

   /**
      Method to do a test comparing the ouptut of the students method against an expected value.
      Will wrap the char in its object type (Character) and then call the object version
      @see #compTest(String, Method, Object, Object, String, Object...)
      @param programName the name of the java class to test
      @param m The method to test use {@link #getMethod(String, String, String[]) getMethod} to get the method
      @param ret The expect output from the method
      @param caller An object the method should be called on (null if a static method)
      @param args Any arguments that need to be passed into the method, can be an array of objects
    */
   public void compTest(String programName, Method m, char ret, Object caller, Object... args) {
      Character i = ret;
      this.compTest(programName, m, i, caller, args);
   }

   /**
      Method to do a test comparing the ouptut of the students method against an expected value.
      Will wrap the double in its object type (Double) and then call the object version
      @see #compTest(String, Method, Object, Object, String, Object...)
      @param programName the name of the java class to test
      @param m The method to test use {@link #getMethod(String, String, String[]) getMethod} to get the method
      @param ret The expect output from the method
      @param caller An object the method should be called on (null if a static method)
      @param args Any arguments that need to be passed into the method, can be an array of objects
    */
   public void compTest(String programName, Method m, double ret, Object caller, Object... args) {
      Double i = ret;
      this.compTest(programName, m, i, caller, args);
   }

   /**
      Method to do a test comparing the ouptut of the students method against an expected value.
      Will wrap the long in its object type (Long) and then call the object version
      @see #compTest(String, Method, Object, Object, String, Object...)
      @param programName the name of the java class to test
      @param m The method to test use {@link #getMethod(String, String, String[]) getMethod} to get the method
      @param ret The expect output from the method
      @param caller An object the method should be called on (null if a static method)
      @param args Any arguments that need to be passed into the method, can be an array of objects
    */
   public void compTest(String programName, Method m, long ret, Object caller, Object... args) {
      Long i = ret;
      this.compTest(programName, m, i, caller, args);
   }

   /**
      Method to do a test comparing the ouptut of the students method against an expected value.
      Will wrap the float in its object type (Float) and then call the object version
      @see #compTest(String, Method, Object, Object, String, Object...)
      @param programName the name of the java class to test
      @param m The method to test use {@link #getMethod(String, String, String[]) getMethod} to get the method
      @param ret The expect output from the method
      @param caller An object the method should be called on (null if a static method)
      @param args Any arguments that need to be passed into the method, can be an array of objects
    */
   public void compTest(String programName, Method m, float ret, Object caller, Object... args) {
      Float i = ret;
      this.compTest(programName, m, i, caller, args);
   }

   /** Method to get a method.
       @see #compTest(String, Method, Object, Object, String, Object...) compTest
       This method takes in the name of the method and class
       and gets the method to use with the compTest.
       @param programName the name of the java class
       @param methodName the name of the method
       @param paramTypes the argument classes that the method takes in
       @return an object representing the method, null if the method does not exist 
    */
   public static Method getMethod(String programName,
                                  String methodName, 
                                  Class<?>... paramTypes) {
      try {
         Class<?> c = Class.forName(programName);
         if (c == null) {
            throw new ClassNotFoundException();
         }
         Method m = c.getDeclaredMethod(methodName, paramTypes);
         return m;
      } catch(Exception e) {
         return null;
      }
   
   }

   /** Method to get a method.
       @see #compTest(String, Method, Object, Object, String, Object...) compTest
       This method takes in the name of the method and class
       and gets the method to use with the compTest.
       @param programName the name of the java class
       @param methodName the name of the method
       @param argTypes the argument classes (in string form) that the method takes in
       @return an object representing the method, null if the method does not exist 
    */
   public static Method getMethod(String programName,
                                  String methodName, 
                                  String[] argTypes) {
      Class<?> args[] = getClasses(argTypes);
      if (args != null) {
         return getMethod(programName, methodName, args);
      }
      return null;
   
   }
   
   /** Test to check whether a class complies with an interface.
       This method checks that a java class has all the methods 
       found in a provided interface. This method is particualary
       useful in a introductory class where methods are specified but
       students are not asked to specifcally implement certain methods.
       This method also allows for checking constructors which an interface
       traditionally does not allow. Just create a method that has the same
       name as the class and any return type and this method will interpret it 
       as a constructor. It can also check the modifiers of the method (public, private, static...). 
       This will return false if either java files does 
       not compile, in addition to if any method is missing.
       @param className the java className to test
       @param interfaceName the name of the java interface that the class must follow
       @param matchModifiers whether to check modifiers in this test
       @return true if all methods found, false otherwise 
    */
   public boolean hasMethodsTest(String className,
                                 String interfaceName,
                                 boolean matchModifiers) {
      int ret = this.compile(interfaceName + ".java");
      int ret2 = this.compile(className + ".java");
      if (!(ret == ret2 && ret == 0)) {
         TestResult trHas = new TestResult("Methods Exists Test For " + className,
                                         "" + this.diffNum,
                                         this.maxScore, this.visibility);
         this.diffNum++;
         trHas.setScore(0);
         trHas.addOutput("FAILURE: Either the Java Class or Interface failed to compile.");
         this.allTestResults.add(trHas, this.checksum);
         return false;
      }
      try {
         Class<?> clas = Class.forName(className);
         Class<?> interfac = Class.forName(interfaceName);
         Method[] interfaceMethods = interfac.getDeclaredMethods();
         boolean noBadMethod = true;
         for (Method m: interfaceMethods) {
            if (m.getName().equals(className)) {//Is a constructor
               boolean status = this.hasConstructorTest(className,
                                                        m.getParameterTypes(),
                                                        m.getModifiers(),
                                                        matchModifiers);
               if (noBadMethod && !status) {
                  noBadMethod = false;
               }
            } else {
               boolean status = this.hasMethodTest(className,
                                                   m.getName(),
                                                   m.getParameterTypes(),
                                                   m.getReturnType(),
                                                   true,
                                                   m.getModifiers(),
                                                   matchModifiers);
               if (noBadMethod && !status) {
                  noBadMethod = false;
               }
            }
         }
         return noBadMethod;
      } catch (Exception e) {
         TestResult trHas = new TestResult("Methods Exists Test For " + className,
                                         "" + this.diffNum,
                                         this.maxScore, this.visibility);
         this.diffNum++;
         trHas.setScore(0);
         trHas.addOutput("FAILURE: Either the Java Class or Interface could not be found.");
         this.allTestResults.add(trHas, this.checksum);
         return false;
      }
      
   }

   /**
      Method to take a list of string modifiers and turn them into a representative integer.
      Java uses integers to store information about a method/field/class's modifiers. This method
      takes in an array of strings and turns it into its representive integer for other tests.
      This method is Case-INSensitive. The following are possible modfiers handled by this method:
      abstract, final, interface, native, private, protected, public, static, strict, synchronized, 
      transient, volatile.
      @param mods a list of modifiers needed (case-insensitive)
      @return an integer containing the information of all the modifiers 
    */
   public static int getModifiers(String[] mods) {
      if (mods == null) {
         return 0;
      }
      int modifiers = 0;
      for (String mod: mods) {
         switch(mod.toLowerCase()) {
         case "abstract":
            modifiers = modifiers | Modifier.ABSTRACT;
            break;
         case "final":
            modifiers = modifiers | Modifier.FINAL;
            break;
         case "interface":
            modifiers = modifiers | Modifier.INTERFACE;
            break;
         case "native":
            modifiers = modifiers | Modifier.NATIVE;
            break;
         case "private":
            modifiers = modifiers | Modifier.PRIVATE;
            break;
         case "protected":
            modifiers = modifiers | Modifier.PROTECTED;
            break;
         case "public":
            modifiers = modifiers | Modifier.PUBLIC;
            break;
         case "static":
            modifiers = modifiers | Modifier.STATIC;
            break;
         case "strict":
            modifiers = modifiers | Modifier.STRICT;
            break;
         case "synchronized":
            modifiers = modifiers | Modifier.SYNCHRONIZED;
            break;
         case "transient":
            modifiers = modifiers | Modifier.TRANSIENT;
            break;
         case "volatile":
            modifiers = modifiers | Modifier.VOLATILE;
            break;
         default:
            break;
         }
      }
      return modifiers;
   }
   
   /** Method to test whether a method exists.
       This method takes in the name of the method and class
       and checks if a method exists. It then adds a TestResult
       of whether the method exists.
       NOTE: Void is a TYPE. If you are checking the return
       it should never be null. To check for a void method. Pass in 
       the string void. 
       NOTE: For modifiers, see {@link #getModifiers(String[]) getModifiers} for a list
       of acceptable modifiers.
       @param programName the name of the java class
       @param methodName the name of the method
       @param argTypes the argument classes (in string form) that the method takes in
       @param returnType the expected return type of the method
       @param checkReturn whether to check the return type of the method
       @param modifiers the expected modifiers for the method 
       @param checkModifiers whether to check the modifiers of the method
       @return true if the method exists, false otherwise
    */
   public boolean  hasMethodTest(String programName,
                                 String methodName, 
                                 String[] argTypes,
                                 String returnType,
                                 boolean checkReturn,
                                 String[] modifiers,
                                 boolean checkModifiers) {
      boolean status = false;
      TestResult trHas = new TestResult("Method Exists Test " + methodName,
                                         "" + this.diffNum,
                                         this.maxScore, this.visibility);
      this.diffNum++;
      Class<?> args[] = getClasses(argTypes);
      if (args != null) {
         try {
            Class<?> c = Class.forName(programName);
            if (c == null) {
               trHas.setScore(0);
               trHas.addOutput("ERROR: Class - " + programName 
                               + " does not exist");
            } else {
               Method m = c.getDeclaredMethod(methodName, args);
               if (m == null) {
                  throw new NoSuchMethodException();
               }
               int mods = getModifiers(modifiers);
               Class<?>[] classes = getClasses((new String[]{returnType}));
               if (!checkReturn || classes.length == 0 || classes[0].equals(m.getReturnType())) {
                  if (!checkModifiers || mods == m.getModifiers()) {
                     trHas.setScore(this.maxScore);
                     trHas.addOutput("SUCCESS: Class - " + programName
                                     + "\nHas a method named: "+ methodName
                                     + "\nWith input parameters:\n");
                     if (argTypes != null && argTypes.length > 0) {
                        for (String arg : argTypes) {
                           trHas.addOutput(arg +"\n");
                        }
                     }
                     if (checkReturn) {
                        trHas.addOutput("And Return Type: " + returnType);
                     }
                     if (checkModifiers) {
                        trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(mods));
                     }
                     status = true;
                  } else {
                     throw new NoSuchMethodException();
                  }
               } else {
                  throw new NoSuchMethodException();
               }
            }
         } catch(Exception e) {
            trHas.setScore(0);
            trHas.addOutput("ERROR: Class - " + programName
                            + "\nDoes not have a method named: "+ methodName
                            + "\nWith input parameters:\n");
            if (argTypes != null) {
               for (String arg : argTypes) {
                  trHas.addOutput(arg +"\n");
               }
            }
            if (checkReturn) {
               trHas.addOutput("And Return Type: " + returnType);
            }
            if (checkModifiers) {
               int mods = getModifiers(modifiers);
               trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(mods));
            }
         }
      } else {
         trHas.setScore(0);
         trHas.addOutput("ERROR: Unable to convert input parameters");
      }
      this.allTestResults.add(trHas, this.checksum);
      return status;
   }



   /** Method to test whether a method exists.
       This method takes in the name of the method and class
       and checks if a method exists. It then adds a TestResult
       of whether the method exists.
       NOTE: Void is a TYPE. If you are checking the return
       it should never be null. To check for a void method. Pass in 
       void.class.
       NOTE: For modifiers, see {@link #getModifiers(String[]) getModifiers} to 
       easily get the desired modifiers for the method. Also See java.lang.reflect.Modifiers
       for more information.
       @param programName the name of the java class
       @param methodName the name of the method
       @param argTypes the argument classes (in string form) that the method takes in
       @param returnType the expected return type of the method
       @param checkReturn whether to check the return type of the method
       @param modifiers the expected modifiers for the method 
       @param checkModifiers whether to check the modifiers of the method
       @return true if the method exists, false otherwise
    */
   public boolean  hasMethodTest(String programName,
                                 String methodName, 
                                 Class<?>[] argTypes,
                                 Class<?> returnType,
                                 boolean checkReturn,
                                 int modifiers,
                                 boolean checkModifiers) {
      boolean status = false;
      TestResult trHas = new TestResult("Method Exists Test " + methodName,
                                         "" + this.diffNum,
                                         this.maxScore, this.visibility);
      this.diffNum++;
      Class<?> args[] = argTypes;
      if (args != null) {
         try {
            Class<?> c = Class.forName(programName);
            if (c == null) {
               trHas.setScore(0);
               trHas.addOutput("ERROR: Class - " + programName 
                               + " does not exist");
            } else {
               Method m = c.getDeclaredMethod(methodName, args);
               if (m == null) {
                  throw new NoSuchMethodException();
               }
               if (!checkReturn || (returnType != null && returnType.equals(m.getReturnType()))) {
                  if (!checkModifiers || modifiers == m.getModifiers()) {
                     trHas.setScore(this.maxScore);
                     trHas.addOutput("SUCCESS: Class - " + programName
                                     + "\nHas a method named: "+ methodName
                                     + "\nWith input parameters:\n");
                     if (argTypes != null && argTypes.length > 0) {
                        String[] strArgs = getStringsForClasses(argTypes);
                        for (String arg : strArgs) {
                           trHas.addOutput(arg +"\n");
                        }
                     }
                     if (checkReturn) {
                        trHas.addOutput("And Return Type: " + getStringForClass(returnType));
                     }
                     if (checkModifiers) {
                        trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(modifiers));
                     }
                     status = true;
                  } else {
                     throw new NoSuchMethodException();
                  }
               } else {
                  throw new NoSuchMethodException();
               }
            }
         } catch(Exception e) {
            trHas.setScore(0);
            trHas.addOutput("ERROR: Class - " + programName
                            + "\nDoes not have a method named: "+ methodName
                            + "\nWith input parameters:\n");
            if (argTypes != null) {
               String[] strArgs = getStringsForClasses(argTypes);
               for (String arg : strArgs) {
                  trHas.addOutput(arg +"\n");
               }
            }
            if (checkReturn) {
               trHas.addOutput("And Return Type: " + getStringForClass(returnType));
            }
            if (checkModifiers) {
               trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(modifiers));
            }
         }
      } else {
         trHas.setScore(0);
         trHas.addOutput("ERROR: Unable to convert input parameters");
      }
      this.allTestResults.add(trHas, this.checksum);
      return status;
   }


   /** Method to test whether a overriden method exists.
       This method takes in the name of the method and class
       and checks if the class overrides a method. It then adds a TestResult
       of whether the method exists.
       NOTE: Void is a TYPE. If you are checking the return
       it should never be null. To check for a void method. Pass in 
       void.class.
       NOTE: For modifiers, see {@link #getModifiers(String[]) getModifiers} to 
       easily get the desired modifiers for the method. Also See java.lang.reflect.Modifiers
       for more information.
       @param programName the name of the java class
       @param methodName the name of the method
       @param argTypes the argument classes (in string form) that the method takes in
       @param returnType the expected return type of the method
       @param checkReturn whether to check the return type of the method
       @param modifiers the expected modifiers for the method 
       @param checkModifiers whether to check the modifiers of the method
       @return true if the method exists, false otherwise
    */
   public boolean  hasOverriddenMethodTest(String programName,
                                 String methodName, 
                                 Class<?>[] argTypes,
                                 Class<?> returnType,
                                 boolean checkReturn,
                                 int modifiers,
                                 boolean checkModifiers) {
      boolean status = false;
      TestResult trHas = new TestResult("Overridden Method Exists Test " + methodName,
                                         "" + this.diffNum,
                                         this.maxScore, this.visibility);
      this.diffNum++;
      Class<?> args[] = argTypes;
      if (args != null) {
         try {
            Class<?> c = Class.forName(programName);
            if (c == null) {
               trHas.setScore(0);
               trHas.addOutput("ERROR: Class - " + programName 
                               + " does not exist");
            } else {
               Method m = c.getMethod(methodName, args);
               if (m == null) {
                  throw new NoSuchMethodException();
               }
               if (!checkReturn || (returnType != null && returnType.equals(m.getReturnType()))) {
                  if (!checkModifiers || modifiers == m.getModifiers()) {
                     if (m.getDeclaringClass().equals(c)) {
                        trHas.setScore(this.maxScore);
                        trHas.addOutput("SUCCESS: Class - " + programName
                                        + "\nHas a method named: "+ methodName
                                        + "\nWith input parameters:\n");
                        if (argTypes != null && argTypes.length > 0) {
                           String[] strArgs = getStringsForClasses(argTypes);
                           for (String arg : strArgs) {
                              trHas.addOutput(arg +"\n");
                           }
                        }
                        if (checkReturn) {
                           trHas.addOutput("And Return Type: " + getStringForClass(returnType));
                        }
                        if (checkModifiers) {
                           trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(modifiers));
                        }
                        status = true;
                     } else {
                        throw new NoSuchMethodException();
                     }
                  } else {
                     throw new NoSuchMethodException();
                  }
               } else {
                  throw new NoSuchMethodException();
               }
            }
         } catch(Exception e) {
            trHas.setScore(0);
            trHas.addOutput("ERROR: Class - " + programName
                            + "\nDoes not have a method named: "+ methodName
                            + "\nWith input parameters:\n");
            if (argTypes != null) {
               String[] strArgs = getStringsForClasses(argTypes);
               for (String arg : strArgs) {
                  trHas.addOutput(arg +"\n");
               }
            }
            if (checkReturn) {
               trHas.addOutput("And Return Type: " + getStringForClass(returnType));
            }
            if (checkModifiers) {
               trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(modifiers));
            }
         }
      } else {
         trHas.setScore(0);
         trHas.addOutput("ERROR: Unable to convert input parameters");
      }
      this.allTestResults.add(trHas, this.checksum);
      return status;
   }

   /** Method to test whether an overriden method exists.
       This method takes in the name of the method and class
       and checks if the class overrides a method. It then adds a TestResult
       of whether the method exists.
       NOTE: Void is a TYPE. If you are checking the return
       it should never be null. To check for a void method. Pass in 
       the string void. 
       NOTE: For modifiers, see {@link #getModifiers(String[]) getModifiers} for a list
       of acceptable modifiers.
       @param programName the name of the java class
       @param methodName the name of the method
       @param argTypes the argument classes (in string form) that the method takes in
       @param returnType the expected return type of the method
       @param checkReturn whether to check the return type of the method
       @param modifiers the expected modifiers for the method 
       @param checkModifiers whether to check the modifiers of the method
       @return true if the method exists, false otherwise
    */
   public boolean  hasOverriddenMethodTest(String programName,
                                 String methodName, 
                                 String[] argTypes,
                                 String returnType,
                                 boolean checkReturn,
                                 String[] modifiers,
                                 boolean checkModifiers) {
      boolean status = false;
      TestResult trHas = new TestResult("Overriden Method Exists Test " + methodName,
                                         "" + this.diffNum,
                                         this.maxScore, this.visibility);
      this.diffNum++;
      Class<?> args[] = getClasses(argTypes);
      if (args != null) {
         try {
            Class<?> c = Class.forName(programName);
            if (c == null) {
               trHas.setScore(0);
               trHas.addOutput("ERROR: Class - " + programName 
                               + " does not exist");
            } else {
               Method m = c.getDeclaredMethod(methodName, args);
               if (m == null) {
                  throw new NoSuchMethodException();
               }
               int mods = getModifiers(modifiers);
               Class<?>[] classes = getClasses((new String[]{returnType}));
               if (!checkReturn || classes.length == 0 || classes[0].equals(m.getReturnType())) {
                  if (!checkModifiers || mods == m.getModifiers()) {
                     if (m.getDeclaringClass().equals(c)) {
                        trHas.setScore(this.maxScore);
                        trHas.addOutput("SUCCESS: Class - " + programName
                                        + "\nHas a method named: "+ methodName
                                        + "\nWith input parameters:\n");
                        if (argTypes != null && argTypes.length > 0) {
                           for (String arg : argTypes) {
                              trHas.addOutput(arg +"\n");
                           }
                        }
                        if (checkReturn) {
                           trHas.addOutput("And Return Type: " + returnType);
                        }
                        if (checkModifiers) {
                           trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(mods));
                        }
                        status = true;
                     } else {
                        throw new NoSuchMethodException();
                     }
                  } else {
                     throw new NoSuchMethodException();
                  }
               } else {
                  throw new NoSuchMethodException();
               }
            }
         } catch(Exception e) {
            trHas.setScore(0);
            trHas.addOutput("ERROR: Class - " + programName
                            + "\nDoes not have a method named: "+ methodName
                            + "\nWith input parameters:\n");
            if (argTypes != null) {
               for (String arg : argTypes) {
                  trHas.addOutput(arg +"\n");
               }
            }
            if (checkReturn) {
               trHas.addOutput("And Return Type: " + returnType);
            }
            if (checkModifiers) {
               int mods = getModifiers(modifiers);
               trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(mods));
            }
         }
      } else {
         trHas.setScore(0);
         trHas.addOutput("ERROR: Unable to convert input parameters");
      }
      this.allTestResults.add(trHas, this.checksum);
      return status;
   }

   

   

   /** Method to test whether a specific constructor exists.
       This method takes in the name of the class and a list
       of expected parameters and checks if the expected constructor exists. 
       It then adds a TestResult of whether the constructor exists.
       NOTE: For modifiers, see {@link #getModifiers(String[]) getModifiers} for a list
       of acceptable modifiers.
       @param className the name of the java class
       @param argTypes the argument classes (in string form) that the constructor takes in
       @param modifiers the expected modifiers for the method 
       @param checkModifiers whether to check the modifiers of the method
       @return true if the constructor exists, false otherwise
    */
   public boolean hasConstructorTest(String className, 
                                     String[] argTypes,
                                     String[] modifiers,
                                     boolean checkModifiers) {
      boolean status = false;
      String testName = "";
      if (argTypes != null) {
         testName = className + " Constructor Exists Test  (" + argTypes.length + "  Parameters)";
      } else {
         testName = className + " Constructor Exists Test  (0 Parameters)";
      }
      TestResult trHas = new TestResult(testName,
                                        "" + this.diffNum,
                                        this.maxScore, this.visibility);
      this.diffNum++;
      Class<?> args[] = getClasses(argTypes);
      if (args != null) {
         try {
            Class<?> c = Class.forName(className);
            if (c == null) {
               trHas.setScore(0);
               trHas.addOutput("ERROR: Class - " + className 
                               + " does not exist");
            } else {
               Constructor<?> m = c.getConstructor(args);
               if (m == null) {
                  throw new NoSuchMethodException();
               }
               int mods = getModifiers(modifiers);
               if (!checkModifiers || mods == m.getModifiers()) {
                  trHas.setScore(this.maxScore);
                  trHas.addOutput("SUCCESS: Class - " + className
                                  + "\nHas a constructor "
                                  + "with input parameters:\n");
                  if (argTypes != null && argTypes.length > 0) {
                     for (String arg : argTypes) {
                        trHas.addOutput(arg +"\n");
                     }
                  }
                  if (checkModifiers) {
                     trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(mods));
                  }
                  status = true;
               }
            }
         } catch(Exception e) {
            trHas.setScore(0);
            trHas.addOutput("ERROR: Class - " + className
                            + "\nDoes not have a constructor "
                            + "with input parameters:\n");
            if (argTypes != null) {
               for (String arg : argTypes) {
                  trHas.addOutput(arg +"\n");
               }
            }
            if (checkModifiers) {
               trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(getModifiers(modifiers)));
            }

         }
      } else {
         trHas.setScore(0);
         trHas.addOutput("ERROR: Unable to convert input parameters");
      }
      this.allTestResults.add(trHas, this.checksum);
      return status;
   }



   /**Method to test whether a specific constructor exists.
       This method takes in the name of the class and a list
       of expected parameters and checks if the expected constructor exists. 
       It then adds a TestResult of whether the constructor exists. 
       NOTE: For modifiers, see {@link #getModifiers(String[]) getModifiers} to 
       easily get the desired modifiers for the method. Also See java.lang.reflect.Modifiers
       for more information.
       @param className the name of the java class
       @param argTypes the argument classes that the constructor takes in
       @param modifiers the expected modifiers for the method 
       @param checkModifiers whether to check the modifiers of the method
       @return true if the constructor exists, false otherwise
    */
   public boolean  hasConstructorTest(String className, 
                                      Class<?>[] argTypes,
                                      int modifiers,
                                      boolean checkModifiers) {
      boolean status = false;
      String testName = "";
      if (argTypes != null) {
         testName = className + " Constructor Exists Test  (" + argTypes.length + "  Parameters)";
      } else {
         testName = className + " Constructor Exists Test  (0 Parameters)";
      }
      TestResult trHas = new TestResult(testName,
                                        "" + this.diffNum,
                                        this.maxScore, this.visibility);
      this.diffNum++;
      Class<?> args[] = argTypes;
      if (args != null) {
         try {
            Class<?> c = Class.forName(className);
            if (c == null) {
               trHas.setScore(0);
               trHas.addOutput("ERROR: Class - " + className 
                               + " does not exist");
            } else {
               Constructor<?> m = c.getConstructor(args);
               if (m == null) {
                  throw new NoSuchMethodException();
               }
               if (!checkModifiers || modifiers == m.getModifiers()) {
                  trHas.setScore(this.maxScore);
                  trHas.addOutput("SUCCESS: Class - " + className
                                  + "\nHas a constructor "
                                  + "with input parameters:\n");
                  if (argTypes != null && argTypes.length > 0) {
                     String[] strArgs = getStringsForClasses(argTypes);
                     for (String arg : strArgs) {
                        trHas.addOutput(arg +"\n");
                     }
                  }
                  if (checkModifiers) {
                     trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(modifiers));
                  }
                  status = true;
               }
            }
         } catch(Exception e) {
            trHas.setScore(0);
            trHas.addOutput("ERROR: Class - " + className
                            + "\nDoes not have a constructor "
                            + "with input parameters:\n");
            if (argTypes != null) {
               String[] strArgs = getStringsForClasses(argTypes);
               for (String arg : strArgs) {
                  trHas.addOutput(arg +"\n");
               }
            }
            if (checkModifiers) {
               trHas.addOutput("\nAnd Modifiers: " + Modifier.toString(modifiers));
            }
         }
      } else {
         trHas.setScore(0);
         trHas.addOutput("ERROR: Unable to convert input parameters");
      }
      this.allTestResults.add(trHas, this.checksum);
      return status;
   }

   
   
   
   /**
      Method to convert the string paramaters to Class{@literal <?>}[].
      Some rules for the string format. Basically take the name 
      that would be in front of an object of that type and that 
      is a string. Order matters as it has to be the same order
      that you want it to be in for the method.
      @param args the list of parameter types
      @return a list of parameter types as a list of classes
    */
   public static Class<?>[] getClasses(String[] args) {
      if (args != null) {
         int argsCount = args.length;
         Class<?>[] ins = new Class<?>[argsCount];
         for (int j = 0; j < argsCount; j++) {
            String inputop = args[j];
            Class<?> c;
            ins[j] = findConverterForObject(inputop).getClassType();
         }
         return ins;
      } else {
         Class<?>[] ins = {};
         return ins;
      }
   }

   private static String[] getStringsForClasses(Class<?>[] args) {
      if (args != null) {
         int argsCount = args.length;
         String[] ins = new String[argsCount];
         for (int j = 0; j < argsCount; j++) {
            Class<?> inputop = args[j];
            ins[j] = findConverterForObject(inputop).getString();
         }
         return ins;
      } else {
         String[] ins = {};
         return ins;
      }

   }

   private static String getStringForClass(Class<?> args) {
      if (args != null) {
         Class<?> inputop = args;
         String ins = findConverterForObject(inputop).getString();
         return ins;
      } else {
         String ins = "";
         return ins;
      }

   }

   
/**
      Runs all the comparison tests for a specific file.
      A comparison test is similar to a {@link #compTest(String, Method, Object, Object, String, Object...) compTest}
      but it is comparing the output of the students program 
      to the output of the same method for a sample program.
      This method will compile the sample program for you.
      The sample programs named: {Program_Name}Sample.java
      All input files are named: {Program_Name}_Comp_#.in
      @param programName the program to do comparison tests on
      @param testCount the number of tests to perform
      @param caller the object the method should be called on (if an instance method)
   */
   public void comparisonTests(String programName, int testCount, Object caller) {
      PrintStream original = System.out;
      System.setOut(new PrintStream(
         new OutputStream() {
            public void write(int b) {
            
            }
         }));
      this.compile(programName+"Sample.java");
      for (int i = 0; i < testCount; i++) {
         String input = programName + "_Comp_" + i + ".in";
         String result;
         java.util.Scanner s;
         try {
            s = new java.util.Scanner(new FileReader(input));
         } catch (FileNotFoundException e) {
            return;
         }
         String method = s.next();
         int argsCount = s.nextInt();
         s.nextLine();
         String[] argStrings = new String[argsCount];
         for (int j = 0; j < argStrings.length; j++) {
            argStrings[j] = s.next();
         }
         Class<?>[] ins = Autograder.getClasses(argStrings);
         boolean stdinput = s.nextInt() == 1;
         s.nextLine();
         Object[] args = new Object[argsCount];
         Object[] argsSample = new Object[argsCount];
         for (int j = 0; j < args.length; j++) {
            Object c, d;
            String val = s.nextLine();
            c = findConverterForObject(ins[j]).convert(val);
            d = findConverterForObject(ins[j]).convert(val);
            args[j] = c;
            argsSample[j] = d;
         }
         Method m = Autograder.getMethod(programName, method, ins);
         Method ms = Autograder.getMethod(programName + "Sample", method, ins);
         Object out = null;
         String stdInputFile = null;
         Math.resetRandom();
         if (stdinput) {
            stdInputFile = input + "put";
            try {
               InputStream in = System.in;
               String ioFilename = input + "put";
               FileInputStream newIo = new FileInputStream(ioFilename);
               System.setIn(newIo);
               out = ms.invoke(caller, argsSample);
               newIo.close();
               System.setIn(in);
            } catch (Exception e) {
               //Do nothing
            }
         } else {
            try {
               out = ms.invoke(caller, argsSample);
            } catch (Exception e) {
               //Do nothing
            }
         }
         
         this.compTest(programName, m, out, caller, stdInputFile, args);
      }
      System.setOut(original);
   }



/**
      Runs a single test comparing the output of a method..
      A comparison test is similar to a {@link #compTest(String, Method, Object, Object, String, Object...) compTest}
      but it is comparing the output of the students program 
      to the output of the same method for a sample program.
      This method will compile the sample program for you.
      The sample programs named: {Program_Name}Sample.java
      All input files are named: {something}.in. If you are
      using stdinput in the method, you need to name the file containing
      the stdinput as {something}.input. The name of the file
      should be the same as the .in file just with a different extension.
      @param programName the program to do comparison tests on
      @param input the filename of the file containing the comparison test information
      @param caller the object the method should be called on (if an instance method)
   */
   public void comparisonTest(String programName, String input, Object caller) {
      PrintStream original = System.out;
      System.setOut(new PrintStream(
         new OutputStream() {
            public void write(int b) {
            
            }
         }));
      this.compile(programName+"Sample.java");
      String result;
      java.util.Scanner s;
      try {
         s = new java.util.Scanner(new FileReader(input));
      } catch (FileNotFoundException e) {
         return;
      }
      String method = s.next();
      int argsCount = s.nextInt();
      s.nextLine();
      String[] argStrings = new String[argsCount];
      for (int j = 0; j < argStrings.length; j++) {
         argStrings[j] = s.next();
      }
      Class<?>[] ins = Autograder.getClasses(argStrings);
      boolean stdinput = s.nextInt() == 1;
      s.nextLine();
      Object[] args = new Object[argsCount];
      Object[] argsSample = new Object[argsCount];
      for (int j = 0; j < args.length; j++) {
         Object c, d;
         String val = s.nextLine();
         c = findConverterForObject(ins[j]).convert(val);
         d = findConverterForObject(ins[j]).convert(val);
         args[j] = c;
         argsSample[j] = d;
      }
      Method m = Autograder.getMethod(programName, method, ins);
      Method ms = Autograder.getMethod(programName + "Sample", method, ins);
      Object out = null;
      String stdInputFile = null;
      Math.resetRandom();
      if (stdinput) {
         stdInputFile = input + "put";
         try {
            InputStream in = System.in;
            String ioFilename = input + "put";
            FileInputStream newIo = new FileInputStream(ioFilename);
            System.setIn(newIo);
            out = ms.invoke(caller, argsSample);
            newIo.close();
            System.setIn(in);
         } catch (Exception e) {
            //Do nothing
         }
      } else {
         try {
            out = ms.invoke(caller, argsSample);
         } catch (Exception e) {
            //Do nothing
         }
      }
         
      this.compTest(programName, m, out, caller, stdInputFile, args);
      System.setOut(original);
   }



   /**
      This is a method to run a set of junit tests on a class.
      This method will run the junits and give points for
      every junit passed and give the faliure if the
      method failed.
      The test file is named: {Program_Name}Test.java
      @param programName the name of the class to test
    */
   public void junitTests(String programName) {
      PrintStream original = System.out;
      System.setOut(new PrintStream(
         new OutputStream() {
            public void write(int b) {

            }
         }));
      String fileName = programName + "Test.java";
      int compilationResult = this.compile(fileName);
      if (compilationResult == 0) {
         try {
            Class<?> clss = Class.forName(programName +"Test");
            JUnitCore junit = new JUnitCore();
            JunitListener listen = new JunitListener(this.maxScore, this.diffNum, programName, this.visibility);
            junit.addListener(listen);
            junit.run(clss);
            this.allTestResults.addAll(listen.allResults(), this.checksum);
            this.diffNum = listen.unitNum();
         } catch (Exception e){
            //System.out.println(e);
         }
      } else {
         //System.err.println(compilationResult);
      }
      System.setOut(original);
   }

 

   /**
      Runs a test to make sure that the student submitted enough methods.
      This has two particular uses. First in Gateway we sometimes ask
      for them to use a minimum number of helper methods in their code
      in order to get them to break their code up into chunks. In addition
      this has the ability to put in an access modifier to be able to check
      that the method is public or private ensuring that a student created
      all the public methods asked and nothing more.
      In java, access modifiers are represented as ints. The Modifier
      class has all of the possibiliites stored as constants.
      @see java.lang.reflect.Modifier java.lang.reflect.Modifier
      <b>This method only looks at declaredMethods not constructors and inherited methods.</b>
      @param programName the name of the java class
      @param quantity the number of methods the class needs.
      @param modifiers the Modifier that we are only looking for
      @param modify whether to use the access modifier or to look for all methods
      @param atLeast true: as least quantity methods, false: exactly quantity methods 
      @return whether the class has enough methods
    */
   public boolean testMethodCount(String programName, Integer quantity, int modifiers, boolean modify, boolean atLeast) {
      boolean passed = false;
      TestResult trMethodCount = new TestResult(programName + " Method Count", 
                                                "" + this.diffNum , this.maxScore, this.visibility);
      this.diffNum++;
      try {
         Class<?> act = Class.forName(programName);
         if (act == null) {
            trMethodCount.setScore(0);
            trMethodCount.addOutput("Faliure: Class " + 
                                    programName + " could not be found!");
         } else {
            int count = 0;
            Method[] methods = act.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
               if (!modify || methods[i].getModifiers() == modifiers) {
                  count++;
               }
            }
            try {
               Method m = act.getMethod("main", String[].class);
               if (m != null) {
                  count--;
               }
            } catch (NoSuchMethodException e) {
               //do nothing
            } catch (SecurityException e) {
               //do nothing
            }
            
            if (count < quantity) {
               trMethodCount.setScore(0);
               trMethodCount.addOutput("Faliure: Class " 
                                       + programName +
                                       " is missing expected methods!");
            } else if (!atLeast && count > quantity) {
               trMethodCount.setScore(0);
               trMethodCount.addOutput("Faliure: Class " 
                                       + programName +
                                       " has unexpected methods!");
            } else {
               trMethodCount.setScore(this.maxScore);
               trMethodCount.addOutput("SUCCESS: Class " + programName +
                                       " has the correct number of methods!");
               passed = true;
            }
         }
      } catch (ClassNotFoundException e) {
         trMethodCount.setScore(0);
         trMethodCount.addOutput("Faliure: Class " 
                                 + programName + 
                                 " could not be found!");
      }
      this.allTestResults.add(trMethodCount, this.checksum);
      return passed;
   }


   /**
      Runs a test to make sure that the student submitted enough constructors.
      This would allow a professor to ensure that a student created multiple 
      constructors if the project description requested it.
      @param programName the name of the java class
      @param quantity the number of methods the class needs.
      @return whether the class has enough constructors
    */
   public boolean testConstructorCount(String programName, Integer quantity) {
      boolean passed = false;
      TestResult trMethodCount = new TestResult(programName + " Method Count", 
                                                "" + this.diffNum , this.maxScore, this.visibility);
      this.diffNum++;
      try {
         Class<?> act = Class.forName(programName);
         if (act == null) {
            trMethodCount.setScore(0);
            trMethodCount.addOutput("Faliure: Class " + 
                                    programName + " could not be found!");
         } else {
            int count = 0;
            Constructor<?>[] methods = act.getConstructors();
            count = methods.length;
            
            if (count < quantity) {
               trMethodCount.setScore(0);
               trMethodCount.addOutput("Faliure: Class " 
                                       + programName +
                                       " is missing expected constructors!");
            } else if (count > quantity) {
               trMethodCount.setScore(0);
               trMethodCount.addOutput("Faliure: Class " 
                                       + programName +
                                       " has unexpected constructors!");
            } else {
               trMethodCount.setScore(this.maxScore);
               trMethodCount.addOutput("SUCCESS: Class " + programName +
                                       " has the correct number of constructors!");
               passed = true;
            }
         }
      } catch (ClassNotFoundException e) {
         trMethodCount.setScore(0);
         trMethodCount.addOutput("Faliure: Class " 
                                 + programName + 
                                 " could not be found!");
      }
      this.allTestResults.add(trMethodCount, this.checksum);
      return passed;
   }
   /**
      Runs a test to make sure that the student code has no public instace variables.
      This method counts the number of fields in the class that are public 
      and gives credit if the user has no public fields and takes off if they do.
      @param programName the name of the java class
      @return whether the class has no public fields
    */
   public boolean testPublicInstanceVariables(String programName) {
      boolean passed = false;
      TestResult trMethodCount = new TestResult(programName + " Check for Public Instance Variables", 
                                                "" + this.diffNum , this.maxScore, this.visibility);
      this.diffNum++;
      try {
         Class<?> act = Class.forName(programName);
         if (act == null) {
            trMethodCount.setScore(0);
            trMethodCount.addOutput("Faliure: Class " + 
                                    programName + " could not be found!");
         } else {
            int count = 0;
            Field[] fields = act.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
               if (Modifier.isPublic(fields[i].getModifiers())) {
                  count++;
               }
            }
            
            if (count > 0) {
               trMethodCount.setScore(0);
               trMethodCount.addOutput("Faliure: Class " 
                                       + programName +
                                       " has public Fields!");
            } else {
               trMethodCount.setScore(this.maxScore);
               trMethodCount.addOutput("SUCCESS: Class " + programName +
                                       " has no public fields!");
               passed = true;
            }
         }
      } catch (ClassNotFoundException e) {
         trMethodCount.setScore(0);
         trMethodCount.addOutput("Faliure: Class " 
                                 + programName + 
                                 " could not be found!");
      }
      this.allTestResults.add(trMethodCount, this.checksum);
      return passed;
   }



   /**
      Runs a test to make sure that the student has an expected field..
      This method checks whether the student submission has a field that 
      matches the name provided. If a type is given, It will also check 
      that the field, if found, is of the expected type. .
      @param programName the name of the java class
      @param fieldName the name of the field to check for
      @param fieldType the expected type of the field. Ignored if null.
      @param modifiers the modifiers to check for
      @param checkModifiers whether to check the modifiers
      @return whether the class has the expected field
    */
   public boolean hasFieldTest(String programName, String fieldName, String fieldType, String[] modifiers, boolean checkModifiers) {
      if (fieldType != null) {
         String[] field = {fieldType};
         Class<?>[] c = getClasses(field);
         int mods = checkModifiers ? getModifiers(modifiers) : 0;
         return this.hasFieldTest(programName, fieldName, c[0], mods, checkModifiers);
      } else {
         Class<?> c = null;
         int mods = checkModifiers ? getModifiers(modifiers) : 0;
         return this.hasFieldTest(programName, fieldName, c, mods, checkModifiers);
      }
   }
   
   /**
      Runs a test to make sure that the student has an expected field..
      This method checks whether the student submission has a field that 
      matches the name provided. If a type is given, It will also check 
      that the field, if found, is of the expected type. .
      @param programName the name of the java class
      @param fieldName the name of the field to check for
      @param fieldType the expected type of the field. Ignored if null.
      @param modifiers the modifiers to check for
      @param checkModifiers whether to check the modifiers
      @return whether the class has the expected field
    */
   public boolean hasFieldTest(String programName, String fieldName, Class<?> fieldType, int modifiers, boolean checkModifiers) {
      boolean passed = false;
      TestResult trMethodCount = new TestResult(programName + " Check for Field: " + fieldName, 
                                                "" + this.diffNum , this.maxScore, this.visibility);
      this.diffNum++;
      try {
         Class<?> act = Class.forName(programName);
         if (act == null) {
            trMethodCount.setScore(0);
            trMethodCount.addOutput("Faliure: Class " + 
                                    programName + " could not be found!");
         } else {
            Field field = act.getDeclaredField(fieldName);
            if (field == null) {
               trMethodCount.setScore(0);
               trMethodCount.addOutput("Faliure: Class " 
                                       + programName +
                                       " does not have the expected Field!");
            } else if (fieldType != null && !field.getType().equals(fieldType)) {
               trMethodCount.setScore(0);
               trMethodCount.addOutput("Faliure: Class " 
                                       + programName +
                                       " has field " + fieldName + " with incorrect type: " + field.getType().toString());
            } else if (!checkModifiers || modifiers == field.getModifiers()) {
               trMethodCount.setScore(this.maxScore);
               trMethodCount.addOutput("SUCCESS: Class " + programName +
                                       " has the expected Field");
               passed = true;
            } else {
               trMethodCount.setScore(0);
               trMethodCount.addOutput("Faliure: Class " 
                                       + programName +
                                       " has field " + fieldName + " with incorrect Modifiers " + Modifier.toString(modifiers));
            }
         }
      } catch (ClassNotFoundException e) {
         trMethodCount.setScore(0);
         trMethodCount.addOutput("Faliure: Class " 
                                 + programName + 
                                 " could not be found!");
      } catch (NoSuchFieldException e) {
         trMethodCount.setScore(0);
         trMethodCount.addOutput("Faliure: Class " 
                                 + programName +
                                 " does not have the expected Field!");
      }
      this.allTestResults.add(trMethodCount, this.checksum);
      return passed;
   }

      /**
      Runs a test to make sure that the student code does not use multiple Scanners..
      While this is not a problem for the average student, this blocks any possible redirection
      based testing that the autograder can do (like all the diff tests). This test is designed
      to show the test automatically if they redeclare scanners but follows the autograders visibility
      otherwise. 
      @param name the name of the java class
      @return whether multiple scanners are declared or not
    */
   public boolean classDoesNotHaveMultipleScanners(String name) {
      boolean passed = true;
      TestResult trArrayList = new TestResult(name + " Check for Scanner Count", 
                                                "" + this.diffNum , this.maxScore, this.visibility);
      this.diffNum++;
      String input = name + ".java";
      String result;
      java.util.Scanner s;
      try {
         s = new java.util.Scanner(new FileReader(input));
      } catch (FileNotFoundException e) {
         trArrayList.setScore(0);
         trArrayList.addOutput("Test failed to open file "+input);
         this.allTestResults.add(trArrayList, this.checksum);
         return false;
      }
      int count = 0;
      while(s.hasNextLine()) {
         String out = s.nextLine();
         if (out.contains("new Scanner(System.in")) {
            count++;
         }
         if (count > 1) {
            trArrayList = new TestResult(name + " Check for Scanner Count", 
                                                "" + this.diffNum , this.maxScore, "visible");
            trArrayList.setScore(0);
            trArrayList.addOutput("Submissions makes multiple scanners connected to System.in. While this is not expressly \n");
            trArrayList.addOutput("prohibited, it stops us from running any tests against this code and it is truly bad coding practice.");
            trArrayList.addOutput("\n\n If you are unsure of how to fix this, please see an instructor and we can help you.");
            this.allTestResults.add(trArrayList, this.checksum);
            return true;
         }
      }
      trArrayList.setScore(this.maxScore);
      trArrayList.addOutput("This submission properly does not use multiple Scanners in their code");
      this.allTestResults.add(trArrayList, this.checksum);
      return false;
   }




   /**
      Runs a test to make sure that the student code does not use ArrayLists.
      This method goes line by line and checks if any line includes an ArrayList.
      @param name the name of the java class
      @return whether the class declares an ArrayList
    */
   public boolean classDoesNotUseArrayList(String name) {
      boolean passed = true;
      TestResult trArrayList = new TestResult(name + " Check for ArrayList Use", 
                                                "" + this.diffNum , this.maxScore, this.visibility);
      this.diffNum++;
      String input = name + ".java";
      String result;
      java.util.Scanner s;
      try {
         s = new java.util.Scanner(new FileReader(input));
      } catch (FileNotFoundException e) {
         trArrayList.setScore(0);
         trArrayList.addOutput("Test failed to open file "+input);
         this.allTestResults.add(trArrayList, this.checksum);
         return false;
      }
      int line = 0;
      while(s.hasNextLine()) {
         line++;
         String out = s.nextLine();
         if (out.contains("ArrayList")) {
            int curPoints = 0;
            for (TestResult t : this.allTestResults.toArray(this.checksum)) {
               curPoints += t.getScore();
            }
            trArrayList.setScore(-curPoints);
            trArrayList.addOutput("Submissions in Gateway Computing should not use ArrayLists in their code \n");
            trArrayList.addOutput("An ArrayList Instance was found on line "+line);
            this.allTestResults.add(trArrayList, this.checksum);
            return false;
         }
      }
      trArrayList.setScore(this.maxScore);
      trArrayList.addOutput("This submission properly does not use ArrayLists in their code");
      this.allTestResults.add(trArrayList, this.checksum);
      return false;
   }

   /**
      Runs a test to make sure that the student code does not use package declarations.
      This method goes line by line and checks if any line includes a package declaration.
      @param name the name of the java class
      @return whether the class declares a package
    */
   public boolean classDoesNotUsePackages(String name) {
      boolean passed = true;
      TestResult trArrayList = new TestResult(name + " Check for Package Use", 
                                                "" + this.diffNum , this.maxScore, this.visibility);
      this.diffNum++;
      String input = this.classNameToFileName(name, true);
      String result;
      java.util.Scanner s;
      try {
         s = new java.util.Scanner(new FileReader(input));
      } catch (FileNotFoundException e) {
         trArrayList.setScore(0);
         trArrayList.addOutput("Test failed to open file "+input);
         this.allTestResults.add(trArrayList, this.checksum);
         return false;
      }
      int lineNum = 0;
      while(s.hasNextLine()) {
         lineNum++;
         String out = s.nextLine();
         java.util.Scanner line = new java.util.Scanner(out);
         if (line.hasNext() && line.next().equals("package") && out.split(" ").length == 2) {
            int curPoints = 0;
            for (TestResult t : this.allTestResults.toArray(this.checksum)) {
               curPoints += t.getScore();
            }
            trArrayList.setScore(-curPoints);
            trArrayList.addOutput("Submissions in Gateway Computing should not use package declarations in their code \n");
            trArrayList.addOutput("A Package declaration was found on line "+lineNum);
            this.allTestResults.add(trArrayList, this.checksum);
            return false;
         }
      }
      trArrayList.setScore(this.maxScore);
      trArrayList.addOutput("This submission properly does not use Packages in their code.");
      this.allTestResults.add(trArrayList, this.checksum);
      return false;
   }

   /** Method to add a seperately made test to the results.
       This allows for people to make child classes of the autograder
       if they need tests that dont currently exist that they would 
       prefer to avoid adding to this class.
       @param name the name of the test
       @param success whether the student passed the test
       @param extraOutput any additional information to display
    */
   public void addTestResult(String name, boolean success, String extraOutput) {
      TestResult tr = new TestResult(name, ""+this.diffNum, this.maxScore, this.visibility);
      this.diffNum++;
      tr.setScore((success) ? this.maxScore : 0);
      tr.addOutput(extraOutput);
      this.allTestResults.add(tr, this.checksum);
   }

   public Object runMethodWithTimeout(Method m, Object caller, Object... args) throws TimeoutException, ExecutionException, InterruptedException, IllegalAccessException, InvocationTargetException {
      brandon.util.Scanner.resetSystemIn();
      FutureTask<Object> timeoutTask = new FutureTask<Object>(new CallableMethod(m, caller, args));
      Thread th = new Thread(timeoutTask);
      th.start();
      Object out;
      try {
         out = timeoutTask.get(waitTime, TimeUnit.SECONDS);
      } catch (TimeoutException e) {
         th.stop();
         timeoutTask.cancel(true);
         throw e;
      }
      return out;
   }

   /** Helper to convert classs name to the filename.
      @param name the name of the file
      @param java whether its a java or class file
      @return the file name with the suffix at the end (.java/.class)
    */
   private String classNameToFileName(String name, boolean java) {
      if (java) {
         return name.contains(".java") ? name : name + ".java";
      } else {
         return name.contains(".class") ? name : name + ".class";
      }
   }

   /**
      Setter of the visibility of the tests.
      This method updates the current visibility for 
      any future tests. That visibility is set until updated 
      upon a future call to this method.
      This method takes in an int from 0-3 where they represent the following:
      <ul>
          <li>0 = visible</li>
          <li>1 = hidden</li>
          <li>2 = after_due_date</li>
          <li>3 = after_published</li>
      </ul>
      @param choice the int representing the desired visibility
    */
   public void setVisibility(int choice) {
      String[] vis = {"visible", "hidden", "after_due_date", "after_published"};
      if (choice > 3 || choice < 0) {
         choice = 1;
      }
      this.visibility = vis[choice];
   }


   /**
      A method to get the current visibility.
      This method returns the visibility as a string.
      @return the current visibility
    */
   public String getVisibility() {
      return this.visibility;
   }

   /**
      A method to set the point value of the tests.
      This method updates the points awarded for passing
      a test and the amount the test is worth. This
      autograder only works in pass fail as it breaks 
      each test to be the smallest possible component.
      The results will stay at this score until changed
      by calling this method again. A score must be positive.
      If the input is not positive then the score will
      automatically be set to 0.1
      @param score the new score value of a test
    */
   public void setScore(double score) {
      if (score < 0.0) {
         score = 0.1;
      }
      this.maxScore = score;
   }

   /**
      A method to get the current single test score.
      This returns the number of points that a test
      is worth.
      @return the point value of passing a test
    */
   public double currentScore() {
      return this.maxScore;
   }

   /**Setter for the amount of time to wait before timing out while running student code.
      The default timeout is 15 seconds.
      @param timeout the amount of time
    */
   public void setTimeout(long timeout) {
      this.waitTime = timeout;
   }

 /**A Class representing a method that can be called.
  Used to allow for timeouts when running students code
 so that instead of the whole autograder timing out and 
 giving no feedback, the method just times out and they
 can still recieve feedback for their work.
*/
 public class CallableMethod implements Callable<Object> {
   /**The method to invoke.*/
   private final Method m;
   /**The object to call the method on.*/
   private final Object caller;
   /**The arguments to pass to the method.*/
   private final Object[] args;

   /**
      The constructor of CallableMethod Class.
      @param m the method to call
      @param caller the object to call the method on
      @param args the arguments to pass to the method
    */
   public CallableMethod(Method m, Object caller, Object... args) {
      this.m = m;
      this.caller = caller;
      this.args = args;
   }

   /**The call that gets run when the method is run.
    @return the result of invoking the method
    @throws Exception on any failiure of m.invoke()
   */
   public Object call() throws Exception {
      return this.m.invoke(this.caller, this.args);
   }
 }


   public class StopExitSecurityManager extends SecurityManager
   {
      private SecurityManager _prevMgr = System.getSecurityManager();
 
      public void checkPermission(Permission perm)
      {
      }
 
      public void checkExit(int status)
      {
         super.checkExit(status);
         throw new ExitTrappedException(); //This throws an exception if an exit is called.
      }
 
      public SecurityManager getPreviousMgr() { return _prevMgr; }
   } 

   static class ExitTrappedException extends SecurityException
   { 
   }
}
