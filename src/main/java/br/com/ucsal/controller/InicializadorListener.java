package br.com.ucsal.controller;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import br.com.ucsal.annotations.Inject;
import br.com.ucsal.annotations.Rota;
import br.com.ucsal.annotations.Singleton;
import br.com.ucsal.commands.Command;
//import br.com.ucsal.commands.rotas.RotaCommand;
import br.com.ucsal.persistencia.PersistenciaFactory;
import br.com.ucsal.persistencia.ProdutoRepository;
import br.com.ucsal.service.ProdutoService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class InicializadorListener implements ServletContextListener {

	String basePackage = "br.com.ucsal";
	private final String PREFIX = "/prova2/view";
	private final Map<String, Command> rotas = new HashMap<>();
	private static final Map<Class<?>, Object> INSTANCES_SINGLETON = new HashMap<>();
	private Map<String, Object> instances_commands = new HashMap<>();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// Carregue suas classes ou inicialize recursos aqui
		System.out.println("Inicializando recursos na inicialização da aplicação");
		try {
			
			processarSingletons(basePackage);
			processarRotas(basePackage);
			injetarDependencias(basePackage);
			sce.getServletContext().setAttribute("command", rotas);

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	// Método para processar as rotas
	public void processarRotas(String basePackage) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		System.out.println("\n===LOG SOBRE ROTAS===");

		Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Rota.class);
		for (Class<?> classe : classes) {

			if (classe.isAnnotationPresent(Rota.class)) {
				System.out.println("\nClasse encontrada: " + getFiltroNomeClasseDeclarada(classe.getName()));
				Rota rotaClasse = classe.getAnnotation(Rota.class);
				for (int i = 0; i < rotaClasse.path().length; i++) {
					Command commandInstance = (Command) getInstanceSingleton(classe);

					String nomeClasse = getFiltroNomeClasseDeclarada(classe.getName());
					instances_commands.put(nomeClasse, commandInstance);

					String path = rotaClasse.path()[i].replaceAll("//", "/");
					System.out.println("Path da classe: " + rotaClasse.path()[i]);
					rotas.put(path, commandInstance);
				}

			}

		}
		System.out.println("\nRotas registradas:");
		for (String key : rotas.keySet()) {
			Object valor = rotas.get(key);
			System.out.println("Chave Registrada: " + key + ", Valor: " + valor);
		}

		System.out.println("\nInstancias registradas:");
		for (String key : instances_commands.keySet()) {
			Object valor = instances_commands.get(key);
			System.out.println("Chave Registrada: " + key + ", Valor: " + valor);
		}

	}

	public Command getCommand(String path) {
		return rotas.get(path);
	}

	public String getFiltroNomeClasseDeclarada(String nomeClasse) {
		return nomeClasse.substring(nomeClasse.lastIndexOf(".") + 1);
	}

//	// Método para processar as instâncias Singletons
	public void processarSingletons(String basePackage) {

		Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
		Set<Class<?>> singletonClasses = reflections.getTypesAnnotatedWith(Singleton.class);
		System.out.println("===LOG SOBRE SINGLETON===\n");

		for (Class<?> clazz : singletonClasses) {

			System.out.println("Classe anotada com @Singleton encontrada: " + clazz.getName());
			getInstanceSingleton(clazz);
			System.out.println("getInstance chamado para: " + clazz.getName() + " pela classe: "
					+ new Throwable().getStackTrace()[1]);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInstanceSingleton(Class<T> clazz) {
		if (!clazz.isAnnotationPresent(Singleton.class)) {
			throw new IllegalArgumentException("A classe " + clazz.getName() + " não está anotada com @Singleton");
		}

		// Retorna a instância existente ou cria uma nova
		return (T) INSTANCES_SINGLETON.computeIfAbsent(clazz, InicializadorListener::createInstanceSingleton);
	}

	private static <T> T createInstanceSingleton(Class<T> clazz) {
		try {
			System.out.println("Instancia criada da classe: " + clazz + "\n");
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Não foi possível criar a instância da classe " + clazz.getName(), e);
		}
	}

	// Método para injetar dependências
	private void injetarDependencias(String basePackage) throws IllegalAccessException, InstantiationException {
		// Usa Reflections para localizar todos os campos anotados com @Inject
		Reflections reflections = new Reflections(basePackage, Scanners.FieldsAnnotated);
		Set<Field> fields = reflections.getFieldsAnnotatedWith(Inject.class);

		System.out.println("\n=== LOG SOBRE INJEÇÃO DE DEPENDÊNCIA ===");

		for (Field field : fields) {
			field.setAccessible(true);

			// Obtém a classe onde o campo está declarado
			Class<?> declaringClass = field.getDeclaringClass();
			String nomeClasse = declaringClass.getSimpleName();

			System.out.println("\nProcessando injeção para o campo: " + field.getName() + " na classe: "
					+ declaringClass.getSimpleName());

			
			// Obter a dependência correspondente ao tipo do campo
			Object dependency = obterDependencia(field.getType());
			if (dependency != null) {
				if (Modifier.isStatic(field.getModifiers())) {
					field.set(null, dependency);
				} else {
					Object instance = instances_commands.get(nomeClasse);
					field.set(instance, dependency);
				}
				System.out.println("Dependência injetada com sucesso na instância: " + field.getName() + " na classe: "
						+ nomeClasse);
			}

		}
	}
	

	// Método para obter a dependência com base no tipo do campo
	private Object obterDependencia(Class<?> tipo) {
			System.out.println("Tipo do atribudo: " + tipo.getName());
		if (ProdutoService.class.isAssignableFrom(tipo)) {
			ProdutoRepository<?, ?> repository = PersistenciaFactory.getProdutoRepository(0);
			if (repository == null) {
				throw new IllegalStateException("ProdutoRepository não pode ser nulo");
			}
			return new ProdutoService(repository);
		}
		throw new IllegalArgumentException("Não foi possível resolver a dependência para o tipo: " + tipo.getName());
	}
	
}