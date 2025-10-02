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
package grails.plugins.rendering.pdf

import grails.plugins.rendering.RenderingService
import groovy.transform.CompileStatic
import org.w3c.dom.Document
import org.xhtmlrenderer.pdf.ITextRenderer

@CompileStatic
class PdfRenderingService extends RenderingService {

    static transactional = false

    protected doRender(Map args, Document document, OutputStream outputStream) {
        def renderer = new ITextRenderer()
        renderer.setDocument(document, args.base as String)
        renderer.layout()
        renderer.createPDF(outputStream)
    }

    protected String getDefaultContentType() {
        "application/pdf"
    }

}
