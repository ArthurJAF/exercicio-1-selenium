# Testes E2E com Selenium – automationexercise.com

Projeto Maven (Java 17+ / JUnit 5 / Selenium 4 / WebDriverManager), no mesmo estilo do `GoogleTest.java` da professora.

## Estrutura

```
automationexercise-selenium/
├── pom.xml
├── PLANO_DE_TESTES.md          <- classes de equivalência e valores limite
└── src/test/java/
    ├── BaseTest.java           <- driver (@BeforeEach/@AfterEach) e helpers
    ├── LoginIncorretoTest.java <- Test Case 3
    └── RegistrarUsuarioTest.java <- Test Case 1
```

## Como rodar no VS Code

1. Instale o **JDK 17 ou superior** e o **Google Chrome**.
2. No VS Code, instale a extensão **Extension Pack for Java** (inclui Maven e Test Runner).
3. Abra a pasta `automationexercise-selenium` (File > Open Folder).
4. Aguarde o VS Code importar o projeto Maven (baixa as dependências na primeira vez).
5. Abra a aba **Testing** (ícone de frasco) e clique em ▶ em uma classe ou método, ou use o ▶ ao lado de `@Test` / `@ParameterizedTest` no código.

Pelo terminal:

```bash
mvn test                                  # todos os testes (abre o Edge)
mvn test -Dtest=LoginIncorretoTest        # só o Test Case 3
mvn test -Dtest=RegistrarUsuarioTest      # só o Test Case 1
mvn test -Dtest=RegistrarUsuarioTest#testCase1_RegistrarUsuario   # só o fluxo completo
mvn test -Dheadless=true                  # sem abrir a janela do navegador
```

## Navegador (Edge ou Chrome)

O padrão é o **Microsoft Edge**. Para o Edge, o teste tenta duas formas de obter o driver: primeiro o Selenium Manager (embutido no Selenium) e depois o WebDriverManager. Se as duas falharem, o erro do teste mostra o motivo de cada uma.

- **Terminal:** `mvn test -Dbrowser=edge` ou `mvn test -Dbrowser=chrome` (também funciona com `-Dtest=...` e `-Dheadless=true`).
- **VS Code (botão ▶ / aba Testing):** o botão não lê o `pom.xml`; para trocar, edite `NAVEGADOR_PADRAO` em `src/test/java/BaseTest.java`.

### Se o Edge não abrir (download do driver falhando)

1. Veja a versão do Edge em `edge://settings/help`.
2. Baixe o **msedgedriver** da mesma versão (e arquitetura, normalmente x64) em <https://developer.microsoft.com/microsoft-edge/tools/webdriver/> e extraia o `msedgedriver.exe`.
3. Em `BaseTest.java`, preencha `CAMINHO_EDGEDRIVER`, por exemplo `"C:\\drivers\\msedgedriver.exe"` (barras duplas no Java).

## Observações

- Cada execução usa um e-mail único (`aluno.qa.<timestamp>@exemplo.com`), então os testes podem ser repetidos sem conflito. Contas criadas pelos testes são excluídas no final; se um teste falhar no meio do fluxo, pode sobrar uma conta de teste no site, o que não atrapalha as próximas execuções.
- O site exibe anúncios (Google Ads) que às vezes cobrem botões. Os métodos `clicar(...)` e `removerAnuncios()` do `BaseTest` tratam isso.
- Os testes "bloqueados pela validação do formulário" dependem de o navegador validar os campos `required` / `type="email"` (HTML5) antes de enviar o formulário.
- Se o site mudar algum seletor (`data-qa`, `id`), ajuste as constantes `By` no topo de cada classe de teste.
- Os limites de tamanho usados (1, 2, 50, 64, 256 caracteres) são fronteiras práticas; o site não publica limites oficiais. Veja `PLANO_DE_TESTES.md`.
