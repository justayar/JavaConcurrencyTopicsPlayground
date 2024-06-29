import static java.lang.Thread.sleep;

public class RunnableTaskWithSleep implements Runnable {
    @Override
    public void run() {
        try {
            sleep(1000);
            System.out.println("RunnableTask state: " + Thread.currentThread().getState());
            System.out.println("RunnableTask: I am running " +  Thread.currentThread().getName());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
