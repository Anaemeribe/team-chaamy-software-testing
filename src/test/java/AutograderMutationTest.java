import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class AutograderMutationTest {
    private static Autograder ag;

    @BeforeEach
    public void init() {
        ag = new Autograder();
    }

    /**
     * Test to check default constructor sets visibility to default "hidden".
     */
    @Test
    public void testDefaultConstructorVisibility() {
        assertEquals("hidden", ag.visibility);
    }

    /**
     * Test to check default constructor sets score to default 0.1.
     */
    @Test
    public void testDefaultConstructorScore() {
        assertEquals(0.1, ag.maxScore);
    }

    /**
     * Test to check constructor properly sets valid visibility.
     * Valid visibility is [0, 3].
     */
    @Test
    public void testConstructorVisibility() {
        ag = new Autograder(0, 0);
        assertEquals("visible", ag.visibility);

        ag = new Autograder(1, 0);
        assertEquals("hidden", ag.visibility);

        ag = new Autograder(2, 0);
        assertEquals("after_due_date", ag.visibility);

        ag = new Autograder(3, 0);
        assertEquals("after_published", ag.visibility);
    }


    /**
     * Test to check constructor properly sets visibility to default given invalid
     * visibility.
     */
    @Test
    public void testConstructorInvalidVisibility() {
        Autograder ag = new Autograder(-1, 0);
        assertEquals("hidden", ag.visibility);

        ag = new Autograder(4, 0);
        assertEquals("hidden", ag.visibility);

        ag = new Autograder(Integer.MAX_VALUE, 0);
        assertEquals("hidden", ag.visibility);

        ag = new Autograder(Integer.MIN_VALUE, 0);
        assertEquals("hidden", ag.visibility);
    }

    /**
     * Test to check constructor properly sets valid score.
     * Valid score is a positive score.
     */
    @Test
    public void testConstructorScore() {
        double[] scores = {0.000001, Double.MAX_VALUE, Double.MIN_VALUE, 9.2, 7375, 45372.38674};
        for (double score : scores) {
            ag = new Autograder(0, score);
            assertEquals(score, ag.maxScore);
        }
    }


    /**
     * Test to check constructor properly sets invalid score to default 0.1.
     * Invalid score is non-positive.
     */
    @Test
    public void testConstructorInvalidScore() {
        // Zero fails this test
        double[] scores = {
//                0,
                -Double.MAX_VALUE,
                -Double.MIN_VALUE,
                -9.2,
                -7375,
                -45372.38674
        };
        for (double score : scores) {
            ag = new Autograder(0, score);
            assertEquals(0.1, ag.maxScore);
        }
    }

    @Test
    public void testConstructorSecurityManager() {
        SecurityManager m = System.getSecurityManager();
        assertSame(m.getClass(), Autograder.StopExitSecurityManager.class);
        ag = new Autograder();
        assertNotEquals(m, System.getSecurityManager());
    }

    private static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    @Test
    public void testRunFinished() {
        SecurityManager m = System.getSecurityManager();
        String filename = "./build/tmp/temp.txt";
        assertThrows(Autograder.ExitTrappedException.class, () -> ag.testRunFinished(filename));
        assertNotEquals(m, System.getSecurityManager());
        AtomicReference<String> s = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            s.set(readFile(filename));
        });
        String result = s.get().trim();
        assertEquals("{\"tests\": []}", result);
        // clean up
        assertDoesNotThrow(() -> {
            Files.delete(Path.of(filename));
        });
    }

    @Test
    public void testSourceExistsInvalidFile() {
        String filename = "doesnotexist";
        assertFalse(ag.testSourceExists(filename));

        filename = "./build"; // directory
        assertFalse(ag.testSourceExists(filename));
    }

    @Test
    public void testSourceExists() {
        String filename = "./build/tmp/temp.txt";
        File file = new File(filename);
        try {
            file.createNewFile();
        } catch (IOException e) {
            fail("Temp file \'" + filename + "\' could not be created!");
        }
        assertTrue(ag.testSourceExists(filename));
        file.delete();
    }

    @Test
    public void testCompile() {
        String filename = "./src/test/resources/Program.java";
        assertEquals(0, ag.compile(filename));
    }

    @Test
    public void testCompileInvalidFile() {
        String filename = "doesnotexist";
        assertNotEquals(0, ag.compile(filename));
    }


    @Test
    public void testTestCompile() {
        String filename = "./src/test/resources/Program";
        assertTrue(ag.testCompiles(filename));
    }

    @Test
    public void testTestCompileInvalidFile() {
        String filename = "doesnotexist";
        assertFalse(ag.testCompiles(filename));
    }

    @Test
    public void testStdOutDiffTestsNoClass() {
        String filename = "brandon.util.NotExist";
        assertEquals(1, ag.diffNum);
        ag.stdOutDiffTests(filename, 1, false, true, 0);
        // TODO create method to compare TestResults
        assertEquals(2, ag.diffNum);
    }

    @Test
    public void testDiffFilesInvalidFile() {
        String invalid = "doesnotexist";
        File file = new File(getClass().getClassLoader().getResource("Program.java").getFile());
        String valid = file.getAbsolutePath();

        assertEquals(1, ag.diffNum);
        assertFalse(ag.diffFiles(invalid, valid));
        assertEquals(2, ag.diffNum);
        assertFalse(ag.diffFiles(valid, invalid));
        assertEquals(3, ag.diffNum);
        assertFalse(ag.diffFiles(invalid, invalid));
        assertEquals(4, ag.diffNum);
    }

    @Test
    public void testDiffFilesSame() {
        File orig = new File(getClass().getClassLoader().getResource("Program.java").getFile());
        File copy = new File("temp.java");
        assertDoesNotThrow(() -> copy.createNewFile());
        assertDoesNotThrow(() ->
                Files.copy(orig.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING)
        );
        String origPath = orig.getAbsolutePath();
        String copyPath = copy.getAbsolutePath();

        assertEquals(1, ag.diffNum);
        assertTrue(ag.diffFiles(origPath, origPath));
        assertEquals(2, ag.diffNum);
        assertTrue(ag.diffFiles(origPath, copyPath));
        assertTrue(ag.diffFiles(copyPath, origPath));

        copy.delete();
    }
//
//
//    @Test
//    public void testDiffFilesDifferent() {
//        String orig = "./src/test/resources/Program.java";
//        // Make copy
//        String copy = "./build/tmp/temp.txt";
//        File temp = new File(copy);
//        assertDoesNotThrow(() -> temp.createNewFile());
//        assertDoesNotThrow(() ->
//                Files.copy(Paths.get(orig), Paths.get(copy), StandardCopyOption.REPLACE_EXISTING)
//        );
//
//        assertEquals(1, ag.diffNum);
//        assertTrue(ag.diffFiles(orig, orig));
//        assertEquals(2, ag.diffNum);
//        assertTrue(ag.diffFiles(orig, copy));
//        assertEquals(3, ag.diffNum);
//        assertTrue(ag.diffFiles(copy, orig));
//        assertEquals(4, ag.diffNum);
//
//        temp.delete();
//    }

}
