/*
 * Copyright 2013 the original author or authors.
 * Copyright 2013 Paxxis Technology LLC
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
package com.paxxis.cornerstone.common;

public interface EncryptionHandler extends PasswordGenerator {

    public void setEncryptionKey(String key);
    public void setEncryptionKeyFile(String fileName);
    public String encrypt(String unencryptedString);
    public String decrypt(String encryptedString);

}
