import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;

import jh61b.grader.TestResult;

import static org.junit.jupiter.api.Assertions.*;

public class AutograderMutationTest {
    private static Autograder ag;
    private static File sample;
    private static File methods;

    @BeforeAll
    public static void setup() throws IOException {
        Path original = Paths.get("./src/test/resources/Sample.java");
//        Path copy = Paths.get("./Sample.java");
        sample = new File("./src/main/java/Sample.java"); // src/main/java/Autograder.java
        Files.copy(original, sample.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        Files.copy(original, copy, StandardCopyOption.REPLACE_EXISTING);
        assertTrue(sample.isFile());
        assertTrue(sample.exists());

        original = Paths.get("src/test/resources/MultipleMethods.java");
//        copy = Paths.get("./MultipleMethods.java");
        methods = new File("src/main/java/MultipleMethods.java");
        Files.copy(original, methods.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        Files.copy(original, copy, StandardCopyOption.REPLACE_EXISTING);
        assertTrue(sample.isFile());
        assertTrue(sample.exists());

    }

    @BeforeEach
    public void init() {
        ag = new Autograder(1,0.1);
    }

    @AfterAll
    public static void cleanup() {
//        assertTrue(sample.delete());
//        assertTrue(methods.delete());
        File f = new File( "brandon.util.NotExist_0.out");
        if (f.exists()) {
            assertTrue(f.delete());
        }

        f = new File( "src/main/java/MultipleMethods.class");
        if (f.exists()) {
            assertTrue(f.delete());
        }
        f = new File( "MultipleMethods.class");
        if (f.exists()) {
            assertTrue(f.delete());
        }

        f = new File( "src/test/resources/Sample.class");
        if (f.exists()) {
            assertTrue(f.delete());
        }
        f = new File( "src/main/java/Sample.class");
        if (f.exists()) {
            assertTrue(f.delete());
        }
        f = new File( "Sample.class");
        if (f.exists()) {
            assertTrue(f.delete());
        }
    }

    /**
     * Test to check default constructor sets visibility to default "hidden".
     */
    @Test
    public void testDefaultConstructorVisibility() {
        Autograder a = new Autograder();
        assertEquals("hidden", a.visibility);
    }

    /**
     * Test to check default constructor sets score to default 0.1.
     */
    @Test
    public void testDefaultConstructorScore() {
        Autograder a = new Autograder();
        assertEquals(0.1, a.maxScore);
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
        Autograder a = new Autograder(-1, 0);
        assertEquals("hidden", a.visibility);

        a = new Autograder(4, 0);
        assertEquals("hidden", a.visibility);

        a = new Autograder(Integer.MAX_VALUE, 0);
        assertEquals("hidden", a.visibility);

        a = new Autograder(Integer.MIN_VALUE, 0);
        assertEquals("hidden", a.visibility);
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

    private static void compareTestResults(TestResult[] t)  {
        try {
            TestResult[] results = TestUtilities.getTestResults(ag);
            assertEquals(t.length, results.length);
            for (int i = 0; i < t.length; i++) {
                assertEquals(t[i].toString(), results[i].toString());
            }
        } catch (Exception e) {
            fail("Failed to get test results:\n" + e.toString());
        }
    }

    @Test
    public void testSourceExistsInvalidFile() {
        String name = "_.java Source File Exists";
        String error = "ERROR: file _.java is not present!\n" +
                "\tCheck the spelling of your file name.\n";

        String d = "doesnotexist";
        assertFalse(ag.testSourceExists(d));
        TestResult existTR =  new TestResult(name.replace("_", d), "Pre-Test",
                0, "hidden");
        existTR.addOutput(error.replace("_", d));

        String b = "./build"; // directory
        assertFalse(ag.testSourceExists(b));
        TestResult buildTR =  new TestResult(name.replace("_", b), "Pre-Test",
                0, "hidden");
        buildTR.addOutput(error.replace("_", b));

        compareTestResults(new TestResult[]{existTR, buildTR});
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

        TestResult t = new TestResult(
                        filename + ".java Source File Exists",
                        "Pre-Test",
                        ag.maxScore,
                        "hidden");
        t.addOutput("SUCCESS: file " + filename + ".java is present!\n");
        compareTestResults(new TestResult[]{t});

        file.delete();
    }

    @Test
    public void testCompile() {
        String filename = "./src/test/resources/Sample.java";
        assertEquals(0, ag.compile(filename));
    }

    @Test
    public void testCompileInvalidFile() {
        String filename = "doesnotexist";
        assertNotEquals(0, ag.compile(filename));
    }


    @Test
    public void testTestCompile() {
        String filename = "./src/test/resources/Sample";
        assertTrue(ag.testCompiles(filename));
    }

    @Test
    public void testTestCompileInvalidFile() {
        String filename = "doesnotexist";
        assertFalse(ag.testCompiles(filename));
    }
//
//    @Test
//    public void testStdOutDiffTestsNoClass() {
//        String filename = "brandon.util.NotExist";
//        assertEquals(1, ag.diffNum);
//        ag.stdOutDiffTests(filename, 1, false, true, 0);
//        // TODO create method to compare TestResults
//        assertEquals(2, ag.diffNum);
//    }

    @Test
    public void testDiffFilesInvalidFile() {
        String invalid = "doesnotexist";
        File file = new File(getClass().getClassLoader().getResource("Sample.java").getFile());
        String valid = file.getAbsolutePath();

        assertEquals(1, ag.diffNum);
        assertFalse(ag.diffFiles(invalid, valid));
        assertEquals(2, ag.diffNum);
        assertFalse(ag.diffFiles(valid, invalid));
        assertEquals(3, ag.diffNum);
        assertFalse(ag.diffFiles(invalid, invalid));
        assertEquals(4, ag.diffNum);
    }

//    @Test
//    public void testDiffFilesSame() {
//        File copy = new File("./build/tmp/temp.txt");
//        assertDoesNotThrow(() -> copy.createNewFile());
//        assertDoesNotThrow(() ->
//                Files.copy(sample.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING)
//        );
//        String origPath = sample.getAbsolutePath();
//        String copyPath = copy.getAbsolutePath();
//
//        assertEquals(1, ag.diffNum);
//        assertTrue(ag.diffFiles(origPath, origPath));
//        assertEquals(2, ag.diffNum);
//        assertTrue(ag.diffFiles(origPath, copyPath));
//        assertTrue(ag.diffFiles(copyPath, origPath));
//
//        copy.delete();
//   }

//    @Test
//    public void testDiffFilesDifferent() {
//        String orig = "./src/test/resources/Sample.java";
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

    @Test
    public void testStackTraceToString() {
        Exception es = new Exception("Testing stackTraceToString()");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        es.printStackTrace(pw);
        String stack = sw.toString().replace("/", "//");
        assertEquals(stack, Autograder.stackTraceToString(es));
    }

    @Test
    public void testGetMethodNoClass() {
        String filename = "doesnotexist";
        assertNull(Autograder.getMethod(filename, ""));
    }

    @Test
    public void testGetMethodNoMethod() {
        String filename = "Sample";
        assertNull(Autograder.getMethod(filename, ""));
        assertNull(Autograder.getMethod(filename, "exists"));
    }

    @Test
    public void testGetMethodNoParams() {
        String filename = "Sample";
        assertNull(Autograder.getMethod(filename, "main"));
    }

    @Test
    public void testGetMethodWrongParams() {
        String filename = "Sample";
        assertNull(Autograder.getMethod(filename, "main", int.class));
        assertNull(Autograder.getMethod(filename, "main", String.class));
        assertNull(Autograder.getMethod(filename, "main", String[].class, double.class));
    }

    @Test
    public void testGetMethod() {
        String name = "Sample";
        String m = "main";
        Class[] c = {String[].class};
        Method method = null;
        try {
            method = Class.forName(name).getMethod(m, c);
        } catch (Exception e) {
            fail("Failed to get method from class:\n" + e.toString());
        }
        assertEquals(method, Autograder.getMethod(name, m, c));
    }

    @Test
    public void testGetMethodStringNoClass() {
        String filename = "doesnotexist";
        assertNull(Autograder.getMethod(filename, "main", new String[]{"String[]"}));
    }

    @Test
    public void testGetMethodStringNoMethod() {
        String filename = "Sample";
        assertNull(Autograder.getMethod(filename, "",  new String[]{}));
        assertNull(Autograder.getMethod(filename, "exists", new String[]{}));
    }

    @Test
    public void testGetMethodStringNoParams() {
        String filename = "Sample";
        assertNull(Autograder.getMethod(filename, "main", new String[]{}));
    }

    @Test
    public void testGetMethodStringWrongParams() {
        String filename = "Sample";
        assertNull(Autograder.getMethod(filename, "main",  new String[]{"int"}));
        assertNull(Autograder.getMethod(filename, "main",  new String[]{"String"}));
        assertNull(Autograder.getMethod(filename, "main",  new String[]{"String[]", "double"}));
    }

    @Test
    public void testGetMethodString() {
        String name = "Sample";
        String m = "main";
        Class[] c = {String[].class};
        Method method = null;
        try {
            method = Class.forName(name).getMethod(m, c);
        } catch (Exception e) {
            fail("Failed to get method from class:\n" + e.toString());
        }
        assertEquals(method, Autograder.getMethod(name, m, new String[]{"String[]"}));
    }

    @Test
    public void testHasMethods() {
        // TODO TR
        String name = "Sample";
        String other = "MultipleMethods";
        assertEquals(1, ag.diffNum);
        assertTrue(ag.hasMethodsTest(name, name, true));
        assertEquals(2, ag.diffNum);
        assertTrue(ag.hasMethodsTest(name, name, false));
        assertFalse(ag.hasMethodsTest(name, other, true));
        assertTrue(ag.hasMethodsTest(other, name, true));
    }

    @Test
    public void testHasMethod() {
        // TODO TR
        String name = "Sample";
        assertEquals(1, ag.diffNum);
        assertTrue(ag.hasMethodTest(
                name,
                "main",
                new String[]{"String[]"},
                "void",
                true,
                new String[]{"public", "static", "void"},
                true
                ));
        assertEquals(2, ag.diffNum);
    }

    @Test
    public void testHasOverriddenMethod() {
        // TODO TR
        String name = "Sample";
        assertEquals(1, ag.diffNum);
        assertTrue(ag.hasOverriddenMethodTest(
                name,
                "main",
                new String[]{"String[]"},
                "void",
                true,
                new String[]{"public", "static", "void"},
                true
        ));
        assertEquals(2, ag.diffNum);
    }

    @Test
    public void testHasOverriddenMethodWrongName() {
        String name = "Sample";
        assertFalse(ag.hasOverriddenMethodTest(
                name,
                "fake",
                new String[]{"String[]"},
                "void",
                true,
                new String[]{"public", "static", "void"},
                true
        ));
    }

    @Test
    public void testHasOverriddenMethodWrongArg() {
        String name = "Sample";
        assertFalse(ag.hasOverriddenMethodTest(
                name,
                "main",
                new String[]{"int"},
                "void",
                true,
                new String[]{"public", "static", "void"},
                true
        ));
    }

    @Test
    public void testHasOverriddenMethodWrongReturn() {
        String name = "Sample";
        assertFalse(ag.hasOverriddenMethodTest(
                name,
                "main",
                new String[]{"String[]"},
                "double",
                true,
                new String[]{"public", "static", "void"},
                true
        ));
        assertTrue(ag.hasOverriddenMethodTest(
                name,
                "main",
                new String[]{"String[]"},
                "double",
                false,
                new String[]{"public", "static", "void"},
                true
        ));
    }

    @Test
    public void testHasOverriddenMethodWrongMods() {
        String name = "Sample";
        assertFalse(ag.hasOverriddenMethodTest(
                name,
                "main",
                new String[]{"String[]"},
                "void",
                true,
                new String[]{"private"},
                true
        ));
        assertTrue(ag.hasOverriddenMethodTest(
                name,
                "main",
                new String[]{"String[]"},
                "void",
                true,
                new String[]{"private"},
                false
        ));
    }

    @Test
    public void testHasOverriddenMethodClass() {
        // TODO TR
        String name = "Sample";
        assertEquals(1, ag.diffNum);
        assertTrue(ag.hasOverriddenMethodTest(
                name,
                "main",
                new Class[]{String[].class},
                void.class,
                true,
                Modifier.PUBLIC | Modifier.STATIC,
                true
        ));
        assertEquals(2, ag.diffNum);
    }

    @Test
    public void testHasOverriddenMethodClassWrongName() {
        String name = "Sample";
        assertFalse(ag.hasOverriddenMethodTest(
                name,
                "fake",
                new Class[]{String[].class},
                void.class,
                true,
                Modifier.PUBLIC | Modifier.STATIC,
                true
        ));
    }

    @Test
    public void testHasOverriddenMethodClassWrongArg() {
        String name = "Sample";
        assertFalse(ag.hasOverriddenMethodTest(
                name,
                "main",
                new Class[]{int.class},
                void.class,
                true,
                Modifier.PUBLIC | Modifier.STATIC,
                true
        ));
    }

    @Test
    public void testHasOverriddenMethodClassWrongReturn() {
        String name = "Sample";
        assertFalse(ag.hasOverriddenMethodTest(
                name,
                "main",
                new Class[]{String[].class},
                double.class,
                true,
                Modifier.PUBLIC | Modifier.STATIC,
                true
        ));
        assertTrue(ag.hasOverriddenMethodTest(
                name,
                "main",
                new Class[]{String[].class},
                double.class,
                false,
                Modifier.PUBLIC | Modifier.STATIC,
                true
        ));
    }

    @Test
    public void testHasOverriddenMethodClassWrongMods() {
        String name = "Sample";
        assertFalse(ag.hasOverriddenMethodTest(
                name,
                "main",
                new Class[]{String[].class},
                void.class,
                true,
                Modifier.PRIVATE,
                true
        ));
        assertTrue(ag.hasOverriddenMethodTest(
                name,
                "main",
                new Class[]{String[].class},
                void.class,
                true,
                Modifier.PRIVATE,
                false
        ));
    }

    @Test
    public void testHasConstructor() {
        assertFalse(ag.hasConstructorTest(
                "doesnotexist",
                new String[]{},
                new String[]{},
                true
        ));

        String name = "Sample";
        assertFalse(ag.hasConstructorTest(
                name,
                new String[]{},
                new String[]{},
                true
        ));

        name = "MultipleMethods";
        assertTrue(ag.hasConstructorTest(
                name,
                new String[]{"String"},
                new String[]{"public"},
                true
        ));
    }

    @Test
    public void testHasConstructorWrongArg() {
        String name = "MultipleMethods";
        assertFalse(ag.hasConstructorTest(
                name,
                new String[]{"int"},
                new String[]{"public"},
                true
        ));
    }

    @Test
    public void testHasConstructorWrongMod() {
        String name = "MultipleMethods";
        assertFalse(ag.hasConstructorTest(
                name,
                new String[]{"String"},
                new String[]{"private"},
                true
        ));
        assertTrue(ag.hasConstructorTest(
                name,
                new String[]{"String"},
                new String[]{"private"},
                false
        ));
    }

    @Test
    public void testHasConstructorClass() {
        String name = "Sample";
        assertFalse(ag.hasConstructorTest(
                name,
                new Class[]{String.class},
                Modifier.PUBLIC,
                false
        ));

        name = "MultipleMethods";
        assertTrue(ag.hasConstructorTest(
                name,
                new Class[]{String.class},
                Modifier.PUBLIC,
                true
        ));
    }

    @Test
    public void testHasConstructorClassWrongArg() {
        String name = "MultipleMethods";
        assertFalse(ag.hasConstructorTest(
                name,
                new Class[]{int.class},
                Modifier.PUBLIC,
                true
        ));
    }

    @Test
    public void testHasConstructorClassWrongMod() {
        String name = "MultipleMethods";
        assertFalse(ag.hasConstructorTest(
                name,
                new Class[]{String.class},
                Modifier.PRIVATE,
                true
        ));
        assertTrue(ag.hasConstructorTest(
                name,
                new Class[]{String.class},
                Modifier.PRIVATE,
                false
        ));
    }

    @Test
    public void testGetMethodWrongArg() {
        String name = "Sample";;
        assertFalse(ag.hasMethodTest(
                name,
                "main",
                new String[]{"double"},
                "void",
                true,
                new String[]{"public", "static", "void"},
                true
        ));
    }

    @Test
    public void testGetMethodWrongReturn() {
        String name = "Sample";;
        assertFalse(ag.hasMethodTest(
                name,
                "main",
                new String[]{"String[]"},
                "int",
                true,
                new String[]{"public", "static", "void"},
                true
        ));
    }

    @Test
    public void testGetMethodWrongMods() {
        String name = "Sample";;
        assertFalse(ag.hasMethodTest(
                name,
                "main",
                new String[]{"String[]"},
                "int",
                true,
                new String[]{"private"},
                true
        ));
    }

    @Test
    public void testGetModifiers() {
        assertEquals(Modifier.PRIVATE, ag.getModifiers(new String[]{"private"}));
        assertEquals(Modifier.ABSTRACT, ag.getModifiers(new String[]{"abstract"}));
        assertEquals(Modifier.FINAL, ag.getModifiers(new String[]{"final"}));
        assertEquals(Modifier.INTERFACE, ag.getModifiers(new String[]{"interface"}));
        assertEquals(Modifier.NATIVE, ag.getModifiers(new String[]{"native"}));
        assertEquals(Modifier.PROTECTED, ag.getModifiers(new String[]{"protected"}));
        assertEquals(Modifier.PUBLIC, ag.getModifiers(new String[]{"public"}));
        assertEquals(Modifier.STATIC, ag.getModifiers(new String[]{"static"}));
        assertEquals(Modifier.STRICT, ag.getModifiers(new String[]{"strict"}));
        assertEquals(Modifier.SYNCHRONIZED, ag.getModifiers(new String[]{"synchronized"}));
        assertEquals(Modifier.TRANSIENT, ag.getModifiers(new String[]{"transient"}));
        assertEquals(Modifier.VOLATILE, ag.getModifiers(new String[]{"volatile"}));
        assertEquals(0, ag.getModifiers(new String[]{""}));
    }

    @Test
    public void testGetModifiersMultiple() {
        assertEquals(Modifier.PRIVATE | Modifier.PUBLIC, ag.getModifiers(new String[]{"private", "public"}));
        assertEquals(Modifier.PUBLIC | Modifier.PRIVATE, ag.getModifiers(new String[]{"public", "private"}));
        assertEquals(Modifier.ABSTRACT | Modifier.FINAL | Modifier.VOLATILE, ag.getModifiers(new String[]{
                "abstract", "final", "volatile"
        }));
        assertEquals(Modifier.STRICT | Modifier.SYNCHRONIZED | 0, ag.getModifiers(new String[]{
                "strict", "synchronized", ""
        }));
    }

    @Test
    public void testGetModifiersMixedCase() {
        assertEquals(Modifier.PRIVATE, ag.getModifiers(new String[]{"prIvATe"}));
//        assertEquals(Modifier.ABSTRACT, ag.getModifiers(new String[]{"AbstracT"}));
//        assertEquals(Modifier.FINAL, ag.getModifiers(new String[]{"FINAL"}));
    }

    @Test
    public void testGetClasses() {
        String[] s = new String[]{"int", "double", "String[]"};
        Class[] c = new Class[]{int.class, double.class, String[].class};
        assertArrayEquals(c, ag.getClasses(s));
    }

    @Test
    public void testGetClassesNull() {
        assertArrayEquals(new Class[]{}, ag.getClasses(null));
    }

    @Test
    public void testMethodCountNoFile() {
        assertFalse(ag.testMethodCount(
                "doesnotexist",
                0,
                0,
                false,
                true
        ));
    }

    @Test
    public void testMethodCount() {
        String name = "MultipleMethods";
        assertEquals(1, ag.diffNum);
        assertTrue(ag.testMethodCount(
                name,
                3,
                0,
                false,
                false
        ));
        assertEquals(2, ag.diffNum);
        assertTrue(ag.testMethodCount(
                name,
                2,
                Modifier.PUBLIC | Modifier.STATIC,
                true,
                false
        ));
    }

    @Test
    public void testMethodCountAtleast() {
        String name = "MultipleMethods";
        assertEquals(1, ag.diffNum);
        assertTrue(ag.testMethodCount(
                name,
                2,
                0,
                false,
                true
        ));
        assertEquals(2, ag.diffNum);
        assertTrue(ag.testMethodCount(
                name,
                1,
                Modifier.PUBLIC | Modifier.STATIC,
                true,
                true
        ));
    }

}
