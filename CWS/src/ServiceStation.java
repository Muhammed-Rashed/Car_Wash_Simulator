import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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

    public void enterQueue(Car car) throws InterruptedException {
        System.out.println(car.getName() + " ARRVES, checking queue space.");
        emptySlots.acquire();
        
        carMutex.acquire();
        
        carQueue.add(car);
        System.out.println(car.getName() + " ENTERS the queue. Queue size: " + carQueue.size());
        
        carMutex.release();
        fullSlots.release();
    }

    public Car takeCar() throws InterruptedException {
        Car car = null;

        fullSlots.acquire();

        carMutex.acquire();

        car = carQueue.remove(0);
        System.out.println(Thread.currentThread().getName() + " takes " + car.getName() + ". Queue size: " + carQueue.size());

        carMutex.release();

        emptySlots.release();

        return car;
    }

    public void startService() throws InterruptedException {
        availablePumps.acquire();
        System.out.println(Thread.currentThread().getName() + " ACQUIRES a service bay and STARTS service.");
    }

    public void finishService() {
        System.out.println(Thread.currentThread().getName() + " FINISHES service and RELEASES the bay.");
        availablePumps.release();
    }
    
    public void runSimulation(int totalCars) {
        ExecutorService pumpPool = Executors.newFixedThreadPool(numPumps);

        // Creating and starting Pump threads (Consumers)
        for (int i = 0; i < numPumps; i++) {
            Pump pump = new Pump("Pump-" + (i + 1), this);
            pumpPool.submit(pump);
        }

        // Creating and starting Car threads (Producers)
        for (int i = 1; i <= totalCars; i++) 
        {
            Car car = new Car("Car-" + i, this);
            car.start();
            try 
            {
                Thread.sleep((int)(Math.random() * 1000));
            } 
            catch (InterruptedException e) 
            {
                Thread.currentThread().interrupt();
            }
        }

        try {
            Thread.sleep(15000);
            pumpPool.shutdownNow();
            pumpPool.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\nAll cars processed; simulation ends.");
    }

    public static void main(String[] args) {
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        
        System.out.print("Enter waiting area capacity (1-10): ");
        int slotSize = scanner.nextInt();

        System.out.print("Enter number of pumps: ");
        int numPumps = scanner.nextInt();

        System.out.print("Enter number of cars to simulate: ");
        int totalCars = scanner.nextInt();

        scanner.close();
        ServiceStation station = new ServiceStation(slotSize, numPumps);
        station.runSimulation(totalCars);
    }
}

class Car extends Thread {
    private final ServiceStation station;

    public Car(String name, ServiceStation station) 
    {
        super(name);
        this.station = station;
    }

    @Override
    public void run() 
    {
        try 
        {
            station.enterQueue(this);
        } 
        catch (InterruptedException e) 
        {
            Thread.currentThread().interrupt();
        }
    }
}

class Pump implements Runnable {
    private final String name;
    private final ServiceStation station;

    public Pump(String name, ServiceStation station) 
    {
        this.name = name;
        this.station = station;
    }

    @Override
    public void run() 
    {
        Thread.currentThread().setName(name);
        try 
        {
            while (true) 
            {
                Car car = station.takeCar();
                station.startService();

                System.out.println(name + " STARTS servicing " + car.getName());
                Thread.sleep((int)(Math.random() * 2000 + 1000));
                System.out.println(name + " FINISHES servicing " + car.getName());

                station.finishService();
            }
        } 
        catch (InterruptedException e) 
        {
            Thread.currentThread().interrupt();
        }
    }
}

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