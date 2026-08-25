package org.springframework.samples.petclinic.system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * Configures internationalization (i18n) and static-resource serving.
 *
 * <p>
 * Locale is resolved from the {@code Accept-Language} header — more idiomatic for a
 * stateless JSON API than the old session-based, {@code ?lang=}-driven resolver, which
 * only made sense for server-rendered pages.
 * </p>
 *
 * @author Anuj Ashok Potdar
 */
@Configuration
@SuppressWarnings("unused")
public class WebConfiguration implements WebMvcConfigurer {

	/**
	 * Resolves locale from the request's {@code Accept-Language} header. Defaults to
	 * English if nothing is specified.
	 * @return header-based {@link LocaleResolver}
	 */
	@Bean
	public LocaleResolver localeResolver() {
		AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
		resolver.setDefaultLocale(Locale.ENGLISH);
		return resolver;
	}

	/**
	 * The SPA shell ({@code index.html}) must never be served stale — unlike the hashed
	 * JS/CSS bundle filenames, which keep the app-wide 12h cache-control default, a new
	 * deploy needs the shell itself to be re-fetched immediately.
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/index.html")
			.addResourceLocations("classpath:/static/")
			.setCacheControl(CacheControl.noStore());
	}

}
