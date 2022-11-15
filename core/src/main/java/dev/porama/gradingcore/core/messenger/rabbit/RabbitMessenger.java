package dev.porama.gradingcore.core.messenger.rabbit;

import com.rabbitmq.client.*;
import dev.porama.gradingcore.core.messenger.Messenger;
import dev.porama.gradingcore.core.messenger.message.Message;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class RabbitMessenger implements Messenger {
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
            channel.exchangeDeclare("gradingcore", BuiltinExchangeType.DIRECT);
            channel.queueDeclare("grading_run", false, false, false, null);
            channel.queueDeclare("grading_result", false, false, false, null);
            channel.queueBind("grading_run", "grading", "run");
            channel.queueBind("grading_result", "grading", "result");

            setupConsumer();

        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException |
                 IOException | TimeoutException e) {
            logger.warn("Rabbit failed to connect: {}", e.getMessage());
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
    }

    public void setupConsumer() throws IOException {
        channel.basicConsume("grading_run",new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //TODO handle
            }
        });
    }

    @Override
    public void publish(Message message){

    }
}
