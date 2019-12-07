package com.ems.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spoon.javadoc.internal.Pair;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

public class Util {
	
	public static boolean isValid(CtType<?> element) {
		return element != null && element.getQualifiedName() != null && (element.isClass() || element.isInterface())
				&& !element.isAnonymous() && !element.isLocalType() && !element.isShadow();
	}
	
	
	/**
	 * Função para calcular os filhos (sub-classes) imediatos de uma Classe
	 * @param type
	 * @return
	 */
	public static Integer getDirectChildrens(CtTypeReference<?> type) {
		return type.getDirectChildren().toArray().length;
	}
	
	
	public static void getCoupled(CtTypeReference<?> type, String couplePrefix) {
		Collection<CtFieldReference<?>> fields = type.getDeclaration().getAllFields();
		
		// Alvos que serão vasculhados nos códigos dos métodos:
		Set<String> coupledTargets = new HashSet<String>();
		
		System.out.println("Atributos da Classe: ");
		
		for (CtFieldReference field : fields) {
			// Verificando quais atributos são relacionados à classes do mesmo pacote
			if (field.getType().toString().contains(couplePrefix)) 
				coupledTargets.add(field.getSimpleName());
			
			
     	}		
			
		Collection<CtMethod<?>> methods = type.getDeclaration().getAllMethods();
		
		// Adicionando os parâmetros que são classes à lista:
		for (CtMethod method: methods) {
			Collection<CtParameter<?>> parameters = method.getParameters();
			
			for (CtParameter param : parameters) {
				if (param.getType().toString().contains(couplePrefix)) 
					coupledTargets.add(param.getSimpleName());
				
			}
		}
		
		//System.out.println("> Atributos Coupled: " + Arrays.toString(coupledTargets.toArray()));
		
		// Varrendo os métodos e procurando e contando as vezes que estes elementos são chamados:
		
	}
	
	
	
	/**
	 * Função para fazer o cálculo DIT: Depth of Inheritance Tree. Ou seja, o maior caminho entre a classe e 
	 * a super classe
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
		//System.out.println(Arrays.toString(path.split("\\.")));
		
		deep = path.split("\\.").length;
		
		System.out.println("Profundidade: " + deep);
		
		return new Pair<String, Integer>(path, deep);
		
	}
	
	private static String getFullInheritanceTree(CtTypeReference<?> type) {
		if (type.isShadow() || type.getSuperclass() == null) 
			return "";
		
		return type.getSimpleName() + "." + getFullInheritanceTree(type.getSuperclass());
	}
	
}
