// Estado do GARÇOM: esperando o cozinheiro terminar o prato.
// Aqui está a dependência entre os agentes (bônus):
// o garçom só sai deste estado quando o COZINHEIRO coloca um prato no balcão.
public class AguardandoPrato implements Estado<Garcom> {

    @Override
    public void entrar(Garcom garcom) {
        garcom.falar("Aguardando o prato ficar pronto.");
    }

    @Override
    public void executar(Garcom garcom) {
        // Condição de saída: tem prato pronto no balcão?
        if (garcom.getBalcao().temPrato())
        {
            garcom.getBalcao().pegarPrato();
            garcom.mudarEstado(new ServindoPrato());
        }
        else 
        {
            garcom.falar("Esperando no balcão...");
        }
    }

    @Override
    public void sair(Garcom garcom) {
        garcom.falar("Peguei o prato no balcão!");
    }
}
