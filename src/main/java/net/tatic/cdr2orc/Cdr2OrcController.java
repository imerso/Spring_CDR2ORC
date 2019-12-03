// Prova de Conceito de um Gerenciador de CDR em formato ORC.
//
// CdrMonitor monitora um diretório específico onde serão postados
// novos arquivos CDR.
//
// Ao ser detectado um novo arquivo CDR, o mesmo será convertido por
// Cdr2Orc para o formato ORC, e salvo em um segundo diretório específico.
//
// Finalizada a conversão, CdrMonitor moverá o arquivo CDR original para um
// terceiro diretório, onde ficará arquivado.
//
// A aplicação Spring oferece uma interface de consultas pela web.
//
// -----------------------------------------------------------
//
// Seguindo Documentação ORC em
// https://orc.apache.org/docs/core-java.html
//
// Seguindo desafio em
// https://bitbucket.org/tatic_rhdev/desafioengenheiro
//
// por Vander, Dez/2019

package net.tatic.cdr2orc;

import com.google.gson.Gson;

import net.tatic.cdr2orc.Orc_Support.CdrMonitor;
import net.tatic.cdr2orc.Orc_Support.Model_Cdr;
import net.tatic.cdr2orc.Orc_Support.Model_Orc;
import net.tatic.cdr2orc.Orc_Support.OrcSearch;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


//@RestController
@Controller
public class Cdr2OrcController
{
	// Caminho onde serão postados novos arquivos CDR
	@Value("${CDR_Path:data/CDR}")
    private String CDR_Path;

	// Caminho onde serão gravados os arquivos ORC gerados
	@Value("${ORC_Path:data/ORC}")
    private String ORC_Path;

    // Caminho onde serão movidos os CDR após convertidos
	@Value("${ARC_Path:data/ARC}")
    private String ARC_Path;

	// Instância do monitor/conversor de arquivos CDR
	private CdrMonitor monitor;

	// Mantém um OrcSearch (buscas) sempre disponível
	private OrcSearch searcher;
	

	@PostConstruct
    public void Init()
    {
		System.out.println("[Controller] CDR_Path: " + CDR_Path);
		System.out.println("[Controller] ORC_Path: " + ORC_Path);
		System.out.println("[Controller] ARC_Path: " + ARC_Path);

		// inicia CdrMonitor (que criará sua própria thread de monitoramento)
		String[] paths = new String[] { CDR_Path, ORC_Path, ARC_Path };
		try
		{
			monitor = new CdrMonitor(paths);
			searcher = monitor.getSearcher();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    }

	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index()
	{
		return "index.html";
	}
	

	// Busca pelas chamadas de um número especificado,
	// dentro de um período.
	//
	// Exemplo:
	//
	// http://localhost:8080/calls/5521987366501/2019-10-01T00:00/2019-10-31T23:59
	//
	@ResponseBody
	@RequestMapping(value = "/calls/{caller}/{start}/{end}", method = RequestMethod.GET)
	public String searchCaller(
								@PathVariable String caller, 
								@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
								LocalDateTime start,
								@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
								LocalDateTime end
								)
	{
		// anota o tempo de início
		long start_millis = System.currentTimeMillis();

		// executa a busca
		ArrayList<Model_Cdr> list = searcher.search_caller(caller, start, end);

		// calcula o tempo de execução da busca
		long end_millis = System.currentTimeMillis();
		long elapsed = end_millis - start_millis;
		System.out.println("Tempo de busca: " + elapsed + "ms");

		// retorna um html rudimentar.
		String json = new Gson().toJson(list);
		return json;
	}
}
