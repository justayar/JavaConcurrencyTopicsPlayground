public class RunnableTask implements Runnable {

    @Override
    public void run() {
        System.out.println("RunnableTask state: " + Thread.currentThread().getState());
        System.out.println("RunnableTask: I am running " +  Thread.currentThread().getName());
    }
}
