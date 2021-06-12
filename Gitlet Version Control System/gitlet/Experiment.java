package gitlet;

public class Experiment {

    public static void main(String[] args) {
        System.out.println(startsWith("alpha", "alph"));
        System.out.println(startsWith("alpha", "beta"));
        System.out.println(startsWith("alpha", "alpha"));
    }

    public static boolean startsWith(String full, String partial) {
        return full.substring(0, partial.length()).equals(partial);
    }

}
