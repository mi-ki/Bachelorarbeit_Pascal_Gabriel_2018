package While;

public class Wone {
    //@public normal_behavior
    //@requires 0 <= n;
    //@ensures \result == n * n;
    public static int test1(int n) {
        int i1 = 0;
        int n1 = n;
        int r1 = 0;
        //@loop_invariant r1 == i1 * n1;
        //@loop_invariant 0 <= i1 && i1 <= n1;
        //@decreases n1 - i1;
        while (i1 < n1) {
            r1 += n1;
            i1++;
        }
        return r1;
    }
    //@public normal_behavior
    //@requires 0 <= n;
    //@ensures \result == n * n * 2;
    public static int test2(int n) {
        int i2 = 0;
        int n2 = n;
        int r2 = 0;
        //@loop_invariant r2 == i2 * n2 * 2;
        //@loop_invariant 0 <= i2 && i2 <= n2;
        //@decreases n2 - i2;
        while (i2 < n2) {
            r2 += (n2 * 2);
            i2++;
        }
        return r2;
    }

    //@public normal_behavior
    //@requires 0 <= n;
    //@ensures \result == true;
    public static boolean relMethod(int n) {
        int x = test1(n);
        int y = test2(n);
        return ((x*2) == y);
    }
}
