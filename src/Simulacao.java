// Classe responsável por gerenciar a execução dos agentes:
// cria o cenário e roda o loop principal (o "game loop").
public class Simulacao {
    // Quantas voltas (ticks) o loop vai rodar
    private static final int TOTAL_TICKS = 30;
    // Pausa entre ticks em milissegundos (0 = sem pausa)
    private static final int PAUSA_MS = 300;

    public static void main(String[] args) {
        System.out.println("=== Abrindo o restaurante ===");

        // Cria o balcão compartilhado e os dois agentes
        Balcao balcao = new Balcao();
        Cozinheiro cozinheiro = new Cozinheiro("Bob", balcao);
        Garcom garcom = new Garcom("Juca", balcao);

        // Loop principal
        int tick = 1;
        while (tick <= TOTAL_TICKS) {
            System.out.println();
            System.out.println("==================== TICK " + tick + " ====================");

            // Cada agente executa UM passo do seu estado atual
            cozinheiro.atualizar();
            garcom.atualizar();

            // Pausa para dar tempo de ler o console
            try {
                Thread.sleep(PAUSA_MS);
            } catch (InterruptedException e) {
                return;
            }

            tick++;
        }

        System.out.println();
        System.out.println("=== Restaurante fechado! Pratos entregues: "
                + garcom.getPratosEntregues() + " ===");
    }
}
