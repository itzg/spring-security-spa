/*
 * Copyright 2018 Geoff Bourne
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package me.itzg.spring.security.spa;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

/**
 * Makes it easier to work with {@link HttpMessageConverters}.
 *
 * @author Geoff Bourne
 * @since Mar 2018
 */
@SuppressWarnings("WeakerAccess")
public class ConverterHelper {
    private HttpMessageConverters httpMessageConverters;

    public ConverterHelper(HttpMessageConverters httpMessageConverters) {
        this.httpMessageConverters = httpMessageConverters;
    }

    /**
     * @param resultClass the class to test for readability
     * @param mediaType   the media type to read
     * @param <T>         the class to test for readability
     * @return an optional containing the first applicable converter
     * @see HttpMessageConverter#canRead(Class, MediaType)
     */
    public <T> Optional<HttpMessageConverter<T>> findConverter(Class<T> resultClass, MediaType mediaType) {
        for (HttpMessageConverter<?> converter : httpMessageConverters.getConverters()) {
            if (converter.canRead(resultClass, mediaType)) {
                //noinspection unchecked
                return Optional.of((HttpMessageConverter<T>) converter);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a suitable converter and if found, uses it to parse the request body.
     *
     * @param req         the body of this request will be parsed
     * @param resultClass the expected class of the request body
     * @param <T>         the expected class of the request body
     * @return the parsing result or null if not parseable
     * @throws IOException in case of I/O errors while reading the request body
     */
    public <T> T parseBody(HttpServletRequest req, Class<T> resultClass) throws IOException {
        final MediaType mediaType = MediaType.parseMediaType(req.getContentType());

        final Optional<HttpMessageConverter<T>> converter = findConverter(resultClass, mediaType);
        if (converter.isPresent()) {
            return converter.get().read(resultClass, new ServletServerHttpRequest(req));
        } else {
            return null;
        }
    }
}
