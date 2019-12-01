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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.RecordReader;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.hive.ql.io.sarg.PredicateLeaf;
import org.apache.hadoop.hive.ql.io.sarg.SearchArgumentFactory;
import java.io.*;
import java.time.*;
import java.util.ArrayList;


public class OrcSearch
{
	// Configuracao OrcFile
	Configuration conf;

	// Modelo da tabela
	Model_Orc model_orc;

	// Lista de arquivos ORC abertos
	ArrayList<Reader> orc_files;


	// Construtor
	public OrcSearch(String orc_path)
	{
		conf = new Configuration();
		model_orc = new Model_Orc();
		orc_files = new ArrayList<Reader>();

		// abre todos os arquivos ORC importados
		open_all(orc_path);
	}


	// Varre o diretorio ORC
	// e abre todos os arquivos
	void open_all(String orc_path)
	{
		// filtro de arquivos ".txt"
		File file_orc = new File(orc_path);
		FilenameFilter filter = new FilenameFilter() { @Override public boolean accept(File f, String name) { return name.endsWith(".orc"); }};

		String[] filenames = file_orc.list(filter);
		for (String filename : filenames)
		{
			String orc_filename = orc_path + "/" + FilenameUtils.getName(filename);
			open(orc_filename);
		}
	}


	// Abre um arquivo individual,
	// e o adiciona 'a lista de arquivos pesquisaveis
	public void open(String orc_file)
	{
		try
		{
			System.out.print("Abrindo " + orc_file + ": ");
			Reader reader = OrcFile.createReader(new Path(orc_file), OrcFile.readerOptions(conf));
			orc_files.add(reader);
			long used_memory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			System.out.println("OK - memoria usada: " + used_memory);
		}
		catch (IOException e)
		{
			System.out.println("ERRO: " + e.getMessage());
		}
	}


	// Retorna lista de chamadas de um numero
	// dentro do periodo especificado.
	//
	//
	//
	// Parametros:
	//
	// String caller: numero originador das chamadas
	// Date start...: data inicial das chamadas
	// Date end.....: data final das chamadas
	public ArrayList<Model_Cdr> search_caller(String caller, LocalDateTime start, LocalDateTime end)
	{
		ArrayList<Model_Cdr> model_list = new ArrayList<Model_Cdr>();

		try
		{
			final long long_caller = Long.parseLong(caller);
			long instant_start = start.toInstant(ZoneOffset.ofHours(-3)).getEpochSecond();
			long instant_end = end.toInstant(ZoneOffset.ofHours(-3)).getEpochSecond();

			for (Reader reader : orc_files)
			{
				Reader.Options readerOptions = new Reader.Options(conf)
					.searchArgument(
					SearchArgumentFactory
							.newBuilder()
							.startAnd()
							.equals("caller", PredicateLeaf.Type.LONG, long_caller)
							.between("start", PredicateLeaf.Type.LONG, instant_start, instant_end)
							.end()
							.build(),
							new String[] { "caller" }
					);

				RecordReader rows = reader.rows(readerOptions);
				VectorizedRowBatch read_batch = model_orc.getBatch();

				while (rows.nextBatch(read_batch))
				{
					for (int r=0; r<read_batch.size; r++)
					{
						long this_caller = (long)model_orc.caller.vector[r];
						long this_start = (long)model_orc.start.vector[r];
						if (this_caller == long_caller && this_start >= instant_start && this_start <= instant_end)
						{
							Model_Cdr model_cdr = new Model_Cdr();
							model_cdr.load(r, model_orc);
							model_list.add(model_cdr);
						}
					}
				}
				rows.close();
			}

			return model_list;
		}
		catch (Exception e)
		{
			System.out.println("[OrcSearch] ERRO: " + e.getMessage());
			return null;
		}
	}


	public boolean search(String field_name, long search_value)
	{
		try
		{
			final long Search_Caller = 5521987366501L;

			Reader reader = OrcFile.createReader(new Path("orc/..."), OrcFile.readerOptions(conf));

			Reader.Options readerOptions = new Reader.Options(conf)
				.searchArgument(
				SearchArgumentFactory
						.newBuilder()
						.equals(field_name, PredicateLeaf.Type.LONG, search_value)
						.build(),
						new String[] { field_name }
				);

			RecordReader rows = reader.rows(readerOptions);
			VectorizedRowBatch read_batch = model_orc.getBatch();

			int right = 0;
			int miss = 0;

			while (rows.nextBatch(read_batch))
			{
				for (int r=0; r<read_batch.size; r++)
				{
					long this_caller = (long)model_orc.caller.vector[r];
					if (this_caller == Search_Caller)
					{
						System.out.println("Caller: " + ((long)model_orc.caller.vector[r]));
						right++;
					}
					else
					{
						miss++;
					}
				}
			}
			rows.close();

			System.out.println("Encontrados: " + right + " em um buffer de " + (miss+right) + " registros.");
			System.out.println("Finalizado.");
			return true;
		}
		catch (Exception e)
		{
			System.out.println("ERRO: " + e.getMessage());
			return false;
		}
	}
}
