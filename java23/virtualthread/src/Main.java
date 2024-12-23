import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class Main
{
    public static void main(String[] args)
    {
       /* try (var executor = Executors.newVirtualThreadPerTaskExecutor())
        {
            IntStream.range(0, 10_000).forEach(i -> {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofSeconds(1));
                    return i;
                });
            });
        }*/
        Thread t = new Thread(() -> {
            try
            {
                Thread.sleep(Duration.of(1, ChronoUnit.MINUTES));
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        });
        t.setDaemon(false);
        t.start();
    }
}