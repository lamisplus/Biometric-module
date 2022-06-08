package org.lamisplus.modules.biometric;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;

@AcrossDepends(
		required = {
				AcrossHibernateJpaModule.NAME
		})
public class BiometricModule extends AcrossModule {
	public static final String NAME = "BiometricModule";


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
