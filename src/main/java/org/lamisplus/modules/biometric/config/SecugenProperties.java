
package org.lamisplus.modules.biometric.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author
 */
@Data
@ConfigurationProperties(prefix = "com.secugen.biometric")
@Configuration("secugenProperties")
public class SecugenProperties {
    
    private String serverUrl;
    
    private String serverPort;
    
    private Long timeout;
    
    private Long quality;
    
}
