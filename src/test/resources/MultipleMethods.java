public class MultipleMethods {
    public MultipleMethods(String s) {
        System.out.println(s);
    }

    public static boolean exists() {
        return true;
    }

    public static String repeat(String m) {
        return m;
    }

    private static float echo(float n) {
        return n;
    }

    public static void main(String[] args) {
        if (exists()) {
            System.out.println(repeat("Hello world"));
        }
    }
}
