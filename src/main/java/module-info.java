open module GradingCore.main {
    // utilities
    requires lombok;
    requires org.jetbrains.annotations;
    requires com.google.gson;
    // logging
    requires org.slf4j;
    requires ch.qos.logback.classic;
    // messaging
    requires com.rabbitmq.client;
    // http
    requires java.net.http;
    // scripting
    requires java.scripting;

    exports dev.porama.gradingcore;
    exports dev.porama.gradingcore.container.data;
    exports dev.porama.gradingcore.utils;
    exports dev.porama.gradingcore.container;
    exports dev.porama.gradingcore.temp;
    exports dev.porama.gradingcore.file;
}