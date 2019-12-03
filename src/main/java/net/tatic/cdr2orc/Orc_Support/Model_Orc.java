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

import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.orc.TypeDescription;
import java.time.*;


public class Model_Orc
{
	// Esquema do modelo
	TypeDescription schema;

	// campos para leitura e escrita
	VectorizedRowBatch batch;
	public LongColumnVector excid;
	public LongColumnVector caller;
	public LongColumnVector called;
	public LongColumnVector serial;
	public LongColumnVector type;
	public LongColumnVector duracao;
	public LongColumnVector start;
	public LongColumnVector cell_in;
	public LongColumnVector cell_out;


	public TypeDescription getSchema() { return schema; }
	public VectorizedRowBatch getBatch() { return batch; }

	// TimeZone GMT-3 para conversões
	public static final ZoneId Zone_SaoPaulo = ZoneId.of("America/Sao_Paulo");
	public static final ZoneId Zone_UTC = ZoneId.of("UTC");

	// Converte data de uma timezone para outra
	// https://stackoverflow.com/questions/34626382/convert-localdatetime-to-localdatetime-in-utc
	public static LocalDateTime toZone(final LocalDateTime time, final ZoneId fromZone, final ZoneId toZone)
    {
        final ZonedDateTime zonedtime = time.atZone(fromZone);
        final ZonedDateTime converted = zonedtime.withZoneSameInstant(toZone);
        return converted.toLocalDateTime();
    }


	// Construtor
	public Model_Orc()
	{
		// EXCHANGE_ID;CALLING_NUMBER;CALLED_NUMBER;SERIAL_NUMBER;CALL_TYPE;CALL_DURATION;START_TIME;IMSI;SWITCH;CELL_IN;CELL_OUT;TECNOLOGIA;FILE_NAME;FIRST_LAC;LAST_LAC;GGSN_ADDRESS
		// 8138;5521987366501;5521908100740;5604248321560188;1;98;2019-10-05T05:56:17;8136378640573727;39;65234;00056;A;calls.txt;f86a89;bf1043;endpoint.ggsn.tatic.com

		//schema = TypeDescription.fromString("struct<excid:int,caller:bigint,called:bigint,serial:bigint,type:tinyint,duration:int,start:bigint,cin:int,cout:int>");

		// prepara esquema
		schema = TypeDescription.createStruct()
			.addField("excid", TypeDescription.createInt())
			.addField("caller", TypeDescription.createLong())
			.addField("called", TypeDescription.createLong())
			.addField("serial", TypeDescription.createLong())
			.addField("type", TypeDescription.createLong())
			.addField("duration", TypeDescription.createInt())
			.addField("start", TypeDescription.createLong())
			.addField("cell_in", TypeDescription.createInt())
			.addField("cell_out", TypeDescription.createInt());

		// prepara batch
		batch = schema.createRowBatch();
		excid = (LongColumnVector) batch.cols[0];
		caller = (LongColumnVector) batch.cols[1];
		called = (LongColumnVector) batch.cols[2];
		serial = (LongColumnVector) batch.cols[3];
		type = (LongColumnVector) batch.cols[4];
		duracao = (LongColumnVector) batch.cols[5];
		start = (LongColumnVector) batch.cols[6];
		cell_in = (LongColumnVector) batch.cols[7];
		cell_out = (LongColumnVector) batch.cols[8];
	}


	// Carrega os campos a partir de uma linha string
	// (a linha precisa estar no formato original do arquivo CDR)
	// Ex:
	// 8138;5521987366501;5521908100740;5604248321560188;1;98;2019-10-05T05:56:17;8136378640573727;39;65234;00056;A;calls.txt;f86a89;bf1043;endpoint.ggsn.tatic.com
	public void load(int row, String line)
	{
		// converte a linha CDR em campos ORC
		String[] fields = line.split(";");
		excid.vector[row] = Integer.parseInt(fields[0]);
		caller.vector[row] = Long.parseLong(fields[1]);
		called.vector[row] = Long.parseLong(fields[2]);
		serial.vector[row] = Long.parseLong(fields[3]);
		type.vector[row] = Integer.parseInt(fields[4]);
		duracao.vector[row] = Long.parseLong(fields[5]);

		// converte datas em UTC
		// assumindo que estão sempre em GMT-3
		// ex: 2019-10-23T13:23:10
		LocalDateTime local_start = LocalDateTime.parse(fields[6]); 
		Instant instant = local_start.atZone(Zone_SaoPaulo).toInstant();

		start.vector[row] = instant.getEpochSecond();
		cell_in.vector[row] = Long.parseLong(fields[7]);
		cell_out.vector[row] = Long.parseLong(fields[8]);
	}
}
