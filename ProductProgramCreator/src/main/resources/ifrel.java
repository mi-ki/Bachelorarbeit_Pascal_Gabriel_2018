public class ifrel {
    private int n1;
    private int n2;
    //@public normal_behaviour
    //@assignable n1;
    //@ensures ((n1 == n) && (n >= 0)) || ((n1 == n * (-1)) && (n<0));
    private void test1(int n) {
        if(n < 0) {
            n1 = n * (-1);
        } else {
            n1 = n;
        }
    }
    /*@
    @public normal_behaviour
    //@assignable n2;
    @ensures ((n2 == n) && (n >= 0)) || ((n2 == n * (-1)) && (n<0));
    */
    private void test2(int n) {
        if (n >= 0) {
            n2 = n;
        } else {
            n2 = n * (-1);
        }
    }
    /*@
    @public normal_behaviour
    @ensures \result == true;
    */
    public boolean rel(int n) {
        test1(n);
        test2(n);
        return n1 == n2;
    }
}
