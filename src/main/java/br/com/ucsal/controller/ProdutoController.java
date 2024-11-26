package br.com.ucsal.controller;

import java.io.IOException;
import java.util.Map;

import br.com.ucsal.commands.Command;
import br.com.ucsal.commands.rotas.RotaCommand;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/view/*")
public class ProdutoController extends HttpServlet {
	private static final long serialVersionUID = 1L;


	private Map<String, Command> rotaCommand;


	@Override
	public void init() throws ServletException {
		super.init();
		try {
//			rotaCommand = new RotaCommand(new InicializadorListener());
//			rotaCommand.getInicializadorListener().processarRotas("br.com.ucsal");
		
		
		} catch (Exception e) {
			throw new ServletException("Erro ao inicializar rotas.", e);
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		rotaCommand = (Map<String, Command>) request.getServletContext().getAttribute("command");
		String path = request.getPathInfo();
		System.out.println(path);

		try {
			rotaCommand.get(path).execute(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao processar a requisição.");
		}
	}

}
