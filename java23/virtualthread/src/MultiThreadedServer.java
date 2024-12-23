import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class MultiThreadedServer {

    private static final int PORT = 8080;
    private static final int MAX_CLIENTS_PER_POOL = 20;
    private static final int MIN_NUM_POOLS = 1; // Number of separate pools

    public static void main(String[] args) throws IOException {
        ExecutorService poolExecutor = Executors.newVirtualThreadPerTaskExecutor();

        // Create and open a server socket channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);

        // Create a single selector for the server socket channel
        Selector serverSelector = Selector.open();
        serverSocketChannel.register(serverSelector, SelectionKey.OP_ACCEPT);

        // Array to hold selectors for client channels
        Selector[] clientSelectors = new Selector[MIN_NUM_POOLS];
        for (int i = 0; i < MIN_NUM_POOLS; i++) {
            clientSelectors[i] = Selector.open();
            ClientPoolHandler clientPoolHandler = new ClientPoolHandler(clientSelectors[i]);
            poolExecutor.submit(clientPoolHandler::run);
        }

        // Accept and handle client connections in separate threads for each pool
        while (true) {
            serverSelector.select();
            Set<SelectionKey> selectedKeys = serverSelector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    // Accept the connection
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = serverChannel.accept();
                    boolean clientAdded = false;

                    // Check if any selector has capacity, otherwise create a new one
                    for (Selector selector : clientSelectors) {
                        int numClients = selector.keys().size() - 1; // Subtract 1 for the server channel
                        if (numClients < MAX_CLIENTS_PER_POOL) {
                            clientChannel.configureBlocking(false);
                            clientChannel.register(selector, SelectionKey.OP_READ);
                            clientAdded = true;
                            break;
                        }
                    }

                    // If no selector has capacity, create a new one and spawn a new thread
                    if (!clientAdded) {
                        Selector newSelector = Selector.open();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(newSelector, SelectionKey.OP_READ);
                        ClientPoolHandler clientPoolHandler = new ClientPoolHandler(newSelector);
                        poolExecutor.submit(clientPoolHandler::run);
                    }
                }
            }
        }
    }

    private static class ClientPoolHandler{
        private Selector selector;

        public ClientPoolHandler(Selector selector) {
            this.selector = selector;
        }

        public void run() {
            try {
                while (true) {
                    selector.select();
                    // Handle selected keys
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}