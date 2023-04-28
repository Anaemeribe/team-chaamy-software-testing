import brandon.convert.ClassConverter;
import jh61b.grader.TestResult;
import net.sf.saxon.expr.Component;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_2.PrivateCar;

import java.io.*;
import java.lang.reflect.Method;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeoutException;


import static org.junit.jupiter.api.Assertions.*;
public class AutograderWhiteboxTest {
    public Autograder autograder;
    public File testFile;
    public TestResult tr;
    private ByteArrayOutputStream outputStreamCaptor;
    @BeforeEach
    public void setUp() {
        testFile = new File("./src/main/java/project_2/Proj2BSample.java");
        autograder = new Autograder(0, 100);
        tr = new TestResult("Test testResult", "5", 100, "visible");
        autograder.addTestResult(tr);

        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterAll
    public static void cleanUp() {
        System.setOut(System.out);
    }

    @Test
    public void testDefaultConstructorWorks() {
        Autograder autograder = new Autograder();
        assertEquals("hidden", autograder.getVisibility());
        assertEquals(0.1, autograder.maxScore);
    }

    @Test
    public void testConstructorWorksVisible() {
        assertEquals("visible", autograder.getVisibility());
    }

    @Test
    public void testConstructorWorksHidden() {
        Autograder hidden_Autograder = new Autograder(1, 100);
        assertEquals("hidden", hidden_Autograder.getVisibility());
    }

    @Test
    public void testConstructorWorksAfterDue() {
        Autograder after_due_date_Autograder = new Autograder(2, 100);
        assertEquals("after_due_date", after_due_date_Autograder.getVisibility());
    }

    @Test
    public void testConstructorWorksAfterPublished() {
        Autograder after_published_Autograder = new Autograder(3, 100);
        assertEquals("after_published", after_published_Autograder.getVisibility());
    }

    @Test
    public void testVisibilityChoiceOutsideRange() {
        autograder.setVisibility(-1);
        assertEquals("hidden", autograder.getVisibility());
        autograder.setVisibility(4);
        assertEquals("hidden", autograder.getVisibility());
    }

    @Test
    public void setScoreLessThanZero() {
        autograder.setScore(-1);
        assertEquals(0.1, this.autograder.maxScore);
    }

    @Test
    public void testAddTestResult() {
        TestResult testResult = new TestResult("Test testResult", "2", 100, "visible");
        assertDoesNotThrow(() -> autograder.addTestResult(testResult));
    }

    @Test
    public void testAddTestResultThrowsNullPointerException() {
        assertThrows(NullPointerException.class,() -> autograder.addTestResult(null));
    }

    @Test
    public void testAddConverter() {
        ClassConverter c = new ClassConverter() {
            @Override
            public Object convert(String args) {
                return null;
            }

            @Override
            public String toString(Object input) {
                return null;
            }
        };
        assertDoesNotThrow(() -> autograder.addConverter(c));
    }

    @Test
    public void testAddConverterThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> autograder.addConverter(null));
    }

    @Test
    public void testTestRunFinishedNoResults() {
        Autograder temp = new Autograder();
        assertDoesNotThrow(() -> temp.testRunFinished());
        assertEquals("{" + String.join(",", new String[] {"\"tests\": []"}) + "}", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testTestRunFinishedOneTest() {
        assertDoesNotThrow(() -> autograder.testRunFinished());
        assertEquals("{" + String.join(",", new String[] {"\"tests\": [" + tr.toJSON() + "]"}) + "}", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testTestRunFinishedMultipleTests() {
        TestResult testResult2 = new TestResult("testMethod2", "2", 10, "hidden");
        autograder.addTestResult(testResult2);
        assertDoesNotThrow(() -> autograder.testRunFinished());
        assertEquals("{" + String.join(",", new String[] {"\"tests\": [" + tr.toJSON() + "," + testResult2.toJSON() + "]"}) + "}", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testTestRunFinishedWithFileName() {
        TestResult tr = new TestResult("Test testResult", "1", 10, "visible");
        autograder.addTestResult(tr);
        assertDoesNotThrow(() -> autograder.testRunFinished("outputFile"));
    }

    @Test
    public void testSourceExistsPasses() {
        assertTrue(autograder.testSourceExists(testFile.getAbsolutePath()));
    }

    @Test
    public void testSourceExistsFailsDirectoryOnly() {
        assertFalse(autograder.testSourceExists("./project_2"));
    }

    @Test
    public void testSourceExistsFailsNonExistingFile() {
        assertFalse(autograder.testSourceExists("nonExistentFile"));
    }

    @Test
    public void testSourceExistsThrowsNullPointerError() {
        assertThrows(NullPointerException.class, () -> autograder.testSourceExists(null));
    }

    @Test
    public void testCompilePasses() {
        assertEquals(0, autograder.compile(testFile.getAbsolutePath()));
    }

    @Test
    public void testCompileNonExistentFile() {
        assertNotEquals(0, autograder.compile("non-testFile"));
    }

    @Test
    public void testCompileNullFile() {
        assertThrows(NullPointerException.class, () -> autograder.compile(null));
    }

    @Test
    public void testCompileFaultyFile() {
        File tempFile = new File(getClass().getClassLoader().getResource("FaultyFile.java").getFile());
        assertNotEquals(0, autograder.compile(tempFile.getAbsolutePath()));
    }

    @Test
    public void testTestCompilesPasses() {
        assertTrue(autograder.testCompiles("./src/test/resources/Compliant"));
    }

    @Test
    public void testTestCompilesFailsFaultyFile() {
        assertFalse(autograder.testCompiles("./src/test/resources/FaultyFile"));
    }

    @Test
    public void testTestCompilesNonExsitentFile() {
        assertFalse(autograder.testCompiles("non-testFile"));
    }

    @Test
    public void testTestCompilesNullFile() {
        assertThrows(NullPointerException.class, () -> autograder.testCompiles(null));
    }

    @Test
    public void testTestCheckstyleEmptyCode() {
        autograder.testCheckstyle("");
        assertEquals("", outputStreamCaptor.toString().trim());
    }

    @Test
    public void testTestCheckstyleNullCode() {
        String code = null;
        assertThrows(IllegalArgumentException.class, () -> autograder.testCheckstyle(code));
    }

    @Test
    public void testTestCheckstyleCodeWithStyleErrors() {
        autograder.testCheckstyle(testFile.getAbsolutePath());
        assertDoesNotThrow(() -> autograder.testRunFinished());
        assertTrue(outputStreamCaptor.toString().trim().contains("did not pass checkstyle"));
    }

    @Test
    public void testTestCheckstyleCodeWithNoStyleErrors() {
        File tempFile = new File("./src/main/java/project_2/Car");
        autograder.testCheckstyle(tempFile.getAbsolutePath());
        assertDoesNotThrow(() -> autograder.testRunFinished());
        assertTrue(outputStreamCaptor.toString().trim().contains("passed checkstyle with no warnings"));
    }

    @Test
    public void testTestCheckstyleCodeWithIOException() {
        File tempFile = new File("./src/main/java/project_2/IOExceptionSample.java");
        autograder.testCheckstyle(tempFile.getAbsolutePath());
        assertDoesNotThrow(() -> autograder.testRunFinished());
        assertTrue(outputStreamCaptor.toString().trim().contains("passed checkstyle with no warnings"));
    }

    @Test
    public void testTestSortedCheckstyleNonExistentFile() {
        assertThrows(Exception.class, () -> autograder.testSortedCheckstyle("non-testFile", 1, false));
    }

    @Test
    public void testTestSortedCheckstylePasses() {
        assertDoesNotThrow(() -> autograder.testSortedCheckstyle(testFile.getAbsolutePath(), 1, false));
        assertDoesNotThrow(() -> autograder.testSortedCheckstyle(testFile.getAbsolutePath(), 2, true));
    }

    @Test
    public void testTestSortedCheckstyleNullFile() {
        assertThrows(NullPointerException.class, () -> autograder.testSortedCheckstyle(null, 1, false));
    }

    @Test
    public void testStdOutDiffTestsWithNullSampleFile() {
        assertDoesNotThrow(() -> autograder.stdOutDiffTests(null, 1, true, true));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests(null, 2, true, false));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests(null, 3, false, true));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests(null, 4, false, false));
    }

    @Test
    public void testStdOutDiffTestsWithNonExistentSampleFile() {
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("", 1, true, true));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("", 2, true, false));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("", 3, false, true));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("", 4, false, false));
    }

    @Test
    public void testStdOutDiffTestsPasses() {
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("./src/main/java/project_2/Proj2B", 1, true, true));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("./src/main/java/project_2/Proj2B", 2, false, true));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("./src/main/java/project_2/Proj2B", 3, true, false));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("./src/main/java/project_2/Proj2B", 4, false, false));
    }

    @Test
    public void testStdOutDiffTestsThrowsIOException() {
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("./src/main/java/project_2/IOException", 1, true, true));
        assertDoesNotThrow(() -> autograder.testRunFinished());
        assertTrue(outputStreamCaptor.toString().trim().contains("could not be found to run Diff Test"));
    }

    @Test
    public void testStdOutDiffTestsThrowsInterruptedException() {
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("./src/main/java/project_2/InterruptedException", 1, true, true));
        assertDoesNotThrow(() -> autograder.testRunFinished());
        assertTrue(outputStreamCaptor.toString().trim().contains("got Interrupted"));
    }

    @Test
    public void testStdOutDiffTestsThrowsIllegalAccessException() {
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("./src/main/java/project_2/IllegalAccessException", 1, true, true));
        assertDoesNotThrow(() -> autograder.testRunFinished());
        assertTrue(outputStreamCaptor.toString().trim().contains("got Interrupted"));
    }

    @Test
    public void testStdOutDiffTestsThrowsNoSuchMethodException() {
        assertDoesNotThrow(() -> autograder.stdOutDiffTests("./src/main/java/project_2/NoSuchMethodException", 1, true, true));
        assertDoesNotThrow(() -> autograder.testRunFinished());
        assertTrue(outputStreamCaptor.toString().trim().contains("got Interrupted"));
    }

    @Test
    public void testLogFileDiffTestsPasses() {
        File logFile = new File("./src/main/java/project_2/Proj2B_Comp_0.in");
        assertDoesNotThrow(() -> autograder.logFileDiffTests(testFile.getAbsolutePath(), 1, "student", logFile.getAbsolutePath(),  true));
    }

    @Test
    public void testLogFileDiffTestsNonExistentFile() {
        assertDoesNotThrow(() -> autograder.logFileDiffTests(testFile.getAbsolutePath(), 1, "student", "non_existent",  true));
    }

    @Test
    public void testLogFileDiffTestsNullFile() {
        assertThrows(NullPointerException.class, () -> autograder.logFileDiffTests(testFile.getAbsolutePath(), 1, "student", null,  true));
    }

    @Test
    public void testCompTest() throws Exception {
        class MyClass {
            public String myMethod(int num, String str) {
                return "Hello World";
            }
        }

        String programName = "MyProgram";
        Method method = MyClass.class.getMethod("myMethod", int.class, String.class);
        Object ret = "Hello World";
        Object caller = new MyClass();
        String stdinput = "input";
        Object[] args = {5, "test"};

        autograder.compTest(programName, method, ret, caller, stdinput, args);
    }

    @Test
    public void testCompTestInt() throws Exception {
        class MyClass {
            public int myMethod(int i) {
                return i;
            }
        }

        String programName = "MyProgram";
        Method method = MyClass.class.getMethod("myMethod", int.class);
        int ret = 5;
        Object caller = new MyClass();
        int[] args = {5};

        autograder.compTest(programName, method, ret, caller, args);
    }

    @Test
    public void testClassDoesNotUsePackagesFileExistsPasses() {
        assertTrue(autograder.classDoesNotUsePackages("./src/main/java/project_2/Car"));
    }

    @Test
    public void testClassDoesNotUsePackagesFileExistsFail() {
        assertFalse(autograder.classDoesNotUsePackages(testFile.getAbsolutePath()));
    }

    @Test
    public void testClassDoesNotUsePackagesFileDoesNotExists() {
        assertThrows(FileNotFoundException.class, () -> autograder.classDoesNotUsePackages("non-existent"));
    }

    @Test
    public void testClassDoesNotUsePackagesFileIsNull() {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotUsePackages(null));
    }

    @Test
    public void testClassDoesNotHaveMultipleScannersOneScanner() {
        assertTrue(autograder.classDoesNotHaveMultipleScanners("./src/main/java/project_2/Proj2BSample"));
    }

    @Test
    public void testClassDoesNotHaveMultipleScannersNoScanner() {
        assertTrue(autograder.classDoesNotHaveMultipleScanners("./src/main/java/project_2/IOExceptionSample"));
    }

    @Test
    public void testClassDoesNotHaveMultipleScannersMultipleScanners() {
        assertFalse(autograder.classDoesNotHaveMultipleScanners("./src/main/java/project_2/MultipleScanners"));
    }

    @Test
    public void testClassDoesNotHaveMultipleScannersNullFile() {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotHaveMultipleScanners(null));
    }

    private static Method myMethod() throws TimeoutException{
        while(true) {
            continue;
        }
    }

    @Test
    public void testHasFieldTestPass() {
        assertTrue(autograder.hasFieldTest("java.lang.Integer", "MAX_VALUE", int.class, Modifier.PUBLIC, false));
    }

    @Test
    public void testHasFieldTestFailNoField() {
        assertFalse(autograder.hasFieldTest("java.lang.Integer", "MAX_STRING", int.class, Modifier.PUBLIC, false));
    }

    @Test
    public void testHasFieldTestFailWrongType() {
        assertFalse(autograder.hasFieldTest("java.lang.Integer", "MAX_VALUE", String.class, Modifier.PUBLIC, false));
    }

    @Test
    public void testHasFieldTestFailWrongModifierCheckModifier() {
        assertFalse(autograder.hasFieldTest("java.lang.Integer", "MAX_VALUE", int.class, Modifier.PRIVATE, true));
    }

    @Test
    public void testHasFieldTestRightModifierButCheckModfierTrue() {
        assertFalse(autograder.hasFieldTest("java.lang.Integer", "MAX_VALUE", int.class, 17, true));
    }

    @Test
    public void testTestPublicInstanceVariablesFails() {
        assertFalse(autograder.testPublicInstanceVariables("java.lang.Integer"));
    }

    @Test
    public void testTestPublicInstanceVariablesPasses() {
        assertTrue(autograder.testPublicInstanceVariables("java.lang.Class"));
    }

    @Test
    public void testTestPublicInstanceVariablesNull() {
        assertThrows(NullPointerException.class, () -> autograder.testPublicInstanceVariables(null));
    }

    @Test
    public void testTestPublicInstanceVariablesFailsClassNotExist() {
        assertFalse(autograder.testPublicInstanceVariables("non-existent-class"));
    }

    @Test
    public void testConstructorCountPasses() {
        assertTrue(autograder.testConstructorCount("java.lang.Integer", 2));
    }

    @Test
    public void testConstructorCountLess() {
        assertFalse(autograder.testConstructorCount("java.lang.Integer", 1));
    }

    @Test
    public void testConstructorCountMore() {
        assertFalse(autograder.testConstructorCount("java.lang.Integer", 3));
    }

    @Test
    public void testComparisonTestJumpEcrypt() {
        assertDoesNotThrow(() -> autograder.comparisonTest("./src/main/java/project_2/Proj2B", "./src/main/java/project_2/Proj2B_Comp_0.in", autograder));
    }

    @Test
    public void testComparisonTestValidateMessage() {
        assertDoesNotThrow(() -> autograder.comparisonTest("./src/main/java/project_2/Proj2B", "./src/main/java/project_2/Proj2B_Comp_1.in", autograder));
    }

    @Test
    public void testComparisonTestMirrorEcrypt() {
        assertDoesNotThrow(() -> autograder.comparisonTest("./src/main/java/project_2/Proj2B", "./src/main/java/project_2/Proj2B_Comp_8.in", autograder));
    }

    @Test
    public void testComparisonTestShiftEcrypt() {
        assertDoesNotThrow(() -> autograder.comparisonTest("./src/main/java/project_2/Proj2B", "./src/main/java/project_2/Proj2B_Comp_9.in", autograder));
    }

    @Test
    public void testComparisonTestShiftEcrypt2() {
        assertDoesNotThrow(() -> autograder.comparisonTest("./src/main/java/project_2/Proj2B", "./src/main/java/project_2/Proj2B.input", autograder));
    }

    @Test
    public void testComparisonTestNonExistingFile() {
        assertDoesNotThrow(() -> autograder.comparisonTest("Non-existing", "./src/main/java/project_2/Proj2B_Comp_9.in", autograder));
    }

    @Test
    public void testHasConstructorTest() {
        assertTrue(autograder.hasConstructorTest("java.lang.Integer", new String[] {"int"}, new String[] {"public"}, true));
    }

    @Test
    public void testHasConstructorTestFailNoClass() {
        assertFalse(autograder.hasConstructorTest("non-exists", new String[] {"int"}, new String[] {"public"}, true));
    }

    @Test
    public void testHasConstructorTestNoArgTypes() {
        assertFalse(autograder.hasConstructorTest("java.lang.Integer", new String[] {"String"}, new String[] {"public"}, true));
    }

    @Test
    public void testHasConstructorTestNoModifier() {
        assertFalse(autograder.hasConstructorTest("java.lang.Integer", new String[] {"int"}, new String[] {"something-new"}, true));
    }

    @Test
    public void testHasConstructorTestNoModifierButCheckIsFalse() {
        assertTrue(autograder.hasConstructorTest("java.lang.Integer", new String[] {"int"}, new String[] {"something-new"}, false));
    }

    @Test
    public void testHasConstructorTestNullClass() {
        assertThrows(NullPointerException.class, () -> autograder.hasConstructorTest(null, new String[] {"int"}, new String[] {"public"}, true));
    }

    @Test
    public void testHasConstructorTestNullTypes() {
        assertFalse(autograder.hasConstructorTest("java.lang.Integer", null, new String[] {"public"}, true));
    }

    @Test
    public void testHasConstructorTestPassNullTypes() {
        assertTrue(autograder.hasConstructorTest("java.lang.Class", new String[] {"int"}, new String[] {"public"}, true));
    }

    @Test
    public void testHasConstructorNullModifiers() {
        assertFalse(autograder.hasConstructorTest("java.lang.Integer", new String[] {"int"}, null, true));
    }

    @Test
    public void testJunitTests() {
        assertDoesNotThrow(() -> autograder.junitTests("src/main/java/project_2/Car"));
    }

    @Test
    public void testGetMethodPass() {
        assertDoesNotThrow(() -> autograder.getMethod("src/main/java/project_2/Car", "getName", new String[]{}));
    }

    @Test
    public void testGetMethodFail() {
        assertDoesNotThrow(() -> autograder.getMethod("src/main/java/project_2/Car", "getColor", new String[] {""}));
    }

    @Test
    public void testHasMethodsTest() {
        assertTrue(autograder.hasMethodsTest("src/main/java/project_2/PrivateCar", "src/main/java/project_2/Car", false));
    }

    @Test
    public void testClassDoesNotUseArrayListPass() {
        assertTrue(autograder.classDoesNotUseArrayList("./src/main/java/project_2/Car"));
    }

    @Test
    public void testClassDoesNotUseArrayListFail() {
        assertFalse(autograder.classDoesNotUseArrayList("./src/main/java/project_2/UsesList"));
    }

    @Test
    public void testClassDoesNotUseArrayListNonExistent() {
        assertThrows(FileNotFoundException.class, ()-> autograder.classDoesNotUseArrayList("non-existent"));
    }

    @Test
    public void testHasMethodTestPass() {
        assertTrue(autograder.hasMethodTest("java.lang.Integer", "compareTo", new Class<?>[] {Integer.class}, int.class, true, Modifier.PUBLIC, false));
    }

    @Test
    public void testHasMethodTestFailMethodNotExist() {
        assertFalse(autograder.hasMethodTest("java.lang.Integer", "not-exist", new Class<?>[] {Integer.class}, int.class, true, Modifier.PUBLIC, false));
    }

    @Test
    public void testHasMethodTestFailWrongType() {
        assertFalse(autograder.hasMethodTest("java.lang.Integer", "compareTo", new Class<?>[] {String.class}, int.class, true, Modifier.PUBLIC, false));
    }

    @Test
    public void testHasMethodTestFailWrongReturnType() {
        assertFalse(autograder.hasMethodTest("java.lang.Integer", "compareTo", new Class<?>[] {Integer.class}, String.class, true, Modifier.PUBLIC, false));
    }

    @Test
    public void testHasMethodTestFailWrongReturnTypeButNoCheck() {
        assertTrue(autograder.hasMethodTest("java.lang.Integer", "compareTo", new Class<?>[] {Integer.class}, String.class, false, Modifier.PUBLIC, false));
    }

    @Test
    public void testHasMethodTestFailWrongModifier() {
        assertFalse(autograder.hasMethodTest("java.lang.Integer", "compareTo", new Class<?>[] {Integer.class}, int.class, true, Modifier.PRIVATE, true));
    }

    @Test
    public void testHasMethodTestFailWrongModifierButNoCheck() {
        assertTrue(autograder.hasMethodTest("java.lang.Integer", "compareTo", new Class<?>[] {Integer.class}, int.class, true, Modifier.PRIVATE, false));
    }

    @Test
    public void testComparisonTest() {
        assertDoesNotThrow(()-> autograder.comparisonTest("./src/main/java/project_2/Proj2B", "./src/main/java/project_2/Proj2B_Comp_0.in", autograder));
    }

    @Test
    public void testCompTestNew() {
        try {
            Method m = Integer.class.getMethod("sum", int.class, int.class);
            assertDoesNotThrow(() -> autograder.compTest("java.lang.Integer", m, 5, new int[] {1,2}));
        } catch (Exception e) {
            assertTrue(false);
        }
    }


//    @Test
//    public void testRunMethodWithTimeoutThrowsException() throws TimeoutException {
//        assertThrows(TimeoutException.class, () -> autograder.runMethodWithTimeout(myMethod(), autograder, null));
//    }
}
