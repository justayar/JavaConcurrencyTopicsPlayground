import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class SynchronizedVsLocks {
    private static int counter = 0;
    private static int counter2 = 0;
    private static int counter3 = 0;
    private static int counter4 = 0;
    private static Map<String, Integer> map = new HashMap<>();


    static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {

        // Simply put, in a multi-threaded environment, a race condition occurs when two or more threads
        // attempt to update mutable shared data at the same time.
        // Java offers a mechanism to avoid race conditions by synchronizing thread access to shared data.

        // -------------------------------
        // Here is the example race condition
        ExecutorService service = Executors.newFixedThreadPool(3);
        Runnable runnable = () -> counter++;

        for (int i=0; i < 1000; i++) {
            service.submit(runnable);
        }

        service.awaitTermination(1000, TimeUnit.MILLISECONDS);

        System.out.println("Counter: " + counter); // everytime returns something different result

        // End of example race condition
        // -------------------------------


        // -synchronized-
        // We can use the synchronized keyword on different levels:
        // Instance methods, Static methods, Code blocks

        // When we use a synchronized block, Java internally uses a monitor,
        // also known as a monitor lock or intrinsic lock, to provide synchronization.
        // These monitors are bound to an object; therefore, all synchronized blocks of the same object
        // can have only one thread executing them at the same time.


        upCounter();


        Runnable runnable3 = () -> synchronisedCalculateWithSynchronizedBlock();

        for (int i=0; i < 1000; i++) {
            service.submit(runnable3);
        }

        service.awaitTermination(1000, TimeUnit.MILLISECONDS);

        System.out.println("Counter: " + counter3); // always returns 1000 as expected


        // Reentrancy: The lock behind the synchronized methods and blocks is a reentrant.
        // This means the current thread can acquire the same synchronized lock over and over again while holding it.
        // As shown below, while in a synchronized block, we can repeatedly acquire the same monitor lock.
        synchronized (lock) {
            System.out.println("First time acquiring it");

            synchronized (lock) {
                System.out.println("Entering again");

                synchronized (lock) {
                    System.out.println("And again");
                }
            }
        }


        // - Locks -
        // A lock is a more flexible and sophisticated thread synchronization mechanism
        // than the standard synchronized block.
        // The Lock interface has been around since Java 1.5. It’s defined inside the
        // java.util.concurrent.lock package, and it provides extensive operations for locking.


        // Lock interface
        // void lock() – Acquire the lock if it’s available. If the lock isn’t available,
        // a thread gets blocked until the lock is released.
        // void lockInterruptibly() – This is similar to the lock(), but it allows the blocked thread
        // to be interrupted and resume the execution through a thrown java.lang.InterruptedException.
        // boolean tryLock() – This is a nonblocking version of lock() method. It attempts to acquire the lock immediately,
        // return true if locking succeeds.
        // boolean tryLock(long timeout, TimeUnit timeUnit) – This is similar to tryLock(), except it waits up the
        // given timeout before giving up trying to acquire the Lock.
        // void unlock() unlocks the Lock instance.

        // A locked instance should always be unlocked to avoid deadlock condition.

        // A recommended code block to use the lock should contain a try/catch and finally block:
        Lock lock = new ReentrantLock();

        lock.lock();
        try {
            counter4++;
        } finally {
            lock.unlock();
        }

        // In addition to the Lock interface, we have a ReadWriteLock interface that maintains a pair of locks,
        // one for read-only operations and one for the write operation. The read lock may be simultaneously held
        // by multiple threads as long as there is no write.


        // Lock Implementations:


        // - ReentrantLock -
        // ReentrantLock class implements the Lock interface. It offers the same concurrency and memory semantics
        // as the implicit monitor lock accessed using synchronized methods and statements, with extended capabilities.

        ReentrantLock reentrantLock = new ReentrantLock();

        reentrantLock.lock();
        try {
            counter4++;
        } finally {
            reentrantLock.unlock();
        }


        // In this case, the thread calling tryLock() will wait for one second and will give up waiting
        // if the lock isn’t available.
        boolean isLockAcquired = reentrantLock.tryLock(1, TimeUnit.SECONDS);

        if (isLockAcquired) {
            try {
                counter4++;
            } finally {
                reentrantLock.unlock();
            }
        }

        // - ReentrantReadWriteLock -
        // ReentrantReadWriteLock declares methods to acquire read or write locks:
        // Lock readLock() returns the lock that’s used for reading.
        // Lock writeLock() returns the lock that’s used for writing.
        ReadWriteLock lock2 = new ReentrantReadWriteLock();
        Lock readLock = lock2.readLock();
        Lock writeLock = lock2.writeLock();

        writeLock.lock();
        try {
            counter4++;
        } finally {
            writeLock.unlock();
        }

        try {
            readLock.lock();
            int currentCounter = counter4;
        } finally {
            readLock.unlock();
        }

        // - StampedLock -
        // StampedLock is introduced in Java 8. It also supports both read and write locks.
        // Another feature provided by StampedLock is optimistic locking. Most of the time, read operations
        // don’t need to wait for write operation completion, and as a result of this, the full-fledged
        // read lock isn’t required.

        StampedLock stampedLock = new StampedLock();
        long stamp = stampedLock.tryOptimisticRead();

        if(!stampedLock.validate(stamp)) {
            stamp = stampedLock.readLock();
            try {
                map.get("1");
            } finally {
                stampedLock.unlock(stamp);
            }
        }

    }


    //  Synchronized Blocks Within Methods
    //  Sometimes we don’t want to synchronize the entire method,
    //  only some instructions within it. We can achieve this by applying synchronized to a block.
    public static void synchronisedCalculateWithSynchronizedBlock() {
        // Notice that we passed a parameter this to the synchronized block. This is the monitor object.
        // The code inside the block gets synchronized on the monitor object.
        // Simply put, only one thread per monitor object can execute inside that code block.
        synchronized (lock) {
            counter3++;
        }
    }

    // Synchronized Instance Methods
    // We can add the synchronized keyword in the method declaration to make the method synchronized.
    public synchronized void synchronisedCalculateNonStatic() {
        counter2 = counter2 + 1;
    }

    // Synchronized Static Methods
    // These methods are synchronized on the Class object associated with the class.
    // Since only one Class object exists per JVM per class, only one thread can execute
    // inside a static synchronized method per class, irrespective of the number of instances it has.
    public static synchronized void synchronisedCalculate() {
        counter2 = counter2 + 1;
    }

    public static void upCounter() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(3);
        Runnable runnable = () -> synchronisedCalculate();

        for (int i=0; i < 1000; i++) {
            service.submit(runnable);
        }

        service.awaitTermination(1000, TimeUnit.MILLISECONDS);

        System.out.println("Counter: " + counter2); // always returns 1000 as expected
    }
}
