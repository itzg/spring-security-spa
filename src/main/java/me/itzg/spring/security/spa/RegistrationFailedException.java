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

import org.springframework.security.core.AuthenticationException;

/**
 * A specialization of {@link AuthenticationException} that is thrown primarily when a user attempts to register
 * with an existing username.
 *
 * @author Geoff Bourne
 * @since Mar 2018
 */
@SuppressWarnings("WeakerAccess")
public class RegistrationFailedException extends AuthenticationException {
    public RegistrationFailedException(String msg, Throwable t) {
        super(msg, t);
    }
}
