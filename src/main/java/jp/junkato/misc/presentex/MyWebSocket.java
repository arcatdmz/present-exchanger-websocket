package jp.junkato.misc.presentex;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;

public class MyWebSocket implements WebSocket, OnTextMessage, OnBinaryMessage {

	static {
		final JFrame frame = new JFrame();

		// Shuffle
		JButton shuffleButton = new JButton(new AbstractAction("Shuffle") {
			private static final long serialVersionUID = 1256479756831700157L;
			@Override
			public void actionPerformed(ActionEvent e) {
				list = new ArrayList<MyWebSocket.Member>();
				index = 0;
				for (Member member : members) {
					if (member.name != null
							&& member.name.length() > 0
							&& member.socket.connection.isOpen()) {
						list.add((int) (list.size() * Math.random()), member);
					}
				}
				System.out.println("---");
				System.out.println(String.format("Shuffle done. %d participants.", list.size()));
			}
		});

		// Next
		JButton nextButton = new JButton(new AbstractAction("Next") {
			private static final long serialVersionUID = 1397100222395005352L;
			@Override
			public void actionPerformed(ActionEvent e) {

				Member sender = list.get(index ++);
				if (index >= list.size()) {
					index = 0;
				}
				Member receiver = list.get(index);

				System.out.println("---");
				try {
					System.out.println(String.format("Sender: %s (%d)", sender.name, sender.id));
					sender.socket.connection.sendMessage("GIFTX " + receiver.name);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					System.out.println(String.format("Receiver: %s (%d)", sender.name, sender.id));
					receiver.socket.connection.sendMessage("GIFTY " + sender.name);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		// Close
		JButton closeButton = new JButton(new AbstractAction("Close") {
			private static final long serialVersionUID = 6992083615385374164L;
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});

		frame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					WebSocketServer.getInstance().stop();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		frame.setContentPane(panel);
		panel.setLayout(new BorderLayout(5, 5));
		panel.add(shuffleButton, BorderLayout.NORTH);
		panel.add(nextButton, BorderLayout.CENTER);
		panel.add(closeButton, BorderLayout.SOUTH);
		frame.pack();

		frame.setVisible(true);
	}

	private static List<Member> list;
	private static int index = 0;

	private static Set<Member> members = new CopyOnWriteArraySet<Member>();

	private static int currentId = 1;

	private static class Member {
		private int id;
		private String name;
		private MyWebSocket socket;
	}

	private Connection connection;
	private Member member;

	public void onOpen(Connection connection) {
		this.connection = connection;
		this.member = new Member();
		this.member.id = currentId ++;
		this.member.socket = this;
		members.add(member);
		System.out.println("connected: " + this + "(" + connection + ")");
	}

	public void onClose(int closeCode, String message) {
		this.member.socket = null;
		this.member = null;
		System.out.println("disconnected: " + this);
	}

	public void onMessage(String data) {
		System.out.println("message received: " + data);
		if (data == null) {
			return;
		}

		// Registration
		if (data.startsWith("REGIS")) {
			this.member.name = data.substring(6);
			try {
				connection.sendMessage(String.format("REGIS %d,%s", member.id, member.name));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		// Login
		if (data.startsWith("LOGIN")) {
			String oldId = data.substring(6);
			int id = Integer.valueOf(oldId);
			Member oldMember = null;
			for (Member member : members) {
				if (member.id == id) {
					oldMember = member;
				}
			}
			if (oldMember != null) {
				members.remove(member);
				this.member = oldMember;
				this.member.socket = this;
			}
			try {
				connection.sendMessage(String.format("REGIS %d,%s", member.id, member.name));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void onMessage(byte[] data, int offset, int length) {
		System.out.println("message received: binary " + length + " bytes");
		for (Member member : members) {
			try {
				// Echo
				member.socket.connection.sendMessage(data, offset, length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
