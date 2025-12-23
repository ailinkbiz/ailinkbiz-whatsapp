# AiLinkBiz – WhatsApp Backend

Backend de automação para WhatsApp desenvolvido em Java com Spring Boot, focado em:

- Atendimento automatizado via WhatsApp
- Fluxo de menu com estado
- Handoff para atendimento humano
- Controle de estado e timeout usando Redis
- Logs de conversas para acompanhamento operacional

## 🧩 Arquitetura

- **Spring Boot** – API REST
- **Redis** – Armazenamento de estado da conversa
- **Twilio WhatsApp API** – Integração com WhatsApp
- **n8n** – Orquestrações externas (em repositório separado)

O Redis é utilizado como **fonte de verdade** do estado da conversa.  
Os logs são mantidos em memória apenas para observabilidade e debug.

## 🔁 Fluxo resumido

1. Usuário envia mensagem
2. Bot responde com menu
3. Usuário escolhe opção
4. Pode ocorrer handoff para humano
5. Timeout encerra automaticamente o handoff por inatividade

## 🧪 Ambiente de desenvolvimento

- Java 17+
- Redis local
- Conta Twilio (sandbox para testes)

## ⚠️ Observações

- Horários são armazenados em UTC no Redis e convertidos para horário local apenas na API.
- Projeto em evolução contínua (MVP técnico).

---

Desenvolvido por AiLinkBiz
