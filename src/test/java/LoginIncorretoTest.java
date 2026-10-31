import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Test Case 3: Login User with incorrect email and password.
 *
 * Entradas escolhidas por Particionamento em Classes de Equivalencia (CE) e
 * Analise de Valor Limite (AVL). Detalhes no PLANO_DE_TESTES.md.
 */
public class LoginIncorretoTest extends BaseTest {

	private static final By EMAIL = By.cssSelector("input[data-qa='login-email']");
	private static final By SENHA = By.cssSelector("input[data-qa='login-password']");
	private static final By BOTAO_LOGIN = By.cssSelector("button[data-qa='login-button']");
	private static final By TITULO_LOGIN = By.xpath("//h2[normalize-space()='Login to your account']");
	private static final By MSG_ERRO = By.xpath("//p[contains(.,'Your email or password is incorrect!')]");

	// ------------------------------------------------------------------
	// Grupo A: entradas VALIDAS para o formulario, mas credenciais erradas.
	// O formulario envia e o site deve exibir a mensagem de erro.
	// ------------------------------------------------------------------
	static Stream<Arguments> credenciaisIncorretas() {
		String emailInexistente = "usuario.inexistente.qa@exemplo.com";
		return Stream.of(
				// descricao, email, senha
				Arguments.of("CE1: e-mail valido nao cadastrado + senha comum", emailInexistente, "SenhaErrada123"),
				Arguments.of("CE2: e-mail em MAIUSCULAS nao cadastrado", "USUARIO.INEXISTENTE.QA@EXEMPLO.COM", "SenhaErrada123"),
				Arguments.of("CE3: e-mail com caracteres especiais validos (' e +)", "o'brien+qa@exemplo.com", "SenhaErrada123"),
				Arguments.of("AVL1: senha com 1 caractere (limite inferior)", emailInexistente, "a"),
				Arguments.of("AVL2: senha com 256 caracteres (acima de tamanhos tipicos)", emailInexistente, "x".repeat(256)),
				Arguments.of("AVL3: e-mail com parte local de 64 caracteres (limite RFC 5321)",
						"a".repeat(64) + "@exemplo.com", "SenhaErrada123"),
				Arguments.of("CE4: senha apenas numerica", emailInexistente, "123456"),
				Arguments.of("CE5: senha com caracteres especiais", emailInexistente, "!@#$%&*()_+-=[]{}"));
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("credenciaisIncorretas")
	public void testLoginComEmailESenhaIncorretos(String descricao, String email, String senha) {
		// Passos 1 a 4: abrir navegador, acessar site, validar home, clicar em Signup / Login
		navegarAteSignupLogin();

		// Passo 5: verificar 'Login to your account'
		WebElement titulo = visivel(TITULO_LOGIN);
		assertTrue(titulo.isDisplayed(), "'Login to your account' nao esta visivel");

		// Passo 6: informar e-mail e senha incorretos
		digitar(EMAIL, email);
		digitar(SENHA, senha);

		// Passo 7: clicar em 'Login'
		clicar(BOTAO_LOGIN);

		// Passo 8: verificar mensagem de erro
		WebElement erro = visivel(MSG_ERRO);
		assertEquals("Your email or password is incorrect!", erro.getText().trim());
	}

	// ------------------------------------------------------------------
	// Grupo B: entradas INVALIDAS (classes invalidas / valores fora do dominio).
	// A validacao HTML5 do navegador deve BLOQUEAR o envio: o usuario permanece
	// em /login, o campo fica :invalid e a mensagem do servidor nao aparece.
	// ------------------------------------------------------------------
	static Stream<Arguments> entradasBloqueadasPeloFormulario() {
		return Stream.of(
				// descricao, email, senha
				Arguments.of("CE-inv1: e-mail sem '@'", "usuarioexemplo.com", "Senha123"),
				Arguments.of("CE-inv2: e-mail sem dominio apos '@'", "usuario@", "Senha123"),
				Arguments.of("CE-inv3: e-mail sem parte local antes de '@'", "@exemplo.com", "Senha123"),
				Arguments.of("CE-inv4: e-mail com espaco", "usuario @exemplo.com", "Senha123"),
				Arguments.of("AVL-inv1: e-mail vazio (0 caracteres)", "", "Senha123"),
				Arguments.of("AVL-inv2: senha vazia (0 caracteres)", "usuario@exemplo.com", ""),
				Arguments.of("AVL-inv3: e-mail e senha vazios", "", ""));
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("entradasBloqueadasPeloFormulario")
	public void testLoginBloqueadoPorValidacaoDoFormulario(String descricao, String email, String senha) {
		navegarAteSignupLogin();

		assertTrue(visivel(TITULO_LOGIN).isDisplayed(), "'Login to your account' nao esta visivel");

		digitar(EMAIL, email);
		digitar(SENHA, senha);
		clicar(BOTAO_LOGIN);

		// o envio foi impedido: continua na pagina de login
		assertTrue(driver.getCurrentUrl().contains("/login"),
				"Deveria permanecer em /login, mas esta em: " + driver.getCurrentUrl());

		// algum campo do formulario de login esta marcado como invalido pelo navegador
		boolean campoInvalido = existe(By.cssSelector(
				"input[data-qa='login-email']:invalid, input[data-qa='login-password']:invalid"));
		assertTrue(campoInvalido, "Esperava um campo invalido (validacao HTML5) para: " + descricao);

		// e o servidor nao chegou a responder com a mensagem de erro de credenciais
		assertFalse(existe(MSG_ERRO), "A mensagem do servidor nao deveria aparecer: " + descricao);
	}

	// ------------------------------------------------------------------
	// Teste simples (roteiro literal do Test Case 3, sem parametrizacao)
	// ------------------------------------------------------------------
	@Test
	public void testCase3_LoginComEmailESenhaIncorretos() {
		navegarAteSignupLogin();
		assertTrue(visivel(TITULO_LOGIN).isDisplayed());
		digitar(EMAIL, "email.incorreto@exemplo.com");
		digitar(SENHA, "senhaincorreta");
		clicar(BOTAO_LOGIN);
		assertTrue(visivel(MSG_ERRO).isDisplayed());
	}
}
