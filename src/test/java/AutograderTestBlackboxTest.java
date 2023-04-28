import jh61b.grader.TestResult;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Null;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
public class AutograderTestBlackboxTest {

    private Autograder autograder;

    private String currentDir;

    private static String INPUT_FILE_PATH;

    private static String BICYCLE_FILE_PATH;

    private static String SAMPLE_PROG_PATH;

    private static String CLASS_SAMPLE_PATH;

    private double DEFAULT_SCORE_VALUE = 1.0;

    @BeforeAll
    public static void beforeAll()
    {
        File file = new File(AutograderTestBlackboxTest.class.getClassLoader().getResource("input.in").getFile());
        INPUT_FILE_PATH = file.getAbsolutePath().replace(".in", "");

        File bicycle = new File(AutograderTestBlackboxTest.class.getClassLoader().getResource("Bicycle.java").getFile());
        BICYCLE_FILE_PATH = bicycle.getAbsolutePath();

        File sample_prog = new File(AutograderTestBlackboxTest.class.getClassLoader().getResource("programSample.java").getFile());
        SAMPLE_PROG_PATH = sample_prog.getAbsolutePath();

        File class_sample = new File(AutograderTestBlackboxTest.class.getClassLoader().getResource("ClassSample.java").getFile());
        CLASS_SAMPLE_PATH = class_sample.getAbsolutePath();
    }

    @BeforeEach
    public void setup()
    {
        autograder = new Autograder();
        currentDir = System.getProperty("user.dir");

        autograder.setScore(DEFAULT_SCORE_VALUE);
    }

    private String removeExtension(String path)
    {
        int index = path.indexOf('.');
        if (index < 0) return path;
        return path.substring(0, index);
    }

    private String removeSample(String path)
    {
        int index = path.indexOf("Sample");
        if (index < 0) return path;
        return path.substring(0, index);
    }

    private String getPath(String resourceName)
    {
        File file = new File(getClass().getClassLoader().getResource(resourceName).getFile());
        return file.getAbsolutePath();
    }

    /*
     * Tests to see if the addTestResult method throws an exception if the parameters are null
     */
    @Test
    public void testAddTestResultThrowsExceptionWhenArgsAreNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.addTestResult(null, false, null));
        assertThrows(NullPointerException.class, () -> autograder.addTestResult(null));
    }

    /*
     * Tests to see if the addTestResult method throws an exception if the parameters are null
     */
    @Test
    public void testAddTestResultVariantThrowsExceptionWhenArgsAreNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.addTestResult(null));
    }

    /*
     * Tests to see if the addTestResult method throws an exception if the test name is empty
     */
    @Test
    public void testAddTestResultThrowsExceptionWhenTestNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.addTestResult("", false, ""));
        TestResult result = new TestResult("", "1", 0, "false");
        assertThrows(IllegalArgumentException.class, () -> autograder.addTestResult(result));
    }

    /*
     * Tests to see if the addTestResult sets the score to zero if success is false.
     */
    @Tag("Integration")
    @Test
    public void testAddTestResultSetsScoreToZeroWhenTestFailed()
    {
        autograder.addTestResult("test", false, "");
        try {
            TestUtilities.CustomTestResult[] result = TestUtilities.getTestResults(autograder);
            assertEquals(1, result.length);
            assertEquals(0, result[0].getScore());
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    /*
     * Tests to see if the addTestResult method sets the test result score to the maxScore
     * if success is true.
     */
    @Tag("Integration")
    @Test
    public void testAddTestResultSetsScoreProperlyWhenTestPassed()
    {
        autograder.addTestResult("test", true, "");
        try {
            TestUtilities.CustomTestResult[] result = TestUtilities.getTestResults(autograder);
            assertEquals(1, result.length);
            assertEquals(DEFAULT_SCORE_VALUE, result[0].getScore());
        } catch (Exception e) {
            assertTrue(false);
        }
    }


    /*
     * Tests to see if the classDoesNotHaveMultipleScanners method throws an exception if the argument is null.
     */
    @Test
    public void testClassDoesNotHaveMultipleScannersThrowsExceptionWhenArgIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotHaveMultipleScanners(null));
    }

    /*
     * Tests to see if the classDoesNotHaveMultipleScanners method throws an exception if the argument is empty.
     */
    @Test
    public void testClassDoesNotHaveMultipleScannersThrowsExceptionWhenArgIsEmpty()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotHaveMultipleScanners(""));
    }

    /*
     * Tests to see if the classDoesNotHaveMultipleScanners method throws an exception if the argument is
     * invalid (e.g. a folder).
     */
    @Test
    public void testClassDoesNotHaveMultipleScannersThrowsExceptionWhenArgIsInvalid()
    {
        String dir = System.getProperty("user.dir");
        assertThrows(IllegalArgumentException.class, () -> autograder.classDoesNotHaveMultipleScanners(dir));
    }

    /*
     * Tests to see if the classDoesNotHaveMultipleScanners returns false if the argument is
     * valid (e.g. a java file) and contains more than one scanner.
     */
    @Test
    public void testClassDoesNotHaveMultipleScannersWorksOnValidFile()
    {
        File file = new File(getClass().getClassLoader().getResource("multiple_scanners.java").getFile());
        assertFalse(autograder.classDoesNotHaveMultipleScanners(file.getAbsolutePath()));
    }

    /*
     * Tests to see if the classDoesNotUseArrayList method throws an exception if the argument is null.
     */
    @Test
    public void testClassDoesNotUseArrayListThrowsExceptionWhenArgIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotUseArrayList(null));
    }

    /*
     * Tests to see if the classDoesNotUseArrayList method throws an exception if the argument is empty.
     */
    @Test
    public void testClassDoesNotUseArrayListThrowsExceptionWhenArgIsEmpty()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotUseArrayList(""));
    }

    /*
     * Tests to see if the classDoesNotUseArrayList method throws an exception if the argument is
     * invalid (e.g. a folder).
     */
    @Test
    public void testClassDoesNotUseArrayListThrowsExceptionWhenArgIsInvalid()
    {
        String dir = System.getProperty("user.dir");
        assertThrows(IllegalArgumentException.class, () -> autograder.classDoesNotUseArrayList(dir));
    }

    /*
     * Tests to see if the classDoesNotUseArrayList returns false if the argument is
     * valid (e.g. a java file) and uses an ArrayList.
     */
    @Test
    public void testClassDoesNotUseArrayListReturnsFalseOnValidFile()
    {
        File file = new File(getClass().getClassLoader().getResource("uses_arraylist.java").getFile());
        String path = file.getAbsolutePath().replace(".java", "");
        assertFalse(autograder.classDoesNotUseArrayList(path));
    }

    /*
     * Tests to see if the classDoesNotUseArrayList returns true if the argument is
     * valid (e.g. a java file) and does not use an ArrayList.
     */
    @Test
    public void testClassDoesNotUseArrayListReturnsTrueOnValidFile()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        String path = file.getAbsolutePath().replace(".java", "");
        assertTrue(autograder.classDoesNotUseArrayList(path));
    }

    /*
     * Tests to see if the classDoesNotUsePackages method throws an exception if the argument is null.
     */
    @Test
    public void testClassDoesNotUsePackagesThrowsExceptionWhenArgIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotUsePackages(null));
    }

    /*
     * Tests to see if the classDoesNotUsePackages method throws an exception if the argument is empty.
     */
    @Test
    public void testClassDoesNotUsePackagesThrowsExceptionWhenArgIsEmpty()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotUsePackages(""));
    }

    /*
     * Tests to see if the classDoesNotUsePackages method throws an exception if the argument is
     * invalid (e.g. a folder).
     */
    @Test
    public void testClassDoesNotUsePackagesThrowsExceptionWhenArgIsInvalid()
    {
        String dir = System.getProperty("user.dir");
        assertThrows(IllegalArgumentException.class, () -> autograder.classDoesNotUsePackages(dir));
    }

    /*
     * Tests to see if the classDoesNotUsePackages returns false if the argument is
     * valid (e.g. a java file) and uses packages.
     */
    @Test
    public void testClassDoesNotUsePackagesWorksOnValidFile()
    {
        File file = new File(getClass().getClassLoader().getResource("multiple_scanners.java").getFile());
        assertFalse(autograder.classDoesNotUsePackages(file.getAbsolutePath()));
    }

    /*
     * Tests to see if the comparisonTest method throws an exception if the programName argument is null.
     */
    @Test
    public void testComparisonTestThrowsExceptionIfNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.comparisonTest(null, "input", new Object()));
    }

    /*
     * Tests to see if the comparisonTest method throws an exception if the programName argument is empty.
     */
    @Test
    public void testComparisonTestThrowsExceptionIfNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.comparisonTest("", "input", new Object()));
    }

    /*
     * Tests to see if the comparisonTest method throws an exception if the input argument is null.
     */
    @Test
    public void testComparisonTestThrowsExceptionIfInputIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        String path = file.getAbsolutePath();
        int indexOfSample = path.indexOf("Sample.java");
        path = path.substring(0, indexOfSample);
        final String finalPath = path;
        assertThrows(NullPointerException.class, () -> autograder.comparisonTest(finalPath, null, new Object()));
    }

    /*
     * Tests to see if the comparisonTest method throws an exception if the input argument is empty.
     */
    @Test
    public void testComparisonTestThrowsExceptionIfInputIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        String path = file.getAbsolutePath();
        int indexOfSample = path.indexOf("Sample.java");
        path = path.substring(0, indexOfSample);
        final String finalPath = path;
        assertThrows(IllegalArgumentException.class, () -> autograder.comparisonTest(finalPath, "", new Object()));
    }

    /*
     * Tests to see if the comparisonTest method throws an exception if the caller argument is null.
     */
    @Test
    public void testComparisonTestThrowsExceptionIfCallerIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        String path = file.getAbsolutePath();
        int indexOfSample = path.indexOf("Sample.java");
        path = path.substring(0, indexOfSample);
        final String finalPath = path;
        assertThrows(NullPointerException.class, () -> autograder.comparisonTest(finalPath, "", null));
    }

    /*
     * Tests to see if the comparisonTests method throws an exception if the programName argument is null.
     */
    @Test
    public void testComparisonTestsThrowsExceptionIfNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.comparisonTests(null, 1, new Object()));
    }

    /*
     * Tests to see if the comparisonTests method throws an exception if the programName argument is empty.
     */
    @Test
    public void testComparisonTestsThrowsExceptionIfNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.comparisonTests("", 1, new Object()));
    }

    /*
     * Tests to see if the comparisonTests method throws an exception if the count is negative.
     */
    @Test
    public void testComparisonTestsThrowsExceptionIfCountIsNegative()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        String path = file.getAbsolutePath();
        int indexOfSample = path.indexOf("Sample.java");
        path = path.substring(0, indexOfSample);
        final String finalPath = path;
        assertThrows(IllegalArgumentException.class, () -> autograder.comparisonTests(finalPath, -11, new Object()));
    }

    /*
     * Tests to see if the comparisonTests method throws an exception if the caller argument is null.
     */
    @Test
    public void testComparisonTestsThrowsExceptionIfCallerIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        String path = file.getAbsolutePath();
        int indexOfSample = path.indexOf("Sample.java");
        path = path.substring(0, indexOfSample);
        final String finalPath = path;
        assertThrows(NullPointerException.class, () -> autograder.comparisonTests(finalPath, 1, null));
    }

    /*
     * Tests to see if the compile method throws an exception if the argument is null.
     */
    @Test
    public void testCompileIfFilenameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.compile(null));
    }

    /*
     * Tests to see if the compile method throws an exception if the argument is empty.
     */
    @Test
    public void testCompileIfFilenameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.compile(""));
    }

    /*
     * Tests to see if the compile method works for a valid Java file.
     */
    @Test
    public void testCompileWorks()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        int result = autograder.compile(file.getAbsolutePath());
        assertEquals(0, result);
    }

    /*
     * Tests to see if the diffFiles method throws an exception if the argument is null.
     */
    @Test
    public void testDiffFilesIfFirstFilenameIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        assertThrows(NullPointerException.class, () -> autograder.diffFiles(null, file.getAbsolutePath()));
    }

    /*
     * Tests to see if the diffFiles method throws an exception if the argument is null.
     */
    @Test
    public void testDiffFilesIfSecondFilenameIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        assertThrows(NullPointerException.class, () -> autograder.diffFiles(file.getAbsolutePath(), null));
    }

    /*
     * Tests to see if the diffFiles method throws an exception if the first argument is empty.
     */
    @Test
    public void testDiffFilesIfFirstFilenameIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        assertThrows(IllegalArgumentException.class, () -> autograder.diffFiles("", file.getAbsolutePath()));
    }

    /*
     * Tests to see if the diffFiles method throws an exception if the second argument is empty.
     */
    @Test
    public void testDiffFilesIfSecondFilenameIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        assertThrows(IllegalArgumentException.class, () -> autograder.diffFiles(file.getAbsolutePath(), ""));
    }

    /*
     * Tests to see if the diffFiles method throws an exception if the first filename
     * is an invalid file.
     */
    @Test
    public void testDiffFilesIfFirstFilenameIsInvalid()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        String dir = System.getProperty("user.dir");
        assertThrows(FileNotFoundException.class, () -> autograder.diffFiles(dir, file.getAbsolutePath()));
    }

    /*
     * Tests to see if the diffFiles method throws an exception if the second filename
     * is an invalid file.
     */
    @Test
    public void testDiffFilesIfSecondFilenameIsInvalid()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        String dir = System.getProperty("user.dir");
        assertThrows(FileNotFoundException.class, () -> autograder.diffFiles(file.getAbsolutePath(), dir));
    }

    /*
     * Tests to see if the diffFiles method returns false for two different files.
     */
    @Test
    public void testDiffFilesWithTwoDifferentFiles()
    {
        File file1 = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        File file2 = new File(getClass().getClassLoader().getResource("multiple_scanners.java").getFile());
        assertFalse(autograder.diffFiles(file1.getAbsolutePath(), file2.getAbsolutePath()));
    }

    /*
     * Tests to see if the diffFiles method returns true for two different files.
     */
    @Test
    public void testDiffFilesWithTwoIdenticalFiles()
    {
        File file1 = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        File file2 = new File(getClass().getClassLoader().getResource("programSample2.java").getFile());
        assertTrue(autograder.diffFiles(file1.getAbsolutePath(), file2.getAbsolutePath()));
    }

    /*
     * Tests to see if the getClasses method throws an exception if the argument is null.
     */
    @Test
    public void testGetClassesThrowsExceptionIfArgumentIsNull()
    {
        assertThrows(NullPointerException.class, () -> Autograder.getClasses(null));
    }

    /*
     * Tests to see if the getClasses method throws an exception if the argument contains null.
     */
    @Test
    public void testGetClassesThrowsExceptionIfArgumentContainsNull()
    {
        assertThrows(NullPointerException.class, () -> Autograder.getClasses(new String[]{"Integer", null}));
    }

    /*
     * Tests to see if the getMethod method throws an exception if the program name is null.
     */
    @Test
    public void testGetMethodThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> Autograder.getMethod(null, "getInt", Integer.class));
    }

    /*
     * Tests to see if the getMethod method throws an exception if the program name is empty.
     */
    @Test
    public void testGetMethodThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> Autograder.getMethod("", "getInt", Integer.class));
    }

    /*
     * Tests to see if the getMethod method throws an exception if the program name is invalid
     * (i.e., not a file).
     */
    @Test
    public void testGetMethodThrowsExceptionIfProgramNameIsInvalid()
    {
        String dir = System.getProperty("user.dir");
        assertThrows(IOException.class, () -> Autograder.getMethod(dir, "getInt", Integer.class));
    }

    /*
     * Tests to see if the getMethod method throws an exception if the method name is null.
     */
    @Test
    public void testGetMethodThrowsExceptionIfMethodNameIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(NullPointerException.class, () -> Autograder.getMethod(file.getAbsolutePath(), null, Integer.class));
    }

    /*
     * Tests to see if the getMethod method throws an exception if the method name is empty.
     */
    @Test
    public void testGetMethodThrowsExceptionIfMethodNameIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(IllegalArgumentException.class, () -> Autograder.getMethod(file.getAbsolutePath(), "", Integer.class));
    }

    /*
     * Tests to see if getMethod returns null if the method name is invalid
     * (i.e., not existent in the file).
     */
    @Test
    public void testGetMethodReturnsNullIfMethodNameIsInvalid()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertNull(Autograder.getMethod(file.getAbsolutePath(), "invalid", Integer.class));
    }

    /*
     * Tests to see if the getMethod method throws an exception if argTypes is null.
     */
    @Test
    public void testGetMethodThrowsExceptionIfArgTypesIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        String[] argTypes = null;
        assertThrows(NullPointerException.class, () -> Autograder.getMethod(file.getAbsolutePath(), "setGear", argTypes));
    }

    /*
     * Tests to see if the getMethod method throws an exception if argTypes array contains null.
     */
    @Test
    public void testGetMethodThrowsExceptionIfArgTypesContainsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        String[] argTypes = new String[] {"Integer", null};
        assertThrows(NullPointerException.class, () -> Autograder.getMethod(file.getAbsolutePath(), "setGear", argTypes));
    }

    /*
     * Tests to see if the getMethod method returns a valid method for a valid file and other parameters.
     */
    @Test
    public void testGetMethodWorksForValidFile()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertNotNull(Autograder.getMethod(file.getAbsolutePath(), "setGear", Integer.class));
    }

    /*
     * Tests to see if the getMethod method returns a valid method for a built-in class.
     */
    @Test
    public void testGetMethodWorksForBuiltInClass()
    {
        assertNotNull(Autograder.getMethod("java.lang.Integer", "toString", new String[0]));
    }

    /*
     * Tests to see if the getModifiers method throws an exception if the argument is null.
     */
    @Test
    public void testGetModifiersThrowsExceptionIfModsIsNull()
    {
        assertThrows(NullPointerException.class, () -> Autograder.getModifiers(null));
    }

    /*
     * Tests to see if the getModifiers method throws an exception if the argument array contains null.
     */
    @Test
    public void testGetModifiersThrowsExceptionIfModsContainsNull()
    {
        String[] mods = new String[] {"abstract", null};
        assertThrows(NullPointerException.class, () -> Autograder.getModifiers(mods));
    }

    /*
     * Tests to see if the getModifiers method throws an exception if the argument array
     * contains an invalid modifier (i.e., a modifier not listed in the documentation).
     */
    @Test
    public void testGetModifiersThrowsExceptionIfModsContainsInvalidModifier()
    {
        String[] mods = new String[] {"invalid"};
        assertThrows(IllegalArgumentException.class, () -> Autograder.getModifiers(mods));
    }

    /*
     * Tests to see if the hasMethodsTest method throws an exception if the class name is null.
     */
    @Test
    public void testHasMethodsTestThrowsExceptionIfClassNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.hasMethodsTest(null, "Cloneable", false));
    }

    /*
     * Tests to see if the hasMethodsTest method throws an exception if the class name is empty.
     */
    @Test
    public void testHasMethodsTestThrowsExceptionIfClassNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.hasMethodsTest("", "Cloneable", false));
    }

    /*
     * Tests to see if the hasMethodsTest method throws an exception if the interface name is null.
     */
    @Test
    public void testHasMethodsTestThrowsExceptionIfInterfaceNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.hasMethodsTest("Integer", null, false));
    }

    /*
     * Tests to see if the hasMethodsTest method throws an exception if the interface name is empty.
     */
    @Test
    public void testHasMethodsTestThrowsExceptionIfInterfaceNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.hasMethodsTest("Integer", "", false));
    }

    /*
     * Tests to see if the hasMethodsTest method works for valid parameters.
     */
    @Test
    public void testHasMethodsTestWorks()
    {
        assertTrue(autograder.hasMethodsTest("java.util.ArrayList", "Cloneable", false));
    }

    /*
     * Tests to see if the hasMethodTest method throws an exception if the program name is null.
     */
    @Test
    public void testHasMethodTestThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.hasMethodTest(null, "setGear", new Class[]{Integer.class}, null, false, 0, false));
    }

    /*
     * Tests to see if the hasMethodTest method throws an exception if the program name is empty.
     */
    @Test
    public void testHasMethodTestThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.hasMethodTest("", "setGear", new Class[]{Integer.class}, null, false, 0, false));
    }

    /*
     * Tests to see if the hasMethodTest method throws an exception if the method name is null.
     */
    @Test
    public void testHasMethodTestThrowsExceptionIfMethodNameIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(NullPointerException.class, () -> autograder.hasMethodTest(file.getAbsolutePath(), null, new Class[]{Integer.class}, null, false, 0, false));
    }

    /*
     * Tests to see if the hasMethodTest method throws an exception if the method name is empty.
     */
    @Test
    public void testHasMethodTestThrowsExceptionIfMethodNameIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(IllegalArgumentException.class, () -> autograder.hasMethodTest(file.getAbsolutePath(), "", new Class[]{Integer.class}, null, false, 0, false));
    }

    /*
     * Tests to see if the hasMethodTest method throws an exception if the return type is null
     * and the check return type parameter is set to true.
     */
    @Test
    public void testHasMethodTestThrowsExceptionIfReturnTypeIsNullAndCheckReturnIsTrue()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(NullPointerException.class, () -> autograder.hasMethodTest(file.getAbsolutePath(), "getGear", new Class[]{Integer.class}, null, true, 0, false));
    }

    /*
     * Tests to see if the hasMethodTest method throws an exception if the argTypes array contains null.
     */
    @Test
    public void testHasMethodTestThrowsExceptionIfArgTypesContainsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        String[] argTypes = new String[] {"Integer", null};
        String[] modifiers = new String[0];
        assertThrows(NullPointerException.class, () -> autograder.hasMethodTest(file.getAbsolutePath(), "setGear", argTypes, "void", false, modifiers, false));
    }

    /*
     * Tests to see if the hasMethodTest method throws an exception if the argTypes array is null.
     */
    @Test
    public void testHasMethodTestThrowsExceptionIfArgTypesIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        String[] argTypes = null;
        String[] modifiers = new String[0];
        assertThrows(NullPointerException.class, () -> autograder.hasMethodTest(file.getAbsolutePath(), "setGear", argTypes, "void", false, modifiers, false));
    }

    /*
     * Tests to see if the hasMethodTest works with a valid set of parameters.
     */
    @Test
    public void testHasMethodTestWorks()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertTrue(autograder.hasMethodTest(file.getAbsolutePath(), "setGear", new Class[]{Integer.class}, null, false, 0, false));
    }

    /*
     * Tests to see if the junitTests method throws an exception if the program name is null.
     */
    @Test
    public void testJunitTestsThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.junitTests(null));
    }

    /*
     * Tests to see if the junitTests method throws an exception if the program name is empty.
     */
    @Test
    public void testJunitTestsThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.junitTests(""));
    }

    /*
     * Tests to see if the junitTests method throws an exception if the program name is invalid
     * (i.e., not a valid file).
     */
    @Test
    public void testJunitTestsThrowsExceptionIfProgramNameIsInvalid()
    {
        assertThrows(IOException.class, () -> autograder.junitTests("invalid"));
    }

    /*
     * Tests to see if the testSourceExists method throws an exception if the program name is null.
     */
    @Test
    public void testSourceExistsThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.testSourceExists(null));
    }

    /*
     * Tests to see if the testSourceExists method throws an exception if the program name is empty.
     */
    @Test
    public void testSourceExistsThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.testSourceExists(""));
    }

    /*
     * Tests to see if the testSourceExists method throws an exception if the program name is invalid
     * (i.e., not a valid file).
     */
    @Test
    public void testSourceExistsThrowsExceptionIfProgramNameIsInvalid()
    {
        assertThrows(IOException.class, () -> autograder.testSourceExists("invalid"));
    }

    /*
     * Tests to see if the testCompiles method throws an exception if the program name is null.
     */
    @Test
    public void testCompilesThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.testCompiles(null));
    }

    /*
     * Tests to see if the testCompiles method throws an exception if the program name is empty.
     */
    @Test
    public void testCompilesThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.testCompiles(""));
    }

    /*
     * Tests to see if the testCompiles method throws an exception if the program name is invalid
     * (i.e., not a valid file).
     */
    @Test
    public void testCompilesThrowsExceptionIfProgramNameIsInvalid()
    {
        String dir = System.getProperty("user.dir");
        assertThrows(IOException.class, () -> autograder.testCompiles(dir));
    }

    /*
     * Tests to see if the testCompiles method returns true for a valid Java file.
     */
    @Test
    public void testCompilesReturnsTrueForValidJavaFile()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertTrue(autograder.testCompiles(file.getAbsolutePath()));
    }

    /*
     * Tests to see if the testCompiles method returns false for an invalid Java file
     * (the file itself is a valid text file).
     */
    @Test
    public void testCompilesReturnsFalseForInvalidJavaFile()
    {
        File file = new File(getClass().getClassLoader().getResource("invalid.java").getFile());
        assertFalse(autograder.testCompiles(file.getAbsolutePath()));
    }

    /*
     * Tests to see if the hasFieldTest method throws an exception if the program name is null.
     */
    @Test
    public void testHasFieldTestThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.hasFieldTest(null, "gear", "int", new String[0], false));
    }

    /*
     * Tests to see if the hasFieldTest method throws an exception if the program name is empty.
     */
    @Test
    public void testHasFieldTestThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.hasFieldTest("", "gear", "int", new String[0], false));
    }

    /*
     * Tests to see if the hasFieldTest method throws an exception if the program name is invalid
     * (i.e, not a valid file).
     */
    @Test
    public void testHasFieldTestThrowsExceptionIfProgramNameIsInvalid()
    {
        assertThrows(IOException.class, () -> autograder.hasFieldTest(currentDir, "gear", "int", new String[0], false));
    }

    /*
     * Tests to see if the hasFieldTest method returns true for a Java file with a valid field.
     */
    @Test
    public void testHasFieldTestReturnsTrueForProgramWithField()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertTrue(autograder.hasFieldTest(file.getAbsolutePath(), "gear", "int", new String[0], false));

        File file2 = new File(getClass().getClassLoader().getResource("Car.java").getFile());
        assertTrue(autograder.hasFieldTest(file2.getAbsolutePath(), "gear", String.class, 0, false));
    }

    /*
     * Tests to see if the hasFieldTest method returns false for a field that does not match the specified modifiers.
     */
    @Test
    public void testHasFieldTestReturnsFalseForProgramWithFieldWhenModifiersDoNotMatch()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertFalse(autograder.hasFieldTest(file.getAbsolutePath(), "gear", "int", new String[0], true));

        File file2 = new File(getClass().getClassLoader().getResource("Car.java").getFile());
        assertFalse(autograder.hasFieldTest(file2.getAbsolutePath(), "gear", String.class, -1, true));
    }

    /*
     * Tests to see if the hasFieldTest method returns false for a field that does not match the specified modifiers
     * and its field type is ignored.
     */
    @Test
    public void testHasFieldTestReturnsFalseForProgramWithFieldWhenModifiersDoNotMatchAndFieldTypeIgnored()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertFalse(autograder.hasFieldTest(file.getAbsolutePath(), "gear", null, new String[0], true));

        File file2 = new File(getClass().getClassLoader().getResource("Car.java").getFile());
        assertFalse(autograder.hasFieldTest(file2.getAbsolutePath(), "gear", null, -1, true));
    }

    /*
     * Tests to see if the hasFieldTest method returns false for a field that is not in the file.
     */
    @Test
    public void testHasFieldTestReturnsFalseForProgramWithoutField()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertFalse(autograder.hasFieldTest(file.getAbsolutePath(), "name", "int", new String[0], false));
    }

    /*
     * Tests to see if the hasFieldTest method throws an exception if the field name is null.
     */
    @Test
    public void testHasFieldTestThrowsExceptionIfFieldNameIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(NullPointerException.class, () -> autograder.hasFieldTest(file.getAbsolutePath(), null, "int", new String[0], false));
    }

    /*
     * Tests to see if the hasFieldTest method throws an exception if the field name is empty.
     */
    @Test
    public void testHasFieldTestThrowsExceptionIfFieldNameIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(IllegalArgumentException.class, () -> autograder.hasFieldTest(file.getAbsolutePath(), "", "int", new String[0], false));
    }

    /*
     * Tests to see if the hasFieldTest method throws an exception if the field type is null.
     */
    @Test
    public void testHasFieldTestDoesNotThrowExceptionIfFieldTypeIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertDoesNotThrow(() -> autograder.hasFieldTest(file.getAbsolutePath(), "gear", null, new String[0], false));
    }

    /*
     * Tests to see if the hasFieldTest method returns true for a field that exists in the file when
     * checking for the field type is ignored.
     */
    @Test
    public void testHasFieldTestReturnsTrueWhenNotCheckingForFieldType()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertTrue(() -> autograder.hasFieldTest(file.getAbsolutePath(), "gear", null, new String[0], false));
    }


    /*
     * Tests to see if the hasConstructorTest method throws an exception if the class name is null.
     */
    @Test
    public void testHasConstructorTestThrowsExceptionWhenClassNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.hasConstructorTest(null, new String[0], new String[0], false));
    }

    /*
     * Tests to see if the hasConstructorTest method throws an exception if the class name is empty.
     */
    @Test
    public void testHasConstructorTestThrowsExceptionWhenClassNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.hasConstructorTest("", new String[0], new String[0], false));
    }

    /*
     * Tests to see if the hasConstructorTest method throws an exception if the class name is non-existent
     * (i.e., not a valid class).
     */
    @Test
    public void testHasConstructorTestThrowsExceptionWhenClassNameIsNonExistent()
    {
        assertThrows(Exception.class, () -> autograder.hasConstructorTest("NonExistent", new String[0], new String[0], false));
    }

    /*
     * Tests to see if the hasConstructorTest method throws an exception if the argTypes array parameter
     * contains null.
     */
    @Test
    public void testHasConstructorTestThrowsExceptionIfArgTypesContainsNull()
    {
        String[] argTypes = new String[] {"int", null};
        assertThrows(NullPointerException.class, () -> autograder.hasConstructorTest("Integer", argTypes, new String[0], false));
    }

    /*
     * Tests to see if the hasConstructorTest method throws an exception if the argTypes parameter is null.
     */
    @Test
    public void testHasConstructorTestThrowsExceptionIfArgTypesIsNull()
    {
        String[] argTypes = null;
        assertThrows(NullPointerException.class, () -> autograder.hasConstructorTest("Integer", argTypes, new String[0], false));
    }

    /*
     * Tests to see if a variant of the hasConstructorTest method throws an exception if the
     * argTypes parameter is null.
     */
    @Test
    public void testHasConstructorTestVariantThrowsExceptionIfArgTypesIsNull()
    {
        Class<?>[] argTypes = null;
        assertThrows(NullPointerException.class, () -> autograder.hasConstructorTest("Integer", argTypes, 0, false));
    }

    /*
     * Tests to see if a variant of the hasConstructorTest method throws an exception if the
     * modifiers array parameter is null and the checkModifiers parameter is true.
     */
    @Test
    public void testHasConstructorTestThrowsExceptionIfModifiersIsNullAndCheckModifiersIsTrue()
    {
        assertThrows(NullPointerException.class, () -> autograder.hasConstructorTest("Integer", new String[0], null, true));
    }

    /*
     * Tests to see if a variant of the hasConstructorTest method returns true for a class with a valid constructor.
     */
    @Test
    public void testHasConstructorReturnsTrueForClassWithValidConstructor()
    {
        assertTrue(autograder.hasConstructorTest("Integer", new String[] {"int"}, new String[0], false));
    }

    /*
     * Tests to see if a variant of the hasConstructorTest method returns false for a class
     * that does not have a constructor that matches the parameters.
     */
    @Test
    public void testHasConstructorReturnsFalseForClassWithNonMatchingConstructor()
    {
        assertFalse(autograder.hasConstructorTest("Integer", new String[] {"Integer"}, new String[0], false));
    }

    /*
     * Tests to see if the addConverter method throws an exception if the parameter is null.
     */
    @Test
    public void testAddConverterThrowsExceptionIfConverterIsNull()
    {
        assertThrows(NullPointerException.class, () -> Autograder.addConverter(null));
    }

    /*
     * Tests to see if the runFinished method throws an exception if the filename is null.
     */
    @Test
    public void testRunFinishedThrowsExceptionIfFilenameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.testRunFinished(null));
    }

    /*
     * Tests to see if the runFinished method throws an exception if the filename is empty.
     */
    @Test
    public void testRunFinishedThrowsExceptionIfFilenameIsEmpty()
    {
        assertThrows(NullPointerException.class, () -> autograder.testRunFinished(""));
    }

    /*
     * Tests to see if the runFinished method throws an exception if the filename is invalid
     * (i.e., not a valid file).
     */
    @Test
    public void testRunFinishedThrowsExceptionIfFilenameIsInvalid()
    {
        assertThrows(IOException.class, () -> autograder.testRunFinished("invalid"));
    }

    /*
     * Tests to see if the runFinished method works for a valid file and does not throw an exception.
     */
    @Test
    public void testRunFinishedWorksIfFilenameIsValid()
    {
        // Get temp file
        try {
            File temp = File.createTempFile("prefix", "suffix");
            assertDoesNotThrow(() -> autograder.testRunFinished(temp.getAbsolutePath()));
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    /*
     * Tests to see if the stdOutDiffTests method throws an exception if the name is null.
     */
    @Test
    public void testStdOutDiffTestsThrowsExceptionIfNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.stdOutDiffTests(null, 1, false, true, 1));
    }

    /*
     * Tests to see if a variant of the stdOutDiffTests method throws an exception if the name is null.
     */
    @Test
    public void testStdOutDiffTestsVariantThrowsExceptionIfNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.stdOutDiffTests(null, 1, false, true));
    }

    /*
     * Tests to see if the stdOutDiffTests method throws an exception if the name is empty.
     */
    @Test
    public void testStdOutDiffTestsThrowsExceptionIfNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.stdOutDiffTests("", 1, false, true, 1));
    }

    /*
     * Tests to see if a variant of the stdOutDiffTests method throws an exception if the name is empty.
     */
    @Test
    public void testStdOutDiffTestsVariantThrowsExceptionIfNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.stdOutDiffTests("", 1, false, true));
    }

    /*
     * Tests to see if the stdOutDiffTests method throws an exception if the numVisible parameter is negative.
     */
    @Test
    public void testStdOutDiffTestsThrowsExceptionIfNameIsNumVisibleIsNegative()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.stdOutDiffTests(SAMPLE_PROG_PATH, 1, false, true, -1));
    }

    /*
     * Tests to see if the stdOutDiffTests method works and does not throw an exception when the parameters are valid.
     */
    @Test
    public void testStdOutDiffTestsWorksWhenInputIsValid()
    {
        String programName = removeExtension(SAMPLE_PROG_PATH);
        assertDoesNotThrow(() -> autograder.stdOutDiffTests(programName, 1, false, true, 0));
    }

    /*
     * Tests to see if the stdOutDiffTests method throws an exception if the count parameter is invalid (i.e., negative).
     */
    @Test
    public void testStdOutDiffTestsThrowsExceptionWhenCountIsInvalid()
    {
        String programName = removeExtension(SAMPLE_PROG_PATH);
        assertThrows(IllegalArgumentException.class, () -> autograder.stdOutDiffTests(programName, -1, false, true, 0));
    }

    /*
     * Tests to see if the stdOutDiffTest method throws an exception if the name is null.
     */
    @Test
    public void testStdOutDiffTestThrowsExceptionIfNameIsNull() throws IOException {
        assertThrows(NullPointerException.class, () -> autograder.stdOutDiffTest(null, INPUT_FILE_PATH, true, true));
    }

    /*
     * Tests to see if the stdOutDiffTest method throws an exception if the name is empty.
     */
    @Test
    public void testStdOutDiffTestThrowsExceptionIfNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> autograder.stdOutDiffTest("", INPUT_FILE_PATH, true, true));
    }

    /*
     * Tests to see if the stdOutDiffTest method throws an exception if the inFile parameter is null.
     */
    @Test
    public void testStdOutDiffTestThrowsExceptionIfInFileIsNull() {
        assertThrows(NullPointerException.class, () -> autograder.stdOutDiffTest(removeSample(SAMPLE_PROG_PATH), null, true, true));
    }

    /*
     * Tests to see if the stdOutDiffTest method throws an exception if the inFile parameter is empty.
     */
    @Test
    public void testStdOutDiffTestThrowsExceptionIfInFileIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> autograder.stdOutDiffTest(removeSample(SAMPLE_PROG_PATH), "", true, true));
    }

    /*
     * Tests to see if the stdOutDiffTest method throws an exception if the program does not have a main method.
     */
    @Test
    public void testStdOutDiffTestThrowsExceptionIfProgramDoesNotHaveMain() {
        assertThrows(Exception.class, () -> autograder.stdOutDiffTest(removeSample(CLASS_SAMPLE_PATH), INPUT_FILE_PATH, true, true));
    }

    /*
     * Tests to see if the stdOutDiffTest method does not throws an exception when comparing two valid files.
     */
    @Tag("Integration")
    @Test
    public void testStdOutDiffTestDoesNotThrowExceptionIfComparingTwoFiles() throws Exception {
        assertDoesNotThrow(() -> autograder.stdOutDiffTest(INPUT_FILE_PATH, INPUT_FILE_PATH, false, true));
        TestUtilities.CustomTestResult[] results = TestUtilities.getTestResults(autograder);
        assertEquals(1, results.length);
        assertEquals(1, results[0].getScore());
    }


    /*
     * Tests to see if the hasOverriddenMethodTest method throws an exception if the program name is null.
     */
    @Test
    public void testHasOverriddenMethodTestThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.hasOverriddenMethodTest(null,
                "method", new String[0], "void", false, new String[0],
                false));
    }

    /*
     * Tests to see if a variant of the hasOverriddenMethodTest method throws an exception if the program name
     * is null.
     */
    @Test
    public void testHasOverriddenMethodTestVariantThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.hasOverriddenMethodTest(null,
                "method", new Class[0], Void.class, false, 0,
                false));
    }


    /*
     * Tests to see if the hasOverriddenMethodTest method throws an exception if the program name is empty.
     */
    @Test
    public void testHasOverriddenMethodTestThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.hasOverriddenMethodTest("",
                "method", new String[0], "void", false, new String[0],
                false));
    }

    /*
     * Tests to see if a variant of the hasOverriddenMethodTest method throws an exception if the program name
     * is empty.
     */
    @Test
    public void testHasOverriddenMethodTestVariantThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.hasOverriddenMethodTest("",
                "method", new Class[0], void.class, false, 0,
                false));
    }

    /*
     * Tests to see if the hasOverriddenMethodTest method throws an exception if the method name parameter is null.
     */
    @Test
    public void testHasOverriddenMethodTestThrowsExceptionIfMethodNameIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(NullPointerException.class, () -> autograder.hasOverriddenMethodTest(file.getAbsolutePath(),
                null, new String[0], "void", false, new String[0],
                false));
    }

    /*
     * Tests to see if a variant of the hasOverriddenMethodTest method throws an exception if the
     * method name parameter is null.
     */
    @Test
    public void testHasOverriddenMethodTestVariantThrowsExceptionIfMethodNameIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(NullPointerException.class, () -> autograder.hasOverriddenMethodTest(file.getAbsolutePath(),
                null, new Class[0], void.class, false, 0,
                false));
    }

    /*
     * Tests to see if the hasOverriddenMethodTest method throws an exception if the method name parameter is empty.
     */
    @Test
    public void testHasOverriddenMethodTestThrowsExceptionIfMethodNameIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(IllegalArgumentException.class, () -> autograder.hasOverriddenMethodTest(file.getAbsolutePath(),
                "", new String[0], "void", false, new String[0],
                false));
    }

    /*
     * Tests to see if a variant of the hasOverriddenMethodTest method throws an exception if the
     * method name parameter is empty.
     */
    @Test
    public void testHasOverriddenMethodTestVariantThrowsExceptionIfMethodNameIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(IllegalArgumentException.class, () -> autograder.hasOverriddenMethodTest(file.getAbsolutePath(),
                "", new Class[0], void.class, false, 0,
                false));
    }

    /*
     * Tests to see if the testCheckstyle method throws an exception if the parameter is null.
     */
    @Test
    public void testCheckstyleIfArgumentIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.testCheckstyle(null));
    }

    /*
     * Tests to see if the testCheckstyle method throws an exception if the parameter is empty.
     */
    @Test
    public void testCheckstyleIfArgumentIsEmpty()
    {
        assertThrows(NullPointerException.class, () -> autograder.testCheckstyle(""));
    }

    /*
     * Tests to see if the testConstructorCount method throws an exception if the programName parameter is null.
     */
    @Test
    public void testConstructorCountThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.testConstructorCount(null, 1));
    }

    /*
     * Tests to see if the testConstructorCount method throws an exception if the programName parameter is empty.
     */
    @Test
    public void testConstructorCountThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.testConstructorCount("", 1));
    }

    /*
     * Tests to see if the testConstructorCount method returns false if the class file does not have
     * a sufficient number of constructors.
     */
    @Test
    public void testConstructorCountReturnsFalseIfDoesNotHaveSufficientConstructors()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertFalse(autograder.testConstructorCount(file.getAbsolutePath(), 2));
    }

    /*
     * Tests to see if the testConstructorCount method returns true if the class has
     * a sufficient number of constructors and is from a file.
     */
    @Test
    public void testConstructorCountReturnsTrueIfHasSufficientConstructorsAndProgramNameIsFile()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertTrue(autograder.testConstructorCount(file.getAbsolutePath(), 1));
    }

    /*
     * Tests to see if the testConstructorCount method returns true if the class has
     * a sufficient number of constructors.
     */
    @Test
    public void testConstructorCountReturnsTrueIfHasSufficientConstructors()
    {
        assertTrue(autograder.testConstructorCount("java.lang.Integer", 2));
    }

    /*
     * Tests to see if the testMethodCount method throws an exception if the programName
     * is null.
     */
    @Test
    public void testMethodCountThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.testMethodCount(null, 1, 0, false, false));
    }

    /*
     * Tests to see if the testMethodCount method throws an exception if the programName
     * is empty.
     */
    @Test
    public void testMethodCountThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.testMethodCount("", 1, 0, false, false));
    }

    /*
     * Tests to see if the testMethodCount method returns true if class has a sufficient number of methods.
     */
    @Test
    public void testMethodCountReturnsTrueIfClassHasSufficientMethodCount()
    {
        assertTrue(autograder.testMethodCount("java.lang.Integer", 1, 0, false, true));
    }

    /*
     * Tests to see if the testMethodCount method returns false if a class does not have a
     * sufficient number of methods.
     */
    @Test
    public void testMethodCountReturnsFalseIfDoesNotMeetMethodCount()
    {
        assertFalse(autograder.testMethodCount("java.lang.Integer", 100, 0, false, false));
    }

    /*
     * Tests to see if using setScore with a non-positive value sets the score to 0.1.
     */
    @Test
    public void testSetScoreWithNonPositiveValue()
    {
        autograder.setScore(-1);
        assertEquals(0.1, autograder.currentScore(), 0.000001);
    }

    /*
     * Tests to see if the setScore method works for setting the score.
     */
    @Test
    public void testSetScoreWorksProperly()
    {
        double scoreValue = 200;
        autograder.setScore(scoreValue);
        assertEquals(scoreValue, autograder.currentScore(), 0.001);
    }

    /*
     * Tests to see if the setScore method throws an exception when the argument is NaN.
     */
    @Test
    public void testSetScoreThrowsExceptionForNanInput()
    {
        double scoreValue = Double.NaN;
        assertThrows(IllegalArgumentException.class, () -> autograder.setScore(scoreValue));
    }

    /*
     * Tests to see if the setScore method throws an exception when the argument is positive infinity.
     */
    @Test
    public void testSetScoreThrowsExceptionForInfinityInput()
    {
        double scoreValue = Double.POSITIVE_INFINITY;
        assertThrows(IllegalArgumentException.class, () -> autograder.setScore(scoreValue));
    }

    /*
     * Tests to see if the setVisibility method throws an exception for an invalid
     * parameter value (i.e., a value not mentioned in the docs).
     */
    @Test
    public void testSetVisibilityThrowsExceptionForInvalidValue()
    {
        assertThrows(Exception.class, () -> autograder.setVisibility(-1));
    }

    /*
     * Tests to see if setVisibility(0) method makes getVisibility() return "visible"
     */
    @Test
    public void testGetVisibilityReturnsVisible()
    {
        autograder.setVisibility(0);
        assertEquals("visible", autograder.getVisibility());
    }

    /*
     * Tests to see if setVisibility(1) method makes getVisibility() return "hidden"
     */
    @Test
    public void testGetVisibilityReturnsHidden()
    {
        autograder.setVisibility(1);
        assertEquals("hidden", autograder.getVisibility());
    }

    /*
     * Tests to see if setVisibility(2) method makes getVisibility() return "after_due_date"
     */
    @Test
    public void testGetVisibilityReturnsAfterDueDate()
    {
        autograder.setVisibility(2);
        assertEquals("after_due_date", autograder.getVisibility());
    }

    /*
     * Tests to see if setVisibility(3) method makes getVisibility() return "after_published"
     */
    @Test
    public void testGetVisibilityReturnsAfterPublished()
    {
        autograder.setVisibility(3);
        assertEquals("after_published", autograder.getVisibility());
    }

    /*
     * Tests to see if logFileDiffTests throws an exception if the name is null.
     */
    @Test
    public void testLogFilesDiffTestsThrowsExceptionIfNameIsNull() throws IOException {
        File temp = File.createTempFile("prefix", "suffix");
        File temp2 = File.createTempFile("prefix", "suffix");

        assertThrows(NullPointerException.class, () -> autograder.logFileDiffTests(null, 0,
                temp.getAbsolutePath(), temp2.getAbsolutePath(), false));
    }

    /*
     * Tests to see if a variant of the logFileDiffTests method throws an exception if the name is null.
     */
    @Test
    public void testLogFilesDiffTestsVariantThrowsExceptionIfNameIsNull() {
        try {
            File temp = File.createTempFile("prefix", "suffix");
            File temp2 = File.createTempFile("prefix", "suffix");

            assertThrows(NullPointerException.class, () -> autograder.logFileDiffTests(null, 0, 0,
                    temp.getAbsolutePath(), temp2.getAbsolutePath(), false));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /*
     * Tests to see if the logFileDiffTests method throws an exception if the name is empty.
     */
    @Test
    public void testLogFilesDiffTestsThrowsExceptionIfNameIsEmpty() {
        try {
            File temp = File.createTempFile("prefix", "suffix");
            File temp2 = File.createTempFile("prefix", "suffix");

            assertThrows(IllegalArgumentException.class, () -> autograder.logFileDiffTests("", 0,
                    temp.getAbsolutePath(), temp2.getAbsolutePath(), false));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Tests to see if a variant of the logFileDiffTests method throws an exception if the name is empty.
     */
    @Test
    public void testLogFilesDiffTestsVariantThrowsExceptionIfNameIsEmpty() {
        try {
            File temp = File.createTempFile("prefix", "suffix");
            File temp2 = File.createTempFile("prefix", "suffix");

            assertThrows(IllegalArgumentException.class, () -> autograder.logFileDiffTests("", 0, 0,
                    temp.getAbsolutePath(), temp2.getAbsolutePath(), false));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Tests to see if the logFileDiffTest method throws an exception if the name is null.
     */
    @Test
    public void testLogFilesDiffTestThrowsExceptionIfNameIsNull() {
        try {
            File temp = File.createTempFile("prefix", "suffix");
            File temp2 = File.createTempFile("prefix", "suffix");
            File temp3 = File.createTempFile("prefix", "suffix");

            assertThrows(IllegalArgumentException.class, () -> autograder.logFileDiffTest(null,
                    temp.getAbsolutePath(), temp2.getAbsolutePath(), temp3.getAbsolutePath(), false));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Tests to see if the logFileDiffTest method throws an exception if the name is empty.
     */
    @Test
    public void testLogFilesDiffTestThrowsExceptionIfNameIsEmpty() {
        try {
            File temp = File.createTempFile("prefix", "suffix");
            File temp2 = File.createTempFile("prefix", "suffix");
            File temp3 = File.createTempFile("prefix", "suffix");

            assertThrows(IllegalArgumentException.class, () -> autograder.logFileDiffTest("",
                    temp.getAbsolutePath(), temp2.getAbsolutePath(), temp3.getAbsolutePath(), false));
        } catch (IOException e) {
            assertTrue(false);
        }
    }

    /*
     * Tests to see if the setTimeout method throws an exception if the parameter value is invalid
     * (i.e., a negative timeout).
     */
    @Test
    public void testSetTimeoutThrowsExceptionIfValueInvalid()
    {
        assertThrows(Exception.class, () -> autograder.setTimeout(-1));
    }


    /*
     * Tests to see if the testPublicInstanceVariables method throws an exception if the programName is null.
     */
    @Test
    public void testPublicInstanceVariablesThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.testPublicInstanceVariables(null));
    }

    /*
     * Tests to see if the testPublicInstanceVariables method throws an exception if the programName is empty.
     */
    @Test
    public void testPublicInstanceVariablesThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.testPublicInstanceVariables(""));
    }

    /*
     * Tests to see if the testPublicInstanceVariables method throws an exception if the programName is invalid
     * (i.e., not a valid file).
     */
    @Test
    public void testPublicInstanceVariablesThrowsExceptionIfProgramNameIsInvalid()
    {
        assertThrows(Exception.class, () -> autograder.testPublicInstanceVariables(currentDir));
    }

    /*
     * Tests to see if the testPublicInstanceVariables method returns true if the
     * class has public instance variables.
     */
    @Test
    public void testPublicInstanceVariablesReturnsTrueIfClassHasPublicVariables()
    {
        File file = new File(getClass().getClassLoader().getResource("Airplane.java").getFile());
        assertTrue(autograder.testPublicInstanceVariables(file.getAbsolutePath()));
    }

    /*
     * Tests to see if the testPublicInstanceVariables method returns false if the
     * class does not have public instance variables.
     */
    @Test
    public void testPublicInstanceVariablesReturnsFalseIfClassDoesNotHavePublicVariables()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertFalse(autograder.testPublicInstanceVariables(file.getAbsolutePath()));
    }

    /*
     * Tests to see if the compTest method throws an exception if the program name is null.
     */
    @Test
    public void testCompTestThrowsExceptionIfProgramNameIsNull()
    {
        Method method = Autograder.getMethod("java.lang.Integer", "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Integer arg = new Integer(3);
        Object returnValue = true;
        assertThrows(NullPointerException.class, () -> autograder.compTest(null, method, returnValue, caller, null, arg));
    }

    /*
     * Tests to see if the compTest method throws an exception if the program name is empty.
     */
    @Test
    public void testCompTestThrowsExceptionIfProgramNameIsEmpty()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Integer arg = new Integer(3);
        Object returnValue = true;
        assertThrows(IllegalArgumentException.class, () -> autograder.compTest("", method, returnValue, caller, null, arg));
    }

    /*
     * Tests to see if the compTest method throws an exception if the method parameter is null.
     */
    @Test
    public void testCompTestThrowsExceptionIfMethodIsNull()
    {
        String programName = "java.lang.Integer";
        Method method = null;
        Integer caller = new Integer(3);
        Integer arg = new Integer(3);
        Object returnValue = true;
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnValue, caller, null, arg));
    }

    /*
     * Tests to see if the compTest method throws an exception if the caller parameter is null.
     */
    @Test
    public void testCompTestThrowsExceptionIfCallerIsNull()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "equals", new Class[]{Object.class});
        Integer caller = null;
        Integer arg = new Integer(3);
        Object returnValue = true;
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnValue, caller, null, arg));
    }

    /*
     * Tests to see if the compTest method throws an exception if the arg parameter is null.
     */
    @Test
    public void testCompTestThrowsExceptionIfArgIsNull()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Integer arg = null;
        Object returnValue = true;
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnValue, caller, null, arg));
    }

    /*
     * Tests to see if the compTest method throws an exception if the arg parameter is an array containing null.
     */
    @Test
    public void testCompTestThrowsExceptionIfArgContainsNull()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Object arg = new Object[] {null};
        Object returnValue = true;
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnValue, caller, null, arg));
    }

    /*
     * Tests to see if the compTest method works and does not throw an exception for valid parameters.
     */
    @Test
    public void testCompTestWorks()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Integer arg = new Integer(3);
        Object returnValue = true;
        assertDoesNotThrow(() -> autograder.compTest(programName, method, returnValue, caller, null, arg));
    }

    /*
     * Tests to see if the compTest method throws an exception if the value for the stdInput parameter is
     * an invalid file.
     */
    @Test
    public void testCompTestThrowsExceptionForInvalidFile()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Integer arg = new Integer(3);
        Object returnValue = true;
        String stdInput = "invalid";
        assertThrows(IOException.class, () -> autograder.compTest(programName, method, returnValue, caller, stdInput, arg));
    }

    /*
     * Tests to see if the compTest method works and does not throw an exception when the value
     * the stdInput parameter is valid and not null because that indicates that it is not ignored.
     */
    @Test
    public void testCompTestWorksWhenStdInputIsValid()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Integer arg = new Integer(3);
        Object returnValue = true;
        String stdInput = INPUT_FILE_PATH + ".in";
        assertDoesNotThrow(() -> autograder.compTest(programName, method, returnValue, caller, stdInput, arg));
    }


    /*
     * Tests to see if a variant of the compTest method (that has a boolean as one of its inputs)
     * throws an exception if the program name is null.
     */
    @Test
    public void testCompTestBoolThrowsExceptionIfProgramNameIsNull()
    {
        Method method = Autograder.getMethod("java.lang.Integer", "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Integer arg = new Integer(3);
        assertThrows(NullPointerException.class, () -> autograder.compTest(null, method, true, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a boolean as one of its inputs)
     * throws an exception if the program name is empty.
     */
    @Test
    public void testCompTestBoolThrowsExceptionIfProgramNameIsEmpty()
    {
        Method method = Autograder.getMethod("java.lang.Integer", "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Integer arg = new Integer(3);
        assertThrows(IllegalArgumentException.class, () -> autograder.compTest("", method, true, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a boolean as one of its inputs)
     * throws an exception if the method parameter is null.
     */
    @Test
    public void testCompTestBoolThrowsExceptionIfMethodIsNull()
    {
        Method method = null;
        Integer caller = new Integer(3);
        Integer arg = new Integer(3);
        String programName = "java.lang.Integer";
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, true, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a boolean as one of its inputs)
     * throws an exception if the caller parameter is null.
     */
    @Test
    public void testCompTestBoolThrowsExceptionIfCallerIsNull()
    {
        Method method = Autograder.getMethod("java.lang.Integer", "equals", new Class[]{Object.class});
        Integer caller = null;
        Integer arg = new Integer(3);
        String programName = "java.lang.Integer";
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, true, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a boolean as one of its inputs)
     * throws an exception if the arg parameter is null.
     */
    @Test
    public void testCompTestBoolThrowsExceptionIfArgIsNull()
    {
        Method method = Autograder.getMethod("java.lang.Integer", "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Object arg = null;
        assertThrows(NullPointerException.class, () -> autograder.compTest("java.lang.Integer", method, false, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a boolean as one of its inputs)
     * throws an exception if the arg parameter is an array containing null.
     */
    @Test
    public void testCompTestBoolThrowsExceptionIfArgContainsNull()
    {
        Method method = Autograder.getMethod("java.lang.Integer", "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Object arg = new Object[]{null};
        assertThrows(NullPointerException.class, () -> autograder.compTest("java.lang.Integer", method, false, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a boolean as one of its inputs)
     * works and does not throw an exception when the parameters are valid.
     */
    @Test
    public void testCompTestBoolWorks()
    {
        Method method = Autograder.getMethod("java.lang.Integer", "equals", new Class[]{Object.class});
        Integer caller = new Integer(3);
        Integer arg = new Integer(3);
        assertDoesNotThrow(() -> autograder.compTest("java.lang.Integer", method, true, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has an int as one of its inputs)
     * throws an exception if the program name is null.
     */
    @Test
    public void testCompTestIntThrowsExceptionIfProgramNameIsNull()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "intValue", new String[0]);
        Integer caller = new Integer(3);
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(null, method, 3, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has an int as one of its inputs)
     * throws an exception if the program name is empty.
     */
    @Test
    public void testCompTestIntThrowsExceptionIfProgramNameIsEmpty()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "intValue", new String[0]);
        Integer caller = new Integer(3);
        Object arg = new Object();
        assertThrows(IllegalArgumentException.class, () -> autograder.compTest("", method, 3, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has an int as one of its inputs)
     * throws an exception if the method parameter is null.
     */
    @Test
    public void testCompTestIntThrowsExceptionIfMethodIsNull()
    {
        String programName = "java.lang.Integer";
        Method method = null;
        Integer caller = new Integer(3);
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, 3, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has an int as one of its inputs)
     * throws an exception if the caller parameter is null.
     */
    @Test
    public void testCompTestIntThrowsExceptionIfCallerIsNull()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "intValue", new String[0]);
        Integer caller = null;
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, 3, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has an int as one of its inputs)
     * throws an exception if the arg parameter is null.
     */
    @Test
    public void testCompTestIntThrowsExceptionIfArgIsNull()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "intValue", new String[0]);
        Integer caller = new Integer(3);
        Object arg = null;
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, 3, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has an int as one of its inputs)
     * throws an exception if the arg parameter is an array containing null.
     */
    @Test
    public void testCompTestIntThrowsExceptionIfArgContainsNull()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "intValue", new String[0]);
        Integer caller = new Integer(3);
        Object arg = new Object[]{null};
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, 3, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has an int as one of its inputs)
     * works and does not throw an exception when the parameters are valid.
     */
    @Test
    public void testCompTestIntWorks()
    {
        String programName = "java.lang.Integer";
        Method method = Autograder.getMethod(programName, "intValue", new String[0]);
        Integer caller = new Integer(3);
        Object arg = new Object();
        assertDoesNotThrow(() -> autograder.compTest(programName, method, 3, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a char as one of its inputs)
     * throws an exception if the program name is null.
     */
    @Test
    public void testCompTestCharThrowsExceptionIfProgramNameIsNull()
    {
        String programName = "java.lang.Character";
        Method method = Autograder.getMethod(programName, "charValue", new String[0]);
        Character caller = new Character('a');
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(null, method, 'a', caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a char as one of its inputs)
     * throws an exception if the program name is empty.
     */
    @Test
    public void testCompTestCharThrowsExceptionIfProgramNameIsEmpty()
    {
        String programName = "java.lang.Character";
        Method method = Autograder.getMethod(programName, "charValue", new String[0]);
        Character caller = new Character('a');
        Object arg = new Object();
        assertThrows(IllegalArgumentException.class, () -> autograder.compTest("", method, 'a', caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a char as one of its inputs)
     * throws an exception if the method parameter is null.
     */
    @Test
    public void testCompTestCharThrowsExceptionIfMethodIsNull()
    {
        String programName = "java.lang.Character";
        Method method = null;
        Character caller = new Character('a');
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, 'a', caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a char as one of its inputs)
     * throws an exception if the caller parameter is null.
     */
    @Test
    public void testCompTestCharThrowsExceptionIfCallerIsNull()
    {
        String programName = "java.lang.Character";
        Method method = Autograder.getMethod(programName, "charValue", new String[0]);
        Character caller = null;
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, 'a', caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a char as one of its inputs)
     * throws an exception if the arg parameter is null.
     */
    @Test
    public void testCompTestCharThrowsExceptionIfArgIsNull()
    {
        String programName = "java.lang.Character";
        Method method = Autograder.getMethod(programName, "charValue", new String[0]);
        Character caller = new Character('a');
        Object arg = null;
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, 'a', caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a char as one of its inputs)
     * throws an exception if the arg parameter is an array containing null.
     */
    @Test
    public void testCompTestCharThrowsExceptionIfArgContainsNull()
    {
        String programName = "java.lang.Character";
        Method method = Autograder.getMethod(programName, "charValue", new String[0]);
        Character caller = new Character('a');
        Object arg = new Object[]{null};
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, 'a', caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a char as one of its inputs)
     * works and does not throw an exception when the parameters are valid.
     */
    @Test
    public void testCompTestCharWorks()
    {
        String programName = "java.lang.Character";
        Method method = Autograder.getMethod(programName, "charValue", new String[0]);
        Character caller = new Character('a');
        Object arg = new Object();
        assertDoesNotThrow(() -> autograder.compTest("java.lang.Character", method, 'a', caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a double as one of its inputs)
     * throws an exception if the program name is null.
     */
    @Test
    public void testCompTestDoubleThrowsExceptionIfProgramNameIsNull()
    {
        String programName = "java.lang.Double";
        Method method = Autograder.getMethod(programName, "doubleValue", new Class[0]);
        double returnVal = 3.0;
        Double caller = new Double(3.0);
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(null, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a double as one of its inputs)
     * throws an exception if the program name is empty.
     */
    @Test
    public void testCompTestDoubleThrowsExceptionIfProgramNameIsEmpty()
    {
        String programName = "java.lang.Double";
        Method method = Autograder.getMethod(programName, "doubleValue", new Class[0]);
        double returnVal = 3.0;
        Double caller = new Double(3.0);
        Object arg = new Object();
        assertThrows(IllegalArgumentException.class, () -> autograder.compTest("", method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a double as one of its inputs)
     * throws an exception if the method parameter is null.
     */
    @Test
    public void testCompTestDoubleThrowsExceptionIfMethodIsNull()
    {
        String programName = "java.lang.Double";
        Method method = null;
        double returnVal = 3.0;
        Double caller = new Double(3.0);
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a double as one of its inputs)
     * throws an exception if the caller parameter is null.
     */
    @Test
    public void testCompTestDoubleThrowsExceptionIfCallerIsNull()
    {
        String programName = "java.lang.Double";
        Method method = Autograder.getMethod(programName, "doubleValue", new Class[0]);
        double returnVal = 3.0;
        Double caller = new Double(3.0);
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a double as one of its inputs)
     * throws an exception if the arg parameter is null.
     */
    @Test
    public void testCompTestDoubleThrowsExceptionIfArgIsNull()
    {
        String programName = "java.lang.Double";
        Method method = Autograder.getMethod(programName, "doubleValue", new Class[0]);
        double returnVal = 3.0;
        Double caller = new Double(returnVal);
        Object arg = null;
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a double as one of its inputs)
     * throws an exception if the arg parameter is an array containing null.
     */
    @Test
    public void testCompTestDoubleThrowsExceptionIfArgContainsNull()
    {
        String programName = "java.lang.Double";
        Method method = Autograder.getMethod(programName, "doubleValue", new Class[0]);
        double returnVal = 3.0;
        Double caller = new Double(returnVal);
        Object arg = new Object[]{null};
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a double as one of its inputs)
     * works and does not throw an exception when the parameters are valid.
     */
    @Test
    public void testCompTestDoubleWorks()
    {
        String programName = "java.lang.Double";
        Method method = Autograder.getMethod(programName, "doubleValue", new Class[0]);
        double returnVal = 3.0;
        Double caller = new Double(returnVal);
        Object arg = new Object();
        assertDoesNotThrow(() -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a long as one of its inputs)
     * throws an exception if the program name is null.
     */
    @Test
    public void testCompTestLongThrowsExceptionIfProgramNameIsNull()
    {
        String programName = "java.lang.Long";
        Method method = Autograder.getMethod(programName, "longValue", new Class[0]);
        long returnVal = 3;
        Long caller = new Long(returnVal);
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(null, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a long as one of its inputs)
     * throws an exception if the program name is empty.
     */
    @Test
    public void testCompTestLongThrowsExceptionIfProgramNameIsEmpty()
    {
        String programName = "java.lang.Long";
        Method method = Autograder.getMethod(programName, "longValue", new Class[0]);
        long returnVal = 3;
        Long caller = new Long(returnVal);
        Object arg = new Object();
        assertThrows(IllegalArgumentException.class, () -> autograder.compTest("", method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a long as one of its inputs)
     * throws an exception if the method parameter is null.
     */
    @Test
    public void testCompTestLongThrowsExceptionIfMethodIsNull()
    {
        String programName = "java.lang.Long";
        Method method = null;
        long returnVal = 3;
        Long caller = new Long(returnVal);
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a long as one of its inputs)
     * throws an exception if the caller parameter is null.
     */
    @Test
    public void testCompTestLongThrowsExceptionIfCallerIsNull()
    {
        String programName = "java.lang.Long";
        Method method = Autograder.getMethod(programName, "longValue", new Class[0]);
        long returnVal = 3;
        Long caller = null;
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a long as one of its inputs)
     * throws an exception if the arg parameter is null.
     */
    @Test
    public void testCompTestLongThrowsExceptionIfArgIsNull()
    {
        String programName = "java.lang.Long";
        Method method = Autograder.getMethod(programName, "longValue", new Class[0]);
        long returnVal = 3;
        Long caller = new Long(returnVal);
        Object arg = null;
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a long as one of its inputs)
     * throws an exception if the arg parameter is an array containing null.
     */
    @Test
    public void testCompTestLongThrowsExceptionIfArgContainsNull()
    {
        String programName = "java.lang.Long";
        Method method = Autograder.getMethod(programName, "longValue", new Class[0]);
        long returnVal = 3;
        Long caller = new Long(returnVal);
        Object arg = new Object[]{null};
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a long as one of its inputs)
     * works and does not throw an exception when the parameters are valid.
     */
    @Test
    public void testCompTestLongWorks()
    {
        String programName = "java.lang.Long";
        Method method = Autograder.getMethod(programName, "longValue", new Class[0]);
        long returnVal = 3;
        Long caller = new Long(returnVal);
        Object arg = new Object();
        assertDoesNotThrow(() -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a float as one of its inputs)
     * throws an exception if the program name is null.
     */
    @Test
    public void testCompTestFloatThrowsExceptionIfProgramNameIsNull()
    {
        String programName = "java.lang.Float";
        Method method = Autograder.getMethod(programName, "floatValue", new Class[0]);
        assertNotNull(method);
        float returnVal = 3.0f;
        Float caller = new Float(returnVal);
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(null, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a float as one of its inputs)
     * throws an exception if the program name is empty.
     */
    @Test
    public void testCompTestFloatThrowsExceptionIfProgramNameIsEmpty()
    {
        String programName = "java.lang.Float";
        Method method = Autograder.getMethod(programName, "floatValue", new Class[0]);
        float returnVal = 3.0f;
        Float caller = new Float(returnVal);
        Object arg = new Object();
        assertThrows(IllegalArgumentException.class, () -> autograder.compTest("", method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a float as one of its inputs)
     * throws an exception if the method parameter is null.
     */
    @Test
    public void testCompTestFloatThrowsExceptionIfMethodIsNull()
    {
        String programName = "java.lang.Float";
        Method method = null;
        float returnVal = 3.0f;
        Float caller = new Float(returnVal);
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a float as one of its inputs)
     * throws an exception if the caller parameter is null.
     */
    @Test
    public void testCompTestFloatThrowsExceptionIfCallerIsNull()
    {
        String programName = "java.lang.Float";
        Method method = Autograder.getMethod(programName, "floatValue", new Class[0]);
        float returnVal = 3.0f;
        Float caller = null;
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a float as one of its inputs)
     * throws an exception if the arg parameter is null.
     */
    @Test
    public void testCompTestFloatThrowsExceptionIfArgIsNull()
    {
        String programName = "java.lang.Float";
        Method method = Autograder.getMethod(programName, "floatValue", new Class[0]);
        float returnVal = 3.0f;
        Float caller = new Float(returnVal);
        Object arg = null;
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a float as one of its inputs)
     * throws an exception if the arg parameter is an array containing null.
     */
    @Test
    public void testCompTestFloatThrowsExceptionIfArgContainsNull()
    {
        String programName = "java.lang.Float";
        Method method = Autograder.getMethod(programName, "floatValue", new Class[0]);
        float returnVal = 3.0f;
        Float caller = new Float(returnVal);
        Object arg = new Object[]{null};
        assertThrows(NullPointerException.class, () -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if a variant of the compTest method (that has a float as one of its inputs)
     * works and does not throw an exception when the parameters are valid.
     */
    @Test
    public void testCompTestFloatWorks()
    {
        String programName = "java.lang.Float";
        Method method = Autograder.getMethod(programName, "floatValue", new Class[0]);
        float returnVal = 3.0f;
        Float caller = new Float(returnVal);
        Object arg = new Object();
        assertDoesNotThrow(() -> autograder.compTest(programName, method, returnVal, caller, arg));
    }

    /*
     * Tests to see if the stackTraceToString method throws an exception if the parameter is null.
     */
    @Test
    public void testStackTraceToStringThrowsExceptionIfArgIsNull()
    {
        assertThrows(NullPointerException.class, () -> Autograder.stackTraceToString(null));
    }

    /*
     * Tests to see if the stackTraceToString method works properly for a valid exception.
     */
    @Test
    public void testStackTraceToStringWorksProperly()
    {
        Exception e = new Exception();
        e.setStackTrace(new StackTraceElement[0]);
        assertEquals("java.lang.Exception\n", Autograder.stackTraceToString(e));
    }

    /*
     * Tests to see if the runMethodWithTimeout method throws an exception if the method
     * parameter is null.
     */
    @Test
    public void testRunMethodWithTimeoutThrowsExceptionIfMethodIsNull()
    {
        Integer caller = new Integer(3);
        Object arg = new Object();
        assertThrows(NullPointerException.class, () -> autograder.runMethodWithTimeout(null, caller, arg));
    }

    /*
     * Tests to see if the runMethodWithTimeout method throws an exception if the caller
     * parameter is null.
     */
    @Test
    public void testRunMethodWithTimeoutThrowsExceptionIfCallerIsNull()
    {
        Integer caller = new Integer(3);
        Object arg = new Object();
        Method method = Autograder.getMethod("java.lang.Integer", "intValue", new Class[0]);
        assertNotNull(method);
        assertThrows(NullPointerException.class, () -> autograder.runMethodWithTimeout(method, null, arg));
    }

    /*
     * Tests to see if the runMethodWithTimeout method throws an exception if the args
     * parameter is null.
     */
    @Test
    public void testRunMethodWithTimeoutThrowsExceptionIfArgIsNull()
    {
        Integer caller = new Integer(3);
        Object arg = null;
        Method method = Autograder.getMethod("java.lang.Integer", "intValue", new Class[0]);
        assertNotNull(method);
        assertThrows(NullPointerException.class, () -> autograder.runMethodWithTimeout(method, caller, arg));
    }

    /*
     * Tests to see if the testSortedCheckstyle method throws an exception if the programName
     * parameter is null.
     */
    @Test
    public void testSortedCheckstyleThrowsExceptionIfProgramNameIsNull()
    {
       assertThrows(NullPointerException.class, () -> autograder.testSortedCheckstyle(null, 0, false));
    }

    /*
     * Tests to see if the testSortedCheckstyle method throws an exception if the programName
     * parameter is empty.
     */
    @Test
    public void testSortedCheckstyleThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.testSortedCheckstyle("", 0, false));
    }

    /*
     * Tests to see if the testSortedCheckstyle method does not throw an exception if the errValue is negative.
     * This is because deducting negative points is equivalent to adding points.
     */
    @Test
    public void testSortedCheckstyleWithNegativeErrValue()
    {
        assertDoesNotThrow(() -> autograder.testSortedCheckstyle(BICYCLE_FILE_PATH, -1, false));
    }

    /*
     * Tests to see if the testSortedCheckstyle method throws an exception if the errValue is NaN.
     */
    @Test
    public void testSortedCheckstyleThrowsExceptionIfErrValueIsNan()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.testSortedCheckstyle(BICYCLE_FILE_PATH, Double.NaN, false));
    }

    /*
     * Tests to see if the testSortedCheckstyle method throws an exception if the errValue is infinity.
     */
    @Test
    public void testSortedCheckstyleThrowsExceptionIfErrValueIsInfinity()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.testSortedCheckstyle(BICYCLE_FILE_PATH, Double.POSITIVE_INFINITY, false));
        assertThrows(IllegalArgumentException.class, () -> autograder.testSortedCheckstyle(BICYCLE_FILE_PATH, Double.NEGATIVE_INFINITY, false));
    }

    /*
     * Tests to see if the testSortedCheckstyle method does not throw an exception if the parameters are valid.
     */
    @Test
    public void testSortedCheckstyleWorks()
    {
        assertDoesNotThrow(() -> autograder.testSortedCheckstyle(BICYCLE_FILE_PATH, 0, false));
    }

    /*
     * Tests to see if the Autograder class constructor throws an exception if the visibility parameter is invalid
     * (i.e., a value not mentioned in documentation).
     */
    @Test
    public void testAutograderConstructorThrowsExceptionForInvalidVisibility()
    {
        assertThrows(IllegalArgumentException.class, () -> new Autograder(-1, 1));
    }

    /*
     * Tests to see if the Autograder class constructor throws an exception if the score parameter is invalid
     * (e.g., negative, infinity, or NaN).
     */
    @Test
    public void testAutograderConstructorThrowsExceptionForInvalidScore()
    {
        assertThrows(IllegalArgumentException.class, () -> new Autograder(0, -1));
        assertThrows(IllegalArgumentException.class, () -> new Autograder(0, Double.NEGATIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new Autograder(0, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new Autograder(0, Double.NaN));
    }



}
