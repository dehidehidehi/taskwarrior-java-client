package de.aaschmid.taskwarrior;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import de.aaschmid.taskwarrior.config.TaskwarriorConfiguration;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;
import de.aaschmid.taskwarrior.ssl.SslContextFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static de.aaschmid.taskwarrior.message.TaskwarriorMessageFactory.deserialize;
import static de.aaschmid.taskwarrior.message.TaskwarriorMessageFactory.serialize;
import static java.util.Objects.requireNonNull;

public class TaskwarriorClient {

    private final TaskwarriorConfiguration config;
    private final SSLContext sslContext;

    public TaskwarriorClient(TaskwarriorConfiguration config) {
        this.config = requireNonNull(config, "'configuration' must not be null.");
        this.sslContext = SslContextFactory.createSslContext(config);
    }

    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "generated try-with-resources code causes failure in Java 11, see https://github.com/spotbugs/spotbugs/issues/756")
    public TaskwarriorMessage sendAndReceive(TaskwarriorMessage message) throws IOException {
        requireNonNull(message, "'message' must not be null.");

        try (Socket socket = sslContext.getSocketFactory().createSocket(config.getServerHost(), config.getServerPort());
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {
            send(message, out);
            return receive(in);
        }
    }

    private void send(TaskwarriorMessage message, OutputStream out) throws IOException {
        out.write(serialize(config, message));
        out.flush();
    }

    private TaskwarriorMessage receive(InputStream in) throws IOException {
        return deserialize(in);
    }
}
