/*
 * Copyright 2010 Grails Plugin Collective
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
package grails.plugins.rendering

import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

@Integration
class ControllerRelativeTemplateSpec extends Specification {

    @Value('${local.server.port}')
    Integer port
    RestTemplate restTemplate = new RestTemplate()

    def 'accessing controllers that does rendering'() {
        when:
        def resp = restTemplate.exchange(
                getUrl(uri),
                HttpMethod.GET,
                null,
                byte[].class
        )
        then:
        resp.statusCode.value() == 200
        resp.headers['Content-Type'] == [expectedContentType]
        where:
        uri            | expectedContentType
        'gif'          | 'image/gif'
        'jpeg'         | 'image/jpeg'
        'png'          | 'image/png'
        'pdf'          | 'application/pdf'
        'dataUriImg'   | 'image/gif'
        'dataUriPdf'   | 'application/pdf'
        'relative'     | 'image/gif'
        'encodingTest' | 'application/pdf'
    }
    
    private String getUrl(String uri) {
        return "http://localhost:${port}/rendering/rendering/${uri}"
    }
}
