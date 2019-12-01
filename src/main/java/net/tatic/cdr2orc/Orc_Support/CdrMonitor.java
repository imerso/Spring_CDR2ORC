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

package net.tatic.cdr2orc.Orc_Support;

import org.apache.commons.io.FilenameUtils;
import java.io.*;
import java.time.*;
import java.nio.file.*;
import java.util.ArrayList;


public class CdrMonitor
{
	private OrcSearch searcher;
	public OrcSearch getSearcher() { return searcher; }
	
	// Verifica se o diretorio existe,
	// e se nao existir, cria.
	boolean checkDir(String dir)
	{
		try
		{
			File file = new File(dir);
			if (!file.exists()) file.mkdirs();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}


	// Move arquivo entre pastas;
	// se o arquivo destino ja' existir,
	// renomeia automaticamente.
	void move(String src, String dst)
	{
		int idx = 0;
		String dst_idx = dst;

		try
		{
			while (new File(dst_idx).exists())
			{
				idx++;
				dst_idx = FilenameUtils.getBaseName(dst) + idx + "." + FilenameUtils.getExtension(dst);
				System.out.println("Alternativo: " + dst_idx);
			}

			// move o arquivo processado para o diretorio de arquivamento
			Files.move(Paths.get(src), Paths.get(dst_idx));
		}
		catch (IOException e)
		{
			System.out.println("FALHA AO MOVER " + src + " para " + dst);
		}
	}


	// Monitora o diretorio CDR
	// e importa novos arquivos para ORC
	//
	// NOTA IMPORTANTE: arquivos ORCFILE a principio sao WRITE_ONCE,
	// ou seja, nao ha' como adicionar novos registros nos mesmos.
	//
	// Pesquisando, vi que existe a possibilidade usando ACID,
	// mas isso requer um pouco mais de tempo e implementacao, pois
	// nao e' tao direto quanto um SQL tradicional, por exemplo.
	//
	// Portanto, nessa versao conceito, cada arquivo CDR e'
	// importado para um arquivo ORC diferente, o que impacta
	// nas buscas tambem.
	public CdrMonitor(String[] args) throws java.io.IOException
	{
		System.out.println("TATIC - GERENCIADOR DE CDR v0.1");

		// verifica se os parametros foram passados corretamente
		if (args.length != 3)
		{
			System.out.println("SINTAXE:");
			System.out.println("Main <dir_CDR> <dir_ORC> <dir_ARC>");
			System.out.println("");
			System.out.println("<dir_CDR>: diretorio onde os arquivos CDR (.txt) serao recebidos");
			System.out.println("<dir_ORC>: diretorio onde os arquivos convertidos em ORC serao gravados");
			System.out.println("<dir_ARC>: diretorio para onde os arquivos CDR (.txt) serao movidos apos convertidos");
			System.exit(-1);
		}

		// verifica se os diretorios existem,
		// e cria se for necessario

		for (int i=0; i<3; i++)
		{
			if (!checkDir(args[i]))
			{
				System.out.println("Erro ao criar diretorio " + args[0]);
				System.exit(-2);
			}
		}


		searcher = new OrcSearch(args[1]);


		// Monitora o diretorio CDR,
		// e sempre que surgir um novo arquivo, converte para ORC.
		new Thread()
		{
			// instancia o conversor
			Cdr2Orc converter = new Cdr2Orc();

			// filtro de arquivos ".txt"
			File file_cdr = new File(args[0]);
			FilenameFilter filter = new FilenameFilter() { @Override public boolean accept(File f, String name) { return name.endsWith(".txt"); }};

			// A cada N segundos,
			// verifica se ha' novos arquivos TXT no diretorio CDR,
			// e se tiver, converte todos.
			@Override
			public void run()
			{
				System.out.println("Monitorando " + args[0] + "...");

				while(true)
				{
					String[] filenames = file_cdr.list(filter);
					for (String filename : filenames)
					{
						String cdr_filename = args[0] + "/" + filename;
						String orc_filename = args[1] + "/" + FilenameUtils.getBaseName(filename) + ".orc";
						System.out.print("Convertendo: " + cdr_filename + " -> " + orc_filename + ": ");

						if (!converter.convert(cdr_filename, orc_filename))
						{
							System.out.println("ERRO");

							// move para a pasta de processados, mas com extensao .err
							String arc_filename = args[2] + "/" + FilenameUtils.getBaseName(cdr_filename) + ".err";
							move(cdr_filename, arc_filename);
						}
						else
						{
							System.out.println("OK");

							// move para a pasta de processados
							String arc_filename = args[2] + "/" + FilenameUtils.getName(cdr_filename);
							move(cdr_filename, arc_filename);

							// abre o novo arquivo ORC
							searcher.open(orc_filename);
						}

						try
						{
							// Respira um pouquinho antes de converter outro
							Thread.sleep(500);
						}
						catch (InterruptedException e)
						{
							interrupt();
						}
					}

					try
					{
						// Respira
						Thread.sleep(2000);
					}
					catch (InterruptedException e)
					{
						interrupt();
					}
				}
			}
		}.start();
	}
}

