package jp.junkato.misc.presentex;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

public class MyWebSocketServlet extends WebSocketServlet {
	private static final long serialVersionUID = 1155764849445104929L;

	public WebSocket doWebSocketConnect(HttpServletRequest request,
			String protocol) {
		return new MyWebSocket();
	}

}
