package org.lamisplus.modules.biometric;

import com.foreach.across.AcrossApplicationRunner;
import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
			method = clazz.getDeclaredMethod("addURL", URL.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		if (method != null) {
			method.setAccessible(true);
		}


		ArrayList<String> jarFileList = new ArrayList<>();
		jarFileList.add(modulePath + File.separator + "FDxSDKPro-1.0.jar");
		for(String jar : jarFileList){
			File f = new File(jar);
			if(!f.exists()){
				try {
					throw new Exception("File [" + jar + "] doesn't exist!");
				} catch (Exception e) {
					LOG.info("File [" + jar + "] doesn't exist!");
					e.printStackTrace();
				}
			}

			System.out.println("Adding jar [" + jar + "]");
			try {
				if (method != null) {
					method.invoke(classLoader, f.toURI());
				}
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
}
