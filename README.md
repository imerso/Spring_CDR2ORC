# Spring_CDR2ORC
Monitora e converte CDR.TXT para ORC e permite pesquisas simples nas bases ORC.

Baseado em Spring Web e ORC 1.7.0.

Estrutura da aplicação:

```
CDR2ORC
   +
   |
   +------ CDR2ORC_lib
   |
   +------ data
             +
             |
             +------ CDR (novos arquivos TXT devem ser copiados aqui)
             |
             +------ ORC (arquivos ORC serão gerados aqui)
             |
             +------ ARC (arquivos CDR processador serão movidos para cá)
```

Ao ser iniciada, a aplicação carregará todos os arquivos ORC presentes no
sub-diretório data/ORC, e chamadas REST poderão ser feitas para executar
consultas pela web.

Por enquanto apenas um tipo de consulta pode ser efetuado:

- Chamadas de um número por período:

	http://<url_servidor>/calls/<número_chamador>/<início YYYY-MM-DDTHH:MM>/<fim YYYY-MM-DDTHH:MM>

Exemplo:

	http://localhost:8080/calls/5521987366501/2019-10-01T00:00/2019-10-31T23:59

Para importar novos arquivos CDR TXT, basta copiá-los para o
diretório data/CDR. A aplicação monitora esse diretório, e sempre que aparece um
novo arquivo, o mesmo é convertido para ORC automaticamente, passando a fazer parte
das próximas buscas.


