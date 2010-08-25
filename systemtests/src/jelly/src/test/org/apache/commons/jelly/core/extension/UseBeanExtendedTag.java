/*
 * Copyright 2002,2004 The Apache Software Foundation.
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
package org.apache.commons.jelly.core.extension;

import org.apache.commons.jelly.tags.core.UseBeanTag;

/**
 * Test tag to check the ignored properties work ok
 */
public class UseBeanExtendedTag extends UseBeanTag {

    /**
     * @see UseBeanTag#UseBeanTag()
     */
    public UseBeanExtendedTag() {
        super();
        addIgnoreProperty("name");
    }

    /**
     * @see UseBeanTag#UseBeanTag(Class)
     */
    public UseBeanExtendedTag(Class defaultClass) {
        super(defaultClass);
        addIgnoreProperty("name");
    }

}
