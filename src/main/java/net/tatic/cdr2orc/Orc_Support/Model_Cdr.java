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


public class Model_Cdr
{
	// campos para leitura e escrita
	public int excid;
	public long caller;
	public long called;
	public long serial;
	public byte type;
	public int duracao;
	public long start;
	public int cell_in;
	public int cell_out;


	// Construtor
	public Model_Cdr()
	{
	}


	// Carrega os campos a partir de uma linha string
	// (a linha precisa estar no formato original do arquivo CDR)
	// Ex:
	// 8138;5521987366501;5521908100740;5604248321560188;1;98;2019-10-05T05:56:17;8136378640573727;39;65234;00056;A;calls.txt;f86a89;bf1043;endpoint.ggsn.tatic.com
	public void load(int row, Model_Orc model_orc)
	{
		// converte a linha ORC de volta em campos CDR
		excid = (int)model_orc.excid.vector[row];
		caller = (long)model_orc.caller.vector[row];
		called = (long)model_orc.called.vector[row];
		serial = (long)model_orc.serial.vector[row];
		type = (byte)model_orc.type.vector[row];
		duracao = (int)model_orc.duracao.vector[row];
		start = (long)model_orc.start.vector[row];
		cell_in = (int)model_orc.cell_in.vector[row];
		cell_out = (int)model_orc.cell_out.vector[row];
	}
}
