import java.io.InputStream;
import java.io.*;
import java.nio.charset.*;
import javax.swing.*;
import java.awt.*;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatViewer extends JFrame {
    private InputStream userInput;

    private JSplitPane splitPane;
    private JPanel top;
    private JPanel bottom;
    private JPanel bottomPanel;
    private JScrollPane scrollChat;
    private JPanel chatPanel;
    private JTextField userText;
    private JButton send;

    public ChatViewer() {
        setTitle("Encrypted Chat Server");
        setLocationRelativeTo(null);
        splitPane = new JSplitPane();
        top = new JPanel();
        bottom = new JPanel();
        bottomPanel = new JPanel();
        scrollChat = new JScrollPane();
        chatPanel = new JPanel();
        JLabel r = new JLabel();
        r.setText("Hello World");
        //chatPanel.add(r);
        userText = new JTextField();
        userText.addActionListener(new GUIInputHandler());
        userInput = new ByteArrayInputStream(userText.getText().getBytes(Charset.forName("UTF-8")));
        send = new JButton("send");
        //send.addActionListener(
        //    new GUIInputHandler()
        //);

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
            //String textInput = userText.getText();
            //JLabel newL = new JLabel();
            //newL.setText(textInput);
            //chatPanel.add(newL);
            //chatPanel.validate();
            userText.setText("");
            //byte[] inputAsArr = textInput.getBytes();
        }
    }

    public InputStream getUserInput() {
        return userInput;
    }
    public static void main(String[] args) {
        //new ChatViewer().setVisible(true);
    }    

    public void showMessage(String message) {
        JLabel msg = new JLabel();
        msg.setText(message);
        chatPanel.add(msg);
        chatPanel.validate();
    }

    public void displayNotification(String message) {

    }
}
