import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

// Mostra os valores das variáveis e limiares de cada agente em barras,
// para ver exatamente POR QUE cada transição acontece.
public class PainelVariaveis extends JPanel {
    private Cozinheiro cozinheiro;
    private Garcom garcom;
    private Balcao balcao;
    private int tick = 0;

    // Valores "na tela": andam suavemente até o valor real (animação das barras)
    private double cansacoTela, progressoTela, tempoMesaTela;

    public PainelVariaveis() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(420, 350));
    }

    public void setAgentes(Cozinheiro cozinheiro, Garcom garcom, Balcao balcao) {
        this.cozinheiro = cozinheiro;
        this.garcom = garcom;
        this.balcao = balcao;
        this.tick = 0;
        cansacoTela = 0;
        progressoTela = 0;
        tempoMesaTela = 0;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    // Chamado ~60 vezes por segundo: aproxima as barras do valor real
    public void animar() {
        if (cozinheiro == null) {
            return;
        }
        cansacoTela += (cozinheiro.getCansaco() - cansacoTela) * 0.15;
        progressoTela += (cozinheiro.getProgressoPrato() - progressoTela) * 0.15;
        tempoMesaTela += (garcom.getTempoNaMesa() - tempoMesaTela) * 0.15;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (cozinheiro == null) {
            return;
        }
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int largura = getWidth() - 30;

        // Tick
        g.setColor(new Color(55, 71, 79));
        g.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g.drawString("Tick " + tick, 15, 46);

        // ----- Cozinheiro -----
        int y = 78;
        titulo(g, "Cozinheiro " + cozinheiro.getNome(), cozinheiro.getNomeEstado(), new Color(230, 81, 0), y);
        y += 12;
        boolean cansado = cozinheiro.getCansaco() >= Cozinheiro.LIMITE_CANSACO;
        barra(g, "Cansaço", cansacoTela, 100, cozinheiro.getCansaco() + " / 100",
                cansado ? new Color(229, 57, 53) : new Color(255, 167, 38),
                Cozinheiro.LIMITE_CANSACO, "limite " + Cozinheiro.LIMITE_CANSACO, y, largura);
        y += 44;
        barra(g, "Progresso do prato", progressoTela, Cozinheiro.PROGRESSO_PRATO_PRONTO,
                cozinheiro.getProgressoPrato() + "%", new Color(67, 160, 71),
                -1, "", y, largura);

        // ----- Garçom -----
        y += 62;
        titulo(g, "Garçom " + garcom.getNome(), garcom.getNomeEstado(), new Color(106, 27, 154), y);
        y += 12;
        barra(g, "Tempo na mesa (timer)", tempoMesaTela, Garcom.TEMPO_ANOTAR_PEDIDO,
                garcom.getTempoNaMesa() + " / " + Garcom.TEMPO_ANOTAR_PEDIDO, new Color(142, 36, 170),
                -1, "", y, largura);
        y += 46;
        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.setColor(Color.DARK_GRAY);
        g.drawString("Pratos entregues: " + garcom.getPratosEntregues(), 15, y);
        for (int i = 0; i < Math.min(garcom.getPratosEntregues(), 12); i++) {
            desenharPratinho(g, 150 + i * 20, y - 5);
        }

        // ----- Balcão -----
        y += 34;
        titulo(g, "Balcão (comunicação)", "", new Color(21, 101, 192), y);
        y += 26;
        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.setColor(Color.DARK_GRAY);
        g.drawString("Pedidos esperando: " + balcao.getPedidos(), 15, y);
        g.drawString("Pratos prontos: " + balcao.getPratos(), 200, y);

        g.dispose();
    }

    private void titulo(Graphics2D g, String nome, String estado, Color cor, int y) {
        g.setColor(cor);
        g.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g.drawString(nome, 15, y);
        if (!estado.isEmpty()) {
            int x = 15 + g.getFontMetrics().stringWidth(nome) + 10;
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            int w = g.getFontMetrics().stringWidth(estado) + 14;
            g.fillRoundRect(x, y - 14, w, 19, 10, 10);
            g.setColor(Color.WHITE);
            g.drawString(estado, x + 7, y);
        }
    }

    // Desenha uma barra de progresso com texto e, opcionalmente, uma linha de limiar
    private void barra(Graphics2D g, String nome, double valor, double maximo, String textoValor,
                       Color cor, int limiar, String textoLimiar, int y, int largura) {
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.setColor(Color.DARK_GRAY);
        g.drawString(nome, 15, y + 4);
        g.drawString(textoValor, 15 + largura - g.getFontMetrics().stringWidth(textoValor), y + 4);

        int by = y + 10;
        g.setColor(new Color(236, 239, 241));
        g.fillRoundRect(15, by, largura, 14, 8, 8);
        int preenchido = (int) (largura * Math.max(0, Math.min(1, valor / maximo)));
        g.setColor(cor);
        g.fillRoundRect(15, by, preenchido, 14, 8, 8);

        if (limiar >= 0) {
            int lx = 15 + (int) (largura * limiar / maximo);
            g.setColor(new Color(183, 28, 28));
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{4, 3}, 0));
            g.drawLine(lx, by - 3, lx, by + 17);
            g.setStroke(new BasicStroke(1));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.drawString(textoLimiar, lx + 3, by + 27);
        }
    }

    private void desenharPratinho(Graphics2D g, int x, int y) {
        g.setColor(Color.WHITE);
        g.fillOval(x - 8, y - 5, 16, 10);
        g.setColor(Color.GRAY);
        g.drawOval(x - 8, y - 5, 16, 10);
        g.setColor(new Color(255, 112, 67));
        g.fillOval(x - 4, y - 3, 8, 5);
    }
}
