import java.util.concurrent.Executor;

public class Invoker implements Executor {

    // Executor is an interface that represents an object that executes provided tasks.

    @Override
    public void execute(Runnable runnable) {
        runnable.run();
    }
}
