package com.ems.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Stream;

import com.ems.util.Util;

import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.javadoc.internal.Pair;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * Hello world!
 *
 */
public class App 
{
	
	private int verbose = 1;
	
	private static final String commons_path = "/home/eduardo/Documentos/ems/java-project/src/projects/commons-collections-master/src/main/java/org/apache/commons/collections4";
	private static final String x_stream_path = "/home/eduardo/Documentos/ems/java-project/src/projects/xstream-v-1.4.x/xstream/src/java/com/thoughtworks/xstream";
	
	
	public static Collection<CtClass<?>> getAllClasses(CtModel model) {
		return model.getElements(
				new TypeFilter<CtClass<?>>(CtClass.class)
				);
	}
	
	
	public static Collection<CtClass<?>> getAllValidClasses(CtModel model) {
		
		Collection<CtClass<?>> classes = new ArrayList();
		
		for (CtClass<?> _class : model.getElements(new TypeFilter<CtClass<?>>(CtClass.class))) {
			if (Util.isValid(_class)) {
				classes.add(_class);
				System.out.println(_class.getSimpleName());
			}
		}
		
		return classes;
		
	}
	
	
	private static HashMap processClasses(Collection<CtClass<?>> classes, String prefix) {
		HashMap<String, HashMap> classesInformations = new HashMap();
		
		
		for (CtClass<?> _class: classes) {
			System.out.println("Analisando: " + _class.getSimpleName());

			HashMap<String, Object> _data = new HashMap();
			
			// Contando a quantidade de métodos:
			_data.put("methods", getMethodsByClass(_class));
			
			// Pegando o caminho e a profundidade das heranças
			Pair path_and_deep = Util.getDepthOfInheritanceTree(_class.getReference());
			
			_data.put("inheritance_path", path_and_deep.a);
			_data.put("inheritance_deep", path_and_deep.b);
			
			// Contando a quantidade de filhos diretos:
			_data.put("childs", Util.getDirectChildrens(_class.getReference()));
			
			// Pegando as classes que são chamadas dentros dos métodos:
			_data.put("coupled", Util.getCbo(_class.getReference(), prefix)); // "org.apache"
			
			_data.put("loc", Util.getLoc(_class.getReference()));
			_data.put("noda", Util.getNoda(_class));
			_data.put("nopa", Util.getNopa(_class));
			_data.put("nopra", Util.getNopra(_class));
			_data.put("nodm", Util.getNodm(_class));
			_data.put("nopm", Util.getNopm(_class));
			_data.put("noprm", Util.getNoprm(_class.getReference()));


			//_data.put("rfc", Util.getRfc(_class.getReference(), "org.apache"));
			
			_data.put("fanin", Util.getFanin(_class.getReference(), classes, prefix));
			_data.put("fanout", Util.getFanout(_class.getReference(), prefix));

			System.out.println(_data.toString());
			
			// Adicionando as métricas para cada classe
			classesInformations.put(_class.getSimpleName(), _data);
			
			System.out.println("\n\nTerminado\n\n");
		}
		
		return classesInformations;
	}
	
	
	/**
	 * Função para fazer o cálculo WMC: Weighted Methods per Class. Ou seja, o número de métodos em uma classe
	 * @param ctClass
	 * @return
	 */
	private static Integer getMethodsByClass(CtClass<?> ctClass) {
		Collection<CtMethod<?>> methods;
		
		try {
			methods = ctClass.getAllMethods();		
		} catch (Exception e) {
			return -1;		
		}
				
		//System.out.println("Métodos da Classe: " + ctClass.getSimpleName());
		
		//for (CtMethod<?> _method : methods) {
		//	System.out.println(_method. + " - " + _method.getSimpleName());
		//}
			
		return methods.toArray().length;
	}
	


	
    public static void main( String[] args )
    {
        System.out.println( "Iniciando Launcher" );
        
        
        SpoonAPI spoon_api = new Launcher();
        HashMap classes_processed = null;
        
	     // path can be a folder or a file
	     // addInputResource can be called several times
        spoon_api.addInputResource(x_stream_path); 
        spoon_api.getEnvironment().setNoClasspath(true);
        spoon_api.getEnvironment().setComplianceLevel(7);
        spoon_api.buildModel();
	
	     CtModel model = spoon_api.getModel();
	     
	     Collection<CtPackage> packages = model.getAllPackages();
	     
	     // Carregando as classes do Projeto:
	     Collection<CtClass<?>> classes = getAllValidClasses(model);
	     
	     classes_processed = processClasses(classes, "com.thoughtworks.xstream");
		
	     System.out.println(classes_processed.toString());

	     
	     Util.saveJson(classes_processed, "x_stream");

    }
}
