/*
 * Copyright (C) 2015 Ferid Cafer
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

package com.ferid.app.frequentcontacts.enums;

/**
 * Created by ferid.cafer on 11/12/2014.
 */
public enum Process {
    CALL(0),
    CHANGE_PHOTO(1),
    DELETE_CONTACT(2);

    private final int value;

    private Process(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
