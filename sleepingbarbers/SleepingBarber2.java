/* Simulate 2 barbers in the Sleeping Barbers problem.

   The extension to 2 barbers is really simple. The sections marked with
   CS-438b show the change to the original SleepingBarber2 class.

   Since the barbers semaphore is a counting semaphore (https://tinyurl.com/yyscpuhw),
   adding a new barber is no more difficult than adding additional customers.
   All that's required is to create a second Barber thread (see the run() method at
   the bottom).

   Optional code has been added so that each Barber has a name, so we can see which 
   barber is cutting hair.

   With the second barber in place, only 5 of 20 customers leave the shop; with just 
   one barber, 10 customers leave.
*/


import java.util.concurrent.*;

public class SleepingBarber2 extends Thread {

    /* PREREQUISITES */


    /* we create the semaphores. First there are no customers and
     the barbers are asleep so we call the constructor with parameter
     0 thus creating semaphores with zero initial permits.
     Semaphore(1) constructs a binary semaphore, as desired. */

    /* CS-438: With the Java Semaphore class, constructing a Semaphore with an argument
       of 0 creates a counting semaphore, so the customers and barbers semaphores are
       counting semaphores (https://tinyurl.com/yyscpuhw). */

    public static Semaphore customers = new Semaphore(0);
    public static Semaphore barbers = new Semaphore(0); // CS-438b: renamed this semaphore
    public static Semaphore accessSeats = new Semaphore(1);

    // CS-438, Assignment 4: The following CUSTOMER_INTERVAL and BARBER_SPEED
    //                       result in 10 customers leaving due to no free seats
    //                       in the waiting room.
    private final int CUSTOMERS = 20;
    private final int CUSTOMER_INTERVAL= 1500; // original value = 2000
    private final int BARBER_SPEED = 6000; // original value = 5000

    /* we denote that the number of chairs in this barbershop is 5. */

    public static final int CHAIRS = 5;

    /* we create the integer numberOfFreeSeats so that the customers
     can either sit on a free seat or leave the barbershop if there
     are no seats available */

    public static int numberOfFreeSeats = CHAIRS;


    /* THE CUSTOMER THREAD */

    class Customer extends Thread {

        /* we create the integer iD which is a unique ID number for every customer
           and a boolean notCut which is used in the Customer waiting loop */

        int iD;
        boolean notCut=true;

        /* Constructor for the Customer */

        public Customer(int i) {
            iD = i;
        }

        public void run() {
            while (notCut) {  // as long as the customer is not cut
                try {
                    accessSeats.acquire();  //tries to get access to the chairs
                    if (numberOfFreeSeats > 0) {  //if there are any free seats
                        System.out.println("Customer " + this.iD + " sitting in waiting room."); // CS-438
                        numberOfFreeSeats--;  //sitting down on a chair
                        customers.release();  //notify a barber that there is a customer
                        accessSeats.release();  // don't need to lock the chairs anymore
                        try {
                            barbers.acquire();  // now it's this customers turn but we have to wait if a barber is busy // CS-438b
                            notCut = false;  // this customer will now leave after the procedure
                            this.get_haircut();  //cutting...
                        } catch (InterruptedException ex) {}
                    }
                    else  {  // there are no free seats
                        System.out.println("There are no free seats. Customer " + this.iD + " has left the barbershop.");
                        accessSeats.release();  //release the lock on the seats
                        notCut=false; // the customer will leave since there are no spots in the queue left.
                    }
                }
                catch (InterruptedException ex) {}
            }
        }

        /* this method will simulate getting a hair-cut */

        public void get_haircut() {
            System.out.println("Customer " + this.iD + " is getting his hair cut");
            try {
                // sleep(5050);
                sleep(BARBER_SPEED + 50); // CS-438
            } catch (InterruptedException ex) {}
        }

    }


    /* THE BARBER THREAD */


    class Barber extends Thread {

        // CS-438b
        private String name;

        // CS-438b
        // public Barber() {}
        public Barber(String n) {
            name = n;
        }

        public void run() {
            while(true) {  // runs in an infinite loop
                try {
                    customers.acquire(); // tries to acquire a customer - if none is available he goes to sleep
                    // accessSeats.release(); // at this time he has been awaken -> want to modify the number of available seats
                    accessSeats.acquire(); // CS-438: I think this should be acquire() if barber is modifying numberOfFreeSeats exclusively.
                    numberOfFreeSeats++; // one chair gets free
                    barbers.release();  // the barber is ready to cut // CS-438b
                    accessSeats.release(); // we don't need the lock on the chairs anymore
                    this.cutHair();  //cutting...
                } catch (InterruptedException ex) {}
            }
        }

        /* this method will simulate cutting hair */

        public void cutHair() {
            // System.out.println("The barber is cutting hair");
            System.out.println(name + " is cutting hair."); // CS-438b
            try {
                // sleep(5000);
                sleep(BARBER_SPEED); // CS-438
            } catch (InterruptedException ex) { }
        }
    }

    /* main method */

    public static void main(String args[]) {

        SleepingBarber2 barberShop = new SleepingBarber2();  //Creates a new barbershop
        barberShop.start();  // Let the simulation begin
    }

    public void run() {
        // Barber giovanni = new Barber();  //Giovanni is the best barber ever
        Barber giovanni = new Barber("Giovanni");  // CS-438b
        giovanni.start();  //Ready for another day of work

        // CS-438b
        Barber baba = new Barber("Baba"); // RIP
        baba.start();

        // CS-438
        System.out.println("Simulation parameters:" +
                           "\n\tHair cutting time: " + BARBER_SPEED + 
                           "\n\tCustomer entry interval: " + CUSTOMER_INTERVAL + "\n");


        /* This method will create new customers for a while */

        for (int i = 1; i <= CUSTOMERS; i++) { // CS-438
            Customer aCustomer = new Customer(i);

            aCustomer.start();

            System.out.println("Customer " + i + " has entered the barber shop."); // CS-438

            try {
                sleep(CUSTOMER_INTERVAL);
            } catch(InterruptedException ex) {};
        }
    }
}
