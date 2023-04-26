import brandon.convert.ClassConverter;
import jh61b.grader.TestResult;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
public class AutograderWhiteboxTest {
    public Autograder autograder;
    public File testFile;
    @BeforeEach
    public void setUp() {
        testFile = new File(getClass().getClassLoader().getResource("project_2/Proj2BSample.java").getFile());
        autograder = new Autograder(0, 100);
    }

    @Test
    public void testConstructorWorks() {
        Autograder hidden_Autograder = new Autograder(1, 100);
        Autograder after_due_date_Autograder = new Autograder(2, 100);
        Autograder after_published_Autograder = new Autograder(3, 100);
        assertEquals("visible", autograder.getVisibility());
        assertEquals("hidden", hidden_Autograder.getVisibility());
        assertEquals("after_due_date", after_due_date_Autograder.getVisibility());
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
        TestResult tr = new TestResult("Test testResult", "5", 100, "visible");
        assertDoesNotThrow(() -> autograder.addTestResult(tr));
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
    public void testTestRunFinished() {
        assertThrows(Exception.class, () -> autograder.testRunFinished());
        TestResult tr = new TestResult("Test testResult", "1", 10, "visible");
        autograder.addTestResult(tr);
        assertDoesNotThrow(() -> autograder.testRunFinished());
    }

    @Test
    public void testTestRunFinishedWithFileName() {
        TestResult tr = new TestResult("Test testResult", "1", 10, "visible");
        autograder.addTestResult(tr);
        assertDoesNotThrow(() -> autograder.testRunFinished("outputFile"));
    }

    @Test
    public void testSourceExists() {
        assertTrue(autograder.testSourceExists(testFile.getAbsolutePath()));
        assertFalse(autograder.testSourceExists("./project_2"));
        assertFalse(autograder.testSourceExists("nonExistentFile"));
    }

    @Test
    public void testCompile() {
        assertEquals(0, autograder.compile(testFile.getAbsolutePath()));
        assertNotEquals(0, autograder.compile("non-testFile"));
    }

    @Test
    public void testTestCompiles() {
        File tempFile = new File(getClass().getClassLoader().getResource("programSample2.java").getFile());
        assertTrue(autograder.testCompiles(testFile.getAbsolutePath()));
        assertTrue(autograder.testCompiles(tempFile.getAbsolutePath()));
        assertFalse(autograder.testCompiles("non-testFile"));
    }

    // TODO: Add more tests
    @Test
    public void testTestCheckstyle() {
        assertDoesNotThrow(() -> autograder.testCheckstyle(testFile.getAbsolutePath()));
        assertThrows(Exception.class,() -> autograder.testCheckstyle("non-testFile"));
    }

    @Test
    public void testTestSortedCheckstyle() {
        assertThrows(Exception.class, () -> autograder.testSortedCheckstyle("non-testFile", 1, false));
        assertDoesNotThrow(() -> autograder.testSortedCheckstyle(testFile.getAbsolutePath(), 1, false));
        assertDoesNotThrow(() -> autograder.testSortedCheckstyle(testFile.getAbsolutePath(), 2, true));
    }

    @Test
    public void testStdOutDiffTests() {
        assertDoesNotThrow(() -> autograder.stdOutDiffTests(testFile.getAbsolutePath(), 1, true, true));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests(testFile.getAbsolutePath(), 2, false, true));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests(testFile.getAbsolutePath(), 3, true, false));
        assertDoesNotThrow(() -> autograder.stdOutDiffTests(testFile.getAbsolutePath(), 4, false, false));
    }


}
