# psicologo-app

API REST para uma plataforma de acolhimento psicológico. Autenticação com JWT, verificação de conta por e-mail e schema versionado com Flyway.

## Stack

Java 21 · Spring Boot 4.1.1 · Spring Security · Spring Data JPA · MySQL · Flyway · JWT (JJWT) · Swagger/OpenAPI

## O que tem

- Cadastro com senha criptografada (BCrypt) e conta em estado `pendente` até confirmação
- Verificação de e-mail por token opaco, armazenado como hash — nunca em texto puro
- Login com JWT, sem sessão em memória
- Identidade do usuário sempre extraída do token, nunca do corpo da requisição
- Erros padronizados em `application/problem+json`
- Schema versionado via Flyway, sem `ddl-auto` controlando produção

## Estrutura

```
autenticacao/   cadastro, verificação, login
usuario/        entidade e perfil
seguranca/      JWT, SecurityConfig, UserDetailsService
shared/         tratamento de erros, converters JPA
```

## Em progresso

Especialistas, Chat e Mensagens — tabelas já existem no schema, implementação ainda não.