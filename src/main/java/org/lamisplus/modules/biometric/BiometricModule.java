package org.lamisplus.modules.biometric;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

@AcrossDepends(
		required = {
				AcrossHibernateJpaModule.NAME
		})
public class BiometricModule extends AcrossModule {
	public static final String NAME = "BiometricModule";
	public static String modulePath = System.getProperty("user.dir");


	public BiometricModule() {
		super ();
		addApplicationContextConfigurer (new ComponentScanConfigurer (
				getClass ().getPackage ().getName () + ".domain",
				getClass ().getPackage ().getName () + ".repository",
				getClass ().getPackage ().getName () + ".config",
				getClass ().getPackage ().getName () + ".services",
				getClass ().getPackage ().getName () + ".controller",
				getClass ().getPackage ().getName () + ".enumeration",
				"org.lamisplus.modules.base.service"
		));

	}
	@Override
	public String getName() {
		return  NAME;
	}

	@Bean
	public void addDependencies(){
		URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class clazz= URLClassLoader.class;

		// Use reflection
		Method method= null;
		try {
			method = clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		method.setAccessible(true);


		//String jarPath = modulePath+ File.separator+"java"+File.separator+"jar"+File.separator;
		ArrayList<String> jarFileList = new ArrayList<String>();
		jarFileList.add(modulePath+"FDxSDKPro-1.0.jar");
		for(String jar : jarFileList){
			File f = new File(jar);
			if(f.exists() == false){
				try {
					throw new Exception("File [" + jar + "] doesn't exist!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			System.out.println("Adding jar [" + jar + "]");
			try {
				method.invoke(classLoader, new Object[] { f.toURL() });
			} catch (IllegalAccessException | InvocationTargetException | MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}
}
