package com.paxxis.cornerstone.messaging;

import com.paxxis.cornerstone.messaging.service.amqp.AMQPServiceBusConnectorIT;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.IOException;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        AMQPServiceBusConnectorIT.class,
})
public class RabbitMQSuiteIT {

    @ClassRule
    public static Timeout timeout = new Timeout(30000);

    public static Process rabbit = null;

    @BeforeClass
    public static void startRabbitMQ() throws IOException, InterruptedException {
        rabbit = new ProcessBuilder("rabbitmq-server").inheritIO().start();
        Thread.sleep(2000);
        try {
            System.err.println("RabbitMQ did not start successfully, exit code: " + rabbit.exitValue());
            rabbit = null;
        } catch(IllegalThreadStateException itse) {
            System.out.println("RabbitMQ started successfully");
        }
    }

    @AfterClass
    public static void stopRabbitMQ() {
        if (rabbit == null) {
            return;
        }

        try {
            new ProcessBuilder("rabbitmqctl", "stop").inheritIO().start();
            Thread.sleep(5000);
            System.out.println("RabbitMQ exited, code: " + rabbit.exitValue());
        } catch(Exception itse) {
            System.err.println("RabbitMQ did not exit, forcibly terminating");
        } finally {
            //no-op if exited already...
            rabbit.destroy();
        }
    }
}
