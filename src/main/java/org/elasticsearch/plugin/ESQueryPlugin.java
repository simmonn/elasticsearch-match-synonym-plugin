/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsearch.plugin;

import org.elasticsearch.mysynonym.SynonymMatchBuilder;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ESQueryPlugin extends Plugin implements SearchPlugin {

    @Override
    public List<QuerySpec<?>> getQueries() {
//        return singletonList(new QuerySpec<>(PositionMatchQuery.NAME, PositionMatchQueryBuilder::new, PositionMatchQueryBuilder::fromXContent));
        return Collections.singletonList(
                new QuerySpec<>(SynonymMatchBuilder.NAME, SynonymMatchBuilder::new, SynonymMatchBuilder::fromXContent)
        );
    }

}
