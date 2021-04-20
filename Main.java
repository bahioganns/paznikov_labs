public class Main {
    public static void main(String[] args) throws InterruptedException {
        int number_of_operations = 150000;
        int numbers_of_threads = 8; //8
        int capacity = 50000;
        int duration = 1;
        int num_retries = 3;


        Action run = new Action(number_of_operations, numbers_of_threads, capacity, duration);

        run.threads_run(num_retries);
    }
}
