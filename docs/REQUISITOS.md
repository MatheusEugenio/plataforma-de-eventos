# Requisitos do projeto

[Voltar ao README](../README.md) · [Visão geral](#visão-geral) · [Requisitos funcionais](#requisitos-funcionais) · [Regras de negócio](#regras-de-negócio) · [Requisitos não funcionais](#requisitos-não-funcionais) · [Limitações e próximos passos](#limitações-e-próximos-passos)

## Visão geral

Este documento descreve os requisitos da plataforma de eventos a partir do código existente. **Implementado** indica comportamento disponível na camada de domínio/serviços; **parcial** indica que há uma base, mas falta integração ou cobertura; **pendente** identifica capacidade ainda não fornecida pelo projeto. A classificação evita tratar como pronta uma funcionalidade que hoje é apenas um modelo ou uma saída de console.

## Requisitos funcionais

| ID | Requisito | Situação |
|---|---|---|
| RF-01 | Criar eventos com nome, descrição, tipo, período e capacidade máxima positiva. Um novo evento começa no estado planejado. | Implementado |
| RF-02 | Confirmar ou cancelar eventos, impedindo confirmação/cancelamento após o evento ser finalizado e repetição de cancelamento. | Implementado |
| RF-03 | Aplicar a política de cancelamento correspondente ao tipo do evento. | Implementado |
| RF-04 | Alterar o período do evento e associar palestras, rejeitando palestras com horários sobrepostos no mesmo evento. | Implementado |
| RF-05 | Cadastrar, buscar e listar participantes; ativar/inativar participantes e alterar seu nível de acesso. | Implementado |
| RF-06 | Inscrever participantes ativos em eventos que possuam palestras, validando nível de acesso e evitando inscrições duplicadas. | Implementado |
| RF-07 | Respeitar a capacidade do evento, mantendo inscrições excedentes em fila de espera e promovendo a próxima pessoa elegível quando uma vaga é liberada. | Implementado |
| RF-08 | Criar palestras associadas a sala, palestrante, período e nome, usando a capacidade da sala como limite de participantes. | Implementado |
| RF-09 | Inscrever participantes em palestras, impedir duplicidade/excesso de capacidade e acompanhar o estado da palestra. | Parcial |
| RF-10 | Verificar se um palestrante está disponível em um intervalo, considerando sua agenda e palestras agendadas. | Parcial |
| RF-11 | Controlar a ocupação e o uso de salas por estados de disponibilidade. | Implementado |
| RF-12 | Notificar observadores quando operações de evento coordenadas pelo serviço forem executadas. | Parcial |
| RF-13 | Persistir eventos e participantes entre execuções. | Pendente |
| RF-14 | Oferecer uma interface para usuários criarem e gerenciarem eventos e inscrições. | Pendente |

## Regras de negócio

### Tipos de evento e acesso

Os tipos de evento possuem pesos de acesso: gratuito (1), pago (2), corporativo (3) e premium (4). Os níveis dos participantes são participante (1), funcionário (2) e empresário (3). Em geral, o nível precisa ser igual ou superior ao peso do evento; o código contém uma exceção que permite nível participante em evento pago.

### Cancelamento

| Tipo | Regra |
|---|---|
| Gratuito | Cancelamento sem restrição de prazo. |
| Pago | Cancelamento bloqueado a menos de 48 horas do início. |
| Corporativo | Cancelamento bloqueado a menos de 24 horas do início. |
| Premium | Cancelamento bloqueado a menos de 12 horas; entre 12 e 48 horas, informa multa de 20%. |

As regras estão implementadas como políticas de domínio. Não há cálculo ou transação de reembolso, cobrança de multa nem integração de pagamento.

### Inscrições e programação

- Um evento não aceita inscrições sem ao menos uma palestra cadastrada.
- Participantes inativos não podem se inscrever em eventos.
- Inscrições excedentes seguem para uma fila; o próximo participante ativo e elegível é promovido quando uma vaga abre.
- Palestras do mesmo evento não podem ter períodos sobrepostos.
- A capacidade de uma palestra deriva da capacidade da sala associada.
- A agenda de disponibilidade do palestrante é considerada na verificação de disponibilidade; ainda não há um fluxo que aplique essa verificação automaticamente ao agendar uma palestra.

## Requisitos não funcionais

| ID | Requisito | Situação |
|---|---|---|
| RNF-01 | Implementação em Java, compilável com JDK 11 ou superior. | Atendido |
| RNF-02 | Manter responsabilidades separadas entre domínio, serviços de aplicação e infraestrutura. | Atendido parcialmente |
| RNF-03 | Validar dados essenciais e rejeitar estados ou operações inválidas com erros explícitos. | Atendido parcialmente |
| RNF-04 | Manter dados disponíveis após encerrar e reiniciar a aplicação. | Pendente |
| RNF-05 | Disponibilizar testes automatizados para as regras centrais do domínio. | Pendente |
| RNF-06 | Enviar notificações por provedores reais de e-mail, telefone ou WhatsApp. | Pendente |

## Limitações e próximos passos

O repositório fornece a camada de domínio e serviços básicos, mas ainda não configura uma aplicação pronta para uso. Não existem API, interface gráfica/CLI, autenticação, autorização de usuários, banco de dados ou provedores externos de notificação. `Main` está vazio e os repositórios mantêm dados apenas em memória.

Para transformar esta base em produto, os próximos passos dependem de decisões de escopo, como a interface desejada, a tecnologia de persistência, os provedores de comunicação e as regras de autenticação. Esses itens são apontados como pendências, não como funcionalidades assumidas ou já implementadas.
