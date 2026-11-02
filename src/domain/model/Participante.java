package domain.model;

import domain.model.enums.Cargo;
import domain.valueobject.Email;
import domain.valueobject.Name;
import domain.model.enums.NiveisDeAcesso;
import domain.model.enums.StatusParticipante;
import infra.notification.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Participante implements Observer {

    private static Long contador = 2L;

    private Long ID;
    private Name name;
    private Email email;
    private Cargo cargo;
    private NiveisDeAcesso nivelDeAcesso;
    private StatusParticipante status;
    private List<Evento> historicoDeEventos = new ArrayList<>();

    public Participante(Name name, Email email, Cargo cargo, NiveisDeAcesso nivelDeAcesso) {

        this.ID = contador++;

        this.name = validateName(name);
        this.email = validateEmail(email);
        this.nivelDeAcesso = validateNivelDeAcesso(nivelDeAcesso);
        this.cargo = validateCargo(cargo);

        this.status = StatusParticipante.ATIVO;
        this.historicoDeEventos = new ArrayList<>();
    }

    public void adicionarEventoAoHistorico(Evento evento) {

        Objects.requireNonNull(evento, "Erro: evento não pode ser nulo.");
        historicoDeEventos.add(evento);
    }

    public void removerEventoDoHistorico(Evento evento) {

        Objects.requireNonNull(evento, "Erro: evento não pode ser nulo.");

        if(!historicoDeEventos.contains(evento)) {
            throw new IllegalArgumentException("Erro: o participante não está inscrito no evento.");
        }

        historicoDeEventos.remove(evento);
    }

    public void mudarStatus(StatusParticipante statusREF) {
        this.status = statusREF;
    }

    public void alterarNivelDeAcesso(NiveisDeAcesso nivelDeAcesso){
        this.nivelDeAcesso = nivelDeAcesso;
    }

    @Override
    public void reagir(Evento evento) {
        System.out.println("Notificação para o participante, do evento "+evento.getNomeEvento()+", envida com sucesso!");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Participante that = (Participante) o;
        return Objects.equals(ID, that.ID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID);
    }
    public NiveisDeAcesso getNivelDeAcesso(){return this.nivelDeAcesso;}
    public Long getID(){return this.ID;}
    public StatusParticipante getStatus() {return status;}

    private Email validateEmail(Email email) { return Objects.requireNonNull(email, "Erro: o email do participante é nulo.");}
    private Name validateName(Name name) { return Objects.requireNonNull(name, "Erro: nome passado para participante é nulo.");}
    private NiveisDeAcesso validateNivelDeAcesso(NiveisDeAcesso nivelDeAcesso) { return Objects.requireNonNull(nivelDeAcesso, "Erro: nível de acesso do participante é nulo.");}
    private Cargo validateCargo(Cargo cargo) { return Objects.requireNonNull(cargo, "Erro: cargo do participante é nulo.");}
}
