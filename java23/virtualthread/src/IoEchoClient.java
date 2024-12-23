import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class IoEchoClient {

    public static void main(String[] args) throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String message;
        Socket socket = new Socket("localhost", 7000);
        while ((message = stdIn.readLine()) != null) {

            System.out.println("Echo client started: {}"+ socket);

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            byte[] bytes = message.getBytes();
            os.write(bytes);

            int totalRead = 0;
            while (totalRead < bytes.length) {
                int read = is.read(bytes, totalRead, bytes.length - totalRead);
                if (read <= 0)
                    break;

                totalRead += read;
               System.out.println("Echo client read: {} byte(s)"+ read);
            }

            System.out.println("Echo client received: {}"+ new String(bytes, StandardCharsets.UTF_8));

            socket.close();
            System.out.println("Echo client disconnected");
        }
    }
}