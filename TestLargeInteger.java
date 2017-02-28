import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/* This class stores and manipulates very large non-negative integer numbers 
   The digits of the number are stored in an array of bytes. */
class LargeInteger {

    /* The digits of the number are stored in an array of bytes. 
       Each element of the array contains a value between 0 and 9. 
       By convention, digits[digits.length-1] correspond to units, 
       digits[digits.length-2] corresponds to tens, digits[digits.length-3] 
       corresponds to hundreds, etc. */

    byte digits[];


    
    /* Constructor that creates a new LargeInteger with n digits */
    public LargeInteger (int n) {
        digits= new byte[n];
    }

        
    /* Constructor that creates a new LargeInteger whose digits are those of the string provided */
    public LargeInteger (String s) {        
        digits = new byte[s.length()]; /* Note on "length" of arrays and strings: Arrays can be seen 
                                          as a class having a member called length. Thus we can access 
                                          the length of digits by writing digits.length
                                          However, in the class String, length is a method, so to access 
                                          it we need to write s.length() */

        for (int i=0;i<s.length();i++) digits[i] = (byte)Character.digit(s.charAt(i),10);
        /* Here, we are using a static method of the Character class, called digit, which 
           translates a character into an integer (in base 10). This integer needs to be 
           cast into a byte. ****/
    }

    /* Constructor that creates a LargeInteger from an array of bytes. Only the bytes  
       between start and up to but not including stop are copied. */
    public LargeInteger (byte[] array, int start, int stop) {
        digits = new byte[stop-start];
        for (int i=0;i<stop-start;i++) digits[i] = array[i+start];
    }


    /* This method returns a LargeInteger where eventual leading zeros are removed. 
       For example, it turns 000123 into 123. Special case: it turns 0000 into 0. */
    public LargeInteger removeLeadingZeros() {
        if (digits[0]!=0) return this;
        int i = 1;
        while (i<digits.length && digits[i]==0) i++;
        if (i==digits.length) return new LargeInteger("0");
        else return new LargeInteger(digits,i,digits.length);
    } // end of removeLeadingZeros
   

    /* This methods multiplies a given LargeInteger by 10^nbDigits, simply by shifting 
       the digits to the left and adding nbDigits zeros at the end */
    public LargeInteger shiftLeft(int nbDigits) {
        LargeInteger ret = new LargeInteger( digits.length + nbDigits );
        for (int i = 0 ; i < digits.length ; i++) ret.digits[ i ] = digits[ i ];
        for (int i = 0; i <  nbDigits; i++) ret.digits[ digits.length + i ] = 0;
        return ret;
    } // end of shiftLeft


      /* Returns true if the value of this is the same as the value of other */
    public boolean equals (LargeInteger other) {
        if ( digits.length != other.digits.length ) return false;
        for (int i = 0 ; i < digits.length ;i++ ) {
            if ( digits[i] != other.digits[i] ) return false;
        }
        return true;
    } // end of equals


      /* Returns true if the value of this is less than the value of other ****/
    public boolean isSmaller (LargeInteger other) {
        if ( digits.length > other.digits.length ) return false;
        if ( digits.length < other.digits.length ) return true;
        for (int i = 0 ; i < digits.length ; i++ ) {
            if ( digits[i] < other.digits[i] ) return true;
            if ( digits[i] > other.digits[i] ) return false;
        }
        return false;
    } // end of isSmaller
    


    /* This method adds two LargeIntegers: the one on which the method is 
       called and the one given as argument. The sum is returned. The algorithms 
       implemented is the normal digit-by-digit addition with carry. */

    LargeInteger add(LargeInteger other) {
        int size = Math.max( digits.length,other.digits.length );

        /* The sum can have at most one more digit than the two operands */
        LargeInteger sum = new LargeInteger( size + 1 ); 
        byte carry = 0;

        for (int i = 0; i < size + 1 ;i++) {
            // sumColumn will contain the sum of the two digits at position i plus the carry
            byte sumColumn = carry; 
            if ( digits.length - i  - 1 >= 0) sumColumn += digits[ digits.length - i - 1 ];
            if (other.digits.length - i - 1  >= 0) sumColumn += other.digits[ other.digits.length - i - 1 ];
            sum.digits[ sum.digits.length - 1 - i ] = (byte)( sumColumn % 10 ); // The i-th digit in the sum is sumColumn mod 10
            carry = (byte)( sumColumn / 10 );          // The carry for the next iteration is sumColumn/10
        }        
        return sum.removeLeadingZeros();
    } // end of add



    /* This method subtracts the LargeInteger other from that from where the method is called.
       Assumption: the argument other contains a number that is not larger than the current number. 
       The algorithm is quite interesting as it makes use of the addition code.
       Suppose numbers X and Y have six digits each. Then X - Y = X + (999999 - Y) - 1000000 + 1.
       It turns out that computing 999999 - Y is easy as each digit d is simply changed to 9-d. 
       Moreover, subtracting 1000000 is easy too, because we just have to ignore the '1' at the 
       first position of X + (999999 - Y). Finally, adding one can be done with the add code we already have.
       This tricks is the equivalent of the method used by most computers to do subtractions on binary numbers. ***/

    public LargeInteger subtract( LargeInteger other ) {
        // if other is larger than this number, simply return 0;
        if (this.isSmaller( other ) || this.equals( other ) ) return new LargeInteger( "0" );

        LargeInteger complement = new LargeInteger( digits.length ); /* complement will be 99999999 - other.digits */
        for (int i = 0; i < digits.length; i++) complement.digits[ i ] = 9;
        for (int i = 0; i < other.digits.length; i++) 
            complement.digits[ digits.length - i - 1 ] -= other.digits[other.digits.length - i -  1];

        LargeInteger temp = this.add( complement );     // add (999999- other.digits) to this
        temp = temp.add(new LargeInteger( "1" ));       // add one

        // return the value of temp, but skipping the first digit (i.e. subtracting 1000000)
        // also making sure to remove leading zeros that might have appeared.
        return new LargeInteger(temp.digits,1,temp.digits.length).removeLeadingZeros();
    } // end of subtract


    /* Returns a randomly generated LargeInteger of n digits */
    public static LargeInteger getRandom( int n ) {
        LargeInteger ret = new LargeInteger( n );
        for (int i = 0 ; i < n ; i++) {
            // Math.random() return a random number x such that 0<= x <1
            ret.digits[ i ]=(byte)( Math.floor( Math.random() * 10) );
            // if we generated a zero for first digit, regenerate a draw
            if ( i==0 && ret.digits[ i ] == 0 ) i--;
        }
        return ret;
    } // end of getRandom



    /* Returns a string describing a LargeInteger 17*/
    public String toString () {        

        /* We first write the digits to an array of characters ****/
        char[] out = new char[digits.length];
        for (int i = 0 ; i < digits.length; i++) out[ i ]= (char) ('0' + digits[i]);

        /* We then call a String constructor that takes an array of characters to create the string */
        return new String(out);
    } // end of toString




    /* This function returns the product of this and other by iterative addition */
    public LargeInteger iterativeAddition(LargeInteger other) {

        // to execute a * b using addition, we need a counter to increment up to 'a' to know when to terminate the loop.
        // Since a * b = b * a , from a correctness standpoint, the choice of either 'a' or 'b' as the terminator is irrelevant.

        // However, to increase efficiency and speed, it would be beneficial to choose the smallest value of either 'a' or 'b' to compare against.
        // This will not only decrease the number of times the loop must be executed, but also since the addition of two byte arrays requires iterating over the whole array,
        // this will also decrease the number of iterations and calls to helper methods.
        // All of this will speed up the execution of this program.

        // declare counter variable along with two placeholder variables to help us decide which of the two byte arrays is the smallest and which is the largest.
        LargeInteger counter;

        // since objects in Java are called by reference, assigning a LargeInteger object to either of these variable will not create a new object, simply a reference to the memory location of the previous object
        // This will allow us to assign 'this' and 'other' to either of these two variables and allow us to manipulate 'smallest' and 'largest' as if manipulating 'this' and 'other' without knowing
        // which is larger or smaller when this method is called.
        LargeInteger smallest;
        LargeInteger largest;

        if (this.digits.length <= other.digits.length){         // if this byte array is smaller, create a new LargeInteger counter object equal to the length of this byte array
            counter = new LargeInteger( this.digits.length );
            smallest = this;                // this signifies that 'this' byte array is the smallest of the two
            largest = other;                // and this is the largest.
        } else {
            counter = new LargeInteger( other.digits.length );  // else create counter object equal to the length of the other byte array
            smallest = other;               // in this case, the 'other' is smaller
            largest = this;                 // and 'this' is larger
        }

        LargeInteger iterator = new LargeInteger("1");      // create a byte array of value 1 to act as an iterator to the counter byte array

        LargeInteger product = new LargeInteger("0");

        // while the counter is less than the value of the smallest byte array
        while (!(smallest.equals(counter))){
            product = product.add(largest);     // add the 'largest' to the product array a 'smallest' number of times

            counter = counter.add(iterator);    // iterate the counter
        }

        return product;                         // return the product of the addition
    } // end of iterativeAddition



    /* This function returns the product of this and other by using the standard multiplication algorithm */
    public LargeInteger standardMultiplication(LargeInteger other) {
        LargeInteger total = new LargeInteger(0);

        // Since c = a * b = b * a ,  the order of multiplication of the two numbers does not matter.
        // In this case, we will refer to 'a' as representing 'this' LongInteger and 'b' as representing the 'other' LongInteger.


        for (int i = 0; i < this.digits.length; i++){       // set up a loop to iterate over the length of 'a'
            int carry = 0;                                  // set the value of carry to 0

            // When multiplying 'b' by any one digit of 'a', the product can be at most the length of 'b' + 1 digits long.
            // However, when preforming standard multiplication, each time you move up to the next digit in 'a', the product is implicitly multiplied by 10^ the position of the digit in 'a' - 1.
            // If 'a' was 123, then the expanded multiplication would be ('b' * 3 * 10^0) + ('b' * 2 * 10^1) + ('b' * 1 * 10^2)
            // Therefore, including zeros, the maximum number of digits the product of 'b' * any digit in 'a' could take up is
            // length of 'b' + 1 + length of 'a' - 1 = length of 'b' + length of 'a'.

            // Therefore, we create a byte array of length 'b' + 'a' to store the product and treat it as a left shift register to determine the power of 10 to apply.
            // In the end this simplifies the column addition that is required at the end at the cost of additional memory space.
            LargeInteger tempArray = new LargeInteger(other.digits.length + this.digits.length);

            for (int j = 0; j < other.digits.length; j++){  // set up the inner loop to iterate over the length of 'b'.

                // the total value of any one column is the value of the carry + the value of the digit in 'a' * the value of the digit in 'b'
                // starting at the end of each of the numbers ('a'.digits.length - 1 or 'b'.digits.length - 1), we shift to the next digit by decrementing by the iterator.
                int columnProduct = (carry + (this.digits[this.digits.length - i - 1] * other.digits[other.digits.length - j - 1]));

                // Since the temporary array we created was of length 'a' + 'b',
                // the last digit in the array is located at 'a' + 'b' - 1. From then on, we shift left by decreasing by the iterator of 'b' in much the same way as we iterated over 'b'.
                // The value to put into the product array is simply the remainder (mod) of the division by 10 of the product of the multiplication of the two numbers plus the carry
                tempArray.digits[other.digits.length + this.digits.length - j - 1] = (byte)(columnProduct % 10);
                carry = columnProduct / 10;     // the carry is simply the floor of the integer division of the columnProduct divided by 10
            }

            tempArray.digits[this.digits.length - 1] = (byte) carry;    // add the carry to the array at the length of 'b' + 1.

            tempArray = tempArray.shiftLeft(i);         // shift the product array to the left by i digits.
                                                        // this is the analogue of multiplying the product by 10 ^ the position in 'a' - 1.

            total = total.add(tempArray);               // add the temporary product array to the total
        }
        return total;
    } // end of standardMultiplication
                


    /* This function returns the product of this and other by using the basic recursive approach described 
       in the homework. Only use the built-in "*" operator to multiply single-digit numbers */
    public LargeInteger recursiveMultiplication( LargeInteger other ) {

        // left and right halves of this and number2                                                                                        
        LargeInteger leftThis, rightThis, leftOther, rightOther;
        LargeInteger term1,  term2,  term3,  term4, sum; // temporary terms                                                                      

        if ( digits.length==1 && other.digits.length==1 ) {
            int product = digits[0] * other.digits[0];
            return new LargeInteger( String.valueOf( product ) );
        }

        int k = digits.length;
        int n = other.digits.length;
        leftThis = new LargeInteger( digits, 0, k - k/2 );
        rightThis = new LargeInteger( digits, k - k/2, k );
        leftOther = new LargeInteger( other.digits, 0, n - n/2 );
        rightOther = new LargeInteger( other.digits, n - n/2, n );

        /* now recursively call recursiveMultiplication to compute the                    
           four products with smaller operands  */

        if ( n > 1 && k > 1 )  term1 = rightThis.recursiveMultiplication(rightOther );
        else term1 = new LargeInteger( "0" );

        if ( k>1 ) term2 = ( rightThis.recursiveMultiplication( leftOther ) ).shiftLeft( n/2 );
        else term2 = new LargeInteger( "0" );

        if ( n>1 ) term3 = ( leftThis.recursiveMultiplication( rightOther ) ).shiftLeft( k/2 );
        else term3 = new LargeInteger( "0" );

        term4 = ( leftThis.recursiveMultiplication( leftOther ) ).shiftLeft( k/2 + n/2 );

        sum = new LargeInteger( "0" );
        sum = sum.add( term1 );
        sum = sum.add( term2 );
        sum = sum.add( term3 );
        sum = sum.add( term4 );

        return sum;
    } // end of recursiveMultiplication             


    /* This method returns the product of this and other by using the faster recursive approach 
       described in the homework. It only uses the built-in "*" operator to multiply single-digit numbers */
    public LargeInteger recursiveFastMultiplication(LargeInteger other) {

        LargeInteger leftThis, rightThis, leftOther, rightOther;
        LargeInteger term1, term2, term3;

        int k = this.digits.length;
        int n = other.digits.length;

        // base case: k = 1 in which case we perform standard multiplication on the two numbers
        if (k == 1){
            return standardMultiplication(other);
        }

        // ensure that b is the longest number
        if (n < k){
            return other.recursiveFastMultiplication(this);
        }

        leftThis = new LargeInteger(this.digits, 0, k - k/2 );
        rightThis = new LargeInteger(this.digits, k - k/2, k );
        leftOther = new LargeInteger(other.digits, 0, n - n/2 );
        rightOther = new LargeInteger(other.digits, n - n/2, n );

        // term1 <- recursiveFastMultiplication ( ra, rb )
        term1 = rightThis.recursiveFastMultiplication( rightOther );

        // term2 <- recursiveFastMultiplication ( la, lb )
        term2 = leftThis.recursiveFastMultiplication( leftOther );

        // 10^(n/2-k/2) term2
        LargeInteger helper1 = term2.shiftLeft( n/2 - k/2 );

        // 10^(n/2-k/2)lb + rb
        LargeInteger helper2 = leftOther.shiftLeft( n/2 - k/2 ).add( rightOther );

        // term3 <- ((la + ra) * (helper2)) - (helper1) - term1
        term3 = leftThis.add( rightThis ).recursiveFastMultiplication( helper2 ).subtract( helper1 ).subtract( term1 );

        // return 10^(k/2 + n/2)term2 + 10^(k/2)term3 + term1
        return term2.shiftLeft( k/2 + n/2 ).add( term3.shiftLeft( k/2 ) ).add( term1 );
    }

}  // end of the LargeInteger class

public class TestLargeInteger {
    private LargeInteger a;                             // Each test will have two LargeInteger objects that are unique to the test and can only be manipulated within the test
    private LargeInteger b;
    private int size;                                   // having a separate size field allows it to applied to both LargeInteger objects when their getRandom method is called

    public void powersOfTwo(int i){                     // method takes an iterator and calculates powers of two. The result is then stored in the size field. Easy way to run tests for integers that are powers of two long
        double numberOfDigits = Math.pow(2, i);

        this.size = (int) numberOfDigits;
    }

    public void getRandom(int size){
        this.a = LargeInteger.getRandom(size);          // get random array whose length is a power of two
        this.b = LargeInteger.getRandom(size);
    }

    public Timer executeIterativeAdditionx1000(){       // one of four execution methods for each algorithm

        long elapsedTime;

            long startTime = System.nanoTime();         // start the timer

        System.out.println("Iterative addition. Size: " + this.size);   // prints to console to keep track of iterations

            for (int j = 0; j < 1000; j++){             // run loop 1000 times
                getRandom(this.size);                   // each time generate new random byte arrays
                a.iterativeAddition(b);                 // run the computation
            }
            elapsedTime = ((System.nanoTime() - startTime) / 1000); // calculate the end time after 1000 computations

        return new Timer(this.size, elapsedTime);       // return Timer object that contains the current array size and how long it took to compute 1000 multiplications on arrays of such a size
    }

    public List<Timer> testIterativeAddition(){
        ExecutorService service = Executors.newSingleThreadExecutor();      // create executor object to store method calls and execute them in a single thread

        TestLargeInteger test = new TestLargeInteger();                     // for each test, a separate test object is created that contains two integers and their size. There exists only one test object for each test.

        List<Timer> list = new ArrayList<>();                               // list to store values of array size and time to compute 1000 multiplications on arrays

        for (int i = 1; i < 15; i++) {                                      // iterator to calculate array sizes. This loop will test byte arrays from 2^1 to 2^14 digits long. For each size, 1000 multiplications will be performed
            test.powersOfTwo(i);                                            // Generate powers of two

            test.getRandom(test.size);                                      // use size to create random byte arrays of that size

            try {                                                           // main scheduler block.
                Future<Timer> future = service.submit(() -> {               // Create future object that stores a method call to execute iterative addition on newly generated byte arrays at a later time
                    return test.executeIterativeAdditionx1000();            // returns the output of the method call
                });

                list.add(future.get(30, TimeUnit.MINUTES));                 // once added, the method call is now executed and will run for a maximum of 30 minutes. If it executes, the created Timer object will be stored in a list
            } catch (TimeoutException e) {                                  // The time limit is not for the total amount of time the test is run. For example, if the method executes successfully in 29 mins, on arrays of length less than 2^14, then the loop will increment the array size, and execute 1000 more multiplications
                System.out.println("Calculation took too long");            // If 1000 multiplications fails to complete in 30 minutes, the loop and the test is prematurely terminated as no further increases in array size will return in less time
                break;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return list;                                                        // returns the list containing the integer sizes and the time it took to compute 1000 multiplications one those integers
    }

    public Timer executeStandardMultiplicationx1000(){                      // same as above
        long elapsedTime;

        long startTime = System.nanoTime();

        System.out.println("Standard Multiplication. Size: " + this.size);

        for (int j = 0; j < 1000; j++){
            getRandom(this.size);
            a.standardMultiplication(b);
        }
        elapsedTime = ((System.nanoTime() - startTime) / 1000);

        return new Timer(this.size, elapsedTime);
    }

    public List<Timer> testStandardMultiplication(){
        ExecutorService service = Executors.newSingleThreadExecutor();

        TestLargeInteger test = new TestLargeInteger();

        List<Timer> list = new ArrayList<>();

        for (int i = 1; i < 15; i++) {
            test.powersOfTwo(i);

            test.getRandom(test.size);

            try {
                Future<Timer> future = service.submit(() -> {
                    return test.executeStandardMultiplicationx1000();
                });

                list.add(future.get(30, TimeUnit.MINUTES));
            } catch (TimeoutException e) {
                System.out.println("Calculation took too long");
                break;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return list;
    }

    public Timer executeRecursiveMultiplicationx1000(){                 // same as above
        long elapsedTime;

        long startTime = System.nanoTime();

        System.out.println("Recursive Multiplication. Size: " + this.size);

        for (int j = 0; j < 1000; j++){
            getRandom(this.size);
            a.recursiveMultiplication(b);
        }
        elapsedTime = ((System.nanoTime() - startTime) / 1000);

        return new Timer(this.size, elapsedTime);
    }

    public List<Timer> testRecursiveMultiplication(){
        ExecutorService service = Executors.newSingleThreadExecutor();

        TestLargeInteger test = new TestLargeInteger();

        List<Timer> list = new ArrayList<>();

        for (int i = 1; i < 15; i++) {
            test.powersOfTwo(i);

            test.getRandom(test.size);

            try {
                Future<Timer> future = service.submit(() -> {
                    return test.executeRecursiveMultiplicationx1000();
                });

                list.add(future.get(30, TimeUnit.MINUTES));
            } catch (TimeoutException e) {
                System.out.println("Calculation took too long");
                break;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return list;
    }

    public Timer executeRecursiveFastMultiplicationx1000(){                     // same as above
        long elapsedTime;

        long startTime = System.nanoTime();

        System.out.println("Fast Recursive Multiplication. Size: " + this.size);

        for (int j = 0; j < 1000; j++){
            getRandom(this.size);
            a.recursiveFastMultiplication(b);
        }
        elapsedTime = ((System.nanoTime() - startTime) / 1000);

        return new Timer(this.size, elapsedTime);
    }

    public List<Timer> testRecursiveFastMultiplication(){
        ExecutorService service = Executors.newSingleThreadExecutor();

        TestLargeInteger test = new TestLargeInteger();

        List<Timer> list = new ArrayList<>();

        for (int i = 1; i < 15; i++) {
            test.powersOfTwo(i);

            test.getRandom(test.size);

            try {
                Future<Timer> future = service.submit(() -> {
                    return test.executeRecursiveFastMultiplicationx1000();
                });

                list.add(future.get(30, TimeUnit.MINUTES));
            } catch (TimeoutException e) {
                System.out.println("Calculation took too long");
                break;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return list;
    }

    public static void main(String args[]) {

        TestLargeInteger test = new TestLargeInteger();                     // create instance of test object to run tests

        List<Timer> iterList = test.testIterativeAddition();                // for integers between 2^1 and 2^14 digits in size, run 1000 multiplications on each and return the list of sizes and time to compute 1000 multiplications

        List<Timer> stdList = test.testStandardMultiplication();

        List<Timer> recList = test.testRecursiveMultiplication();

        List<Timer> recFastList = test.testRecursiveFastMultiplication();

        try {
            FileWriter writer = new FileWriter("executionTimes.csv");       // write output to a csv file to import to a spreadsheet

            writer.write("Powers of Two,IterativeAddition,StandardMultiplication,RecursiveMultiplication,RecursiveFastMultiplication\n");       // column headers

            for (int i = 0; i < 15; i++){                                   // since we generated array sizes from 2^1 to 2^14, each list can only be 14 entries long

                double power = Math.pow(2, i + 1);                          // calculate array size used

                writer.write(String.valueOf(power) + ",");                 // start of row

                try {
                    writer.write(String.valueOf(iterList.get(i).getElapsedExecutionTime()) + ",");     // get the time needed to compute 1000 multiplications
                } catch (IndexOutOfBoundsException e){
                    writer.write("null,");                                                             // if the computation took too long, it would be terminated early and the list would be truncated. If no value exists at that index, then it receives a default value of null
                }

                try {
                    writer.write(String.valueOf(stdList.get(i).getElapsedExecutionTime()) + ",");
                } catch (IndexOutOfBoundsException e){
                    writer.write("null,");
                }

                try {
                    writer.write(String.valueOf(recList.get(i).getElapsedExecutionTime()) + ",");
                } catch (IndexOutOfBoundsException e){
                    writer.write("null,");
                }

                try {
                    writer.write(String.valueOf(recFastList.get(i).getElapsedExecutionTime()) + "\n");    // end the row and move to the next one
                } catch (IndexOutOfBoundsException e){
                    writer.write("null\n");
                }
            }

            writer.close();                                                                                 // write buffer to file

        } catch (IOException e){
            System.out.println("Couldn't write to file");
        }

//        System.out.println(a + " + " + b + " = i" + a.add( b ) );
//        System.out.println(b + " - " + a + " = " + b.subtract( a ) );
//        System.out.println(b + " * " + a + " = " + b.recursiveMultiplication( a ) );

        System.out.println("\nEnd of Testing.");                                                            // terminate the test

    }
}

class Timer{                                                                                // stores the size of the array and time needed to compute
    private int powersOfTwo;
    private long elapsedExecutionTime;

    public Timer(int power, long time){
        this.powersOfTwo = power;
        this.elapsedExecutionTime = time;
    }

    public int getPowersOfTwo(){
        return this.powersOfTwo;
    }

    public long getElapsedExecutionTime(){
        return this.elapsedExecutionTime;
    }

    public static void main(String[] args){

    }
}



                
        
