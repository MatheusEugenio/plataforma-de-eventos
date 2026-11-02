package infra.repository;

import domain.model.Participante;

import java.util.*;

public class ParticipanteRepository {

    private Map<Long, Participante> participantesCadastrados;

    public ParticipanteRepository() {
        this.participantesCadastrados = new HashMap<>();
    }

    public void save(Participante participante){

        Objects.requireNonNull(participante);

        participantesCadastrados.put(participante.getID(), participante);
    }

    public Participante findById(Long id){

        validarId(id);

        if (!participantesCadastrados.containsKey(id)){
            throw new IllegalArgumentException("Erro: esse Id não existe no banco.");
        }

        return participantesCadastrados.get(id);
    }

    public boolean existsById(Long id) {
        validarId(id);
        return participantesCadastrados.containsKey(id);
    }

    public List<Participante> findAll(){
        return new ArrayList<>(participantesCadastrados.values());
    }

    private void validarId(Long id) {
        Objects.requireNonNull(id, "Erro: o id não pode ser nulo.");
        if (id <= 0) {
            throw new IllegalArgumentException("Erro: o id deve ser maior que zero.");
        }
    }

}
