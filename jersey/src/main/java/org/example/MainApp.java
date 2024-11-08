package org.example;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.server.ResourceConfig;


public class MainApp {

    public static void main(String[] args) throws Exception {
        // 创建 Jetty 服务器
        Server server = new Server(8080);

        // 创建 ServletContextHandler
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Jersey 配置
        ResourceConfig config = new ResourceConfig();
        config.register(MultiPartFeature.class);
        config.packages("org.example.resources");  // 你的 REST 服务类所在的包
        ServletHolder servlet = new ServletHolder(new ServletContainer(config));
        context.addServlet(servlet, "/*");

//         添加 UnauthorizedOperationFilter 过滤器
        FilterHolder filterHolder = new FilterHolder(new UnauthorizedOperationFilter());
        context.addFilter(filterHolder, "/*", null);

        server.setHandler(context);

        // 启动服务器
        server.start();
        server.join();
    }
}
