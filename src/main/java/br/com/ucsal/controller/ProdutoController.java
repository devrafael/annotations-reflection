package br.com.ucsal.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import br.com.ucsal.commands.rotas.RotaCommand;


@WebServlet("/view/*")
public class ProdutoController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private RotaCommand registradorRota;
	
	public ProdutoController() {
		super();
	}

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			registradorRota = new RotaCommand(new InicializadorListener());
			registradorRota.getInicializadorListener().processarRotas("br.com.ucsal");
		} catch (Exception e) {
			throw new ServletException("Erro ao inicializar rotas.", e);
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();
		System.out.println(path);

		try {
			registradorRota.execute(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao processar a requisição.");
		}
	}
}
