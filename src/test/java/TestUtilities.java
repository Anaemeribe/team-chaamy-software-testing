import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jh61b.grader.TestResult;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class TestUtilities {

    private static ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private static PrintStream outStream = new PrintStream(byteArrayOutputStream);

    private static void redirectStdOut()
    {
        System.setOut(outStream);
    }


    public static CustomTestResult[] getTestResults(Autograder autograder) throws Exception {
        System.setOut(outStream);
        autograder.testRunFinished();
        System.setOut(System.out);

        // Convert byte into string
        String json = byteArrayOutputStream.toString();
        Type listOfMyClassObject = new TypeToken<ArrayList<TestResult>>() {}.getType();
        Gson gson = new Gson();
        TestResultHolder x = gson.fromJson(json, TestResultHolder.class);
        return x.tests;
    }

    public class TestResultHolder {
        public CustomTestResult[] tests;
    }

    public class CustomTestResult {
        private String name;
        private String number;
        private double maxScore;
        private double score;
        private String visibility;

        private String output;

        public double getScore() {
            return score;
        }
    }
}
