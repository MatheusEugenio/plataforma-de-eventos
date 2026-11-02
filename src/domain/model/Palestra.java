package domain.model;

import domain.valueobject.IntervaloDeTempo;
import domain.valueobject.Name;
import domain.model.enums.StatusPalestra;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Palestra {

    public static Long contador = 1L;

    private Long ID;
    private Sala salaDaPalestra;
    private IntervaloDeTempo horario;
    private Palestrante palestrante;
    private Name nome;
    private int limiteDeParticipantes;
    private StatusPalestra status;
    private Set<Participante> inscritosNaPalestra;

    public Palestra(Builder builder) {

        this.ID = contador++;

        this.salaDaPalestra = validateSala(builder.salaDaPalestra);
        this.horario = validateDuracao(builder.horario);
        this.palestrante = validatePalestrante(builder.palestrante);
        this.nome = validateName(builder.nome);

        this.limiteDeParticipantes = salaDaPalestra.getCapacidadeDaSala();
        validateCapacidade(this.limiteDeParticipantes);

        this.status = StatusPalestra.VAZIA;
        this.inscritosNaPalestra = new HashSet<>();
    }

    public void usarSala(){
        mudarStatus(StatusPalestra.EM_ANDAMENTO);
    }

    public void inscreverNaPalestra(Participante participante){
        Objects.requireNonNull(participante, "Erro: o participante é nulo.");

        if (status == StatusPalestra.EM_ANDAMENTO || status == StatusPalestra.FINALIZADA) {
            throw new IllegalStateException("Não é possível inscrever participantes em uma palestra iniciada ou finalizada.");
        }

        if (this.inscritosNaPalestra.contains(participante)) {
            throw new IllegalArgumentException("O participante já está inscrito na palestra!");
        }

        if (this.inscritosNaPalestra.size() >= limiteDeParticipantes){
            throw new IllegalArgumentException("A palestra já está lotada!");
        }

        this.inscritosNaPalestra.add(participante);
        this.status = this.inscritosNaPalestra.size() == limiteDeParticipantes
                ? StatusPalestra.LOTADA : StatusPalestra.PREENCHENDO;
    }

    public void mudarStatus(StatusPalestra status){this.status = Objects.requireNonNull(status);}

    public boolean conflitaComAlgumaPalestra(IntervaloDeTempo intervaloNovo){
        return horario.conflitaCom(intervaloNovo);
    }

    public boolean horariEstaDisponivel(IntervaloDeTempo horarioEstabelecido){
        return horario.contem(horarioEstabelecido);
    }

    public Set<Participante> getInscritosNaPalestra() {
        return Collections.unmodifiableSet(new HashSet<>(inscritosNaPalestra));
    }
    public IntervaloDeTempo getHorario() {return horario;}
    public StatusPalestra getStatus() {return status;}

    private Sala validateSala(Sala sala){return Objects.requireNonNull(sala, "Erro: o objeto de sala é nulo.");}
    private IntervaloDeTempo validateDuracao(IntervaloDeTempo tempo){ return Objects.requireNonNull(tempo, "Erro: o tempo é nulo.");}
    private Palestrante validatePalestrante(Palestrante palestrante){ return Objects.requireNonNull(palestrante, "Erro: o palestrante é nulo.");}
    private Name validateName(Name name) { return Objects.requireNonNull(name, "Erro: nome passado para participante é nulo.");}
    private void validateCapacidade(int capacidadeDaSala) {if (capacidadeDaSala <= 0){ throw new IllegalArgumentException("Erro: o número máximo de participantes da palestra não pode ser menor ou igual a 0."); }}

    public static class Builder {

        private Sala salaDaPalestra;
        private IntervaloDeTempo horario;
        private Palestrante palestrante;
        private Name nome;
        private Long ID;

        public Builder salaDaPalestra(Sala salaDaPalestra) {

            this.salaDaPalestra = salaDaPalestra;
            return this;
        }

        public Builder horarioDaPalestra(IntervaloDeTempo horario) {

            this.horario= horario;
            return this;
        }

        public Builder palestrante(Palestrante palestrante) {

            this.palestrante = palestrante;
            return this;
        }

        public Builder nome(Name name) {

            this.nome = name;
            return this;
        }

        public Palestra build() {
            return new Palestra(this);
        }
    }
}