package com.ems.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spoon.javadoc.internal.Pair;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.sniper.internal.ElementSourceFragment;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {

	public static boolean isValid(CtType<?> element) {
		return element != null && element.getQualifiedName() != null && (element.isClass() || element.isInterface())
				&& !element.isAnonymous() && !element.isLocalType() && !element.isShadow();
	}

	/**
	 * Função para calcular os filhos (sub-classes) imediatos de uma Classe
	 * 
	 * @param type
	 * @return
	 */
	public static Integer getDirectChildrens(CtTypeReference<?> type) {
		return type.getDirectChildren().toArray().length;
	}

	/**
	 * Função para devolver o número absoluto, por classe, do acoplamento com outras
	 * classes do mesmo projeto, especificado pelo parâmetro 'couplePrefix'.
	 * 
	 * @param type
	 * @param couplePrefix prefixo do pacote das classes. É necessário apenas
	 *                     verificar estes atributos
	 */
	public static Map<String, Integer> getCbo(CtTypeReference<?> type, String couplePrefix) {
		Collection<CtFieldReference<?>> fields = type.getDeclaration().getAllFields();
		Collection<CtMethod<?>> methods = type.getDeclaration().getMethods();
		Collection<CtParameter<?>> parameters;
		Collection<CtStatement> code;

		// Alvos que serão vasculhados nos códigos dos métodos:
		HashMap<String, String> coupledTargets = new HashMap<String, String>();
		HashMap<String, Integer> accumulated = new HashMap<String, Integer>();

		// Pegando todos os atributos da Classe:
		for (CtFieldReference field : fields) {
			System.out.println("Atributo: " + field.getType().toString());
			// Verificando quais atributos são relacionados à classes do mesmo pacote
			if (field.getType().toString().contains(couplePrefix)) {
				// System.out.println("Adicionando: " + field.getType().getSimpleName() + " - "
				// + field.getSimpleName());
				coupledTargets.put(field.getType().getSimpleName(), field.getSimpleName());
			}

		}

		// Pegando todos os parâmetros dos métodos:
		for (CtMethod method : methods) {
			parameters = method.getParameters();

			for (CtParameter param : parameters) {
				if (param.getType().toString().contains(couplePrefix))
					coupledTargets.put(param.getType().getSimpleName(), param.getSimpleName());
			}
		}

		System.out.println("Alvos a serem buscados: " + Arrays.asList(coupledTargets)); // method 1

		// Caso não tenhamos alvos, podemos retornar:
		if (coupledTargets.isEmpty())
			return null;

		// Contando a quantidade de vezes que outras classes são chamadas dentro da
		// classe em questão
		for (CtMethod method : methods) {

			if (method.getBody() == null)
				continue;

			code = method.getBody().getStatements();

			if (code.isEmpty())
				continue;

			// Para cada linha do código:
			for (CtStatement line : code) {

				// Vamos ver se há chamadas aos nossos alvos:
				for (Map.Entry coupled : coupledTargets.entrySet()) {

					if (line.toString().contains(coupled.getValue().toString() + ".")) {

						// Verificando se a chave existe no Mapa de Acúmulo de Chamadas:
						if (!accumulated.containsKey(coupled.getKey().toString()))
							accumulated.put(coupled.getKey().toString(), 0);

						// Atualizando o Mapa:
						accumulated.put(coupled.getKey().toString(), accumulated.get(coupled.getKey().toString()) + 1);

					}
				}

				// System.out.println("Analisando Código: " + line.toString());
			}

		}

		return accumulated;

	}

	
	public static Map<String, Integer> getRfc(CtTypeReference<?> type, String couplePrefix) {
		Collection<CtMethod<?>> methods = type.getDeclaration().getAllMethods();

		// Pegando todos os parâmetros dos métodos:
		for (CtMethod method : methods) {
			System.out.println(method.getSimpleName() + " - " + method.getType().getSuperclass() + " - "
					+ method.getParent(CtClass.class).getQualifiedName());

		}

		return null;
	}

	/**
	 * Função para retornar a quantidade de linhas de uma classe
	 * 
	 * @param type
	 * @return
	 */
	public static Integer getLoc(CtTypeReference<?> type) {

		int startLine = type.getDeclaration().getPosition().getLine();
		int endLine = type.getDeclaration().getPosition().getEndLine();
		return ((endLine - startLine) == 0) ? (1) : ((endLine - startLine) - 1);
	}

	/**
	 * Função para retornar a quantidade de atributos declarados de uma classe:
	 * 
	 * @param type
	 * @return
	 */
	public static Integer getNoda(CtClass<?> type) {
		return type.getFields().size();
	}

	/**
	 * Função para contar a quantidade de atributos públicos de uma classe;
	 * 
	 * @param type
	 * @return
	 */
	public static Integer getNopa(CtClass<?> type) {
		Collection<CtField<?>> fields = type.getFields();
		Integer count = 0;

		for (CtField<?> field : fields) {
			if (field.isPublic())
				count = count + 1;
		}

		return count;
	}

	/**
	 * Função para contar a quantidade de atributos privados de uma classe;
	 * 
	 * @param type
	 * @return
	 */
	public static Integer getNopra(CtClass<?> type) {
		Collection<CtField<?>> fields = type.getFields();
		Integer count = 0;

		for (CtField<?> field : fields) {
			if (field.isPrivate())
				count = count + 1;
		}

		return count;
	}

	/**
	 * Função para contar a quantidade de métodos de uma classe;
	 * 
	 * @param type
	 * @return
	 */
	public static Integer getNodm(CtClass<?> type) {
		return type.getMethods().size();

	}

	/**
	 * Função para contar a quantidade de métodos públicos de uma classe;
	 * 
	 * @param type
	 * @return
	 */
	public static Integer getNopm(CtClass<?> type) {
		Collection<CtMethod<?>> methods = type.getMethods();
		Integer count = 0;

		for (CtMethod<?> method : methods) {
			if (method.isPublic())
				count = count + 1;
		}

		return count;
	}

	/**
	 * Função para contar a quantidade de métodos privados de uma classe;
	 * 
	 * @param type
	 * @return
	 */
	public static Integer getNoprm(CtTypeReference<?> type) {
		Collection<CtMethod<?>> methods = type.getDeclaration().getMethods();
		Integer count = 0;

		for (CtMethod<?> method : methods) {
			if (method.isPrivate())
				count = count + 1;
		}

		return count;
	}

	/**
	 * Função para calcular a quantidade de referências, montado a partir do
	 * conjunto de classes em questão, recebida pela classe referenciada em 'type'
	 * 
	 * @param type
	 * @param classes
	 * @param couplePrefix
	 * @return
	 */
	public static Integer getFanin(CtTypeReference<?> type, Collection<CtClass<?>> classes, String couplePrefix) {
		System.out.println("-- Fanin p/  " + type.getTypeDeclaration().getSimpleName() + " -- ");

		Collection<CtField<?>> fields;
		Collection<CtMethod<?>> methods;
		Collection<CtParameter<?>> parameters;

		// Alvos que serão vasculhados nos códigos dos métodos:
		Set<String> fanin = new HashSet();
		Integer count = 0;

		// Para cada classe:
		for (CtClass _class : classes) {
			// System.out.println("> " + _class.getSimpleName());

			// Não contando a própria classe
			if (_class.getSimpleName().equals(type.getSimpleName()))
				continue;

			fields = _class.getFields();
			methods = _class.getMethods();

			// Pegando todos os atributos da Classe:
			for (CtField field : fields) {

				// Verificando quais atributos são relacionados à classes do mesmo pacote
				if (field.getType().toString().contains(couplePrefix)) {
					// System.out.println(">> " + field.getType().getSimpleName() + " - " +
					// type.getSimpleName() + " - " +
					// field.getType().getSimpleName().equals(type.getSimpleName().toString()));

					if (field.getType().getSimpleName().equals(type.getSimpleName())) {
						System.out.println("! ADICIONANDO");
						fanin.add(field.getType().getSimpleName());
						count++;
					}

				}

			}

			// Pegando todos os parâmetros dos métodos:
			for (CtMethod method : methods) {
				parameters = method.getParameters();

				for (CtParameter param : parameters) {
					if (param.getType().toString().contains(couplePrefix)) {
						// System.out.println(">>> " + param.getType().getSimpleName() + " - " +
						// type.getSimpleName());

						if (param.getType().getSimpleName().equals(type.getSimpleName())) {
							System.out.println("! ADICIONANDO");
							fanin.add(param.getType().getSimpleName());
							count++;
						}

					}

				}
			}

			// System.out.println("Valores p/ " + _class.getSimpleName() + " :" +
			// Arrays.toString(coupledTargets.toArray()));

		}

		System.out.println(">> Fanin: " + fanin.size() + " | " + count);

		return count;

	}

	public static Integer getFanout(CtTypeReference<?> type, String couplePrefix) {
		System.out.println("-- Fanout -- ");

		Collection<CtField<?>> fields = type.getDeclaration().getFields();
		Collection<CtMethod<?>> methods = type.getDeclaration().getMethods();
		Collection<CtParameter<?>> parameters;

		// Alvos que serão vasculhados nos códigos dos métodos:
		Set<String> coupledTargets = new HashSet<String>();

		// Pegando todos os atributos da Classe:
		for (CtField field : fields) {
			// System.out.println("Atributo: " + field.getType().toString());
			// Verificando quais atributos são relacionados à classes do mesmo pacote
			if (field.getType().toString().contains(couplePrefix)) {
				System.out.println(">> Adicionando: " + field.getType().getSimpleName());
				coupledTargets.add(field.getType().getSimpleName());
			}

		}

		// Pegando todos os parâmetros dos métodos:
		for (CtMethod method : methods) {
			parameters = method.getParameters();

			for (CtParameter param : parameters) {
				if (param.getType().toString().contains(couplePrefix))
					System.out.println(">> Adicionando: " + param.getType().getSimpleName());
				coupledTargets.add(param.getType().getSimpleName());
			}
		}

		System.out.println(">> Fanout: " + coupledTargets.size());

		return coupledTargets.size();

	}

	/**
	 * Função para fazer o cálculo DIT: Depth of Inheritance Tree. Ou seja, o maior
	 * caminho entre a classe e a super classe
	 * 
	 * @param ctClass
	 * @return
	 */
	public static Pair<String, Integer> getDepthOfInheritanceTree(CtTypeReference<?> type) {
		String path = getFullInheritanceTree(type);
		Integer deep = 0;

		if (path.length() == 0)
			return new Pair<String, Integer>(path, deep);

		// Retirando o último '.' que é montado na recursão
		path = path.substring(0, path.length() - 1);

		// Montando o tamanho da árvore:
		// System.out.println(Arrays.toString(path.split("\\.")));

		deep = path.split("\\.").length;

		System.out.println("Profundidade: " + deep);

		return new Pair<String, Integer>(path, deep);

	}


	private static String getFullInheritanceTree(CtTypeReference<?> type) {
		if (type.isShadow() || type.getSuperclass() == null)
			return "";

		return type.getSimpleName() + "." + getFullInheritanceTree(type.getSuperclass());
	}


	public static void saveJson(HashMap input_data, String file_name) {
		System.out.println("Salvando o hashmap.");
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			mapper.writeValue(new File(file_name + ".json"), input_data);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
	}
}
