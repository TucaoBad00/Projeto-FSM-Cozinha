import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

// Desenha a cozinha e anima os personagens.
// IMPORTANTE: este painel NÃO decide nada. Ele só LÊ o estado atual de cada agente
// e desenha o personagem no lugar certo (fogão, sofá, balcão, mesa...).
public class PainelCozinha extends JPanel {
    // Tamanho "de projeto" da cena; ela é esticada para caber no painel
    private static final int LARGURA = 820;
    private static final int ALTURA = 420;
    // Quanto tempo (s) um balão de fala fica na tela
    private static final double DURACAO_FALA = 2.6;

    private Cozinheiro cozinheiro;
    private Garcom garcom;
    private Balcao balcao;

    // Posição atual de cada personagem: {x, y}. Ela anda até a posição do estado.
    private final double[] posCozinheiro = new double[2];
    private final double[] posGarcom = new double[2];
    private boolean cozinheiroAndando, garcomAndando;
    private double velocidadeAndar = 300; // pixels por segundo

    // Falas recentes de cada personagem (balões)
    private final ArrayList<Fala> falasCozinheiro = new ArrayList<>();
    private final ArrayList<Fala> falasGarcom = new ArrayList<>();

    // Texto da última transição, mostrado embaixo do personagem
    private String transicaoCozinheiro = "", transicaoGarcom = "";
    private double transicaoCozinheiroEm = -99, transicaoGarcomEm = -99;

    // Para animar itens aparecendo no balcão e o prato na mesa
    private int pedidosAntes, pratosAntes, entreguesAntes;
    private double pedidoApareceuEm = -99, pratoApareceuEm = -99, pratoNaMesaEm = -99;

    private long ultimoQuadro = System.nanoTime();

    // Uma fala = texto + momento em que apareceu
    private static class Fala {
        String texto;
        double tempo;

        Fala(String texto, double tempo) {
            this.texto = texto;
            this.tempo = tempo;
        }
    }

    public PainelCozinha() {
        setBackground(new Color(55, 71, 79));
    }

    // Tempo atual em segundos (usado em todas as animações)
    private double agora() {
        return System.currentTimeMillis() / 1000.0;
    }

    public void setAgentes(Cozinheiro cozinheiro, Garcom garcom, Balcao balcao) {
        this.cozinheiro = cozinheiro;
        this.garcom = garcom;
        this.balcao = balcao;
        // Coloca cada um direto no lugar do estado inicial
        double[] alvoC = alvoCozinheiro();
        double[] alvoG = alvoGarcom();
        posCozinheiro[0] = alvoC[0];
        posCozinheiro[1] = alvoC[1];
        posGarcom[0] = alvoG[0];
        posGarcom[1] = alvoG[1];
        falasCozinheiro.clear();
        falasGarcom.clear();
        transicaoCozinheiroEm = -99;
        transicaoGarcomEm = -99;
        pratoNaMesaEm = -99;
        pedidosAntes = balcao.getPedidos();
        pratosAntes = balcao.getPratos();
        entreguesAntes = garcom.getPratosEntregues();
    }

    // Personagens andam mais rápido quando a simulação está acelerada
    public void setFatorVelocidade(double fator) {
        velocidadeAndar = 300 * fator;
    }

    // Recebe cada linha do log e transforma as falas dos agentes em balões
    public void receberMensagem(String linha) {
        if (linha.startsWith("[Cozinheiro ")) {
            adicionarFala(falasCozinheiro, linha.substring(linha.indexOf("] ") + 2));
        } else if (linha.startsWith("[Garçom ")) {
            adicionarFala(falasGarcom, linha.substring(linha.indexOf("] ") + 2));
        }
    }

    private void adicionarFala(ArrayList<Fala> falas, String texto) {
        // Mesma frase de novo (ex.: "Esperando no balcão..."): só renova o tempo do balão
        if (!falas.isEmpty() && falas.get(falas.size() - 1).texto.equals(texto)) {
            falas.get(falas.size() - 1).tempo = agora();
            return;
        }
        falas.add(new Fala(texto, agora()));
        // Guarda só as 2 últimas falas
        while (falas.size() > 2) {
            falas.remove(0);
        }
    }

    public void mostrarTransicao(boolean doCozinheiro, String texto) {
        if (doCozinheiro) {
            transicaoCozinheiro = texto;
            transicaoCozinheiroEm = agora();
        } else {
            transicaoGarcom = texto;
            transicaoGarcomEm = agora();
        }
    }

    // ---------- Para onde cada personagem vai em cada estado ----------
    private double[] alvoCozinheiro() {
        switch (cozinheiro.getNomeEstado()) {
            case "Cozinhando":
                return new double[]{135, 200};  // em frente ao fogão
            case "Descansando":
                return new double[]{105, 318};  // no sofá
            default:
                return new double[]{350, 230};  // EsperandoPedido: perto do balcão
        }
    }

    private double[] alvoGarcom() {
        switch (garcom.getNomeEstado()) {
            case "AguardandoPrato":
                return new double[]{490, 230};  // do lado do salão do balcão
            case "ServindoPrato":
                return new double[]{615, 268};  // levando o prato até a mesa
            default:
                return new double[]{610, 225};  // AnotandoPedido: na mesa
        }
    }

    // Move "pos" em direção ao "alvo". Devolve true se ainda está andando.
    private boolean mover(double[] pos, double[] alvo, double dt) {
        double dx = alvo[0] - pos[0];
        double dy = alvo[1] - pos[1];
        double distancia = Math.hypot(dx, dy);
        double passo = velocidadeAndar * dt;
        if (distancia <= passo) {
            pos[0] = alvo[0];
            pos[1] = alvo[1];
            return false;
        }
        pos[0] += dx / distancia * passo;
        pos[1] += dy / distancia * passo;
        return true;
    }

    // Chamado ~60 vezes por segundo pelo timer de animação
    public void animar() {
        long quadro = System.nanoTime();
        double dt = Math.min((quadro - ultimoQuadro) / 1e9, 0.05);
        ultimoQuadro = quadro;
        if (cozinheiro == null) {
            return;
        }

        cozinheiroAndando = mover(posCozinheiro, alvoCozinheiro(), dt);
        garcomAndando = mover(posGarcom, alvoGarcom(), dt);

        // Detecta itens novos no balcão / prato entregue (para animar o "pop")
        if (balcao.getPedidos() > pedidosAntes) {
            pedidoApareceuEm = agora();
        }
        if (balcao.getPratos() > pratosAntes) {
            pratoApareceuEm = agora();
        }
        if (garcom.getPratosEntregues() > entreguesAntes) {
            pratoNaMesaEm = agora();
        }
        pedidosAntes = balcao.getPedidos();
        pratosAntes = balcao.getPratos();
        entreguesAntes = garcom.getPratosEntregues();

        repaint();
    }

    // =====================================================================
    //                              DESENHO
    // =====================================================================
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        if (cozinheiro == null) {
            return;
        }
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Estica a cena de 820x420 para o tamanho do painel, mantendo a proporção
        double escala = Math.min(getWidth() / (double) LARGURA, getHeight() / (double) ALTURA);
        g.translate((getWidth() - LARGURA * escala) / 2, (getHeight() - ALTURA * escala) / 2);
        g.scale(escala, escala);
        g.clipRect(0, 0, LARGURA, ALTURA);

        double t = agora();
        String estadoC = cozinheiro.getNomeEstado();
        String estadoG = garcom.getNomeEstado();

        desenharCenario(g);
        desenharFogao(g, t, estadoC.equals("Cozinhando"));
        desenharSofa(g);
        desenharBalcao(g, t);
        desenharMesa(g, t);

        // ----- Cozinheiro -----
        double cx = posCozinheiro[0], cy = posCozinheiro[1];
        desenharPessoa(g, cx, cy, t, cozinheiroAndando, true, estadoC.equals("Descansando"));
        if (estadoC.equals("Descansando") && !cozinheiroAndando) {
            desenharZzz(g, cx, cy, t);
        }
        desenharNome(g, "Cozinheiro " + cozinheiro.getNome(), cx, cy);

        // ----- Garçom -----
        double gx = posGarcom[0], gy = posGarcom[1];
        desenharPessoa(g, gx, gy, t, garcomAndando, false, false);
        if (estadoG.equals("AnotandoPedido") && !garcomAndando) {
            desenharBloquinho(g, gx, gy, t);
        }
        if (estadoG.equals("ServindoPrato")) {
            desenharPrato(g, gx + 24, gy - 2, 1.0);
        }
        desenharNome(g, "Garçom " + garcom.getNome(), gx, gy);

        // Textos de transição e balões por cima de tudo
        desenharTransicao(g, transicaoCozinheiro, transicaoCozinheiroEm, cx, cy, t);
        desenharTransicao(g, transicaoGarcom, transicaoGarcomEm, gx, gy, t);
        // No fogão os balões vão para o lado, para não esconder as chamas e a panela
        boolean aoLado = estadoC.equals("Cozinhando");
        desenharFalas(g, falasCozinheiro, cx, cy, t, 5, 390, aoLado);
        desenharFalas(g, falasGarcom, gx, gy, t, 425, 815, false);

        g.dispose();
    }

    private void desenharCenario(Graphics2D g) {
        // Chão da cozinha (azulejos)
        g.setColor(new Color(236, 239, 241));
        g.fillRect(0, 60, 410, 360);
        g.setColor(new Color(207, 216, 220));
        for (int x = 0; x <= 410; x += 30) {
            g.drawLine(x, 60, x, 420);
        }
        for (int y = 60; y <= 420; y += 30) {
            g.drawLine(0, y, 410, y);
        }
        // Chão do salão (madeira)
        g.setColor(new Color(222, 196, 160));
        g.fillRect(410, 60, 410, 360);
        g.setColor(new Color(205, 175, 135));
        for (int y = 60; y <= 420; y += 24) {
            g.drawLine(410, y, 820, y);
        }
        // Paredes
        g.setColor(new Color(176, 190, 197));
        g.fillRect(0, 0, 410, 60);
        g.setColor(new Color(161, 136, 127));
        g.fillRect(410, 0, 410, 60);
        g.setColor(new Color(0, 0, 0, 40));
        g.fillRect(0, 56, 820, 4);

        g.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g.setColor(new Color(55, 71, 79));
        g.drawString("COZINHA", 16, 36);
        g.setColor(new Color(62, 39, 35));
        g.drawString("SALÃO", 690, 36);

        // Geladeira (decoração)
        g.setColor(new Color(245, 245, 245));
        g.fillRoundRect(318, 50, 62, 92, 10, 10);
        g.setColor(new Color(176, 190, 197));
        g.drawRoundRect(318, 50, 62, 92, 10, 10);
        g.drawLine(318, 82, 380, 82);
        g.fillRect(370, 60, 4, 14);
        g.fillRect(370, 92, 4, 20);
    }

    private void desenharFogao(Graphics2D g, double t, boolean cozinhando) {
        g.setColor(new Color(69, 90, 100));
        g.fillRoundRect(60, 55, 160, 80, 12, 12);
        g.setColor(new Color(38, 50, 56));
        g.fillOval(82, 75, 40, 40);
        g.fillOval(152, 75, 40, 40);
        g.setColor(new Color(96, 125, 139));
        g.drawOval(88, 81, 28, 28);
        g.drawOval(158, 81, 28, 28);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.setColor(new Color(207, 216, 220));
        g.drawString("fogão", 122, 130);

        if (cozinhando) {
            // Chamas que tremem
            for (int i = 0; i < 5; i++) {
                double altura = 10 + 6 * Math.sin(t * 18 + i * 1.7);
                int fx = 88 + i * 7;
                g.setColor(i % 2 == 0 ? new Color(255, 111, 0) : new Color(255, 202, 40));
                g.fillPolygon(new int[]{fx, fx + 4, fx + 8}, new int[]{108, (int) (108 - altura), 108}, 3);
            }
            // Panela
            g.setColor(new Color(33, 33, 33));
            g.fillRoundRect(80, 80, 44, 20, 8, 8);
            g.fillRect(124, 86, 26, 5);
            // Vapor subindo
            for (int i = 0; i < 3; i++) {
                double fase = (t * 0.9 + i / 3.0) % 1.0;
                int alfa = (int) (170 * (1 - fase));
                g.setColor(new Color(255, 255, 255, alfa));
                int vx = (int) (92 + i * 10 + Math.sin(t * 3 + i) * 4);
                int vy = (int) (76 - fase * 45);
                int r = (int) (6 + fase * 8);
                g.fillOval(vx - r / 2, vy - r / 2, r, r);
            }
            // Barra de progresso do prato na parede
            int progresso = Math.min(cozinheiro.getProgressoPrato(), 100);
            g.setColor(new Color(255, 255, 255, 200));
            g.fillRoundRect(120, 8, 180, 20, 10, 10);
            g.setColor(new Color(67, 160, 71));
            g.fillRoundRect(120, 8, (int) (180 * progresso / 100.0), 20, 10, 10);
            g.setColor(new Color(33, 33, 33));
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.drawString("Prato: " + progresso + "%", 180, 23);
        }
    }

    private void desenharSofa(Graphics2D g) {
        g.setColor(new Color(0, 121, 107));
        g.fillRoundRect(40, 285, 140, 30, 14, 14);   // encosto
        g.setColor(new Color(38, 166, 154));
        g.fillRoundRect(40, 312, 140, 40, 12, 12);   // assento
        g.setColor(new Color(0, 105, 92));
        g.fillRoundRect(30, 295, 18, 60, 10, 10);    // braços
        g.fillRoundRect(172, 295, 18, 60, 10, 10);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.setColor(new Color(69, 90, 100));
        g.drawString("descanso", 86, 372);
    }

    private void desenharBalcao(Graphics2D g, double t) {
        g.setColor(new Color(121, 85, 72));
        g.fillRect(398, 112, 44, 232);
        g.setColor(new Color(161, 136, 127));
        g.fillRoundRect(394, 106, 52, 236, 10, 10);
        g.setColor(new Color(93, 64, 55));
        g.drawRoundRect(394, 106, 52, 236, 10, 10);
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.setColor(new Color(62, 39, 35));
        g.drawString("BALCÃO", 397, 98);

        // Pedidos (papeizinhos) — "pulam" quando aparecem
        for (int i = 0; i < balcao.getPedidos(); i++) {
            double escala = pulo(t - pedidoApareceuEm);
            desenharPedido(g, 420, 160 + i * 8, escala);
        }
        // Pratos prontos
        for (int i = 0; i < balcao.getPratos(); i++) {
            double escala = pulo(t - pratoApareceuEm);
            desenharPrato(g, 420, 270 - i * 10, escala);
        }
    }

    // Efeito de "pulo": começa grande e volta ao tamanho normal
    private double pulo(double tempo) {
        if (tempo < 0 || tempo > 0.4) {
            return 1.0;
        }
        return 1.0 + 0.6 * (1 - tempo / 0.4);
    }

    private void desenharPedido(Graphics2D g, double x, double y, double escala) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.scale(escala, escala);
        g2.rotate(-0.15);
        g2.setColor(Color.WHITE);
        g2.fillRect(-11, -14, 22, 28);
        g2.setColor(new Color(144, 164, 174));
        g2.drawRect(-11, -14, 22, 28);
        for (int i = 0; i < 4; i++) {
            g2.drawLine(-7, -8 + i * 6, 7, -8 + i * 6);
        }
        g2.dispose();
    }

    private void desenharPrato(Graphics2D g, double x, double y, double escala) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.scale(escala, escala);
        g2.setColor(Color.WHITE);
        g2.fillOval(-18, -9, 36, 18);
        g2.setColor(new Color(158, 158, 158));
        g2.drawOval(-18, -9, 36, 18);
        g2.setColor(new Color(255, 112, 67));
        g2.fillOval(-10, -8, 14, 10);
        g2.setColor(new Color(102, 187, 106));
        g2.fillOval(1, -7, 10, 8);
        g2.dispose();
    }

    private void desenharMesa(Graphics2D g, double t) {
        // Cadeira + cliente
        g.setColor(new Color(121, 85, 72));
        g.fillRoundRect(748, 222, 40, 50, 8, 8);
        g.setColor(new Color(66, 165, 245));
        g.fillRoundRect(748, 246, 26, 30, 10, 10);
        g.setColor(new Color(255, 213, 170));
        g.fillOval(746, 216, 30, 30);
        g.setColor(new Color(93, 64, 55));
        g.fillArc(746, 214, 30, 20, 0, 180);

        boolean comendo = (t - pratoNaMesaEm) < 4.0;
        g.setColor(new Color(62, 39, 35));
        g.fillOval(754, 229, 4, 4);
        g.fillOval(764, 229, 4, 4);
        g.setStroke(new BasicStroke(1.8f));
        if (comendo) {
            g.drawArc(754, 232, 14, 8, 180, 180); // sorriso
        } else {
            g.drawLine(756, 238, 766, 238);
        }
        g.setStroke(new BasicStroke(1));

        // Mesa
        g.setColor(new Color(141, 110, 99));
        g.fillOval(640, 200, 100, 100);
        g.setColor(new Color(255, 248, 225));
        g.fillOval(648, 208, 84, 84);

        if (comendo) {
            desenharPrato(g, 700, 250, 1.1);
            g.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g.setColor(new Color(46, 125, 50));
            g.drawString("Delícia!", 738, 206 - (int) (Math.sin(t * 4) * 3));
        }
    }

    // Desenha um bonequinho. "chef" = cozinheiro (chapéu branco); senão garçom (colete e gravata).
    private void desenharPessoa(Graphics2D g, double x, double y, double t, boolean andando,
                                boolean chef, boolean dormindo) {
        int ix = (int) x;
        int iy = (int) (y - (andando ? Math.abs(Math.sin(t * 14)) * 3 : 0));
        int perna = andando ? (int) (Math.sin(t * 14) * 5) : 0;

        // Sombra
        g.setColor(new Color(0, 0, 0, 40));
        g.fillOval(ix - 18, (int) y + 34, 36, 10);

        // Pernas
        g.setColor(new Color(55, 71, 79));
        g.fillRoundRect(ix - 10 + perna, iy + 24, 8, 16, 4, 4);
        g.fillRoundRect(ix + 2 - perna, iy + 24, 8, 16, 4, 4);

        // Corpo
        if (chef) {
            g.setColor(Color.WHITE);
            g.fillRoundRect(ix - 17, iy - 10, 34, 38, 12, 12);
            g.setColor(new Color(176, 190, 197));
            g.drawRoundRect(ix - 17, iy - 10, 34, 38, 12, 12);
            g.setColor(new Color(120, 144, 156));
            g.fillOval(ix - 2, iy, 4, 4);
            g.fillOval(ix - 2, iy + 10, 4, 4);
        } else {
            g.setColor(new Color(38, 50, 56));
            g.fillRoundRect(ix - 17, iy - 10, 34, 38, 12, 12);
            g.setColor(Color.WHITE);
            g.fillPolygon(new int[]{ix - 7, ix + 7, ix}, new int[]{iy - 10, iy - 10, iy + 10}, 3);
            g.setColor(new Color(211, 47, 47));
            g.fillPolygon(new int[]{ix - 6, ix, ix - 6}, new int[]{iy - 10, iy - 7, iy - 4}, 3);
            g.fillPolygon(new int[]{ix + 6, ix, ix + 6}, new int[]{iy - 10, iy - 7, iy - 4}, 3);
        }

        // Cabeça
        g.setColor(new Color(255, 213, 170));
        g.fillOval(ix - 15, iy - 40, 30, 30);
        g.setColor(new Color(215, 170, 130));
        g.drawOval(ix - 15, iy - 40, 30, 30);

        // Olhos (fechados quando dorme)
        g.setColor(new Color(33, 33, 33));
        if (dormindo) {
            g.drawLine(ix - 8, iy - 27, ix - 3, iy - 27);
            g.drawLine(ix + 3, iy - 27, ix + 8, iy - 27);
        } else {
            g.fillOval(ix - 7, iy - 29, 4, 4);
            g.fillOval(ix + 3, iy - 29, 4, 4);
        }
        g.drawArc(ix - 5, iy - 23, 10, 6, 180, 180);

        // Chapéu de chef ou cabelo
        if (chef) {
            g.setColor(Color.WHITE);
            g.fillRect(ix - 12, iy - 50, 24, 14);
            g.fillOval(ix - 17, iy - 62, 16, 16);
            g.fillOval(ix - 8, iy - 66, 17, 17);
            g.fillOval(ix + 1, iy - 62, 16, 16);
            g.setColor(new Color(207, 216, 220));
            g.drawRect(ix - 12, iy - 50, 24, 14);
        } else {
            g.setColor(new Color(78, 52, 46));
            g.fillArc(ix - 15, iy - 42, 30, 22, 0, 180);
        }
    }

    private void desenharNome(Graphics2D g, String nome, double x, double y) {
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(nome) + 10;
        g.setColor(new Color(255, 255, 255, 200));
        g.fillRoundRect((int) x - w / 2, (int) y + 44, w, 16, 8, 8);
        g.setColor(new Color(33, 33, 33));
        g.drawString(nome, (int) x - w / 2 + 5, (int) y + 56);
    }

    // Bloquinho e lápis mexendo (garçom anotando)
    private void desenharBloquinho(Graphics2D g, double x, double y, double t) {
        int bx = (int) x + 16, by = (int) y - 4;
        g.setColor(Color.WHITE);
        g.fillRect(bx, by, 14, 18);
        g.setColor(new Color(144, 164, 174));
        g.drawRect(bx, by, 14, 18);
        int lapis = (int) (Math.sin(t * 20) * 3);
        g.setColor(new Color(255, 193, 7));
        g.setStroke(new BasicStroke(3));
        g.drawLine(bx + 6 + lapis, by + 10, bx + 18 + lapis, by - 2);
        g.setStroke(new BasicStroke(1));
    }

    // "Z z z" subindo (cozinheiro descansando)
    private void desenharZzz(Graphics2D g, double x, double y, double t) {
        for (int i = 0; i < 3; i++) {
            double fase = (t * 0.7 + i / 3.0) % 1.0;
            int alfa = (int) (255 * (1 - fase));
            g.setColor(new Color(21, 101, 192, alfa));
            g.setFont(new Font("Segoe UI", Font.BOLD, 12 + (int) (fase * 10)));
            g.drawString("Z", (int) (x + 16 + fase * 22), (int) (y - 40 - fase * 40));
        }
    }

    // Texto laranja "Estado A → Estado B" embaixo do personagem quando troca de estado
    private void desenharTransicao(Graphics2D g, String texto, double quando, double x, double y, double t) {
        double idade = t - quando;
        if (idade > 2.0) {
            return;
        }
        float alfa = (float) Math.max(0, Math.min(1, (2.0 - idade) / 0.5));
        Composite antigo = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));

        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(texto) + 14;
        int bx = (int) Math.max(4, Math.min(LARGURA - w - 4, x - w / 2.0));
        int by = (int) y + 64;
        g.setColor(new Color(230, 81, 0));
        g.fillRoundRect(bx, by, w, 20, 10, 10);
        g.setColor(Color.WHITE);
        g.drawString(texto, bx + 7, by + 14);

        g.setComposite(antigo);
    }

    // Balões de fala acima da cabeça (a mais nova fica mais perto da cabeça)
    private void desenharFalas(Graphics2D g, ArrayList<Fala> falas, double x, double y, double t,
                               int minX, int maxX, boolean aoLado) {
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        int nivel = 0;
        for (int i = falas.size() - 1; i >= 0; i--) {
            Fala fala = falas.get(i);
            double idade = t - fala.tempo;
            if (idade > DURACAO_FALA) {
                continue;
            }
            float alfa = (float) Math.min(1, (DURACAO_FALA - idade) / 0.5);
            Composite antigo = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));

            int w = fm.stringWidth(fala.texto) + 16;
            int h = 22;
            int bx, by;
            if (aoLado) {
                // Balões à direita do personagem, um embaixo do outro
                bx = (int) Math.max(minX, Math.min(maxX - w, x + 24));
                by = (int) y - 50 + nivel * 26;
            } else {
                // Balões acima da cabeça, um em cima do outro
                bx = (int) Math.max(minX, Math.min(maxX - w, x - w / 2.0));
                by = (int) y - 100 - nivel * 26;
            }

            g.setColor(Color.WHITE);
            g.fillRoundRect(bx, by, w, h, 12, 12);
            g.setColor(new Color(120, 144, 156));
            g.drawRoundRect(bx, by, w, h, 12, 12);
            if (nivel == 0 && !aoLado) {
                // "Rabinho" do balão apontando para a cabeça
                int rx = (int) Math.max(bx + 10, Math.min(bx + w - 10, x));
                g.setColor(Color.WHITE);
                g.fillPolygon(new int[]{rx - 6, rx + 6, rx}, new int[]{by + h - 1, by + h - 1, by + h + 8}, 3);
                g.setColor(new Color(120, 144, 156));
                g.drawLine(rx - 6, by + h, rx, by + h + 8);
                g.drawLine(rx + 6, by + h, rx, by + h + 8);
            }
            g.setColor(new Color(33, 33, 33));
            g.drawString(fala.texto, bx + 8, by + 15);

            g.setComposite(antigo);
            nivel++;
        }
    }
}
