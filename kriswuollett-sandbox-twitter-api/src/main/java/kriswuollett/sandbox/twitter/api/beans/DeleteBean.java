/**
 * Copyright 2012 Kristopher Wuollett
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
package kriswuollett.sandbox.twitter.api.beans;

import kriswuollett.sandbox.twitter.api.Delete;
import kriswuollett.sandbox.twitter.api.TwitterStreamObjectType;

/**
 * @author kris
 */
public class DeleteBean implements Delete
{
    @Override
    public TwitterStreamObjectType getType()
    {
        return TwitterStreamObjectType.DELETE;
    }

    @Override
    public void clear()
    {
    }
}