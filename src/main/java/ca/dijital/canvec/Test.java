package ca.dijital.canvec;

public class Test {

    public static void main(String[] a){
	System.out.println("Architecture, Engineering, And Drafting" == "Architecture, Engineering, And Drafting");
	System.out.println(new String("Architecture, Engineering, And Drafting".getBytes()) == new String("Architecture, Engineering, And Drafting".getBytes()));
	System.out.println("Architecture, Engineering, And Drafting".equals("Architecture, Engineering, And Drafting"));
    }
}
