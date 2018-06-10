public class recursion {

    int par;

    //Fakultät
    //@public normal_behaviour
    //@requires n >= 0;
    //@ensures par == (\product int i; 1 <= i && i < n + 1; i);
    //@measured_by n;
    public void methode1(int n) {
        if (n >= 1) {
            methode1( n-1);
            par = n * par;
        } else {
            par = 1;
        }
    }

    //@public normal_behaviour
    //@requires n >= 0;
    //@ensures \result == (\product int i; 1 <= i && i < n + 1; i);
    //@measured_by n;
    public int methode2(int n) {
        if (n == 0) {
            return 1;
        } else {
            return n * methode2( n-1);
        }
    }

    //@public normal_behaviour
    //@requires n >= 0 && n < 3;
    //@ensures \result;
    public boolean rel(int n) {
        methode1(n);
        int x = par;
        int y = methode2(n);
        return x == y;
    }
}
