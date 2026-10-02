import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

// "Escuta" tudo que o programa imprime com System.out.println.
// Cada linha continua indo para o console normal E também é entregue
// para a tela visual (log, balões de fala...).
// Assim os agentes e estados não precisam saber que a tela existe.
public class CapturaConsole extends OutputStream {
    private final ByteArrayOutputStream linhaAtual = new ByteArrayOutputStream();
    private final PrintStream consoleOriginal;
    private final Consumer<String> destino;

    private CapturaConsole(PrintStream consoleOriginal, Consumer<String> destino) {
        this.consoleOriginal = consoleOriginal;
        this.destino = destino;
    }

    // Troca o System.out por um que também manda as linhas para "destino"
    public static void instalar(Consumer<String> destino) {
        PrintStream original = System.out;
        CapturaConsole captura = new CapturaConsole(original, destino);
        System.setOut(new PrintStream(captura, true, StandardCharsets.UTF_8));
    }

    // Recebe o texto byte a byte; quando chega uma quebra de linha, a linha está completa
    @Override
    public void write(int b) {
        if (b == '\n') {
            String linha = linhaAtual.toString(StandardCharsets.UTF_8).replace("\r", "");
            linhaAtual.reset();
            consoleOriginal.println(linha); // continua aparecendo no console
            destino.accept(linha);          // e vai para a tela
        } else {
            linhaAtual.write(b);
        }
    }
}
