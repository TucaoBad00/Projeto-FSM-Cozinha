import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

// Versão VISUAL da simulação (Java Swing, que já vem no Java — nada externo).
// Ela faz o mesmo papel da classe Simulacao: cria os agentes e roda o loop,
// mas em vez de um "while" usa um Timer que chama avancarTick() de tempos em tempos.
// Os agentes e estados são EXATAMENTE os mesmos do modo console.
public class SimulacaoVisual {
    // Tempo entre ticks na velocidade 1x (milissegundos)
    private static final int INTERVALO_BASE_MS = 1000;

    // ----- Simulação -----
    private Balcao balcao;
    private Cozinheiro cozinheiro;
    private Garcom garcom;
    private int tick = 0;

    // ----- Tela -----
    private final JFrame janela = new JFrame("Máquina de Estados — Cozinha do Restaurante");
    private final PainelCozinha painelCozinha = new PainelCozinha();
    private final PainelVariaveis painelVariaveis = new PainelVariaveis();
    private final PainelLog painelLog = new PainelLog();
    private final PainelDiagrama diagramaCozinheiro = new PainelDiagrama(
            "Agente A — Cozinheiro",
            new String[]{"EsperandoPedido", "Cozinhando", "Descansando"},
            new int[][]{{0, 1}, {1, 0}, {1, 2}, {2, 0}},
            new String[]{"tem pedido", "pronto, cansaço < 60", "pronto, cansaço ≥ 60", "cansaço = 0"},
            new Color(230, 81, 0));
    private final PainelDiagrama diagramaGarcom = new PainelDiagrama(
            "Agente B — Garçom",
            new String[]{"AnotandoPedido", "AguardandoPrato", "ServindoPrato"},
            new int[][]{{0, 1}, {1, 2}, {2, 0}},
            new String[]{"tempo ≥ 2", "tem prato no balcão", "serviu"},
            new Color(106, 27, 154));

    private final JButton botaoPlay = new JButton("Pausar");
    private final JLabel rotuloVelocidade = new JLabel("1.0x");

    // Timer dos ticks (o "loop principal") e timer da animação (~60 quadros/s)
    private final Timer timerTicks = new Timer(INTERVALO_BASE_MS, e -> avancarTick());
    private final Timer timerAnimacao = new Timer(16, e -> animar());

    public SimulacaoVisual() {
        montarJanela();
        // A partir daqui, todo System.out.println também aparece na tela
        CapturaConsole.instalar(this::receberLinha);
        reiniciar();
    }

    public static void main(String[] args) {
        // Toda tela Swing deve ser criada na thread da interface
        SwingUtilities.invokeLater(() -> new SimulacaoVisual().mostrar());
    }

    public void mostrar() {
        janela.setVisible(true);
        timerAnimacao.start();
        timerTicks.start();
    }

    // ---------------- Lógica da simulação ----------------

    // Cria balcão e agentes do zero (botão Reiniciar)
    private void reiniciar() {
        painelLog.limpar();
        tick = 0;
        System.out.println("=== Abrindo o restaurante ===");
        balcao = new Balcao();
        cozinheiro = new Cozinheiro("Bob", balcao);
        garcom = new Garcom("Juca", balcao);

        painelCozinha.setAgentes(cozinheiro, garcom, balcao);
        painelVariaveis.setAgentes(cozinheiro, garcom, balcao);
        diagramaCozinheiro.reiniciar(cozinheiro.getNomeEstado());
        diagramaGarcom.reiniciar(garcom.getNomeEstado());
    }

    // Um passo do loop principal: igual ao corpo do "while" da classe Simulacao
    private void avancarTick() {
        tick++;
        System.out.println();
        System.out.println("==================== TICK " + tick + " ====================");

        // Guarda os estados de antes para descobrir se houve transição
        String cozinheiroAntes = cozinheiro.getNomeEstado();
        String garcomAntes = garcom.getNomeEstado();

        // Cada agente executa UM passo do seu estado atual
        cozinheiro.atualizar();
        garcom.atualizar();

        String cozinheiroDepois = cozinheiro.getNomeEstado();
        String garcomDepois = garcom.getNomeEstado();

        // Avisa a tela sobre o que aconteceu neste tick
        diagramaCozinheiro.registrarTick(tick, cozinheiroAntes, cozinheiroDepois);
        diagramaGarcom.registrarTick(tick, garcomAntes, garcomDepois);
        if (!cozinheiroAntes.equals(cozinheiroDepois)) {
            painelCozinha.mostrarTransicao(true, cozinheiroAntes + " → " + cozinheiroDepois);
        }
        if (!garcomAntes.equals(garcomDepois)) {
            painelCozinha.mostrarTransicao(false, garcomAntes + " → " + garcomDepois);
        }
        painelVariaveis.setTick(tick);
    }

    // Cada linha impressa vai para o log e para os balões de fala
    private void receberLinha(String linha) {
        painelLog.adicionar(linha);
        painelCozinha.receberMensagem(linha);
    }

    private void animar() {
        painelCozinha.animar();
        painelVariaveis.animar();
        diagramaCozinheiro.repaint();
        diagramaGarcom.repaint();
    }

    // ---------------- Montagem da janela ----------------

    private void montarJanela() {
        // Barra de controles (topo)
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        JButton botaoPasso = new JButton("Próximo tick");
        JButton botaoReiniciar = new JButton("Reiniciar");
        JSlider velocidade = new JSlider(1, 8, 2); // valor / 2 = fator (0.5x a 4x)

        botaoPlay.addActionListener(e -> alternarPlay());
        botaoPasso.addActionListener(e -> {
            pausar();          // passo a passo: pausa e avança só 1 tick
            avancarTick();
        });
        botaoReiniciar.addActionListener(e -> reiniciar());
        velocidade.addChangeListener(e -> mudarVelocidade(velocidade.getValue() / 2.0));
        velocidade.setPreferredSize(new Dimension(160, 30));

        JLabel dica = new JLabel("   Siga os estados no diagrama, as barras e o log ao mesmo tempo.");
        dica.setForeground(Color.GRAY);

        controles.add(botaoPlay);
        controles.add(botaoPasso);
        controles.add(botaoReiniciar);
        controles.add(new JLabel("   Velocidade:"));
        controles.add(velocidade);
        controles.add(rotuloVelocidade);
        controles.add(dica);

        // Centro: cena em cima, diagramas embaixo
        JPanel diagramas = new JPanel(new GridLayout(1, 2, 6, 0));
        diagramas.add(diagramaCozinheiro);
        diagramas.add(diagramaGarcom);
        diagramas.setPreferredSize(new Dimension(840, 250));

        JPanel centro = new JPanel(new BorderLayout(0, 6));
        centro.add(painelCozinha, BorderLayout.CENTER);
        centro.add(diagramas, BorderLayout.SOUTH);

        // Direita: variáveis em cima, log embaixo
        painelVariaveis.setBorder(BorderFactory.createTitledBorder("Variáveis e limiares"));
        JPanel direita = new JPanel(new BorderLayout(0, 6));
        direita.add(painelVariaveis, BorderLayout.NORTH);
        direita.add(painelLog, BorderLayout.CENTER);
        direita.setPreferredSize(new Dimension(440, 800));

        JPanel conteudo = new JPanel(new BorderLayout(6, 6));
        conteudo.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));
        conteudo.add(controles, BorderLayout.NORTH);
        conteudo.add(centro, BorderLayout.CENTER);
        conteudo.add(direita, BorderLayout.EAST);

        janela.setContentPane(conteudo);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setSize(1320, 860);
        janela.setLocationRelativeTo(null);
    }

    private void alternarPlay() {
        if (timerTicks.isRunning()) {
            pausar();
        } else {
            timerTicks.start();
            botaoPlay.setText("Pausar");
        }
    }

    private void pausar() {
        timerTicks.stop();
        botaoPlay.setText("Continuar");
    }

    private void mudarVelocidade(double fator) {
        timerTicks.setDelay((int) (INTERVALO_BASE_MS / fator));
        painelCozinha.setFatorVelocidade(fator);
        rotuloVelocidade.setText(fator + "x");
    }
}
