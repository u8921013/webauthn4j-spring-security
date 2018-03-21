/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sharplab.springframework.security.webauthn.attestation.statement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.security.cert.X509Certificate;

@JsonIgnoreProperties(value = "format")
public class NoneAttestationStatement implements WebAuthnAttestationStatement {

    public static final String FORMAT = "none";

    @JsonIgnore
    @Override
    public String getFormat() {
        return FORMAT;
    }

    @JsonIgnore
    @Override
    public AttestationType getAttestationType() {
        return AttestationType.None;
    }

    @JsonIgnore
    @Override
    public X509Certificate getEndEntityCertificate() {
        throw new UnsupportedOperationException();
    }
}