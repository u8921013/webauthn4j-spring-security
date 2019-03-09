/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sharplab.springframework.security.webauthn.sample.infrastructure.util.jpa.converter;

import com.webauthn4j.converter.util.CborConverter;
import com.webauthn4j.response.attestation.authenticator.CredentialPublicKey;
import com.webauthn4j.util.Base64UrlUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class CredentialPublicKeyConverter implements AttributeConverter<CredentialPublicKey, String> {

    private CborConverter cborConverter = new CborConverter(); //TODO

    @Override
    public String convertToDatabaseColumn(CredentialPublicKey attribute) {
        return Base64UrlUtil.encodeToString(cborConverter.writeValueAsBytes(attribute));
    }

    @Override
    public CredentialPublicKey convertToEntityAttribute(String dbData) {
        byte[] data = Base64UrlUtil.decode(dbData);
        return cborConverter.readValue(data, CredentialPublicKey.class);
    }
}