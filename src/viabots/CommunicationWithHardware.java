package viabots;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;


/**
 * tries to connect to server socket on localhost, port 55555, sends commands, reads replys, checks connection state
 */
public class CommunicationWithHardware extends Thread {
    static final int PORT = 55555;
    static final int SO_READ_TIMEOUT_MS = 10000;
    static final int MAX_MSG_LENGTH = 100;

    byte[] commandInsertPartA = new byte[]{56, 67, 68};//short version
    Socket socket = null;
    DataInputStream din = null;
    DataOutputStream dout = null;
    final Integer syncLock = 1;
    volatile boolean isRunning = true;
    public boolean isConnected() {
        synchronized (syncLock) {
            if (socket != null) return true;

        }
        return false;
    }

    @Override
    public void run() {
        super.run();
        while (isRunning) {
            while (socket == null && isRunning) {
                connect();

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    void connect() {
        synchronized (syncLock) {
            try {
                socket = new Socket("localhost", PORT);
                din = new DataInputStream(socket.getInputStream());
                dout = new DataOutputStream(socket.getOutputStream());
                System.out.println(getClass().getName() + ": connected");
            } catch (IOException e) {
                //e.printStackTrace();
                // System.out.println(getClass().getName() + " :unable to connect to server");
                socket = null;// if any of streams were not created
            }
        }
    }

    public void sendString(String s) {
        sendBytes(s.getBytes());
    }
    public void sendBytes(byte[] bytes) {
        synchronized (syncLock) {
            if (socket != null) {
                try {
                    dout.write(bytes);
                } catch (IOException e) {
                    //e.printStackTrace();
                    socket = null;
                }
            } else {
                // System.out.println(getClass().getCanonicalName() + ": socket  null");
            }
        }
    }

    /**
     * data received within timeout is returned as String,
     * if timeout is reached or socket is closed than IOException is thrown
     */
    public String listenForReplyWTimeout() throws IOException {
        if (socket == null) throw new IOException();

        {

            socket.setSoTimeout(SO_READ_TIMEOUT_MS);
            byte[] reply = new byte[MAX_MSG_LENGTH];
            int len = 0;
            synchronized (syncLock) {
                if (socket != null) {
                    try {
                        len = din.read(reply);
                    } catch (java.net.SocketTimeoutException ste) {

                    }
                }
            }
            if (len <= 0) {
                socket = null;
                System.out.println("err: reply stream ended");
                throw new IOException();
            }
            String res = new String(reply, 0, len);

            return res;
        }


    }
}