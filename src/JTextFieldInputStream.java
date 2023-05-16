import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.IOException;
import javax.swing.JTextField;
public class JTextFieldInputStream extends InputStream {
    byte[] data;
    int offset = 0;
    
    public JTextFieldInputStream(JTextField textField) {
        // the key listener implementation will process each key stroke and add it to a buffer byte array which will be sent through the inputstream
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyStroke) {
                if (keyStroke.getKeyChar() == '\n') {
                    data = textField.getText().getBytes();
                    offset = 0;
                    textField.setText("");
                }
                super.keyReleased(keyStroke);
            }

        });

        textField.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    textField.setText("");
                }
            }
        );

    }

    @Override
    public int read() throws IOException {
        if (offset > data.length) {return -1;}
        else {return this.data[offset++];}
    }
}
