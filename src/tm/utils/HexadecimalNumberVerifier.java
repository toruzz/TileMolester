package tm.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


 /**
 * Key adapter which only allows hexadecimal values in the field.
 */
public class HexadecimalNumberVerifier extends KeyAdapter {
        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            JTextField tf = (JTextField) e.getSource();
            if (!((Character.digit(c, 16) != -1) ||
                    (c == KeyEvent.VK_BACK_SPACE) ||
                    (c == KeyEvent.VK_DELETE))) {
                Toolkit.getDefaultToolkit().beep();
                e.consume();
            }
            else if (tf.getText().length() == tf.getColumns()) {
                Toolkit.getDefaultToolkit().beep();
                e.consume();
            }
        }
    }