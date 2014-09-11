import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.params.MainNetParams;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static final NetworkParameters MAINNET = MainNetParams.get();
    private static final int NUM_PROCS = Runtime.getRuntime().availableProcessors();
    private static final String TARGET = "1s2gx";

    public static void main(String... args) {
        AtomicBoolean keepSearching = new AtomicBoolean(true);
        ExecutorService executor = Executors.newFixedThreadPool(NUM_PROCS);

        System.out.println("searching for address starting with " + TARGET + "...");

        for (int i = 0; i < NUM_PROCS; i++) {
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
    }
}