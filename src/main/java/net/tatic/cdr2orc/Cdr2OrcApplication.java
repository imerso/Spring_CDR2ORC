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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Cdr2OrcApplication
{
	// Entrada da Aplicação
	public static void main(String[] args)
	{
		SpringApplication.run(Cdr2OrcApplication.class, args);
	}
}
