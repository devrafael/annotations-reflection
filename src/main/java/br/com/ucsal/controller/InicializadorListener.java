package br.com.ucsal.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import br.com.ucsal.annotations.Rota;
import br.com.ucsal.commands.Command;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;


@WebListener
public class InicializadorListener implements ServletContextListener {

	String basePackage = "br.com.ucsal";
	private final String PREFIX = "/prova2/view";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// Carregue suas classes ou inicialize recursos aqui
		System.out.println("Inicializando recursos na inicialização da aplicação");
		try {
			processarRotas(basePackage);
			
		
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		System.out.println("passei pelo processador de rotas");
	}

	private final Map<String, Command> rotas = new HashMap<>();

	public void processarRotas(String basePackage) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated, Scanners.MethodsAnnotated);
		Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Rota.class);

		for (Class<?> classe : classes) {

			if (classe.isAnnotationPresent(Rota.class)) {
				System.out.println("Classe encontrada: " + classe.getName());
				Rota rotaClasse = classe.getAnnotation(Rota.class);
				for (int i = 0; i < rotaClasse.path().length; i++) {

					Command commandInstance = (Command) classe.getDeclaredConstructor().newInstance();
					String path = PREFIX + rotaClasse.path()[i].replaceAll("//", "/");
					System.out.println("Path da classe: " + rotaClasse.path()[i]);
					rotas.put(path, commandInstance);
				}

			}

		}
		System.out.println("Rotas registradas:");
		for (String key : rotas.keySet()) {
			System.out.println("Chave Registrada: " + key);
		}

	}

	public Command getCommand(String path) {
		return rotas.get(path);
	}

}