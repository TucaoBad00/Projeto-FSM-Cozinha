// Estado do GARÇOM: levando o prato até a mesa.
public class ServindoPrato implements Estado<Garcom> {

    @Override
    public void entrar(Garcom garcom) {
        garcom.falar("Levando o prato até a mesa.");
    }

    @Override
    public void executar(Garcom garcom) {
        // Ação: servir (contador de pratos +1)
        garcom.servirPrato();
        garcom.falar("Bom apetite! | Pratos entregues: " + garcom.getPratosEntregues());

        // Servir leva 1 tick; depois volta a atender outra mesa
        garcom.mudarEstado(new AnotandoPedido());
    }

    @Override
    public void sair(Garcom garcom) {
        garcom.falar("Mesa servida. Próximo cliente!");
    }
}
