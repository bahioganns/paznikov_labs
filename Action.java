import java.util.EmptyStackException;
import java.util.concurrent.ThreadLocalRandom;

public class Action {

    int number_of_operations, numbers_of_threads, capacity, duration;
    Thread[] threads;

    public enum type_of_stack{ lock_free_stack, elim_backoff_stack }


    public Action(int number_of_operations, int numbers_of_threads, int capacity, int duration){
        this.number_of_operations = number_of_operations;
        this.numbers_of_threads = numbers_of_threads;
        this.capacity = capacity;
        this.duration = duration;
        this.threads = new Thread[numbers_of_threads];

    }

    public long run(type_of_stack type_of_stack) throws InterruptedException {
        LockFreeStack<Integer> stack;
        if (Action.type_of_stack.lock_free_stack == type_of_stack){
            stack = new LockFreeStack<Integer>();
        }
        else if (Action.type_of_stack.elim_backoff_stack == type_of_stack){
            stack = new EliminationBackoffStack<Integer>(this.capacity, this.duration);
        }
        else {
            return -1;
        }

        long start, end;

        for (int i=0; i<capacity; i++){
            int rand_int = ThreadLocalRandom.current().nextInt();
            stack.push(rand_int);
        }

        for (int i=0; i<numbers_of_threads; i++){
            threads[i] = new RandomOps(i, this.number_of_operations, stack);
        }

        start = System.currentTimeMillis();
        for (int i=0; i<numbers_of_threads; i++){
            threads[i].start();
        }

        for(int i=0; i<numbers_of_threads; i++){
            threads[i].join();
        }
        end = System.currentTimeMillis();

        return end-start;

    }


    public void threads_run(int number_of_tries)throws InterruptedException{

        int numbers_of_threads = this.numbers_of_threads;


        long[] lock_time_list = new long[numbers_of_threads];
        long[] elim_time_list = new long[numbers_of_threads];

        for (int i=0; i<numbers_of_threads; i++){
            this.numbers_of_threads = i+1;
            lock_time_list[i] = this.run(type_of_stack.lock_free_stack);
            elim_time_list[i] = this.run(type_of_stack.elim_backoff_stack);

            long lockTimeAvg = 0;
            long elimTimeAvg = 0;
            for (int j=0; j<number_of_tries; j++){
                lockTimeAvg += this.run(type_of_stack.lock_free_stack);
                elimTimeAvg += this.run(type_of_stack.elim_backoff_stack);
            }
            lockTimeAvg /= (long)number_of_tries;
            elimTimeAvg /= (long)number_of_tries;

            lock_time_list[i] = lockTimeAvg;
            elim_time_list[i] = elimTimeAvg;


        }
        
        System.out.println("\tLockFreeStack\tElimBackoffStack");
        
        for (int i=0; i<numbers_of_threads; i++){
            System.out.printf("%d\t\t%d ms\t\t%d ms\n" ,i+1, lock_time_list[i], elim_time_list[i]);
        }


    }

    static class RandomOps extends Thread{

        int thread_id;
        int numOps;
        int number_of_operations;
        LockFreeStack<Integer> stack;

        RandomOps(int i, int number_of_operations, LockFreeStack<Integer> stack){
            this.thread_id = i;
            this.numOps = 0;
            this.number_of_operations = number_of_operations;
            this.stack = stack;
        }

        public void run(){
            while (this.numOps < this.number_of_operations){
                int rand_int = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
                int op = rand_int % 2;

                switch (op) {
                    case 0:
                        this.stack.push(rand_int);
                        this.numOps++;
                        break;
                    case 1:
                        try {
                            this.stack.pop();
                            this.numOps++;
                        } catch (EmptyStackException e) {
                            System.out.println("Stack Empty");
                        }
                        break;
                    default:
                        break;
                }
            }

        }

    }



}
