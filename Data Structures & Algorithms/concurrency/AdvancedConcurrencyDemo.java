import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AdvancedConcurrencyDemo {
    public volatile boolean isRunning = true;

    ReentrantLock lock = new ReentrantLock(true); // Fair lock to ensure threads acquire the lock in order

    Condition notFull = lock.newCondition();
    Condition notEmpty = lock.newCondition();

    int capacity = 5;
    Queue<String> queue = new LinkedList<>();

    public void produce(String item) throws InterruptedException {
        lock.lockInterruptibly(); // Acquire the lock, allowing interruption
        try {
            while (queue.size() == capacity && isRunning) {
                System.out.println(Thread.currentThread().getName() + " waiting to produce.. " + item);
                notFull.await(); // Wait until there is space in the queue
            }
            if (!isRunning) {
                System.out.println(Thread.currentThread().getName() + " stopping production.");
                return; // Exit if the system is no longer running
            }
            queue.add(item);
            System.out.println(Thread.currentThread().getName() + " produced: " + item);
            notEmpty.signal(); // Signal that there is an item available
        } finally {
            lock.unlock();
        }
    }

    public String consume() throws InterruptedException {
        lock.lockInterruptibly(); // Acquire the lock, allowing interruption
        try {
            while (queue.isEmpty() && isRunning) {
                System.out.println(Thread.currentThread().getName() + " waiting to consume..");
                notEmpty.await(); // Wait until there is an item in the queue
            }
            if (!isRunning) {
                System.out.println(Thread.currentThread().getName() + " stopping consumption.");
                return null; // Exit if the system is no longer running
            }
            String item = queue.poll();
            System.out.println(Thread.currentThread().getName() + " consumed: " + item);
            notFull.signal(); // Signal that there is space available
            return item;
        } finally {
            lock.unlock();
        }
    }

    public void monitorHealth() throws InterruptedException {
        try {
            if(lock.tryLock(2, TimeUnit.SECONDS)) { // Try to acquire the lock with a timeout
                System.out.println("Monitoring health: Queue size is " + queue.size());
            } else {
                System.out.println("Could not acquire lock for health monitoring.");
            }
        } finally {
            lock.unlock();
        }
    }

    public void shutdown() {
        isRunning = false;
        lock.lock();
        try {
            notFull.signalAll(); // Wake up all waiting producers
            notEmpty.signalAll(); // Wake up all waiting consumers
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Create a shared resource
        AdvancedConcurrencyDemo demo = new AdvancedConcurrencyDemo();
        CountDownLatch latch = new CountDownLatch(30);

        for(int i=0; i<15; i++) {
            final int itemNumber = i;
            Thread.ofVirtual().name("Producer - " + itemNumber)
            .start(() -> {
                try {
                    demo.produce("Producer - " + itemNumber);
                } catch (InterruptedException e) {}
                latch.countDown();
            });

            Thread.ofVirtual().name("Consumer - " + itemNumber)
            .start(() -> {
                try {
                    demo.consume(); 
                } catch (InterruptedException e) {}
                latch.countDown();
            });
        }

        Thread.ofVirtual().name("Monitor").start(() -> {
            try {
                demo.monitorHealth();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });

        // Wait for all virtual threads to finish their tasks
        latch.await();
        demo.shutdown();
    }
}