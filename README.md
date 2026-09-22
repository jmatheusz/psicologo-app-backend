# psicologo-app

API REST de acolhimento psicologico. Esta entrega cobre o modulo de acesso: cadastro,
verificacao de e-mail por token, login com JWT e perfil do usuario autenticado.

## Stack

- Java 21 / Spring Boot 3.5.5
- Spring Web, Spring Data JPA, Spring Security, Bean Validation
- MySQL 8 + Flyway
- JJWT 0.12.6
- springdoc-openapi (Swagger UI)

## Estrutura

```
src/main/java/com/acolher/psicologo
├── autenticacao
│   ├── dominio      AutenticacaoToken, TipoToken, repositorio
│   ├── dto          NovoUsuario, Credenciais, TokenDeAcesso, ...
│   ├── servico      CadastroService, AutenticacaoService, VerificacaoDeEmailService
│   └── web          AuthController
├── config           propriedades tipadas, CORS, OpenAPI
├── notificacao      EnviadorDeEmail (implementacoes log e smtp)
├── seguranca        SecurityConfig, UsuarioDetailsService, jwt
├── shared           tratamento de erros, converters JPA
└── usuario          entidade Usuario, repositorio, servico, PerfilController
```

## Preparando o ambiente

1. Crie o banco vazio. O Flyway cria as tabelas na primeira execucao:

```sql
CREATE DATABASE acolher_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Gere a chave do JWT (a aplicacao espera Base64):

```bash
openssl rand -base64 48
```

3. Copie `.env.example` e exporte as variaveis, ou crie um
`src/main/resources/application-local.properties` com os mesmos valores.

## Executando

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

No perfil `dev` o e-mail de verificacao nao e enviado: o link aparece no log da aplicacao,
o que permite testar o fluxo completo sem configurar SMTP. Para enviar de verdade, rode com
o perfil `prod` (ou defina `APP_EMAIL_MODO=smtp`) e preencha as variaveis `MAIL_*`.

Swagger UI: http://localhost:8080/swagger-ui.html

## Fluxo de acesso

| Metodo | Rota | Acesso | Descricao |
|---|---|---|---|
| POST | `/auth/cadastro` | publico | cria a conta com status `pendente` e dispara o e-mail |
| GET | `/auth/verificar?token=` | publico | ativa a conta e consome o token |
| POST | `/auth/reenviar-verificacao` | publico | reenvia o link, sempre com resposta 202 |
| POST | `/auth/login` | publico | devolve o JWT |
| GET | `/me` | autenticado | perfil do usuario do token |
| PATCH | `/me` | autenticado | atualiza nome, avatar e anonimato |

Conta com status diferente de `ativo` nao autentica: `pendente` retorna 403 pedindo a
confirmacao do e-mail, `suspenso` e `banido` retornam 403 de conta bloqueada.

## Decisoes que valem registro

- O token de verificacao e gerado com `SecureRandom` e gravado como hash SHA-256. A coluna
  `token` nunca guarda o valor enviado ao usuario.
- `Usuario.idAutenticacao_Token` nao foi mapeado. A FK e circular e redundante, ja que
  `Autenticacao_Token.usuario_id` cobre o relacionamento.
- `_anonimo` e um `ENUM('sim','nao')` no banco e um `boolean` no dominio, convertido por
  `SimNaoConverter`.
- `ddl-auto` esta em `none` porque o schema pertence ao Flyway. Para ligar `validate`, esteja
  ciente de que o Hibernate pode reclamar das colunas `ENUM` do MySQL, que ele espera como
  `VARCHAR`.
- Os triggers do schema sinalizam `SQLSTATE 45000`, que chega como
  `DataIntegrityViolationException` e e traduzido para 409 pelo `ApiExceptionHandler`.
- Todos os erros respondem em `application/problem+json` (RFC 7807).
- Nenhum endpoint aceita identificador de usuario vindo do corpo da requisicao. A identidade
  sempre sai do token.

## Proximos modulos

Especialistas, Chat, Mensagem e Conteudo Educativo ja tem tabela criada pela migration V1.
Ao implementa-los, mantenha a regra acima e lembre que `Chat.especialista_usuario_id`
referencia `Usuario`, e nao `Especialidades`.
