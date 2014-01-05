package jp.junkato.misc.presentex;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class WebSocketServer {
	private static WebSocketServer instance;

	public static WebSocketServer getInstance() {
		if (instance == null) {
			instance = new WebSocketServer();
		}
		return instance;
	}

	public static void main(String[] args) throws Exception {
		WebSocketServer.getInstance().start();
	}

	private Server server;

	public WebSocketServer() {
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads(500);
		server = new Server(8040);
		server.setThreadPool(threadPool);

		ResourceHandler rh = new ResourceHandler();
		rh.setResourceBase(this.getClass().getClassLoader().getResource("html")
				.toExternalForm());

		MyWebSocketServlet wss = new MyWebSocketServlet();
		ServletHolder sh = new ServletHolder(wss);
		ServletContextHandler sch = new ServletContextHandler();
		sch.addServlet(sh, "/ws/*");

		HandlerList hl = new HandlerList();
		hl.setHandlers(new Handler[] { rh, sch });
		server.setHandler(hl);
	}

	public void start() throws Exception {
		server.start();
		server.join();
	}

	public void stop() throws Exception {
		server.stop();
	}

}