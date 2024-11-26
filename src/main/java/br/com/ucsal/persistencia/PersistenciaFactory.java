package br.com.ucsal.persistencia;

import br.com.ucsal.controller.InicializadorListener;

public class PersistenciaFactory {

	public static final int PRODUTO_MEMORIA = 0;
	public static final int PRODUTO_HSQL = 1;
	
	
	public static ProdutoRepository<?, ?> getProdutoRepository(int type) {
		ProdutoRepository<?, ?> produtoRepository;
		switch (type) {
		case PRODUTO_MEMORIA: {
			produtoRepository = InicializadorListener.getInstanceSingleton(MemoriaProdutoRepository.class);
			break;
		}
		case PRODUTO_HSQL: {
			produtoRepository = new HSQLProdutoRepository();
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		}
		return produtoRepository;
	}
}
