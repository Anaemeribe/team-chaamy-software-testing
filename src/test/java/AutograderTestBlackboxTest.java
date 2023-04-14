import jh61b.grader.TestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

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
}
