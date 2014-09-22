import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.params.MainNetParams;
import com.google.common.base.Stopwatch;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class Main {
    private static final NetworkParameters MAINNET = MainNetParams.get();
    private static final String TARGET = "1s2";

    static {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");
    }

    public static void main(String... args) throws InterruptedException, IOException {
        System.out.println("Hit return to start");
        System.in.read();

        Stopwatch timer = Stopwatch.createStarted();

        /*
        System.out.println("imperative:");
        findImperative();
        System.out.println(timer);

        timer.reset();
        timer.start();
        */

        System.out.println("stream:");
        findWithStream();
        System.out.println(timer);
    }

    public static void findWithStream() {
        Optional<ECKey> k =
                Stream.generate(ECKey::new)
                        .parallel()
                        .filter(key -> key.toAddress(MAINNET).toString().startsWith(TARGET))
                        .findAny();

        System.out.println("Address: " + k.get().toAddress(MAINNET));
        System.out.println("Privkey: " + k.get().getPrivateKeyEncoded(MAINNET));
    }


    public static void findImperative() throws InterruptedException {
        int numProcs = Runtime.getRuntime().availableProcessors();
        AtomicBoolean keepSearching = new AtomicBoolean(true);
        ExecutorService executor = Executors.newFixedThreadPool(numProcs);

        for (int i = 0; i < numProcs; i++) {
            executor.execute(() -> {
                while (keepSearching.get()) {
                    ECKey key = new ECKey();
                    Address address = key.toAddress(MAINNET);
                    if (address.toString().startsWith(TARGET)
                            && keepSearching.compareAndSet(true, false)) {
                        System.out.println("Address: " + address);
                        System.out.println("Privkey: " + key.getPrivateKeyEncoded(MAINNET));
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }
}