package org.lamisplus.modules.biometric.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import org.springframework.core.annotation.Order;

@Order(1)
@Installer(name = "schema-installer",
        description = "Installs the required database tables",
        version = 11)
public class BiometricModuleInstaller extends AcrossLiquibaseInstaller {
    public BiometricModuleInstaller() {
        super("classpath:installers/biometric/schema/schema.xml");
    }
}
