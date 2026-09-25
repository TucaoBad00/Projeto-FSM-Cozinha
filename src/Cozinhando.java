// Estado do COZINHEIRO: preparando um prato.
public class Cozinhando implements Estado<Cozinheiro> {

    @Override
    public void entrar(Cozinheiro cozinheiro) {
        // Todo prato novo começa do zero
        cozinheiro.setProgressoPrato(0);
        cozinheiro.falar("Acendendo o fogão. Hora de cozinhar!");
    }

    @Override
    public void executar(Cozinheiro cozinheiro) {
        // Ação: cozinhar (progresso +25 e cansaço +10)
        cozinheiro.cozinhar();
        cozinheiro.falar("Cozinhando... | Progresso: " + cozinheiro.getProgressoPrato()
                + "% | Cansaço: " + cozinheiro.getCansaco());

        // Condição de saída: o prato ficou pronto?
        if (cozinheiro.getProgressoPrato() >= Cozinheiro.PROGRESSO_PRATO_PRONTO) {
            // Avisa o garçom colocando o prato no balcão (comunicação)
            cozinheiro.getBalcao().colocarPrato();

            // Para onde ir depois: descansar ou esperar outro pedido?
            if (cozinheiro.getCansaco() >= Cozinheiro.LIMITE_CANSACO) {
                cozinheiro.mudarEstado(new Descansando());
            } else {
                cozinheiro.mudarEstado(new EsperandoPedido());
            }
        }
    }

    @Override
    public void sair(Cozinheiro cozinheiro) {
        cozinheiro.falar("Prato finalizado. Desligando o fogão.");
    }
}
