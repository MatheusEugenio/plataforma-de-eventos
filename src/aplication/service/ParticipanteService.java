package aplication.service;

import domain.exception.ParticipanteInvalidoException;
import domain.model.Participante;
import domain.model.enums.Cargo;
import domain.model.enums.NiveisDeAcesso;
import domain.model.enums.StatusParticipante;
import domain.valueobject.Email;
import domain.valueobject.Name;
import infra.repository.ParticipanteRepository;

import java.util.List;

public class ParticipanteService {

    private ParticipanteRepository participanteRepository;

    public ParticipanteService(ParticipanteRepository participanteRepository) {
        this.participanteRepository = participanteRepository;
    }

    public void cadastrarParticipante(Name name, Email email, Cargo cargo, NiveisDeAcesso nivelDeAcesso) {

        Participante novoParticipante = new Participante(name, email, cargo, nivelDeAcesso);

        participanteRepository.save(novoParticipante);
    }

    public Participante buscarParticipante(Long IDParticipante){

        if (!participanteExiste(IDParticipante)){
            throw new ParticipanteInvalidoException("Erro: participante não existe.");
        }

        Participante participanteEncotrado = participanteRepository.findById(IDParticipante);

        return participanteEncotrado;
    }

    public void ativarParticipante(Long idParticipante) {

        Participante participanteAlvo = participanteRepository.findById(idParticipante);

        if (participanteAlvo.getStatus().equals(StatusParticipante.ATIVO)) {
            System.out.println("Participante já está ativo.");
            return;
        }

        participanteAlvo.mudarStatus(StatusParticipante.ATIVO);
        participanteRepository.save(participanteAlvo);
    }

    public void inativarParticipar(Long IDParticipante) {

        Participante participanteAlvo = participanteRepository.findById(IDParticipante);

        if (participanteAlvo.getStatus().equals(StatusParticipante.INATIVO)){
            System.out.println("Participante já está inativo.");
            return;
        }

        participanteAlvo.mudarStatus(StatusParticipante.INATIVO);
        participanteRepository.save(participanteAlvo);
    }

    public void alterarNivelDeAcesso(Long idDoParticipante, NiveisDeAcesso nivelDeAcesso){

        Participante participanteAlvo = participanteRepository.findById(idDoParticipante);

        participanteAlvo.alterarNivelDeAcesso(nivelDeAcesso);
        participanteRepository.save(participanteAlvo);
        //notificar
    }

    public void listarParticipantes(){

        List<Participante> participantesCadastrados = participanteRepository.findAll();

        participantesCadastrados.forEach(System.out::println);
    }

    public boolean participanteExiste(Long IDParticipante){
        return participanteRepository.existsById(IDParticipante);
    }
}
