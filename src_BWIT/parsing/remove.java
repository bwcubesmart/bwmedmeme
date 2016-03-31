import java.util.Scanner;

public class remove {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter String:");
	String output=input.next();
        output = output.replaceAll("\\P{L}","");
	output = output.substring(0, 1).toUpperCase() + output.substring(1);
        System.out.println("Output:"+output);


        }

}
