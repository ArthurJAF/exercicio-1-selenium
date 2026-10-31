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
import org.openqa.selenium.support.ui.Select;

/**
 * Test Case 1: Register User.
 *
 * Entradas escolhidas por Particionamento em Classes de Equivalencia (CE) e
 * Analise de Valor Limite (AVL). Detalhes no PLANO_DE_TESTES.md.
 */
public class RegistrarUsuarioTest extends BaseTest {

	// --- Formulario de cadastro inicial (Name + Email) ---
	private static final By TITULO_SIGNUP = By.xpath("//h2[normalize-space()='New User Signup!']");
	private static final By SIGNUP_NOME = By.cssSelector("input[data-qa='signup-name']");
	private static final By SIGNUP_EMAIL = By.cssSelector("input[data-qa='signup-email']");
	private static final By SIGNUP_BOTAO = By.cssSelector("button[data-qa='signup-button']");
	private static final By ERRO_EMAIL_EXISTENTE = By.xpath("//p[contains(.,'Email Address already exist!')]");

	// --- Formulario ENTER ACCOUNT INFORMATION ---
	private static final By TITULO_ACCOUNT_INFO = By.xpath(
			"//b[translate(normalize-space(.),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"
					+ "='ENTER ACCOUNT INFORMATION']");

	// --- Resultado ---
	private static final By CONTA_CRIADA = By.cssSelector("h2[data-qa='account-created']");
	private static final By CONTA_EXCLUIDA = By.cssSelector("h2[data-qa='account-deleted']");
	private static final By BOTAO_CONTINUE = By.cssSelector("a[data-qa='continue-button']");
	private static final By LOGADO_COMO = By.xpath("//a[contains(.,'Logged in as')]");
	private static final By LINK_DELETAR = By.cssSelector("a[href='/delete_account']");
	private static final By LINK_LOGOUT = By.cssSelector("a[href='/logout']");

	private static final String SENHA_PADRAO = "Senha@12345";

	// ------------------------------------------------------------------
	// Test Case 1 completo (fluxo feliz, passos 1 a 18)
	// ------------------------------------------------------------------
	@Test
	public void testCase1_RegistrarUsuario() {
		String nome = "Aluno QA";
		String email = emailUnico();

		// Passos 1 a 4: abrir navegador, acessar site, validar home, clicar em Signup / Login
		navegarAteSignupLogin();

		// Passo 5: verificar 'New User Signup!'
		assertTrue(visivel(TITULO_SIGNUP).isDisplayed(), "'New User Signup!' nao esta visivel");

		// Passos 6 e 7: informar nome e e-mail, clicar em 'Signup'
		digitar(SIGNUP_NOME, nome);
		digitar(SIGNUP_EMAIL, email);
		clicar(SIGNUP_BOTAO);

		// Passo 8: verificar 'ENTER ACCOUNT INFORMATION'
		assertTrue(visivel(TITULO_ACCOUNT_INFO).isDisplayed(), "'ENTER ACCOUNT INFORMATION' nao esta visivel");

		// Passos 9 a 13: preencher dados, marcar checkboxes, preencher endereco, criar conta
		preencherDadosDaConta(SENHA_PADRAO, true);
		preencherEndereco("Aluno", "QA");
		clicar(By.cssSelector("button[data-qa='create-account']"));

		// Passo 14: verificar 'ACCOUNT CREATED!'
		assertEquals("ACCOUNT CREATED!", visivel(CONTA_CRIADA).getText().trim().toUpperCase());

		// Passo 15: clicar em 'Continue'
		continuar();

		// Passo 16: verificar 'Logged in as username'
		WebElement logado = visivel(LOGADO_COMO);
		assertTrue(logado.getText().contains(nome),
				"Esperava 'Logged in as " + nome + "', mas veio: " + logado.getText());

		// Passo 17: clicar em 'Delete Account'
		clicar(LINK_DELETAR);

		// Passo 18: verificar 'ACCOUNT DELETED!' e clicar em 'Continue'
		assertEquals("ACCOUNT DELETED!", visivel(CONTA_EXCLUIDA).getText().trim().toUpperCase());
		continuar();
	}

	// ------------------------------------------------------------------
	// Grupo A: nomes VALIDOS (classes validas + valores limite).
	// O fluxo deve avancar ate 'ENTER ACCOUNT INFORMATION' e o nome deve vir preenchido.
	// (nao cria a conta, entao nao ha o que limpar)
	// ------------------------------------------------------------------
	static Stream<Arguments> nomesValidos() {
		return Stream.of(
				// descricao, nome
				Arguments.of("AVL1: nome com 1 caractere (limite inferior)", "A"),
				Arguments.of("AVL2: nome com 2 caracteres (limite inferior + 1)", "Al"),
				Arguments.of("CE1: nome comum com espaco", "Maria Silva"),
				Arguments.of("CE2: nome com acentos", "Jose da Conceicao Araujo"),
				Arguments.of("CE3: nome com numeros", "Aluno 2026"),
				Arguments.of("AVL3: nome longo com 50 caracteres", "N".repeat(50)));
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("nomesValidos")
	public void testSignupComNomeValido(String descricao, String nome) {
		navegarAteSignupLogin();
		assertTrue(visivel(TITULO_SIGNUP).isDisplayed());

		digitar(SIGNUP_NOME, nome);
		digitar(SIGNUP_EMAIL, emailUnico());
		clicar(SIGNUP_BOTAO);

		assertTrue(visivel(TITULO_ACCOUNT_INFO).isDisplayed(),
				"Deveria avancar para 'ENTER ACCOUNT INFORMATION' com: " + descricao);
		assertEquals(nome, visivel(By.id("name")).getAttribute("value"),
				"O nome informado deveria vir preenchido no formulario seguinte");
	}

	// ------------------------------------------------------------------
	// Grupo B: entradas INVALIDAS bloqueadas pela validacao do formulario.
	// O usuario deve permanecer na tela de login/signup.
	// ------------------------------------------------------------------
	static Stream<Arguments> entradasInvalidasSignup() {
		return Stream.of(
				// descricao, nome, email
				Arguments.of("AVL-inv1: nome vazio (0 caracteres)", "", "aluno.valido@exemplo.com"),
				Arguments.of("AVL-inv2: e-mail vazio (0 caracteres)", "Aluno QA", ""),
				Arguments.of("CE-inv1: e-mail sem '@'", "Aluno QA", "alunoexemplo.com"),
				Arguments.of("CE-inv2: e-mail sem dominio apos '@'", "Aluno QA", "aluno@"),
				Arguments.of("CE-inv3: e-mail sem parte local antes de '@'", "Aluno QA", "@exemplo.com"),
				Arguments.of("CE-inv4: e-mail com espaco", "Aluno QA", "aluno qa@exemplo.com"),
				Arguments.of("AVL-inv3: nome e e-mail vazios", "", ""));
	}

	@ParameterizedTest(name = "[{index}] {0}")
	@MethodSource("entradasInvalidasSignup")
	public void testSignupBloqueadoPorValidacaoDoFormulario(String descricao, String nome, String email) {
		navegarAteSignupLogin();
		assertTrue(visivel(TITULO_SIGNUP).isDisplayed());

		digitar(SIGNUP_NOME, nome);
		digitar(SIGNUP_EMAIL, email);
		clicar(SIGNUP_BOTAO);

		assertTrue(driver.getCurrentUrl().contains("/login"),
				"Deveria permanecer em /login, mas esta em: " + driver.getCurrentUrl());
		assertTrue(existe(By.cssSelector(
				"input[data-qa='signup-name']:invalid, input[data-qa='signup-email']:invalid")),
				"Esperava um campo invalido (validacao HTML5) para: " + descricao);
		assertFalse(existe(TITULO_ACCOUNT_INFO),
				"Nao deveria chegar em 'ENTER ACCOUNT INFORMATION': " + descricao);
	}

	// ------------------------------------------------------------------
	// Grupo C: classe invalida "e-mail ja cadastrado" (regra de negocio).
	// Cria a conta, faz logout, tenta cadastrar o mesmo e-mail e depois exclui a conta.
	// ------------------------------------------------------------------
	@Test
	public void testSignupComEmailJaCadastrado() {
		String nome = "Aluno Repetido";
		String email = emailUnico();

		// cria a conta
		navegarAteSignupLogin();
		digitar(SIGNUP_NOME, nome);
		digitar(SIGNUP_EMAIL, email);
		clicar(SIGNUP_BOTAO);
		assertTrue(visivel(TITULO_ACCOUNT_INFO).isDisplayed());
		preencherDadosDaConta(SENHA_PADRAO, false);
		preencherEndereco("Aluno", "Repetido");
		clicar(By.cssSelector("button[data-qa='create-account']"));
		assertEquals("ACCOUNT CREATED!", visivel(CONTA_CRIADA).getText().trim().toUpperCase());
		continuar();
		assertTrue(visivel(LOGADO_COMO).isDisplayed());

		// logout e nova tentativa com o mesmo e-mail
		clicar(LINK_LOGOUT);
		assertTrue(visivel(TITULO_SIGNUP).isDisplayed());
		digitar(SIGNUP_NOME, "Outro Nome");
		digitar(SIGNUP_EMAIL, email);
		clicar(SIGNUP_BOTAO);

		assertEquals("Email Address already exist!", visivel(ERRO_EMAIL_EXISTENTE).getText().trim());

		// limpeza: entra com a conta criada e exclui
		digitar(By.cssSelector("input[data-qa='login-email']"), email);
		digitar(By.cssSelector("input[data-qa='login-password']"), SENHA_PADRAO);
		clicar(By.cssSelector("button[data-qa='login-button']"));
		assertTrue(visivel(LOGADO_COMO).isDisplayed());
		clicar(LINK_DELETAR);
		assertEquals("ACCOUNT DELETED!", visivel(CONTA_EXCLUIDA).getText().trim().toUpperCase());
		continuar();
	}

	// ------------------------------------------------------------------
	// Metodos auxiliares de preenchimento
	// ------------------------------------------------------------------

	/** Passos 9, 10 e 11: titulo, senha, data de nascimento e checkboxes. */
	private void preencherDadosDaConta(String senha, boolean marcarCheckboxes) {
		clicar(By.id("id_gender1")); // Title: Mr.
		digitar(By.id("password"), senha);
		new Select(visivel(By.id("days"))).selectByValue("15");
		new Select(visivel(By.id("months"))).selectByValue("5");
		new Select(visivel(By.id("years"))).selectByValue("1995");
		if (marcarCheckboxes) {
			clicar(By.id("newsletter")); // Sign up for our newsletter!
			clicar(By.id("optin")); // Receive special offers from our partners!
		}
	}

	/** Passo 12: nome, sobrenome, empresa, endereco, pais, estado, cidade, CEP e celular. */
	private void preencherEndereco(String primeiroNome, String sobrenome) {
		digitar(By.id("first_name"), primeiroNome);
		digitar(By.id("last_name"), sobrenome);
		digitar(By.id("company"), "Faculdade QA");
		digitar(By.id("address1"), "Rua das Flores, 100");
		digitar(By.id("address2"), "Apto 12");
		new Select(visivel(By.id("country"))).selectByVisibleText("Canada");
		digitar(By.id("state"), "Ontario");
		digitar(By.id("city"), "Toronto");
		digitar(By.id("zipcode"), "M5V 2T6");
		digitar(By.id("mobile_number"), "+1 416 555 0100");
	}
}