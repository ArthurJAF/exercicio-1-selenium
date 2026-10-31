# Plano de Testes – automationexercise.com

Atividade de Qualidade e Teste: testes end-to-end com Selenium para **Test Case 3 (Login com e-mail/senha incorretos)** e **Test Case 1 (Registrar usuário)**.

Técnicas usadas para derivar as entradas:

- **Particionamento em Classes de Equivalência (CE):** divide o domínio de cada campo em classes cujos valores devem ter o mesmo comportamento; testa-se um representante de cada classe.
- **Análise de Valor Limite (AVL):** testa os valores nas fronteiras das classes (mínimo, mínimo+1, máximo etc.), onde os defeitos costumam aparecer.

> Observação: o site não documenta limites de tamanho para os campos. Os limites abaixo vêm de convenções (RFC 5321: parte local do e-mail ≤ 64 caracteres) e de valores "de fronteira práticos" (0, 1, 2 e tamanhos grandes). Se algum limite real for descoberto, basta ajustar a lista de argumentos do teste.

---

## 1. Login com e-mail e senha incorretos (Test Case 3)

Classe: `LoginIncorretoTest`

### 1.1 Campo E-mail (`input type="email"`, obrigatório)

| Classe | Tipo | Exemplo | Resultado esperado |
|---|---|---|---|
| CE1 – e-mail bem formado, não cadastrado | Válida | `usuario.inexistente.qa@exemplo.com` | Mensagem *Your email or password is incorrect!* |
| CE2 – e-mail em maiúsculas | Válida | `USUARIO.INEXISTENTE.QA@EXEMPLO.COM` | Mensagem de erro de credenciais |
| CE3 – e-mail com caracteres especiais válidos | Válida | `o'brien+qa@exemplo.com` | Mensagem de erro de credenciais |
| CE-inv1 – sem `@` | Inválida | `usuarioexemplo.com` | Envio bloqueado pelo formulário |
| CE-inv2 – sem domínio após `@` | Inválida | `usuario@` | Envio bloqueado |
| CE-inv3 – sem parte local | Inválida | `@exemplo.com` | Envio bloqueado |
| CE-inv4 – com espaço | Inválida | `usuario @exemplo.com` | Envio bloqueado |

Valores limite do e-mail:

| Valor limite | Entrada | Resultado esperado |
|---|---|---|
| AVL-inv1 – 0 caracteres | `""` | Envio bloqueado (campo obrigatório) |
| AVL3 – parte local com 64 caracteres (máximo RFC) | `aaaa…(64)@exemplo.com` | Mensagem de erro de credenciais |

### 1.2 Campo Senha (texto, obrigatório)

| Classe / limite | Entrada | Resultado esperado |
|---|---|---|
| CE4 – somente numérica | `123456` | Mensagem de erro de credenciais |
| CE5 – caracteres especiais | `!@#$%&*()_+-=[]{}` | Mensagem de erro de credenciais |
| CE comum | `SenhaErrada123` | Mensagem de erro de credenciais |
| AVL-inv2 – 0 caracteres | `""` | Envio bloqueado (campo obrigatório) |
| AVL1 – 1 caractere (limite inferior válido) | `a` | Mensagem de erro de credenciais |
| AVL2 – 256 caracteres (muito acima do típico) | `x` × 256 | Mensagem de erro de credenciais (sem quebrar a página) |
| AVL-inv3 – e-mail e senha vazios | `""` / `""` | Envio bloqueado |

### 1.3 Casos de teste implementados

| Método | Entradas | Qtde |
|---|---|---|
| `testLoginComEmailESenhaIncorretos` (parametrizado) | CE1, CE2, CE3, CE4, CE5, AVL1, AVL2, AVL3 | 8 |
| `testLoginBloqueadoPorValidacaoDoFormulario` (parametrizado) | CE-inv1..4, AVL-inv1..3 | 7 |
| `testCase3_LoginComEmailESenhaIncorretos` (roteiro literal) | e-mail/senha incorretos simples | 1 |

---

## 2. Registrar usuário (Test Case 1)

Classe: `RegistrarUsuarioTest`

### 2.1 Campo Nome do signup (texto, obrigatório)

| Classe / limite | Entrada | Resultado esperado |
|---|---|---|
| AVL-inv1 – 0 caracteres | `""` | Envio bloqueado |
| AVL1 – 1 caractere (limite inferior válido) | `A` | Avança para *ENTER ACCOUNT INFORMATION*, nome preenchido |
| AVL2 – 2 caracteres | `Al` | Avança |
| CE1 – nome comum com espaço | `Maria Silva` | Avança |
| CE2 – nome com acentos | `Jose da Conceicao Araujo` | Avança |
| CE3 – nome com números | `Aluno 2026` | Avança |
| AVL3 – nome longo (50 caracteres) | `N` × 50 | Avança |

### 2.2 Campo E-mail do signup (obrigatório, único)

| Classe | Tipo | Entrada | Resultado esperado |
|---|---|---|---|
| CE – e-mail válido e inédito | Válida | `aluno.qa.<timestamp>@exemplo.com` | Avança |
| CE – e-mail já cadastrado | Inválida (regra de negócio) | e-mail de conta recém-criada | Mensagem *Email Address already exist!* |
| CE-inv1 – sem `@` | Inválida | `alunoexemplo.com` | Envio bloqueado |
| CE-inv2 – sem domínio | Inválida | `aluno@` | Envio bloqueado |
| CE-inv3 – sem parte local | Inválida | `@exemplo.com` | Envio bloqueado |
| CE-inv4 – com espaço | Inválida | `aluno qa@exemplo.com` | Envio bloqueado |
| AVL-inv2 – 0 caracteres | Limite | `""` | Envio bloqueado |

### 2.3 Casos de teste implementados

| Método | Descrição | Qtde |
|---|---|---|
| `testCase1_RegistrarUsuario` | Fluxo completo do Test Case 1 (passos 1–18): cria e exclui a conta | 1 |
| `testSignupComNomeValido` (parametrizado) | AVL1, AVL2, AVL3, CE1, CE2, CE3 | 6 |
| `testSignupBloqueadoPorValidacaoDoFormulario` (parametrizado) | AVL-inv1..3, CE-inv1..4 | 7 |
| `testSignupComEmailJaCadastrado` | Cria conta, faz logout, repete o e-mail, verifica erro e exclui a conta | 1 |

**Total: 31 execuções de teste** (≥ 5 entradas por funcionalidade).

---

## 3. Dados fixos do cadastro completo (Test Case 1)

| Campo | Valor |
|---|---|
| Title | Mr. |
| Senha | `Senha@12345` |
| Data de nascimento | 15 / May / 1995 |
| Newsletter e ofertas de parceiros | marcados |
| First / Last name | Aluno / QA |
| Company | Faculdade QA |
| Address / Address2 | Rua das Flores, 100 / Apto 12 |
| Country / State / City | Canada / Ontario / Toronto |
| Zipcode / Mobile | M5V 2T6 / +1 416 555 0100 |

## 4. Mapeamento passo a passo (Test Case 1)

| Passo | Onde está no código |
|---|---|
| 1 | `BaseTest.createDriver()` |
| 2, 3, 4 | `BaseTest.navegarAteSignupLogin()` |
| 5 | `TITULO_SIGNUP` visível |
| 6, 7 | `digitar(SIGNUP_NOME…)`, `digitar(SIGNUP_EMAIL…)`, `clicar(SIGNUP_BOTAO)` |
| 8 | `TITULO_ACCOUNT_INFO` visível |
| 9, 10, 11 | `preencherDadosDaConta(...)` |
| 12 | `preencherEndereco(...)` |
| 13 | clique em `create-account` |
| 14 | `CONTA_CRIADA` = "ACCOUNT CREATED!" |
| 15 | clique em `Continue` |
| 16 | `LOGADO_COMO` contém o nome |
| 17 | clique em `Delete Account` |
| 18 | `CONTA_EXCLUIDA` = "ACCOUNT DELETED!" e clique em `Continue` |
