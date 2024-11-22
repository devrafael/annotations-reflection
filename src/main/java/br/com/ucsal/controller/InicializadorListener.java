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
	private static final Map<Class<?>, Object> instances = new HashMap<>();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// Carregue suas classes ou inicialize recursos aqui
		System.out.println("Inicializando recursos na inicialização da aplicação");
		try {
			processarSingletons(basePackage);
			injetarDependencias(basePackage);
			processarRotas(basePackage);

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public void processarRotas(String basePackage) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		System.out.println("\n===LOG SOBRE ROTAS===");

		Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Rota.class);
		for (Class<?> classe : classes) {

			if (classe.isAnnotationPresent(Rota.class)) {
				System.out.println("\nClasse encontrada: " + classe.getName());
				Rota rotaClasse = classe.getAnnotation(Rota.class);
				for (int i = 0; i < rotaClasse.path().length; i++) {
					Command commandInstance = (Command) getInstanceSingleton(classe);

					String path = PREFIX + rotaClasse.path()[i].replaceAll("//", "/");
					System.out.println("Path da classe: " + rotaClasse.path()[i]);
					rotas.put(path, commandInstance);
				}

			}

		}
		System.out.println("\nRotas registradas:");
		for (String key : rotas.keySet()) {
			System.out.println("Chave Registrada: " + key);
		}

	}

	public Command getCommand(String path) {
		return rotas.get(path);
	}

	public void processarSingletons(String basePackage) {

		Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
		Set<Class<?>> singletonClasses = reflections.getTypesAnnotatedWith(Singleton.class);
		System.out.println("===LOG SOBRE SINGLETON===\n");

		for (Class<?> clazz : singletonClasses) {

			System.out.println("Classe anotada com @Singleton encontrada: " + clazz.getName());
			getInstanceSingleton(clazz); // Garante a criação da instância Singleton
			// Log para identificar quem está chamando
			System.out.println(
					"getInstance chamado para: " + clazz.getName() + " pela classe: " + new Throwable().getStackTrace()[1]);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInstanceSingleton(Class<T> clazz) {
		if (!clazz.isAnnotationPresent(Singleton.class)) {
			throw new IllegalArgumentException("A classe " + clazz.getName() + " não está anotada com @Singleton");
		}

		// Retorna a instância existente ou cria uma nova
		return (T) instances.computeIfAbsent(clazz, InicializadorListener::createInstanceSingleton);
	}

	private static <T> T createInstanceSingleton(Class<T> clazz) {
		try {
			System.out.println("Instancia criada da classe: " + clazz + "\n");
			// System.out.flush();
			return clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Não foi possível criar a instância da classe " + clazz.getName(), e);
		}
	}

	// Método para injetar dependências em campos anotados com @Inject
	private void injetarDependencias(String basePackage) throws IllegalAccessException, InstantiationException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Rota.class);
		System.out.println("\n===LOG SOBRE INJEÇÃO DE DEPENDÊNCIA===");

		for (Class<?> clazz : classes) {
			// Processa os campos anotados com @Inject
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(Inject.class)) {
					field.setAccessible(true);
					System.out.println("\nCampo: " + field.getName());

					Object dependency = obterDependencia(field.getType());

					if (dependency != null) {
						if (Modifier.isStatic(field.getModifiers())) {
							field.set(null, dependency);
						} else {
							Object instance = getInstanceSingleton(clazz);
							field.set(instance, dependency);
						}
						System.out.println("Dependência injetada com sucesso na instância: " + field.getName()
						+ " na classe: " + clazz.getName());
					}

				}
			}
		}
	}

	private Object obterDependencia(Class<?> tipo) {
		if (ProdutoService.class.isAssignableFrom(tipo)) {
			ProdutoRepository<?, ?> repository = PersistenciaFactory.getProdutoRepository(1);
			if (repository == null) {
				throw new IllegalStateException("ProdutoRepository não pode ser nulo");
			}
			return new ProdutoService(repository);
		}
		throw new IllegalArgumentException("Não foi possível resolver a dependência para o tipo: " + tipo.getName());
	}

}