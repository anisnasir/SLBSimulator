package slb;

public class PrimeGenerator {
    boolean isPrime;

    public PrimeGenerator()
    {
    }

    public int nextPrime (int num)
    {
        for (int i=2; i < num; i++) // The first prime number is 2 and the prime numbers only have to go up to a number the user inputs. 
        {
            for (int j = 3; j<=i/2; j+=2) // The next prime number is 3 and I attempted to loop through to get the next odd number.
            {
                if (num % i == 0) //if the number (upper limit) mod a "prime number" is 0, then that means that number is not really "prime" after all. 
                {
                    break;
                }
            }
        }

        return num;
    }

}