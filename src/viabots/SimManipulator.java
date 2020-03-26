package viabots;

import java.util.concurrent.ConcurrentLinkedDeque;

public class SimManipulator {
    long pickupTime;
    long insertTime;
    ConcurrentLinkedDeque<String> hardwareMsgQueue;
ManipulatorType manipType;

    public SimManipulator(ConcurrentLinkedDeque<String> hardwareMsgQueue, ManipulatorType manipType) {

        this.hardwareMsgQueue = hardwareMsgQueue;
      this.manipType= manipType;
      setTimes();
    }

    void insertConeInPosition(int position) {
        new Thread(new InsertionTimer(insertTime)).start();
    }

    void pickupCone() {
        new Thread(new InsertionTimer(pickupTime)).start();

    }

    void setTimes(){
        switch (manipType){
            case SIM_BAXTER:
                pickupTime= SimManipulatorAgent.BAXTER_PICKUP_TIME;
                insertTime= SimManipulatorAgent.BAXTER_INSERT_TIME;
                break;
            case SIM_IRB120:
                pickupTime= SimManipulatorAgent.ABB_PICKUP_TIME;
                insertTime= SimManipulatorAgent.ABB_INSERT_TIME;
                break;
            case SIM_SMALL_ONE:
                pickupTime= SimManipulatorAgent.SMALL_PICKUP_TIME;
                insertTime= SimManipulatorAgent.SMALL_INSERT_TIME;
                break;
            default:

        }
    }

    public class InsertionTimer implements Runnable {
        long timeout;
        volatile boolean isRunning = true;

        public InsertionTimer(long timeout) {
            this.timeout = timeout;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();

            while (isRunning) {
                if (System.currentTimeMillis() - startTime > timeout) isRunning = false;
                else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
            // notify master,that insertion is complete
            hardwareMsgQueue.addLast("INSERTED");// add letter
        }
    }

}
