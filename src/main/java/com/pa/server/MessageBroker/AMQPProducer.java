package com.pa.server.MessageBroker;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Component
public class AMQPProducer {

    @Value("${spring.cloudamqp.url}")
    private String AMQPUrl;

    @Value("${spring.cloudamqp.queue}")
    private String queueName;

    public Channel initConnection() throws IOException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(AMQPUrl);

        //Recommended settings
        factory.setRequestedHeartbeat(30);
        factory.setConnectionTimeout(30000);

        Connection connection = factory.newConnection();

        return connection.createChannel();
    }

    public void sendMessage(Channel channel, String message) throws IOException {
        boolean durable = false;    //durable - RabbitMQ will never lose the queue if a crash occurs
        boolean exclusive = false;  //exclusive - if queue only will be used by one connection
        boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

        channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);

        String exchangeName = "";
        String routingKey = "uploadQueue";
        channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
    }
}