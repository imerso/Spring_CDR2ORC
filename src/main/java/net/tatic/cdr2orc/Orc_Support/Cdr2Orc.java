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

// https://orc.apache.org/docs/core-java.html
//
// Prova de conceito de um gerenciador de CDR em formato ORC.
// Converte um arquivo CDR em um banco ORC
//
// por Vander, Dez/2019
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.OrcFile;
import org.apache.orc.Writer;
import java.io.*;


public class Cdr2Orc
{
	// Configuracao OrcFile
	Configuration conf;

	// Modelo da tabela
	Model_Orc model;


	// Construtor
	public Cdr2Orc()
	{
		conf = new Configuration();
		model = new Model_Orc();
	}


	// Parametros:
	//
	// String cdr_filename: caminho completo do arquivo origem em formato TXT
	// String orc_filename: caminho completo do arquivo destino em formato ORC
	public boolean convert(String cdr_filename, String orc_filename)
	{
		// verifica se o arquivo de origem existe mesmo
		if (!new File(cdr_filename).exists()) return false;

		// verifica se o db em formato ORC existe
		// (nessa versao conceito, precisa ser um novo ORC a cada chamada)
		if (new File(orc_filename).exists()) return false;

		try
		{
			// cria um novo banco ORC
			Writer writer = OrcFile.createWriter(new Path(orc_filename), OrcFile.writerOptions(conf).setSchema(model.getSchema()));

			// pega batch para escrita
			VectorizedRowBatch batch = model.getBatch();

			// abre o arquivo CDR
			BufferedReader reader = new BufferedReader(new FileReader(cdr_filename));

			// primeira linha do CDR
			String line = reader.readLine();

			while (line != null)
			{
				int row = batch.size++;

				// converte a linha CDR em campos ORC
				model.load(row, line);

				// descarrega batch sempre que necessario
				if (batch.size == batch.getMaxSize())
				{
					writer.addRowBatch(batch);
					batch.reset();
				}

				// proxima linha do CDR
				line = reader.readLine();
			}

			// descarrega ultimo batch
			if (batch.size != 0)
			{
				writer.addRowBatch(batch);
				batch.reset();
			}
			writer.close();
			reader.close();
		}
		catch(IOException e)
		{
			System.out.print(e.getMessage());
			return false;
		}

		return true;
	}
}
