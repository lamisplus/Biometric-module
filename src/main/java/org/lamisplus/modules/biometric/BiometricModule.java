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
}
