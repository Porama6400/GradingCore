package dev.porama.gradingcore.core.messenger.rabbit;

import com.rabbitmq.client.*;
import dev.porama.gradingcore.core.exception.ExceptionUtils;
import dev.porama.gradingcore.core.grader.data.GradingRequest;
import dev.porama.gradingcore.core.grader.data.GradingResult;
import dev.porama.gradingcore.core.messenger.Messenger;
import dev.porama.gradingcore.core.messenger.message.NestMessageWrapper;
import dev.porama.gradingcore.core.metrics.MetricsManager;
import dev.porama.gradingcore.core.scheduler.RequestFuturePair;
import dev.porama.gradingcore.core.utils.SerializerUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RabbitMessenger implements Messenger {
    private static final String GRADING_QUEUE_NAME = "grading-grade";
    private static final String GRADING_RESULT_QUEUE_NAME = "grading-result";
    @Getter
    private final String uri;
    @Getter
    private final Logger logger;
    private final MetricsManager metricsManager;
    private final int parallelism;
    @Getter
    private ConnectionFactory connectionFactory;
    @Getter
    private Connection connection;
    @Getter
    private Channel channel;
    @Getter
    private Channel listenerChannel;

    public RabbitMessenger(MetricsManager metricsManager, String uri, int parallelism) {
        this.metricsManager = metricsManager;
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
            try {
                channel.queueDeclare(GRADING_RESULT_QUEUE_NAME, true, false, false, null);
                channel.queueDeclare(GRADING_QUEUE_NAME, true, false, false, null);
            } catch (Exception ignored) {

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
    public void listen(ExecutorService executor, Consumer<RequestFuturePair> request) {
        executor.submit(() -> {
            try {
                listenBlocking(request);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public void listenBlocking(Consumer<RequestFuturePair> requestConsumer) throws IOException {
        listenerChannel.basicQos(parallelism);
        listenerChannel.basicConsume(GRADING_QUEUE_NAME, false, new DefaultConsumer(listenerChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                GradingRequest request;
                try {
                    request = SerializerUtils.fromJson(body, GradingRequest.class);
                } catch (Exception ex) {
                    listenerChannel.basicAck(envelope.getDeliveryTag(), false);
                    logger.error("Deserialization error: " + Arrays.toString(body), ex);
                    return;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug(SerializerUtils.toJson(request));
                }

                CompletableFuture<GradingResult> future = new CompletableFuture<>();
                requestConsumer.accept(new RequestFuturePair(request, future));

                future.handle((result, throwable) -> {
                    throwable = ExceptionUtils.unwrapCompletionException(throwable);
                    try {
                        if (result != null) {
                            metricsManager.handleResponse(request, result);
                            publishResult(result);

                            logger.info("Completed grading " + request.getId() + " - " + result.getStatus());
                            listenerChannel.basicAck(envelope.getDeliveryTag(), false);
                        } else if (ExceptionUtils.isRetryAllowed(throwable)) {
                            logger.error("Failed to grade " + request.getId(), throwable);
                            listenerChannel.basicNack(envelope.getDeliveryTag(), false, true);
                        } else {
                            logger.error("Failed to grade (non retriable)" + request.getId(), throwable);
                            listenerChannel.basicAck(envelope.getDeliveryTag(), false);
                        }

                    } catch (Throwable ex) {
                        logger.error("Failed to publish reply for submission " + request.getId(), ex);
                    }
                    return null;
                });
            }
        });
    }

    @Override
    public void publishResult(GradingResult result) throws IOException {
        String string = SerializerUtils.toJson(new NestMessageWrapper<>("result", result));
        if (logger.isDebugEnabled()) {
            logger.debug("Publishing " + string);
        }
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
