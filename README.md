# 🎟️ Plataforma de Eventos

> Uma base Java para modelar a organização de eventos, palestras, participantes e suas regras de negócio.

[Visão geral](#visão-geral) · [Funcionalidades](#funcionalidades) · [Arquitetura](#arquitetura) · [Como executar](#como-executar) · [Estado atual](#estado-atual) · [Estrutura do projeto](#estrutura-do-projeto)

---

## Visão geral

Este projeto implementa o domínio de uma plataforma de eventos em Java. A estrutura separa as regras de negócio das operações de aplicação e de infraestrutura, com modelos para eventos, participantes, palestras, palestrantes e salas.

O foco atual está na **modelagem do domínio**: inscrições, capacidade e lista de espera, conflitos de horário, níveis de acesso, políticas de cancelamento e notificações por observadores. Os repositórios disponíveis armazenam dados apenas em memória.

## Funcionalidades

- **Eventos:** criação por Builder, confirmação, cancelamento, alteração de data, inclusão de palestras e gerenciamento de inscrições.
- **Participantes:** cadastro, consulta, listagem, ativação/inativação e alteração do nível de acesso.
- **Capacidade e espera:** controle de vagas de evento e encaminhamento de inscrições excedentes para uma fila de espera.
- **Palestras e espaços:** validação de capacidade da sala, inscrições em palestras e detecção de conflitos de horário dentro de um evento.
- **Acesso:** validação do nível de acesso do participante conforme o tipo de evento.
- **Cancelamento:** estratégia escolhida pelo tipo de evento (gratuito, pago, corporativo ou premium).
- **Notificações:** mecanismo Observer para reagir às mudanças de eventos; há implementações relacionadas a e-mail, telefone e WhatsApp.
- **Disponibilidade:** objetos de valor para representar intervalos de tempo e verificar a disponibilidade de palestrantes.

## Exemplo rápido

O exemplo abaixo cria um evento, salva-o no repositório em memória e o confirma. A confirmação notifica os observadores configurados:

```java
import aplication.service.EventoService;
import domain.model.Evento;
import domain.model.enums.TipoEvento;
import domain.valueobject.IntervaloDeTempo;
import domain.valueobject.Name;
import infra.notification.NotificationService;
import infra.notification.ObserverEmail;
import infra.repository.EventsRepository;

import java.time.LocalDateTime;
import java.util.List;

public class Exemplo {
    public static void main(String[] args) {
        Evento evento = new Evento.Builder()
                .nomeEvento(new Name("Encontro Java"))
                .descricao("Um encontro para a comunidade Java")
                .tipo(TipoEvento.EVENTO_GRATUITO)
                .dataDeDuracao(new IntervaloDeTempo(
                        LocalDateTime.now().plusDays(10),
                        LocalDateTime.now().plusDays(10).plusHours(4)))
                .capacidadeMaxima(100)
                .build();

        EventsRepository eventos = new EventsRepository();
        eventos.save(evento);

        NotificationService notificacoes =
                new NotificationService(List.of(new ObserverEmail()));
        EventoService servico = new EventoService(eventos, notificacoes);
        servico.confirmarEvento(evento.getID());
    }
}
```

Para inscrever participantes, o evento precisa ter pelo menos uma palestra. A inscrição também valida o nível de acesso e, quando a capacidade do evento é atingida, encaminha participantes adicionais para a lista de espera.

## Arquitetura

O código está organizado em quatro áreas:

| Área | Responsabilidade | Código |
|---|---|---|
| `domain` | Modelos, enums, objetos de valor, políticas e exceções do negócio | [`src/domain`](src/domain) |
| `aplication` | Serviços que coordenam operações de eventos e participantes | [`src/aplication/service`](src/aplication/service) |
| `infra` | Repositórios em memória e notificações | [`src/infra`](src/infra) |
| Entrada | Ponto de entrada Java | [`src/Main.java`](src/Main.java) |

### Modelos principais

- [`Evento`](src/domain/model/Evento.java) concentra o estado do evento, suas palestras, participantes inscritos e fila de espera.
- [`Participante`](src/domain/model/Participante.java) mantém dados e histórico de eventos.
- [`Palestra`](src/domain/model/Palestra.java), [`Palestrante`](src/domain/model/Palestrante.java) e [`Sala`](src/domain/model/Sala.java) representam a programação e os recursos físicos.
- [`IntervaloDeTempo`](src/domain/valueobject/IntervaloDeTempo.java), [`Email`](src/domain/valueobject/Email.java) e [`Name`](src/domain/valueobject/Name.java) encapsulam valores com validações.

### Regras e serviços

- [`EventoService`](src/aplication/service/EventoService.java) coordena operações de eventos com o repositório e o serviço de notificações.
- [`ParticipanteService`](src/aplication/service/ParticipanteService.java) coordena cadastro e manutenção de participantes.
- [`FactoryPoliticaCancelamento`](src/domain/policy/FactoryPoliticaCancelamento.java) associa cada tipo de evento à sua política de cancelamento.
- As políticas estão em [`src/domain/policy`](src/domain/policy); as exceções de domínio, em [`src/domain/exception`](src/domain/exception).
- [`NotificationService`](src/infra/notification/NotificationService.java) distribui notificações aos observadores registrados.

### Políticas de cancelamento

| Tipo de evento | Regra implementada |
|---|---|
| Gratuito | Permite o cancelamento sem restrição de prazo. |
| Pago | Impede o cancelamento a menos de 48 horas do início. |
| Corporativo | Impede o cancelamento a menos de 24 horas do início. |
| Premium | Impede o cancelamento a menos de 12 horas; entre 12 e 48 horas, informa uma multa de 20%. |

As regras são aplicadas ao cancelar um evento. O código não integra pagamentos nem processa reembolsos: a multa é apenas informada no console.

## Como executar

### Requisitos

- JDK **11 ou superior** (`javac` e `java` disponíveis no terminal).
- Não há dependências externas nem configuração de Maven ou Gradle neste repositório.

### Compilar e iniciar

Na raiz do repositório, compile todos os arquivos Java para a pasta `out` e execute a classe `Main`.

**Windows PowerShell**

```powershell
New-Item -ItemType Directory -Force out | Out-Null
$sources = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $sources
java -cp out Main
```

**Linux / macOS**

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name '*.java')
java -cp out Main
```

> O comando de execução compila e inicia o ponto de entrada, mas atualmente `Main` não apresenta menu nem demonstração: seu método `main` está vazio. As funcionalidades estão implementadas como classes de domínio e serviços, prontas para serem instanciadas por uma aplicação ou testes.

## Estado atual

Este repositório é uma **implementação em desenvolvimento da camada de domínio**, não uma plataforma pronta para usuários finais. Em particular:

- Os repositórios guardam os objetos em mapas locais; os dados não são persistidos entre execuções.
- Não há interface web, API HTTP, banco de dados ou fluxo de linha de comando configurado.
- O ponto de entrada [`Main`](src/Main.java) está vazio.
- Não há configuração de suíte de testes automatizados no projeto no momento.
- Os observadores de e-mail, telefone e WhatsApp apenas imprimem mensagens no console; não enviam comunicações por serviços externos.

## Estrutura do projeto

```text
src/
├── Main.java
├── aplication/
│   └── service/
│       ├── EventoService.java
│       └── ParticipanteService.java
├── domain/
│   ├── exception/
│   ├── model/
│   │   └── enums/
│   ├── policy/
│   └── valueobject/
└── infra/
    ├── notification/
    └── repository/
```

## Navegação rápida

- [Voltar ao início](#-plataforma-de-eventos)
- [Ver os serviços de aplicação](src/aplication/service)
- [Ver os modelos de domínio](src/domain/model)
- [Ver as políticas de cancelamento](src/domain/policy)
- [Ver as notificações](src/infra/notification)
- [Ver os repositórios](src/infra/repository)
