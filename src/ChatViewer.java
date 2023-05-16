import java.io.InputStream;
import java.io.*;
import java.nio.charset.*;
import java.util.Scanner;

import javax.swing.*;
import java.awt.*;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatViewer extends JFrame {
    private JSplitPane splitPane;
    private JPanel top;
    private JPanel bottom;
    private JPanel bottomPanel;
    private JScrollPane scrollChat;
    private JTextArea chatPanel;
    private JTextField userText;
    private JButton send;
    private PrintWriter clientOutputStream;
    private Client c;

    public ChatViewer(Client c) {
        this.c = c;
        this.clientOutputStream = c.getOut();
        setTitle("Encrypted Chat Server");
        setLocationRelativeTo(null);
        splitPane = new JSplitPane();
        top = new JPanel();
        bottom = new JPanel();
        bottomPanel = new JPanel();
        scrollChat = new JScrollPane();
        chatPanel = new JTextArea();
        userText = new JTextField();
        userText.addActionListener(
            new GUIInputHandler()
        );
        send = new JButton("send");
        send.addActionListener(
            new GUIInputHandler()
        );

        setSize(500, 500);
        getContentPane().setLayout(new GridLayout());
        getContentPane().add(splitPane);

        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(400);
        splitPane.setTopComponent(top);
        splitPane.setBottomComponent(bottom);
        bottom.setLayout(new BoxLayout(bottom,  BoxLayout.Y_AXIS));
        top.setLayout(new BoxLayout(top,  BoxLayout.Y_AXIS));
        top.add(scrollChat);
        scrollChat.setViewportView(chatPanel);

        bottom.add(bottomPanel);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.add(userText);
        bottomPanel.add(send);
    }

    public class GUIInputHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String textInput = userText.getText();
            userText.setText("");
            ByteArrayInputStream bais = new ByteArrayInputStream(textInput.getBytes());
            clientOutputStream.println(c.encrypt(textInput));
        }
    }

    public void showMessage(String message) {
        chatPanel.append(message + "\n");
        chatPanel.validate();
    }

    public void displayNotification(String message) {

    }
}
