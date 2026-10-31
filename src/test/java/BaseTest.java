import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Classe base: cria/encerra o driver (mesmo padrao do GoogleTest da professora)
 * e concentra metodos auxiliares usados pelos testes.
 */
public abstract class BaseTest {

	protected static final String URL_BASE = "https://automationexercise.com/";

	/** Navegador usado quando nenhum -Dbrowser e informado: "chrome" ou "edge". */
	private static final String NAVEGADOR_PADRAO = "edge";

	/**
	 * Opcional: caminho do msedgedriver.exe baixado manualmente, caso o download
	 * automatico falhe. Exemplo: "C:\\drivers\\msedgedriver.exe". Deixe vazio para usar o automatico.
	 */
	private static final String CAMINHO_EDGEDRIVER = "C:\\drivers\\msedgedriver.exe";

	protected WebDriver driver;
	protected WebDriverWait wait;

	static {
		// silencia o aviso inofensivo de CDP (Selenium 4.25 x Edge 156)
		java.util.logging.Logger.getLogger("org.openqa.selenium.devtools.CdpVersionFinder")
				.setLevel(java.util.logging.Level.OFF);
		java.util.logging.Logger.getLogger("org.openqa.selenium.chromium.ChromiumDriver")
				.setLevel(java.util.logging.Level.OFF);
	}

	// ---------------------------------------------------------------
	// Ciclo de vida (Passo 1 dos Test Cases: "Launch browser")
	// ---------------------------------------------------------------
	@BeforeEach
	public void createDriver() {
		// Pelo terminal: mvn test -Dbrowser=edge  ou  mvn test -Dbrowser=chrome
		// No VS Code (botao ▶): mude NAVEGADOR_PADRAO acima.
		String navegador = System.getProperty("browser", NAVEGADOR_PADRAO).trim().toLowerCase();

		// Se voce baixou o msedgedriver manualmente, informe o caminho em CAMINHO_EDGEDRIVER.
		if (!CAMINHO_EDGEDRIVER.isBlank()) {
			System.setProperty("webdriver.edge.driver", CAMINHO_EDGEDRIVER);
		}

		driver = navegador.equals("edge") ? criarEdge() : criarChrome();
		driver.manage().window().maximize();
		wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	}

	private EdgeOptions opcoesEdge() {
		EdgeOptions options = new EdgeOptions();
		options.addArguments("--disable-notifications", "--disable-popup-blocking");
		if (Boolean.getBoolean("headless")) { // mvn test -Dheadless=true
			options.addArguments("--headless=new", "--window-size=1920,1080");
		}
		return options;
	}

	/**
	 * Edge: tenta (1) o Selenium Manager, embutido no Selenium, e (2) o WebDriverManager.
	 * Se as duas falharem, a mensagem de erro mostra o motivo de cada uma.
	 */
	private WebDriver criarEdge() {
		List<String> erros = new ArrayList<>();
		try {
			return new EdgeDriver(opcoesEdge());
		} catch (RuntimeException e) {
			erros.add("[Selenium Manager] " + resumo(e));
		}
		try {
			return WebDriverManager.edgedriver().capabilities(opcoesEdge()).create();
		} catch (RuntimeException e) {
			erros.add("[WebDriverManager] " + resumo(e));
		}
		throw new IllegalStateException("Nao foi possivel iniciar o Edge.\n" + String.join("\n---\n", erros));
	}

	private WebDriver criarChrome() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--disable-notifications", "--disable-popup-blocking");
		if (Boolean.getBoolean("headless")) {
			options.addArguments("--headless=new", "--window-size=1920,1080");
		}
		return WebDriverManager.chromedriver().capabilities(options).create();
	}

	/** Causa raiz da excecao, em texto curto. */
	private String resumo(Throwable e) {
		Throwable causa = e;
		while (causa.getCause() != null) {
			causa = causa.getCause();
		}
		String msg = String.valueOf(causa.getMessage());
		if (msg.length() > 700) {
			msg = msg.substring(0, 700) + "...";
		}
		return causa.getClass().getSimpleName() + ": " + msg;
	}

	@AfterEach
	public void quitDriver() {
		if (driver != null) {
			driver.quit();
		}
	}

	// ---------------------------------------------------------------
	// Passos comuns aos dois Test Cases (passos 2, 3 e 4)
	// ---------------------------------------------------------------

	/** Passos 2, 3 e 4: abre o site, verifica a home e clica em 'Signup / Login'. */
	protected void navegarAteSignupLogin() {
		driver.get(URL_BASE);
		aceitarCookies();
		removerAnuncios();

		// Passo 3: home page visivel
		assertTrue(driver.getTitle().contains("Automation Exercise"),
				"Titulo inesperado da home: " + driver.getTitle());
		WebElement logo = visivel(By.cssSelector("div.logo img"));
		assertTrue(logo.isDisplayed(), "Logo da home nao esta visivel");

		// Passo 4: clicar em 'Signup / Login'
		clicar(By.cssSelector("a[href='/login']"));
	}

	// ---------------------------------------------------------------
	// Helpers
	// ---------------------------------------------------------------

	protected WebElement visivel(By locator) {
		try {
			return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		} catch (TimeoutException e) {
			// mostra onde o teste parou, para facilitar achar a causa
			String cabecalho = existe(By.id("header"))
					? driver.findElement(By.id("header")).getText().replaceAll("\\s+", " ")
					: "(pagina sem #header)";
			throw new AssertionError("Elemento nao ficou visivel: " + locator
					+ "\n  URL atual: " + driver.getCurrentUrl()
					+ "\n  Titulo da pagina: " + driver.getTitle()
					+ "\n  Texto do cabecalho: " + cabecalho, e);
		}
	}

	/**
	 * Clica em 'Continue' e garante que a navegacao aconteceu. Se um anuncio
	 * intersticial do Google (#google_vignette) segurar a navegacao, vai direto para a home.
	 */
	protected void continuar() {
		String urlAntes = driver.getCurrentUrl();
		clicar(By.cssSelector("a[data-qa='continue-button']"));
		try {
			new WebDriverWait(driver, Duration.ofSeconds(5)).until(d ->
					!d.getCurrentUrl().equals(urlAntes) && !d.getCurrentUrl().contains("google_vignette"));
		} catch (TimeoutException e) {
			driver.get(URL_BASE);
		}
	}

	protected boolean existe(By locator) {
		return !driver.findElements(locator).isEmpty();
	}

	protected void digitar(By locator, String texto) {
		WebElement el = visivel(locator);
		el.clear();
		el.sendKeys(texto);
	}

	/**
	 * Clique tolerante a anuncios: o site exibe overlays do Google Ads que podem
	 * interceptar o clique. Se isso acontecer, remove os anuncios e clica via JS.
	 */
	protected void clicar(By locator) {
		removerAnuncios();
		WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
		try {
			wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
		} catch (ElementClickInterceptedException | TimeoutException e) {
			removerAnuncios();
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(locator));
		}
	}

	protected void removerAnuncios() {
		((JavascriptExecutor) driver).executeScript(
				"document.querySelectorAll("
						+ "'ins.adsbygoogle, [class*=\"adsbygoogle\"], iframe[id^=\"aswift\"], "
						+ "iframe[id^=\"google_ads\"], .google-auto-placed, #ad_position_box')"
						+ ".forEach(function(e){e.remove();});");
	}

	/** Fecha o banner de consentimento de cookies, caso apareca. */
	protected void aceitarCookies() {
		By botao = By.cssSelector("button.fc-cta-consent");
		if (existe(botao)) {
			try {
				driver.findElement(botao).click();
			} catch (Exception ignorado) {
				// banner nao e essencial para o teste
			}
		}
	}

	/** Localiza um elemento pelo texto, ignorando maiusculas/minusculas (CSS do site usa uppercase). */
	protected By textoIgnorandoCaixa(String tag, String textoEmMaiusculas) {
		return By.xpath("//" + tag + "[translate(normalize-space(.),"
				+ "'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')='" + textoEmMaiusculas + "']");
	}

	/** Gera e-mail valido e unico a cada execucao (o site nao aceita e-mail repetido). */
	protected String emailUnico() {
		return "aluno.qa." + System.currentTimeMillis() + "@exemplo.com";
	}
}