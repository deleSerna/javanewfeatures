import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

//Ref https://github.com/aliakh/demo-sockets-io-nio-nio2/blob/master/src/main/java/demo/nio/server/selector/NioMultiplexingEchoServer.java
public class NioSelectorEchoServer  {

    private static boolean active = true;

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.bind(new InetSocketAddress("localhost", 7000));
        System.out.println("Echo server started: {}"+ serverSocketChannel);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (active) {
            int selected = selector.select(); // blocking
            System.out.println("selected: {} key(s)"+ selected);

            Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
            while (keysIterator.hasNext()) {
                SelectionKey key = keysIterator.next();

                if (key.isAcceptable()) {
                    accept(selector, key);
                }
                if (key.isReadable()) {
                    keysIterator.remove();
                    read(selector, key);
                }
                if (key.isWritable()) {
                    keysIterator.remove();
                    write(key);
                }
            }
        }

        serverSocketChannel.close();
        System.out.println("Echo server finished");
    }

    private static void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept(); // can be non-blocking
        if (socketChannel != null) {
            System.out.println("Connection is accepted: {}"+ socketChannel);

            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
        }
    }

    private static void read(Selector selector, SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = socketChannel.read(buffer); // can be non-blocking
        System.out.println("Echo server read: {} byte(s)"+ read);

        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        String message = new String(bytes, StandardCharsets.UTF_8);
        System.out.println("Echo server received: {}"+ message);
        if (message.trim().equals("bye")) {
            active = false;
        }

        buffer.flip();
        socketChannel.register(selector, SelectionKey.OP_WRITE, buffer);
    }

    private static void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        socketChannel.write(buffer); // can be non-blocking
        socketChannel.close();

        buffer.flip();
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        String message = new String(bytes, StandardCharsets.UTF_8);
        System.out.println("Echo server sent: {}"+ message);
    }
}