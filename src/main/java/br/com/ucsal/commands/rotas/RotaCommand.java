package br.com.ucsal.commands.rotas;

import java.io.IOException;

import br.com.ucsal.annotations.Inject;
import br.com.ucsal.commands.Command;
import br.com.ucsal.controller.InicializadorListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class RotaCommand implements Command{
	
	private InicializadorListener inicializadorListener;
	
	public RotaCommand(InicializadorListener inicializadorListener) {
		this.inicializadorListener = inicializadorListener;
	}
	

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getRequestURI().replaceAll("//", "/");
		System.out.println("Path recebido: " + path);

		Command command = inicializadorListener.getCommand(path);
		if (command != null) {
			command.execute(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Rota não encontrada.");
		}
	}

	public InicializadorListener getInicializadorListener() {
		return inicializadorListener;
	}
	
	
	
	
}
