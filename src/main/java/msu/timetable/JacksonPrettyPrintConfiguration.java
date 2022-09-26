package msu.timetable;


import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/** This class's purpose is to return JSON data to clients in a human-readable way */
@Configuration
public class JacksonPrettyPrintConfiguration extends WebMvcConfigurationSupport {
    @Override
    protected void extendMessageConverters( List<HttpMessageConverter<?>> converters ) {
        for ( HttpMessageConverter<?> converter : converters ) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.setPrettyPrint( true );
            }
        }
    }
}
