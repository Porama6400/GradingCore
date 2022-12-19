package dev.porama.gradingcore.core.messenger.rabbit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;
import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import dev.porama.gradingcore.core.messenger.Messenger;
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
    private static final String GRADING_QUEUE_NAME = "grading-grade";
    private static final String GRADING_RESULT_QUEUE_NAME = "grading-result-test";
    @Getter
    private final String uri;
    @Getter
    private final Logger logger;
    private final int parallelism;
    @Getter
    private ConnectionFactory connectionFactory;
    @Getter
    private Connection connection;
    @Getter
    private Channel channel;
    @Getter
    private Channel listenerChannel;

    public RabbitMessenger(String uri, int parallelism) {
        this.parallelism = parallelism;
        logger = LoggerFactory.getLogger(RabbitMessenger.class);
        Objects.requireNonNull(uri);

        logger.debug("Creating RabbitMQ messenger with URI: {}", uri);

        this.uri = uri;
        try {
            connectionFactory = new ConnectionFactory();
            connectionFactory.setUri(uri);
            connection = connectionFactory.newConnection();

            channel = connection.createChannel();
            try{
                channel.queueDeclare(GRADING_QUEUE_NAME, false, false, false, null);
                channel.queueDeclare(GRADING_RESULT_QUEUE_NAME, false, false, false, null);
            }
            catch (Exception ignored){

            }

            listenerChannel = connection.createChannel();
            listenerChannel.basicQos(1);

        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException |
                 IOException | TimeoutException e) {
            logger.warn("Rabbit failed to connect: {}", e.getMessage());
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
    }

    @Override
    public void listen(Function<GradingRequest, CompletableFuture<GradingResult>> requestConsumer) throws IOException {
        listenerChannel.basicQos(parallelism);
        listenerChannel.basicConsume(GRADING_QUEUE_NAME, false, new DefaultConsumer(listenerChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                GradingRequest request;
                try {
                    request = ConfigUtils.fromJson(body, GradingRequest.class);
                } catch (Exception ex) {
                    listenerChannel.basicAck(envelope.getDeliveryTag(), false);
                    return;
                }

                logger.info(ConfigUtils.toJson(request));
                CompletableFuture<GradingResult> future = requestConsumer.apply(request);
                future.thenAccept((result) -> {
                    try {
                        publishResult(result);
                        logger.debug("ACK");
                        listenerChannel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                }).exceptionally((ex) -> {
                    ex.printStackTrace();
                    try {
                        logger.debug("NACK");
                        listenerChannel.basicNack(envelope.getDeliveryTag(), false, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    return null;

                });

            }
        });
    }

    @Override
    public void publishResult(GradingResult result) throws IOException {
        String string = ConfigUtils.toJson(result);
        channel.basicPublish("", GRADING_RESULT_QUEUE_NAME, null, string.getBytes());
    }

    @Override
    public void shutdown() {
        try {
            if (listenerChannel != null)
                listenerChannel.close();

            if (channel != null)
                channel.close();

            if (connection != null)
                connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
