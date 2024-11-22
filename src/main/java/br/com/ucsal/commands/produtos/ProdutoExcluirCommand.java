package br.com.ucsal.commands.produtos;

import java.io.IOException;

import br.com.ucsal.annotations.Inject;
import br.com.ucsal.annotations.Rota;
import br.com.ucsal.annotations.Singleton;
import br.com.ucsal.commands.Command;
import br.com.ucsal.persistencia.HSQLProdutoRepository;
import br.com.ucsal.persistencia.PersistenciaFactory;
import br.com.ucsal.service.ProdutoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Rota(path="/excluirProduto")
@Singleton
public class ProdutoExcluirCommand implements Command {
	private static final long serialVersionUID = 1L;
	
	@Inject
	private ProdutoService produtoService;

	public ProdutoExcluirCommand() {}

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Lógica de exclusão
		Integer id = Integer.parseInt(request.getParameter("id"));
		produtoService.removerProduto(id);
		response.sendRedirect("listarProdutos");
	}

}
