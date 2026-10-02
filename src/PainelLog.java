import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.awt.Font;

// Mostra o mesmo log do console, mas colorido para facilitar a leitura.
public class PainelLog extends JScrollPane {
    private final JTextPane texto = new JTextPane();
    private final StyledDocument documento = texto.getStyledDocument();

    public PainelLog() {
        texto.setEditable(false);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        texto.setBackground(new Color(250, 250, 250));
        setViewportView(texto);
        setBorder(BorderFactory.createTitledBorder("Log (mesma saída do console)"));
    }

    public void limpar() {
        texto.setText("");
    }

    // Adiciona uma linha com a cor de acordo com quem falou
    public void adicionar(String linha) {
        SimpleAttributeSet estilo = new SimpleAttributeSet();

        if (linha.contains(">>> TRANSIÇÃO")) {
            StyleConstants.setForeground(estilo, new Color(230, 81, 0));
            StyleConstants.setBold(estilo, true);
        } else if (linha.startsWith("====")) {
            StyleConstants.setForeground(estilo, new Color(96, 125, 139));
            StyleConstants.setBold(estilo, true);
        } else if (linha.contains("[Balcão]")) {
            StyleConstants.setForeground(estilo, new Color(21, 101, 192));
        } else if (linha.startsWith("[Cozinheiro")) {
            StyleConstants.setForeground(estilo, new Color(183, 28, 28));
        } else if (linha.startsWith("[Garçom")) {
            StyleConstants.setForeground(estilo, new Color(106, 27, 154));
        }

        try {
            documento.insertString(documento.getLength(), linha + "\n", estilo);
            // Evita que o log cresça para sempre: apaga o começo quando fica muito grande
            if (documento.getLength() > 80000) {
                documento.remove(0, 30000);
            }
        } catch (BadLocationException e) {
            // não acontece: sempre inserimos no final
        }

        // Rola automaticamente para a última linha
        texto.setCaretPosition(documento.getLength());
    }
}
