package eco.nightgrid._0.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration(proxyBeanMethods = false)
public class SessionConfig {

	@Bean
	public CookieSerializer cookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName("NIGHTGRID_SESSION");
		serializer.setCookiePath("/");
		serializer.setUseHttpOnlyCookie(true);
		serializer.setSameSite("Lax");
		return serializer;
	}
}
