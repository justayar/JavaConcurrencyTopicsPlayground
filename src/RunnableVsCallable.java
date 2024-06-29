import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RunnableVsCallable {
    public static void main(String[] args) {

        // Since Java’s early days, multithreading has been a major aspect of the language.
        // Runnable is the core interface provided for representing multithreaded tasks,
        // and Java 1.5 provided Callable as an improved version of Runnable.

        // -Runnable-
        // The Runnable interface is a functional interface and has a single run() method
        // that doesn’t accept any parameters or return any values.
        // This works for situations where we aren’t looking for a result of the thread execution, such as incoming events logging.
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("I am a runnable task.");
            }
        };

        // -Callable-
        // The Callable interface is a generic interface containing a single call() method that returns a generic value V.
        Callable<Integer> callable = new Callable<Integer>() {
            @Override
            public Integer call() {
                System.out.println("I am a callable task.");
                return 1;
            }
        };

        // Both interfaces are designed to represent a task that can be run by multiple threads.
        // We can run Runnable tasks using the Thread class or ExecutorService,
        // whereas we can only run Callables using the ExecutorService.
        Thread threadWithRunnable = new Thread(runnable);
        threadWithRunnable.start();

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.submit(runnable);

        // The result of call() method is returned within a Future object.
        // Future is used to represent the result of an asynchronous operation
        // What’s more, the cancel(boolean mayInterruptIfRunning) API cancels the operation and
        // releases the executing thread. If the value of mayInterruptIfRunning is true,
        // the thread executing the task will be terminated instantly.
        // Otherwise, in-progress tasks will be allowed to complete.
        Future<Integer> callableResult = executorService.submit(callable);

        // We can use following code snippet to check if the future result is ready
        // and fetch the data if the computation is done:
        if (callableResult.isDone() && !callableResult.isCancelled()) {
            try {
                int result = callableResult.get();
                System.out.println("The result is " + result);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        // We can also specify a timeout for a given operation.
        // If the task takes more than this time, a TimeoutException is thrown.
        try {
            callableResult.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }


        // -Exception Handling Runnable-
        // With runnable;Since the method signature does not have the “throws” clause specified,
        // we don’t have a way to propagate further checked exceptions.


        // -Exception Handling Callable-
        // Callable‘s call() method contains the “throws Exception” clause, so we can easily propagate checked exceptions.
        // In case of running a Callable using an ExecutorService, the exceptions are collected in the Future object.
        // We can check this by making a call to the Future.get() method.
        // This will throw an ExecutionException, which wraps the original exception.
        try {
            System.out.println("The result is " + callableResult.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


        executorService.shutdown();
    }
}
