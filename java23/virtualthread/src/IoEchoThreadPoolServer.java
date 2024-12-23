import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class IoEchoThreadPoolServer  {

    private static final AtomicBoolean active = new AtomicBoolean(true);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(7000);
        System.out.println("Echo server started: {}"+ serverSocket);

        ExecutorService executorService;
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        //executorService = Executors.newFixedThreadPool(5);
        while (active.get()) {
            System.out.println("Echo server is waiting..");
            Socket socket = serverSocket.accept(); // blocking
            System.out.println("Echo server has received a connection..");
            executorService.submit(new Worker(socket));
        }

        System.out.println("Echo server is finishing");
        executorService.shutdown();
        while (!executorService.isTerminated()) {
        }

        serverSocket.close();
        System.out.println("Echo server finished");
    }

    private static class Worker implements Runnable {

        private final Socket socket;

        Worker(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                System.out.println("Connection accepted: {}"+ socket);

                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();

                int read;
                byte[] bytes = new byte[1024];
                while ((read = is.read(bytes)) != -1) { // blocking
                    System.out.println("Echo server read: {} byte(s)"+ read);

                    String message = new String(bytes, 0, read, StandardCharsets.UTF_8);
                    System.out.println("Echo server received: {}"+ message);
                    if (message.trim().equals("bye")) {
                        active.set(false);
                    }
                    os.write(bytes, 0, read); // blocking
                }
            } catch (IOException e) {
                System.out.println("Exception during socket reading/writing"+ e);
            } finally {
                try {
                    socket.close();
                    System.out.println("Connection closed");
                } catch (IOException e) {
                    System.out.println("Exception during socket closing"+ e);
                }
            }
        }
    }
}