import java.util.Scanner;
import java.util.Random;


public class GuessGame {
    public static void main(String args[]){
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to the Guessing game");
        System.out.println("you have only 10 chances to guess the correct number!!! ");

        int guess=0;

        Random r = new Random();

        int number=r.nextInt(0,101);

        int attempt=0;
        System.out.print("Enter your guess between 0 and 100: ");
        while(attempt <10 && guess!=number){

            guess = sc.nextInt();
            attempt++;

            if(guess>number){
                System.out.println("too high");
            }
            else if(guess<number){
                 System.out.println("too low");
            }
            else{
                System.out.println("you guessed it right!!!!!!!");
            }

        }
        if(guess!=number) {
            System.out.println("out of moves :( , Correct number was " + number);
        }
 sc.close();


    }
}
