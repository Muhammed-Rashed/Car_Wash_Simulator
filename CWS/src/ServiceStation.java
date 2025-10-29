import java.util.Queue;
import java.util.Queue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The ServiceStation class acts as the Bounded Buffer manager.
 * It initializes the shared queue, the mutex, and the three semaphores
 * (emptySlots, fullSlots, and availablePumps) to coordinate
 * Producers (Cars) and Consumers (Pumps).
 */
public class ServiceStation {
    // Shared
    private final List<Car> carQueue;
    private final Mutex carMutex;
    private final Semaphore emptySlots;
    private final Semaphore fullSlots;
    private final Semaphore availablePumps;

    private final int slotSize;
    private final int numPumps;
    
    public ServiceStation(int slotSize, int numPumps) {
        this.slotSize = slotSize;
        this.numPumps = numPumps;
        
        this.carQueue = new LinkedList<>();
        this.carMutex = new Mutex();
        
        this.emptySlots = new Semaphore(slotSize);
        this.fullSlots = new Semaphore(0);
        this.availablePumps = new Semaphore(numPumps);

        System.out.println("\n--- Service Station Initialized ---");
        System.out.println("Garage Waiting Area Size: " + slotSize);
        System.out.println("Number of Service Bays (Pumps): " + numPumps);
        System.out.println("-----------------------------------\n");
    }

    // --- Methods for Car (Producer) interaction ---
    public void enterQueue(Car car) throws InterruptedException {
        System.out.println(car.getName() + " ARRVES, checking queue space.");
        emptySlots.acquire();
        
        carMutex.acquire();
        
        carQueue.add(car);
        System.out.println(car.getName() + " ENTERS the queue. Queue size: " + carQueue.size());
        
        carMutex.release();
        fullSlots.release();
    }

    // --- Methods for Pump (Consumer) interaction ---
    public Car takeCar() throws InterruptedException {
        Car car = null;

        // 1. Wait
        fullSlots.acquire();

        // 2. Acquire Mutex
        carMutex.acquire();

        // Remove Car from the Queue
        car = carQueue.remove(0);
        System.out.println(Thread.currentThread().getName() + " takes " + car.getName() + ". Queue size: " + carQueue.size());

        // Release Mutex
        carMutex.release();

        // Release emptySlots
        emptySlots.release();

        return car;
    }

    // --- Methods for Pump (Consumer) Service flow ---
    public void startService() throws InterruptedException {
        availablePumps.acquire();
        System.out.println(Thread.currentThread().getName() + " ACQUIRES a service bay and STARTS service.");
    }

    public void finishService() {
        System.out.println(Thread.currentThread().getName() + " FINISHES service and RELEASES the bay.");
        availablePumps.release();
    }
    
    public void runSimulation() {
        ExecutorService pumpPool = Executors.newFixedThreadPool(numPumps);
        for (int i = 0; i < numPumps; i++) {
            // TODO:IDK just add the pumps stuff or something....Look Ali i tried
        }
        try {
            Thread.sleep(10000);
            pumpPool.shutdownNow();
            pumpPool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // This Main method is for demonstration/testing purposes
    public static void main(String[] args) {
        // TODO: read
        System.out.println("Remove this when you are done with everything Ali and and add your code");
    }
}

// TODO: Add your Car and Pump Classes here

class Semaphore {
    private int permits;
    public Semaphore(int initialPermits) {
        if (initialPermits < 0) {
            throw new IllegalArgumentException("Permits cannot be negative");
        }
        this.permits = initialPermits;
    }

    public synchronized void acquire() throws InterruptedException {
        while (permits == 0) {
            wait();
        }
        permits--;
    }

    public synchronized void release() {
        permits++;
        notify();
    }
}

class Mutex {
    private boolean locked = false;

    public synchronized void acquire() throws InterruptedException {
        while (locked) {
            wait();
        }
        locked = true;
    }

    public synchronized void release() {
        locked = false;
        notify();
    }
}