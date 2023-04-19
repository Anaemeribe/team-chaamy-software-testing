import jh61b.grader.TestResult;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class AutograderTestBlackboxTest {

    private Autograder autograder;

    @BeforeEach
    public void setup()
    {
        autograder = new Autograder();
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
     * Tests to see if the classDoesNotHaveMultipleScanners method throws an exception if the argument is null
     */
    @Test
    public void testClassDoesNotHaveMultipleScannersThrowsExceptionWhenArgIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotHaveMultipleScanners(null));
    }

    /*
     * Tests to see if the classDoesNotHaveMultipleScanners method throws an exception if the argument is empty
     */
    @Test
    public void testClassDoesNotHaveMultipleScannersThrowsExceptionWhenArgIsEmpty()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotHaveMultipleScanners(""));
    }

    /*
     * Tests to see if the classDoesNotHaveMultipleScanners method throws an exception if the argument is
     * invalid (e.g. a folder)
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
     * Tests to see if the classDoesNotUseArrayList method throws an exception if the argument is null
     */
    @Test
    public void testClassDoesNotUseArrayListThrowsExceptionWhenArgIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotUseArrayList(null));
    }

    /*
     * Tests to see if the classDoesNotUseArrayList method throws an exception if the argument is empty
     */
    @Test
    public void testClassDoesNotUseArrayListThrowsExceptionWhenArgIsEmpty()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotUseArrayList(""));
    }

    /*
     * Tests to see if the classDoesNotUseArrayList method throws an exception if the argument is
     * invalid (e.g. a folder)
     */
    @Test
    public void testClassDoesNotUseArrayListThrowsExceptionWhenArgIsInvalid()
    {
        String dir = System.getProperty("user.dir");
        assertThrows(IllegalArgumentException.class, () -> autograder.classDoesNotUseArrayList(dir));
    }

    /*
     * Tests to see if the classDoesNotUseArrayList returns false if the argument is
     * valid (e.g. a java file) and uses an ArrayList
     */
    @Test
    public void testClassDoesNotUseArrayListWorksOnValidFile()
    {
        File file = new File(getClass().getClassLoader().getResource("uses_arraylist.java").getFile());
        assertFalse(autograder.classDoesNotUseArrayList(file.getAbsolutePath()));
    }

    /*
     * Tests to see if the classDoesNotUsePackages method throws an exception if the argument is null
     */
    @Test
    public void testClassDoesNotUsePackagesThrowsExceptionWhenArgIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotUsePackages(null));
    }

    /*
     * Tests to see if the classDoesNotUsePackages method throws an exception if the argument is empty
     */
    @Test
    public void testClassDoesNotUsePackagesThrowsExceptionWhenArgIsEmpty()
    {
        assertThrows(NullPointerException.class, () -> autograder.classDoesNotUsePackages(""));
    }

    /*
     * Tests to see if the classDoesNotUsePackages method throws an exception if the argument is
     * invalid (e.g. a folder)
     */
    @Test
    public void testClassDoesNotUsePackagesThrowsExceptionWhenArgIsInvalid()
    {
        String dir = System.getProperty("user.dir");
        assertThrows(IllegalArgumentException.class, () -> autograder.classDoesNotUsePackages(dir));
    }

    /*
     * Tests to see if the classDoesNotUsePackages returns false if the argument is
     * valid (e.g. a java file) and uses packages
     */
    @Test
    public void testClassDoesNotUsePackagesWorksOnValidFile()
    {
        File file = new File(getClass().getClassLoader().getResource("multiple_scanners.java").getFile());
        assertFalse(autograder.classDoesNotUsePackages(file.getAbsolutePath()));
    }

    // Comparison test tests

    /*
     * Tests to see if the comparisonTest method throws an exception if the programName argument is null
     */
    @Test
    public void testComparisonTestThrowsExceptionIfNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.comparisonTest(null, "input", new Object()));
    }

    /*
     * Tests to see if the comparisonTest method throws an exception if the programName argument is empty
     */
    @Test
    public void testComparisonTestThrowsExceptionIfNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.comparisonTest("", "input", new Object()));
    }

    /*
     * Tests to see if the comparisonTest method throws an exception if the input argument is null
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
     * Tests to see if the comparisonTest method throws an exception if the input argument is empty
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
     * Tests to see if the comparisonTest method throws an exception if the caller argument is null
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
     * Tests to see if the comparisonTests method throws an exception if the programName argument is null
     */
    @Test
    public void testComparisonTestsThrowsExceptionIfNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.comparisonTests(null, 1, new Object()));
    }

    /*
     * Tests to see if the comparisonTests method throws an exception if the programName argument is empty
     */
    @Test
    public void testComparisonTestsThrowsExceptionIfNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.comparisonTests("", 1, new Object()));
    }

    /*
     * Tests to see if the comparisonTests method throws an exception if the count is negative
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
     * Tests to see if the comparisonTests method throws an exception if the caller argument is null
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

    // compile method

    /*
     * Tests to see if the compile method throws an exception if the argument is null
     */
    @Test
    public void testCompileIfFilenameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.compile(null));
    }

    /*
     * Tests to see if the compile method throws an exception if the argument is empty
     */
    @Test
    public void testCompileIfFilenameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.compile(""));
    }

    /*
     * Tests to see if the compile method works for a valid Java file
     */
    @Test
    public void testCompileWorks()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        int result = autograder.compile(file.getAbsolutePath());
        assertEquals(0, result);
    }

    // diff files

    /*
     * Tests to see if the diffFiles method throws an exception if the argument is null
     */
    @Test
    public void testDiffFilesIfFirstFilenameIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        assertThrows(NullPointerException.class, () -> autograder.diffFiles(null, file.getAbsolutePath()));
    }

    /*
     * Tests to see if the diffFiles method throws an exception if the argument is null
     */
    @Test
    public void testDiffFilesIfSecondFilenameIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        assertThrows(NullPointerException.class, () -> autograder.diffFiles(file.getAbsolutePath(), null));
    }

    /*
     * Tests to see if the diffFiles method throws an exception if the first argument is empty
     */
    @Test
    public void testDiffFilesIfFirstFilenameIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        assertThrows(IllegalArgumentException.class, () -> autograder.diffFiles("", file.getAbsolutePath()));
    }

    /*
     * Tests to see if the diffFiles method throws an exception if the second argument is empty
     */
    @Test
    public void testDiffFilesIfSecondFilenameIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        assertThrows(IllegalArgumentException.class, () -> autograder.diffFiles(file.getAbsolutePath(), ""));
    }

    /*
     * Tests to see if the diffFiles method throws an exception if the first filename
     * is an invalid file
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
     * is an invalid file
     */
    @Test
    public void testDiffFilesIfSecondFilenameIsInvalid()
    {
        File file = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        String dir = System.getProperty("user.dir");
        assertThrows(FileNotFoundException.class, () -> autograder.diffFiles(file.getAbsolutePath(), dir));
    }

    /*
     * Tests to see if the diffFiles method returns false for two different files
     */
    @Test
    public void testDiffFilesWithTwoDifferentFiles()
    {
        File file1 = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        File file2 = new File(getClass().getClassLoader().getResource("multiple_scanners.java").getFile());
        assertFalse(autograder.diffFiles(file1.getAbsolutePath(), file2.getAbsolutePath()));
    }

    /*
     * Tests to see if the diffFiles method returns true for two different files
     */
    @Test
    public void testDiffFilesWithTwoIdenticalFiles()
    {
        File file1 = new File(getClass().getClassLoader().getResource("programSample.java").getFile());
        File file2 = new File(getClass().getClassLoader().getResource("programSample2.java").getFile());
        assertTrue(autograder.diffFiles(file1.getAbsolutePath(), file2.getAbsolutePath()));
    }

    /*
     * Tests to see if the getClasses method throws an exception if the argument is null
     */
    @Test
    public void testGetClassesThrowsExceptionIfArgumentIsNull()
    {
        assertThrows(NullPointerException.class, () -> Autograder.getClasses(null));
    }

    /*
     * Tests to see if the getClasses method throws an exception if the argument contains null
     */
    @Test
    public void testGetClassesThrowsExceptionIfArgumentContainsNull()
    {
        assertThrows(NullPointerException.class, () -> Autograder.getClasses(new String[]{"Integer", null}));
    }

    // Get method tests
    @Test
    public void testGetMethodThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> Autograder.getMethod(null, "getInt", Integer.class));
    }

    @Test
    public void testGetMethodThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> Autograder.getMethod("", "getInt", Integer.class));
    }

    @Test
    public void testGetMethodThrowsExceptionIfProgramNameIsInvalid()
    {
        String dir = System.getProperty("user.dir");
        assertThrows(IOException.class, () -> Autograder.getMethod(dir, "getInt", Integer.class));
    }

    @Test
    public void testGetMethodThrowsExceptionIfMethodNameIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(NullPointerException.class, () -> Autograder.getMethod(file.getAbsolutePath(), null, Integer.class));
    }

    @Test
    public void testGetMethodThrowsExceptionIfMethodNameIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(IllegalArgumentException.class, () -> Autograder.getMethod(file.getAbsolutePath(), "", Integer.class));
    }

    @Test
    public void testGetMethodThrowsExceptionIfMethodNameIsInvalid()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertNull(Autograder.getMethod(file.getAbsolutePath(), "invalid", Integer.class));
    }

    @Test
    public void testGetMethodThrowsExceptionIfArgTypesIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        String[] argTypes = null;
        assertThrows(NullPointerException.class, () -> Autograder.getMethod(file.getAbsolutePath(), "setGear", argTypes));
    }

    @Test
    public void testGetMethodThrowsExceptionIfArgTypesContainsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        String[] argTypes = new String[] {"Integer", null};
        assertThrows(NullPointerException.class, () -> Autograder.getMethod(file.getAbsolutePath(), "setGear", argTypes));
    }

    @Test
    public void testGetMethodWorks()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertNotNull(Autograder.getMethod(file.getAbsolutePath(), "setGear", Integer.class));
    }

    // getModifiers tests
    @Test
    public void testGetModifiersThrowsExceptionIfModsIsNull()
    {
        assertThrows(NullPointerException.class, () -> Autograder.getModifiers(null));
    }

    @Test
    public void testGetModifiersThrowsExceptionIfModsContainsNull()
    {
        String[] mods = new String[] {"abstract", null};
        assertThrows(NullPointerException.class, () -> Autograder.getModifiers(mods));
    }

    @Test
    public void testGetModifiersThrowsExceptionIfModsContainsInvalidModifier()
    {
        String[] mods = new String[] {"invalid"};
        assertThrows(IllegalArgumentException.class, () -> Autograder.getModifiers(mods));
    }

    // hasMethods test
    @Test
    public void testHasMethodsTestThrowsExceptionIfClassNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.hasMethodsTest(null, "Cloneable", false));
    }

    @Test
    public void testHasMethodsTestThrowsExceptionIfClassNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.hasMethodsTest("", "Cloneable", false));
    }

    @Test
    public void testHasMethodsTestThrowsExceptionIfInterfaceNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.hasMethodsTest("Integer", null, false));
    }

    @Test
    public void testHasMethodsTestThrowsExceptionIfInterfaceNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.hasMethodsTest("Integer", "", false));
    }

    @Test
    public void testHasMethodsTestWorks()
    {
        assertTrue(autograder.hasMethodsTest("java.util.ArrayList", "Cloneable", false));
    }

    // test hasMethod

    @Test
    public void testHasMethodThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.hasMethodTest(null, "setGear", new Class[]{Integer.class}, null, false, 0, false));
    }

    @Test
    public void testHasMethodThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.hasMethodTest("", "setGear", new Class[]{Integer.class}, null, false, 0, false));
    }

    @Test
    public void testHasMethodThrowsExceptionIfMethodNameIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(NullPointerException.class, () -> autograder.hasMethodTest(file.getAbsolutePath(), null, new Class[]{Integer.class}, null, false, 0, false));
    }

    @Test
    public void testHasMethodThrowsExceptionIfMethodNameIsEmpty()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(IllegalArgumentException.class, () -> autograder.hasMethodTest(file.getAbsolutePath(), "", new Class[]{Integer.class}, null, false, 0, false));
    }

    @Test
    public void testHasMethodThrowsExceptionIfReturnTypeIsNullAndCheckReturnIsTrue()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertThrows(NullPointerException.class, () -> autograder.hasMethodTest(file.getAbsolutePath(), "getGear", new Class[]{Integer.class}, null, true, 0, false));
    }

    @Test
    public void testHasMethodThrowsExceptionIfArgTypesContainsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        String[] argTypes = new String[] {"Integer", null};
        String[] modifiers = new String[0];
        assertThrows(NullPointerException.class, () -> autograder.hasMethodTest(file.getAbsolutePath(), "setGear", argTypes, "void", false, modifiers, false));
    }

    @Test
    public void testHasMethodThrowsExceptionIfArgTypesIsNull()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        String[] argTypes = null;
        String[] modifiers = new String[0];
        assertThrows(NullPointerException.class, () -> autograder.hasMethodTest(file.getAbsolutePath(), "setGear", argTypes, "void", false, modifiers, false));
    }

    @Test
    public void testHasMethodWorks()
    {
        File file = new File(getClass().getClassLoader().getResource("Bicycle.java").getFile());
        assertTrue(autograder.hasMethodTest(file.getAbsolutePath(), "setGear", new Class[]{Integer.class}, null, false, 0, false));
    }

    // test junitTests

    @Test
    public void testJunitTestsThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.junitTests(null));
    }

    @Test
    public void testJunitTestsThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.junitTests(""));
    }

    @Test
    public void testJunitTestsThrowsExceptionIfProgramNameIsInvalid()
    {
        assertThrows(IOException.class, () -> autograder.junitTests("invalid"));
    }

    // test source exists

    @Test
    public void testSourceExistsThrowsExceptionIfProgramNameIsNull()
    {
        assertThrows(NullPointerException.class, () -> autograder.testSourceExists(null));
    }

    @Test
    public void testSourceExistsThrowsExceptionIfProgramNameIsEmpty()
    {
        assertThrows(IllegalArgumentException.class, () -> autograder.testSourceExists(""));
    }

    @Test
    public void testSourceExistsThrowsExceptionIfProgramNameIsInvalid()
    {
        assertThrows(IOException.class, () -> autograder.testSourceExists("invalid"));
    }

    @Test
    public void testConstructorWorks() {
        assertEquals("hidden", autograder.getVisibility());
        assertEquals("visible", autograder.getVisibility());
    }

    @Test
    public void testAddTestResult() {
        TestResult tr = new TestResult("Test testResult",
                "5",
                100, "visible");
        autograder.addTestResult(tr);
    }

}
