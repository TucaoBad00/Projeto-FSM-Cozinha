import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

// Desenha o diagrama de estados de UM agente e destaca, ao vivo:
// - o estado atual (colorido);
// - "executar()" a cada tick;
// - "sair()" e "entrar()" + a seta usada quando acontece uma transição.
public class PainelDiagrama extends JPanel {
    private final String titulo;
    private final String[] estados;   // nomes dos 3 estados (posições: esq, dir, baixo)
    private final int[][] arestas;    // cada aresta: {origem, destino}
    private final String[] rotulos;   // texto de cada aresta (a regra)
    private final Color cor;          // cor do agente

    private int estadoAtual = 0;
    private int ultimaAresta = -1;    // índice da aresta usada na última transição
    private String ultimaTransicao = "nenhuma ainda";
    private int estadoAnterior = -1;
    private long transicaoEm = 0;     // quando aconteceu a última transição (ms)
    private long tickEm = 0;          // quando aconteceu o último tick (ms)

    private static final int LARG_NO = 150, ALT_NO = 38;

    public PainelDiagrama(String titulo, String[] estados, int[][] arestas, String[] rotulos, Color cor) {
        this.titulo = titulo;
        this.estados = estados;
        this.arestas = arestas;
        this.rotulos = rotulos;
        this.cor = cor;
        setBackground(Color.WHITE);
    }

    // Volta ao estado inicial (usado no botão Reiniciar)
    public void reiniciar(String estadoInicial) {
        estadoAtual = indice(estadoInicial);
        ultimaAresta = -1;
        estadoAnterior = -1;
        ultimaTransicao = "nenhuma ainda";
        tickEm = 0;
        transicaoEm = 0;
    }

    // Chamado depois de cada tick com o estado de antes e o de depois
    public void registrarTick(int tick, String antes, String depois) {
        tickEm = System.currentTimeMillis();
        int de = indice(antes);
        int para = indice(depois);
        estadoAtual = para;

        if (de != para) {
            // Houve transição: descobre qual seta foi usada
            estadoAnterior = de;
            transicaoEm = tickEm;
            ultimaTransicao = antes + " → " + depois + "  (tick " + tick + ")";
            for (int i = 0; i < arestas.length; i++) {
                if (arestas[i][0] == de && arestas[i][1] == para) {
                    ultimaAresta = i;
                }
            }
        }
    }

    private int indice(String nomeEstado) {
        for (int i = 0; i < estados.length; i++) {
            if (estados[i].equals(nomeEstado)) {
                return i;
            }
        }
        return 0;
    }

    // Posição (centro) de cada estado dentro do painel
    private double centroX(int i) {
        double[] fracoes = {0.19, 0.81, 0.5};
        return getWidth() * fracoes[i];
    }

    private double centroY(int i) {
        double[] fracoes = {0.32, 0.32, 0.76};
        return getHeight() * fracoes[i];
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        long agora = System.currentTimeMillis();
        double tempoTransicao = (agora - transicaoEm) / 1000.0; // segundos desde a transição
        double tempoTick = (agora - tickEm) / 1000.0;           // segundos desde o tick

        // Título
        g.setColor(cor.darker());
        g.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g.drawString(titulo, 10, 20);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.setColor(Color.DARK_GRAY);
        String textoAtual = "estado atual: " + estados[estadoAtual];
        g.drawString(textoAtual, getWidth() - g.getFontMetrics().stringWidth(textoAtual) - 10, 20);

        // ----- Setas (transições) -----
        for (int i = 0; i < arestas.length; i++) {
            boolean destacada = (i == ultimaAresta && tempoTransicao < 1.5);
            desenharAresta(g, i, destacada);
        }

        // ----- Caixas (estados) -----
        for (int i = 0; i < estados.length; i++) {
            desenharEstado(g, i, tempoTick);
        }

        // ----- Etiquetas dos métodos do padrão State -----
        if (tempoTransicao < 1.2 && estadoAnterior >= 0) {
            // Transição: mostra sair() no estado antigo e entrar() no novo
            desenharEtiqueta(g, estadoAnterior, "sair()", new Color(198, 40, 40), tempoTransicao / 1.2);
            desenharEtiqueta(g, estadoAtual, "entrar()", new Color(46, 125, 50), tempoTransicao / 1.2);
        } else if (tempoTick < 0.8) {
            // Tick normal: o estado atual roda executar()
            desenharEtiqueta(g, estadoAtual, "executar()", new Color(21, 101, 192), tempoTick / 0.8);
        }

        // Rodapé: última transição
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.setColor(Color.GRAY);
        g.drawString("Última transição: " + ultimaTransicao, 10, getHeight() - 8);

        g.dispose();
    }

    private void desenharEstado(Graphics2D g, int i, double tempoTick) {
        int x = (int) (centroX(i) - LARG_NO / 2.0);
        int y = (int) (centroY(i) - ALT_NO / 2.0);
        boolean atual = (i == estadoAtual);

        // "Pulso" em volta do estado atual a cada tick (mostra que executar() rodou)
        if (atual && tempoTick < 0.6) {
            int cresce = (int) (tempoTick * 20);
            float alfa = (float) (1 - tempoTick / 0.6);
            g.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), (int) (alfa * 160)));
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(x - cresce, y - cresce, LARG_NO + cresce * 2, ALT_NO + cresce * 2, 18, 18);
        }

        g.setColor(atual ? cor : new Color(245, 245, 245));
        g.fillRoundRect(x, y, LARG_NO, ALT_NO, 16, 16);
        g.setColor(atual ? cor.darker() : new Color(189, 189, 189));
        g.setStroke(new BasicStroke(atual ? 2.5f : 1.5f));
        g.drawRoundRect(x, y, LARG_NO, ALT_NO, 16, 16);

        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.setColor(atual ? Color.WHITE : Color.GRAY);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(estados[i], (int) centroX(i) - fm.stringWidth(estados[i]) / 2, (int) centroY(i) + 5);
    }

    private void desenharAresta(Graphics2D g, int i, boolean destacada) {
        int de = arestas[i][0];
        int para = arestas[i][1];
        double x1 = centroX(de), y1 = centroY(de);
        double x2 = centroX(para), y2 = centroY(para);

        // Se existe a seta no sentido contrário, desloca para as duas não ficarem em cima uma da outra
        double deslocamento = 0;
        for (int[] outra : arestas) {
            if (outra[0] == para && outra[1] == de) {
                deslocamento = 9;
            }
        }
        double dx = x2 - x1, dy = y2 - y1;
        double comprimento = Math.hypot(dx, dy);
        double ux = dx / comprimento, uy = dy / comprimento; // direção
        double px = -uy * deslocamento, py = ux * deslocamento; // perpendicular

        // Corta a linha na borda das caixas
        double corte = Math.min((LARG_NO / 2.0) / Math.max(Math.abs(ux), 0.001),
                (ALT_NO / 2.0) / Math.max(Math.abs(uy), 0.001)) + 4;
        double ax = x1 + ux * corte + px, ay = y1 + uy * corte + py;
        double bx = x2 - ux * corte + px, by = y2 - uy * corte + py;

        Color corSeta = destacada ? new Color(230, 81, 0) : new Color(120, 120, 120);
        g.setColor(corSeta);
        g.setStroke(new BasicStroke(destacada ? 3.5f : 1.5f));
        g.drawLine((int) ax, (int) ay, (int) bx, (int) by);

        // Ponta da seta
        Path2D ponta = new Path2D.Double();
        ponta.moveTo(bx, by);
        ponta.lineTo(bx - ux * 10 - uy * 5, by - uy * 10 + ux * 5);
        ponta.lineTo(bx - ux * 10 + uy * 5, by - uy * 10 - ux * 5);
        ponta.closePath();
        g.fill(ponta);

        // Texto da regra no meio da seta
        g.setFont(new Font("Segoe UI", destacada ? Font.BOLD : Font.PLAIN, 11));
        FontMetrics fm = g.getFontMetrics();
        String rotulo = rotulos[i];
        double mx = (ax + bx) / 2;
        double my = (ay + by) / 2;
        if (Math.abs(uy) < 0.3) {
            // Seta horizontal: texto acima ou abaixo da linha (do lado em que ela foi deslocada)
            my += (py >= 0 ? 16 : -6);
            mx -= fm.stringWidth(rotulo) / 2.0;
        } else {
            // Seta inclinada: texto do lado de FORA do diagrama, para não encavalar no meio
            if (mx > getWidth() / 2.0) {
                mx += 10;
            } else {
                mx -= 10 + fm.stringWidth(rotulo);
            }
        }
        g.drawString(rotulo, (int) mx, (int) my);
    }

    // Etiqueta que "sobe e some" em cima de um estado (ex.: "entrar()")
    private void desenharEtiqueta(Graphics2D g, int i, String texto, Color corEtiqueta, double progresso) {
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int largura = fm.stringWidth(texto) + 12;
        int x = (int) centroX(i) - largura / 2;
        int y = (int) (centroY(i) - ALT_NO / 2.0 - 24 - progresso * 8);
        int alfa = (int) (255 * Math.max(0, 1 - progresso * progresso));

        g.setColor(new Color(corEtiqueta.getRed(), corEtiqueta.getGreen(), corEtiqueta.getBlue(), alfa));
        g.fillRoundRect(x, y, largura, 18, 10, 10);
        g.setColor(new Color(255, 255, 255, alfa));
        g.drawString(texto, x + 6, y + 13);
    }
}
