# Argos — Serviço de Cadastro e Notificação de Usuários (Java/Spring Boot)

Microsserviço responsável pelo **cadastro de usuários** do sistema Argos e pelo **envio do token de verificação de acesso** por e-mail. Faz parte do ecossistema Argos, que também inclui:

- [`site-institucional`](https://github.com/Grupo-9-Projeto/site-institucional) — front-end institucional/dashboard
- [`web-data-viz`](https://github.com/Grupo-9-Projeto/web-data-viz) — camada de visualização de dados

---

## 1. Visão geral

Este serviço expõe um único fluxo de negócio: **criação de um novo usuário associado a uma empresa e a um gestor**, seguido do **envio automático de um token de verificação por e-mail**. O token gerado é obrigatório para que o usuário conclua seu cadastro no sistema — ele é a credencial que confirma que o e-mail informado pertence de fato ao destinatário e libera o acesso à plataforma.

O serviço **não expõe (ainda) um endpoint de confirmação/validação do token** — apenas a geração, persistência e o envio. A etapa de confirmação do cadastro a partir do token é uma responsabilidade a ser implementada (ver seção [8. Pontos em aberto](#8-pontos-em-aberto-e-dívidas-técnicas)).

---

## 2. Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 (`spring-boot-starter-parent`) |
| Web | `spring-boot-starter-web` / `spring-boot-starter-webmvc` |
| Persistência | `spring-boot-starter-data-jpa` (Hibernate) |
| Banco de dados | MySQL (`mysql-connector-j`) |
| E-mail | `spring-boot-starter-mail` + Jakarta Mail API (`jakarta.mail.*`) |
| Boilerplate | Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`) |
| Build | Maven (`mvnw`) |
| Testes | `spring-boot-starter-test`, `spring-boot-starter-data-jpa-test`, `spring-boot-starter-webmvc-test`, `spring-boot-starter-mail-test` |

---

## 3. Estrutura de pacotes

```
com.argos
├── ArgosApplication.java        # entry point (@SpringBootApplication)
├── controller
│   └── UsuarioController.java   # camada REST
├── dtos
│   └── UsuarioRequest.java      # DTO de entrada (payload da API)
├── entity
│   └── Usuario.java             # entidade JPA / tabela `usuario`
├── repository
│   └── UsuarioRepository.java   # JpaRepository<Usuario, Long>
└── service
    ├── UsuarioService.java      # regra de negócio de criação + geração de token
    └── EmailService.java        # montagem e disparo do e-mail via SMTP
```

Arquitetura em camadas clássica (Controller → Service → Repository), sem uso de DTO de saída (mapper) nem de camada de validação (Bean Validation) no momento.

---

## 4. Modelo de dados

### Entidade `Usuario` (tabela `usuario`)

| Campo | Tipo | Coluna | Observações |
|---|---|---|---|
| `id` | `Long` | `id` | PK, `GenerationType.IDENTITY` |
| `empresa_id` | `Long` | `empresa_id` | FK lógica para a empresa (sem `@ManyToOne` mapeado — armazenado como escalar) |
| `gestor_id` | `Long` | `gestor_id` | FK lógica para o gestor responsável (idem) |
| `email` | `String` | `email` | E-mail do usuário a ser cadastrado |
| `cargo` | `String` | `cargo` | Cargo/função do usuário |
| `token` | `String` | `token` | Token de verificação gerado no momento da criação |

> `spring.jpa.hibernate.ddl-auto=none` — o schema **não é gerado automaticamente** pelo Hibernate. A tabela `usuario` precisa existir previamente no banco `argos_db`, criada via script/migração manual.

### DTO `UsuarioRequest` (payload de entrada)

```json
{
  "cargo": "string",
  "email": "string",
  "gestorId": 1,
  "empresaId": 1
}
```

O campo `id` está presente no código, porém comentado (`//private Long id;`) — não é aceito no payload atual.

---

## 5. API REST

### `POST /usuarios/criar-usuario`

Cria um novo usuário e dispara o e-mail de verificação.

**Request body**

```json
{
  "cargo": "Analista de Redes",
  "email": "usuario@empresa.com",
  "gestorId": 3,
  "empresaId": 1
}
```

**Comportamento**
1. Recebe o `UsuarioRequest`.
2. Gera um token alfanumérico aleatório de 16 caracteres (`UsuarioService.gerarTokenAleatorio()`), a partir do conjunto `A–Z0–9`.
3. Instancia a entidade `Usuario`, associando `cargo`, `email`, `empresa_id`, `gestor_id` e o `token` gerado.
4. Persiste o usuário via `UsuarioRepository.save()` — o token já é salvo no banco **antes** da confirmação de entrega do e-mail.
5. Invoca `EmailService.enviarEmail(usuario, token)`, que monta o corpo HTML do e-mail e o envia via SMTP.

**Resposta atual**

O método do controller é `void` e não define `@ResponseStatus`/`ResponseEntity`, portanto a resposta HTTP padrão é **`200 OK` sem corpo**, mesmo em caso de falha no envio do e-mail (a exceção de `MessagingException` é apenas logada via `printStackTrace()`, não propagada). Ver seção 8.

Não há tratamento de erro para e-mail duplicado, `empresaId`/`gestorId` inexistentes ou payload inválido.

---

## 6. Serviço de e-mail (`EmailService`)

- Utiliza a API **Jakarta Mail** diretamente (não o `JavaMailSender` do Spring, apesar da dependência `spring-boot-starter-mail` estar declarada).
- Autenticação SMTP feita via `Authenticator`/`PasswordAuthentication`, com host, porta (587) e STARTTLS configurados via `java.util.Properties`.
- **Credenciais do remetente estão hardcoded na classe** (`user` e `password` como `final String`), e não lidas de `application.properties`/variáveis de ambiente — apesar de `application.properties` já conter chaves equivalentes (`spring.mail.*`) que não são efetivamente utilizadas por essa implementação. Isso é uma inconsistência a ser corrigida (ver seção 8).
- O corpo do e-mail é um template HTML embutido via *text block* Java (`"""..."""`), com:
  - Header com logo do Argos (carregada de `raw.githubusercontent.com/Grupo-9-Projeto/site-institucional`).
  - Bloco de destaque (`token-box`) exibindo o token de verificação.
  - Placeholder `${tokenAcesso}` substituído via `String.replace()` (não é um engine de template real).
- Assunto fixo: **"Código de verificação de cadastro - Argos"**.
- Remetente exibido como **"Argos"** (`argos.techn@gmail.com`).

---

## 7. Configuração (`application.properties`)

```properties
spring.application.name=argos

spring.datasource.url=jdbc:mysql://localhost:3306/argos_db
spring.datasource.username=argos_user
spring.datasource.password=sptech

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=seu-email@gmail.com
spring.mail.password=sua-senha-de-app
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

server.port=3333
```

> ⚠️ As chaves `spring.mail.*` acima **não têm efeito hoje**, pois `EmailService` não usa o `JavaMailSender`/`spring.mail.*` — a autenticação é feita com credenciais fixas na classe. Para colocar o serviço em produção com segurança, essas credenciais devem migrar para variáveis de ambiente (ex.: `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`) e o `EmailService` deve passar a consumi-las (via `@Value` ou injeção do `JavaMailSender`), removendo qualquer segredo do código-fonte.

---

## 8. Pontos em aberto e dívidas técnicas

Registrados aqui para rastreabilidade — refletem o estado atual do código, não requisitos formais:

- **Sem endpoint de confirmação do token**: hoje o sistema gera e envia o token, mas não há rota (`/usuarios/confirmar-cadastro` ou similar) para validá-lo e efetivar o cadastro.
- **Credenciais SMTP hardcoded** em `EmailService` — risco de segurança; devem ser externalizadas.
- **Erro de e-mail silencioso**: falha no envio (`MessagingException`) é apenas impressa no console (`printStackTrace()`), sem rollback do usuário já persistido nem retorno de erro ao cliente da API.
- **Sem Bean Validation** no `UsuarioRequest` (`@NotBlank`, `@Email`, etc.) — payloads inválidos ou incompletos não são rejeitados antes da persistência.
- **Sem tratamento de duplicidade** de e-mail (não há `unique` na coluna nem verificação prévia).
- **Import não utilizado/indevido** em `UsuarioController`: `org.apache.catalina.User` (dependência de Tomcat, sem relação com o domínio — deve ser removido).
- **Token sem expiração**: o campo `token` não possui TTL nem timestamp de geração; não há como invalidar um token antigo.
- **`UsuarioService.saveUser()`** é um método público não utilizado pelo fluxo atual (persistência direta sem geração de token/envio de e-mail) — possivelmente resquício de refatoração.
- **DDL manual**: como `ddl-auto=none`, a criação/evolução do schema da tabela `usuario` depende de script externo, que não está versionado neste repositório.

---

## 9. Como executar localmente

**Pré-requisitos**: Java 21, Maven (ou usar o wrapper `./mvnw`), instância MySQL acessível.

```bash
# 1. Criar o banco e o usuário no MySQL
CREATE DATABASE argos_db;
CREATE USER 'argos_user'@'localhost' IDENTIFIED BY 'sptech';
GRANT ALL PRIVILEGES ON argos_db.* TO 'argos_user'@'localhost';

# 2. Criar a tabela `usuario` manualmente (ddl-auto=none)
CREATE TABLE usuario (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  empresa_id BIGINT,
  gestor_id BIGINT,
  email VARCHAR(255),
  cargo VARCHAR(255),
  token VARCHAR(255)
);

# 3. Build
./mvnw clean package

# 4. Executar
java -jar target/argos-0.0.1-SNAPSHOT.jar
# ou
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:3333`.

**Teste rápido do endpoint:**

```bash
curl -X POST http://localhost:3333/usuarios/criar-usuario \
  -H "Content-Type: application/json" \
  -d '{
        "cargo": "Analista NOC",
        "email": "teste@exemplo.com",
        "gestorId": 1,
        "empresaId": 1
      }'
```

Se as credenciais SMTP estiverem válidas, o usuário informado em `email` receberá a mensagem com o token de verificação.
