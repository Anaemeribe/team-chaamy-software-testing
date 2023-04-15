import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AutograderMutationTest {
    private static Autograder ag;

    @BeforeEach
    public void init() {
        ag = new Autograder();
    }

    @Test
    public void testDefaultConstructorVisibility() {
        assertEquals("visible", ag.visibility);
    }
//
//    @Test
//    public void testDefaultConstructorScore() {
//        assertEquals(0.1, ag.maxScore);
//    }
//
//    @Test
//    public void testConstructorVisibility() {
//        ag = new Autograder(0, 0);
//        assertEquals("visible", ag.visibility);
//
//        ag = new Autograder(1, 0);
//        assertEquals("hidden", ag.visibility);
//
//        ag = new Autograder(2, 0);
//        assertEquals("after_due_date", ag.visibility);
//
//        ag = new Autograder(3, 0);
//        assertEquals("after_published", ag.visibility);
//    }
//
//    @Test
//    public void testConstructorInvalidVisibility() {
//        Autograder ag = new Autograder(-1, 0);
//        assertEquals("hidden", ag.visibility);
//
//        ag = new Autograder(4, 0);
//        assertEquals("hidden", ag.visibility);
//
//        ag = new Autograder(Integer.MAX_VALUE, 0);
//        assertEquals("hidden", ag.visibility);
//
//        ag = new Autograder(Integer.MIN_VALUE, 0);
//        assertEquals("hidden", ag.visibility);
//    }

}
