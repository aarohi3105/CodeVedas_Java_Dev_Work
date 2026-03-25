import java.util.*;
class Calci{
    public static void main(String args[]){
        double n1,n2,result=0;
        char sign;
        Scanner sc= new Scanner(System.in);
        n1= sc.nextDouble();
        n2= sc.nextDouble();
        System.out.println("Enter operator (+, -, *, /):");
        sign = sc.next().charAt(0);
        switch(sign){
            case '+':
                result=n1+n2;
                break;
            case '-':
                result=n1-n2;
                break;
            case '*':
                result = n1*n2;
                break;
            case:'/':
                result = n1/n2;
                break;
            default:
                System.out.println("give valid input");
        }
        System.out.println("Result = " + result);
    }
}