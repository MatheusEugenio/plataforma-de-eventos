package domain.model;

import domain.exception.ParticipanteInvalidoException;
import domain.model.enums.NiveisDeAcesso;
import domain.model.enums.StatusParticipante;
import domain.valueobject.IntervaloDeTempo;
import domain.valueobject.Name;
import domain.policy.PoliticaCancelamento;
import domain.model.enums.TipoEvento;
import domain.model.enums.StatusEvento;
import domain.exception.ConflitoHorarioException;
import domain.policy.FactoryPoliticaCancelamento;

import java.time.LocalDateTime;
import java.util.*;

public class Evento {

    private static Long contador = 1L;

    private Long ID;
    private Name nomeEvento;
    private String descricao;
    private TipoEvento tipo;
    private IntervaloDeTempo dataDeDuracaoDoEvento;
    private StatusEvento status;
    private int capacidadeMaxima;
    private PoliticaCancelamento politicaCancelamento;

    private Set<Participante> listaDeInscritos;
    private Queue<Participante> listaDeEspera;
    private List<Palestra> listaDePalestras;

    public Evento(Builder builder) {

        this.ID = contador++;

        this.nomeEvento = builder.nomeEvento;
        this.descricao = builder.descricao;
        this.tipo = builder.tipo;
        this.dataDeDuracaoDoEvento = builder.dataDeDuracaoDoEvento;
        this.capacidadeMaxima = builder.capacidadeMaxima;
        this.politicaCancelamento = FactoryPoliticaCancelamento.createPoliticaCancelamento(this.tipo);

        this.status = StatusEvento.PLANEJADO;

        this.listaDeInscritos = new HashSet<>();
        this.listaDePalestras = new ArrayList<>();
        this.listaDeEspera = new LinkedList<>();
    }

    public void confirmar(){

        if (this.status == StatusEvento.FINALIZADO){
            throw new IllegalArgumentException("Erro: o evento já finalizado.");
        }

        if (this.status == StatusEvento.CANCELADO){
            throw new IllegalArgumentException("Erro: o evento está cancelado.");
        }

        this.status = StatusEvento.CONFIRMADO;
    }

    public void cancelar(){

        if (this.status == StatusEvento.FINALIZADO){
            throw new IllegalArgumentException("Erro: o evento já finalizado.");
        }

        if (this.status == StatusEvento.CANCELADO){
            throw new IllegalArgumentException("Erro: o evento já está cancelado.");
        }

        politicaCancelamento.executarPolitica(this);
        this.status = StatusEvento.CANCELADO;
    }

    public synchronized void inscreverParticipanteNoEvento(Participante participante){

        Objects.requireNonNull(participante, "Erro: o participante é nulo");

        if (status == StatusEvento.CANCELADO || status == StatusEvento.FINALIZADO) {
            throw new IllegalStateException("Não é possível inscrever participantes em um evento cancelado ou finalizado.");
        }

        if (participante.getStatus() != StatusParticipante.ATIVO) {
            throw new ParticipanteInvalidoException("Erro: participantes inativos não podem se inscrever.");
        }

        if (listaDePalestras.isEmpty()) {
            throw new IllegalArgumentException("Erro: não haverá palestras neste evento.");
        }

        if (listaDeInscritos.contains(participante) || listaDeEspera.contains(participante)){
            throw new ParticipanteInvalidoException("Não pode inscrever participantes repetidos!");
        }

        boolean podeParticipar = validarNivelDeAcessoDoParticipante(participante.getNivelDeAcesso());

        if (!podeParticipar){
            throw new ParticipanteInvalidoException("Erro: o participante não pode participar pois não possui nível de acesso esperado para o evento. "+participante.getNivelDeAcesso()+" < "+ tipo);
        }

        if (listaDeInscritos.size() >= capacidadeMaxima){
            System.out.println("A lista de participantes já está lotada! O participante '"+participante+"' foi movido para a lista de espera.");
            listaDeEspera.add(participante);
            return;
        }

        participante.adicionarEventoAoHistorico(this);
        this.listaDeInscritos.add(participante);
    }

    public synchronized void cancelarInscricaoDoParticipanteNoEvento(Participante participante){
        Objects.requireNonNull(participante, "Erro: o participante é nulo.");

        if (listaDeEspera.remove(participante)) {
            return;
        }

        if (!this.listaDeInscritos.contains(participante)){
            throw new IllegalArgumentException("Erro: particpante não está inscrito.");
        }

        participante.removerEventoDoHistorico(this);
        this.listaDeInscritos.remove(participante);
        promoverProximoDaListaDeEspera();
    }

    public void adicionarPalestra(Palestra palestra){
        Objects.requireNonNull(palestra, "Erro: a palestra não pode ser nula.");

        if (listaDePalestras.contains(palestra)){
            throw new IllegalArgumentException("Erro: a palestra já está na cadastrada.");
        }

        boolean possuiPalestraNoMesmoHorario = this.listaDePalestras.stream()
                .anyMatch(p -> p.conflitaComAlgumaPalestra(palestra.getHorario()));

        if (possuiPalestraNoMesmoHorario){
            throw new ConflitoHorarioException("Erro: já possui uma palestra nesse horário.");
        }

        this.listaDePalestras.add(palestra);
    }

    public void alterarData(IntervaloDeTempo novaData){
        Objects.requireNonNull(novaData, "Erro: a nova data é nula.");
        this.dataDeDuracaoDoEvento = new IntervaloDeTempo(novaData.getInicio(), novaData.getFim());
    }

    private boolean validarNivelDeAcessoDoParticipante(NiveisDeAcesso nivelDeAcessoDoParticipante){

        if (nivelDeAcessoDoParticipante == NiveisDeAcesso.PARTICIPANTE  && tipo == TipoEvento.EVENTO_PAGO){ //exceção
            return true;
        }

        if (nivelDeAcessoDoParticipante.getPeso() < tipo.getPeso()){
            return false;
        }

        return true;
    }

    private void promoverProximoDaListaDeEspera(){
        while (!listaDeEspera.isEmpty()) {
            Participante proximo = listaDeEspera.peek();
            if (proximo.getStatus() != StatusParticipante.ATIVO
                    || !validarNivelDeAcessoDoParticipante(proximo.getNivelDeAcesso())) {
                listaDeEspera.remove();
                continue;
            }

            listaDeEspera.remove();
            proximo.adicionarEventoAoHistorico(this);
            listaDeInscritos.add(proximo);
            return;
        }
    }

    public String getNomeEvento() {return nomeEvento.getValue();}
    public Long getID(){return ID;}
    public String getDescricao() {return descricao;}
    public TipoEvento getTipo() {return tipo;}
    public StatusEvento getStatus() {return status;}
    public int getCapacidadeMaxima() {return capacidadeMaxima;}
    public Set<Participante> getParticipantesInscritos() {
        return Collections.unmodifiableSet(new HashSet<>(listaDeInscritos));
    }
    public List<Participante> getListaDeEspera() {
        return Collections.unmodifiableList(new ArrayList<>(listaDeEspera));
    }
    public List<Palestra> getPalestras() {
        return Collections.unmodifiableList(new ArrayList<>(listaDePalestras));
    }
    public void setStatus(StatusEvento status){this.status = Objects.requireNonNull(status);}
    public LocalDateTime getHorarioDeComecoDoEvento(){return this.dataDeDuracaoDoEvento.getInicio();}

    public static class Builder {

        private Name nomeEvento;
        private String descricao;
        private TipoEvento tipo;
        private IntervaloDeTempo dataDeDuracaoDoEvento;
        private int capacidadeMaxima;

        public Builder nomeEvento(Name nomeEvento){
            Objects.requireNonNull(nomeEvento);
            this.nomeEvento = nomeEvento;
            return this;
        }

        public Builder descricao(String descricao){
            Objects.requireNonNull(descricao);
            this.descricao = descricao;
            return this;
        }

        public Builder tipo(TipoEvento tipo){
            Objects.requireNonNull(tipo);
            this.tipo = tipo;
            return this;
        }

        public Builder dataDeDuracao(IntervaloDeTempo data){
            Objects.requireNonNull(data);
            this.dataDeDuracaoDoEvento = data;
            return this;
        }

        public Builder capacidadeMaxima(int capacidade){

            if (capacidade <= 0){
                throw new IllegalArgumentException("Erro: capacidade deve ser maior que zero.");
            }

            this.capacidadeMaxima = capacidade;
            return this;
        }

        public Evento build(){
            Objects.requireNonNull(nomeEvento, "Erro: o nome do evento é obrigatório.");
            Objects.requireNonNull(descricao, "Erro: a descrição do evento é obrigatória.");
            Objects.requireNonNull(tipo, "Erro: o tipo do evento é obrigatório.");
            Objects.requireNonNull(dataDeDuracaoDoEvento, "Erro: o período do evento é obrigatório.");
            if (capacidadeMaxima <= 0) {
                throw new IllegalArgumentException("Erro: capacidade deve ser maior que zero.");
            }
            return new Evento(this);
        }
    }
}