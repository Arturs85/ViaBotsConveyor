package viabots;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class TwoWaySerialComm {
    InputStream in;
    OutputStream out;
    boolean isConnected = false;
    CommPort commPort;
    ConveyorAgent owner;
    public Integer txBufferLock = Integer.valueOf(0);
SerialWriter sendingThread;

    public TwoWaySerialComm(ConveyorAgent conveyorAgent) {
        super();
        owner = conveyorAgent;
    }

    void connectToFirstPort() throws Exception {
        String portName = PortsChecker.getFirstSerialPortName();
        if (portName != null) {
            connect(portName);
            isConnected = true;
        } else {
            System.out.println("No free serial ports available");
            throw new IOException();
        }
    }

    void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();

                (new Thread(new SerialReader(in))).start();
             sendingThread= ((new SerialWriter(out)));
                new Thread(sendingThread).start();

            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    public void disconect() {

        try {
            sendingThread.isRunning=false;
            if (in != null) in.close();
            if (out != null) out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (commPort != null & isConnected) commPort.close();

    }

  public   void writeToOutputQeue(char command) {
synchronized (sendingThread.txBuffer){
    sendingThread.txBuffer.addLast(command);

}
    }

    /**
     *
     */
    public class SerialReader implements Runnable {
        InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;
            try {
                while ((len = this.in.read(buffer)) > -1) {
//                    for (int i = 0; i < len; i++) {
//                        System.out.print((Byte.toUnsignedInt(buffer[i]))+" ");
//
//                    }
                    String recData = new String(buffer, 0, len);

                    owner.onSerialInput(recData.charAt(0));
                    // System.out.print(recData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */
    public static class SerialWriter implements Runnable {
        OutputStream out;
        LinkedList<Character> txBuffer = new LinkedList<Character>();

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

       public volatile boolean isRunning = true;

        public void run() {
            while (isRunning) {

                synchronized (txBuffer) {
                    if (!txBuffer.isEmpty()) {
                        char d = txBuffer.getFirst();

                        try {

                            this.out.write(d);
                            txBuffer.removeFirst();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //send msgs with less frequency than receivers readout so receiver does not lose any of msgs
                try {
                    Thread.sleep(110);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            //(new TwoWaySerialComm()).connect("/dev/ttyUSB1");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}