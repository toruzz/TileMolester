package tm.utils;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 * Key adapter which only allows numbers in the field.
 */
public class DecimalNumberVerifier extends KeyAdapter {
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        JTextField tf = (JTextField)e.getSource();
        if (!(c == KeyEvent.VK_BACK_SPACE) ||
                (c == KeyEvent.VK_DELETE)) {
            if (!Character.isDigit(c)) {
                Toolkit.getDefaultToolkit().beep();
                e.consume();
            }
            else if (tf.getText().length() == tf.getColumns()) {
                Toolkit.getDefaultToolkit().beep();
                e.consume();
            }
        }
    }
}