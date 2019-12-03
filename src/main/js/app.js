'use strict';

const React = require('react');
const ReactDOM = require('react-dom');

//
class App extends React.Component
{
	constructor(props)
	{
		super(props);

		this.state =
		{
			results: [],
		};
	}


	componentDidMount()
	{
	}


	render()
	{
		var _this = this;
		var list = this.state.results.map((res, i) =>
		{
			/*
			<td>Data</td>
			<td>Originador</td>
			<td>Receptor</td>
			<td>Antena Saída</td>
			<td>Antena Entrada</td>
			<td>Duração</td>
			*/

			return (
			<tr key={i}>
			<td>{res.start}</td>
			<td>{res.caller}</td>
			<td>{res.called}</td>
			<td>{res.cell_in}</td>
			<td>{res.cell_out}</td>
			<td>{res.duracao}</td>
			</tr>
			)
		});
		
		return (
			<div>
				<h1>Consulta Chamadas</h1>
				
				<form id="FrmSearchCaller" name="FrmSearchCaller" method="" autoComplete="off" action="">
				<table>
					<tbody>
					<tr>
						<td>
							Data Inicial<br/>
							<input type="date" id="_Start" name="_Start" required></input>
						</td>
						<td>
							Data Final<br/>
							<input type="date" id="_End" name="_End" required></input>
						</td>
						<td>
							Número Originador<br/>
							<input type="number" min="11111111" max="9999999999999" id="_Caller" name="_Caller" required></input>
						</td>
					</tr>
					</tbody>
				</table>
				</form>
				<div>
					<button
					onClick = { () =>
						{
							var caller = document.FrmSearchCaller.elements["_Caller"].value;
							var start = document.FrmSearchCaller.elements["_Start"].value + "T00:00";
							var end = document.FrmSearchCaller.elements["_End"].value + "T23:59";
							var url = "/calls/" + caller + "/" + start + "/" + end;

							if (caller.length < 8 || start.length != 16 || end.length != 16)
							{
								alert("Por favor, preencha todos os campos.");
								return;
							}
							
							
							fetch(url,
							{ 
								method: 'GET',
								headers:
								{
									'Accept': 'application/json',
									'Content-Type': 'application/json'
								}
							})
							.then(function(res)
							{
								// success
								res.json().then(info =>
								{
									for (var i=0; i<info.length; i++)
									{
										// converte de epochtime para date
										info[i].start = new Date(info[i].start*1000).toString("dd/mm/yyyy");

										// converte de segundos para hora, minutos e segundos
										var hours = Math.floor(info[i].duracao / 3600);
										var minutes = Math.floor((info[i].duracao % 3600) / 60);
										var seconds = (info[i].duracao % 3600) % 60;
										info[i].duracao = hours + "h " + minutes + "m " + seconds + "s";
									}
									_this.setState({results: info});
									console.log(_this.state.results.length);
								});
							})
							.catch(error =>
							{
								// failure
								console.log("ERRO: " + error);
								_this.setState({results: []});
							});
						}
					}
					>
						Pesquisar
					</button>
				</div>
				
				{this.state.results.length > 0 &&
				(
						<div>
						<h2>{this.state.results.length} Chamadas:</h2>
						<table><tbody>
						<tr>
						<td>Data</td>
						<td>Originador</td>
						<td>Receptor</td>
						<td>Antena Saída</td>
						<td>Antena Entrada</td>
						<td>Duração</td>
						</tr>
						{list}
						</tbody></table>
						</div>
				)}
				
			</div>
		)
	}
}

ReactDOM.render
(
	<App />,
	document.getElementById('react')
)
