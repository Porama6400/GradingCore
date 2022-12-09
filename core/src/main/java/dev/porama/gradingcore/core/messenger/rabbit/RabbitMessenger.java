package dev.porama.gradingcore.core.messenger.rabbit;

import com.rabbitmq.client.*;
import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import dev.porama.gradingcore.core.messenger.Messenger;
import dev.porama.gradingcore.core.messenger.message.Message;
import dev.porama.gradingcore.core.utils.ConfigUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class RabbitMessenger implements Messenger {
    private static final String GRADING_QUEUE_NAME = "grading_queue";
    private static final String GRADING_RESULT_QUEUE_NAME = "grading_result";
    @Getter
    private final String uri;
    @Getter
    private final Logger logger;
    @Getter
    private ConnectionFactory connectionFactory;
    @Getter
    private Connection connection;
    @Getter
    private Channel channel;
    @Getter
    private Channel listenerChannel;

    public RabbitMessenger(String uri) {
        logger = LoggerFactory.getLogger(RabbitMessenger.class);
        Objects.requireNonNull(uri);

        logger.debug("Creating RabbitMQ messenger with URI: {}", uri);

        this.uri = uri;
        try {
            connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(uri);
            connection = connectionFactory.newConnection();

            channel = connection.createChannel();
            channel.exchangeDeclare("grading", BuiltinExchangeType.DIRECT);
            channel.queueDeclare(GRADING_QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(GRADING_RESULT_QUEUE_NAME, false, false, false, null);
            channel.queueBind(GRADING_QUEUE_NAME, "grading", "run");
            channel.queueBind(GRADING_RESULT_QUEUE_NAME, "grading", "result");
            listenerChannel = connection.createChannel();
            listenerChannel.basicQos(1);

        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException |
                 IOException | TimeoutException e) {
            logger.warn("Rabbit failed to connect: {}", e.getMessage());
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
    }

    public void listen(Function<GradingRequest, CompletableFuture<GradingResult>> requestConsumer) throws IOException {
        listenerChannel.basicQos(1);
        listenerChannel.basicConsume(GRADING_QUEUE_NAME, false, new DefaultConsumer(listenerChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                GradingRequest request = ConfigUtils.fromJson(body, GradingRequest.class);
                CompletableFuture<GradingResult> future = requestConsumer.apply(request);
                future.thenAccept((result) -> {
                    try {
                        // TODO publish result
                        listenerChannel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                }).exceptionally((ex) -> {
                    try {
                        listenerChannel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return null;

                });

            }
        });
    }

    @Override
    public void publish(Message message) {

    }
}
