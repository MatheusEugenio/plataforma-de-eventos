package domain.model;

import domain.valueobject.Name;
import domain.valueobject.Recurso;
import domain.model.enums.StatusDaSala;

import java.util.List;
import java.util.Objects;

public class Sala {

    private Long ID;
    private Name nome;
    private int capacidadeDaSala;
    private List<Recurso> recursos;
    private StatusDaSala status;

    public Sala(int capacidadeDaSala, Name nome, Long ID) {
        validateID(ID);
        validateName(nome);
        validadeCapacidade(capacidadeDaSala);
        this.capacidadeDaSala = capacidadeDaSala;
        this.nome = nome;
        this.ID = ID;
        this.status = StatusDaSala.DESOCUPADA;
    }

    public void ocuparSala(){

        if (this.status != StatusDaSala.DESOCUPADA){
            throw new IllegalArgumentException("A sala já está ocupada.");
        }
        this.status = StatusDaSala.OCUPADA;
    }

    public void usarSala(){

        if (this.status != StatusDaSala.OCUPADA){
            throw new IllegalArgumentException("A sala precisa estar ocupada antes de ser usada.");
        }
        this.status = StatusDaSala.EM_USO;
    }

    public void finalizarUso(){

        if (status != StatusDaSala.EM_USO){
            throw new IllegalArgumentException("Erro: impossível finalizar uso da sala pois a sala não está sendo usada.");
        }
        this.status = StatusDaSala.DESOCUPADA;
    }

    public int getCapacidadeDaSala() {return capacidadeDaSala;}
    public StatusDaSala getStatus() {return status;}
    private void validateID(Long id) {Objects.requireNonNull(id, "Erro, ID é não pode ser nulo.");}
    private void validateName(Name name) {Objects.requireNonNull(name, "Erro: nome passado para participante é nulo.");}
    private void validadeCapacidade(int capacidadeDaSala) {if (capacidadeDaSala <= 0){ throw new IllegalArgumentException("Erro: a capacidade máxima da sala não pode ser menor ou igual a 0."); }}
}
