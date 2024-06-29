import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Executor_ExecutorService {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {

        // -Executors-
        // Executor is a functional interface that represents an object that executes provided tasks.
        // It only includes single abstract method: void execute(Runnable command);
        // ExecutorService automatically provides a pool of threads and an API for assigning tasks to it.
        Invoker invoker = new Invoker();
        invoker.execute(() -> {
            System.out.println("RunnableLambda state:" + Thread.currentThread().getState());
            System.out.println("RunnableLambda: I am running");
        });


        RunnableTask runnableTask = new RunnableTask();
        invoker.execute(runnableTask);


        // -ExecutorService-
        // ExecutorService is a complete solution for asynchronous processing.
        // It manages an in-memory queue and schedules submitted tasks based on thread availability.
        // interface ExecutorService extends Executor, AutoCloseable


        // Now we can create the ExecutorService instance and assign this task.
        // At the time of creation, we need to specify the thread-pool size.
        // Executors class have static methods that create ExecutorService objects
        // ThreadPoolExecutor extends AbstractExecutorService implements ExecutorService extend Executor
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // If we want to create a single-threaded ExecutorService instance,
        // we can use Executors.newSingleThreadExecutor() to create the instance.
        // It is pointless, it is same with new Thread()
        ExecutorService executorServiceSingleThread = Executors.newSingleThreadExecutor();

        // We can use both Runnable and Callable interface to define the task.
        executorServiceSingleThread.submit(new RunnableTask());
        executorServiceSingleThread.shutdown();

        // Once the executor is created, we can use it to submit the task.
        RunnableTask runnableTask2 = new RunnableTask();
        executorService.submit(runnableTask2);

        RunnableTaskWithSleep runnableTask3 = new RunnableTaskWithSleep();
        executorService.submit(runnableTask3);

        // Difference between execute and submit methods
        // execute() and submit() methods serve the purpose of submitting tasks to an ExecutorService object.
        // The execute() method accepts a Runnable task, while the submit() method accepts both Runnable and Callable tasks.
        // The execute() method does not have a return value, whereas the submit() method returns a Future object.
        Runnable runnableWithExecute = () -> System.out.println("I am a runnable object executed with execute method");
        executorService.execute(runnableWithExecute);


        // invokeAny - invokeAll
        // invokeAny() assigns a collection of tasks to an ExecutorService, causing each to run,
        // and returns the result of a successful execution of one task (if there was a successful execution).
        Callable<Integer> callable = () -> 1;

        List<Callable<Integer>> callableList = new ArrayList<>();
        callableList.add(callable);
        callableList.add(callable);
        callableList.add(callable);

        Integer result = executorService.invokeAny(callableList);

        // invokeAll() assigns a collection of tasks to an ExecutorService, causing each to run,
        // and returns the result of all task executions in the form of a list of objects of type Future:
        List<Future<Integer>> resultList = executorService.invokeAll(callableList);


        // -ScheduledExecutorService-
        // ScheduledExecutorService is a similar interface to ExecutorService, but it can perform tasks periodically.
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        // Executor and ExecutorService‘s methods are scheduled on the spot without introducing any artificial delay.
        // Zero or any negative value signifies that the request needs to be executed instantly.
        ScheduledFuture<Integer> scheduledFuture = scheduledExecutorService.schedule(() -> {
            System.out.println("I am running scheduled thread");
            return 2;
        }, 1, TimeUnit.SECONDS);

        System.out.println("Scheduled thread result is " + scheduledFuture.get());

        // ScheduledExecutorService can also schedule the task after some given fixed delay
        // scheduleAtFixedRate: creates and executes a periodic action that is invoked firstly after the provided initial delay,
        // and subsequently with the given period until the service instance shutdowns.
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            System.out.println("I am running scheduled thread with with fixed rate");
        }, 1, 2, TimeUnit.SECONDS);

        // scheduleWithFixedDelay: method creates and executes a periodic action that is invoked firstly after the provided
        // initial delay,and repeatedly with the given delay between the termination of the executing one
        // and the invocation of the next one.
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            System.out.println("I am running scheduled thread with with fixed delay");
        }, 1, 1, TimeUnit.SECONDS);

        //scheduledExecutorService.shutdown();

        // the ExecutorService will not be automatically destroyed when there is no task to process.
        // It will stay alive and wait for new work to do.
        // ExecutorService also comes with two out-of-the-box execution termination methods.
        // The first one is shutdown(); it waits until all the submitted tasks finish executing.
        // The other method is shutdownNow() which attempts to terminate all actively executing tasks
        // and halts the processing of waiting tasks.
        executorService.shutdown();


        // This method returns a list of tasks that are waiting to be processed.
        // It is up to the developer to decide what to do with these tasks.
        List<Runnable> notExecutedTasks = executorService.shutdownNow();

        // One good way to shut down the ExecutorService (which is also recommended by Oracle)
        // is to use both of these methods combined with the awaitTermination() method.
        // With this approach, the ExecutorService will first stop taking new tasks and
        // then wait up to a specified period of time for all tasks to be completed.
        // If that time expires, the execution is stopped immediately.
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

    }
}