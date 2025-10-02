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
package grails.plugins.rendering.document

import grails.core.GrailsApplication
import grails.util.GrailsUtil
import grails.util.Holders
import groovy.text.Template
import groovy.transform.CompileStatic
import org.grails.gsp.GroovyPagesTemplateEngine
import org.grails.web.pages.GroovyPagesUriSupport
import org.w3c.dom.Document
import org.xhtmlrenderer.resource.XMLResource
import org.xml.sax.InputSource

@CompileStatic
class XhtmlDocumentService {

    static transactional = false

    GroovyPagesTemplateEngine groovyPagesTemplateEngine
    GroovyPagesUriSupport groovyPagesUriService
    GrailsApplication grailsApplication

    Document createDocument(Map args) {
        createDocument(generateXhtml(args))
    }

    protected Document createDocument(String xhtml) {
        try {
            createDocument(new InputSource(new StringReader(xhtml)))
        } catch (XmlParseException e) {
            if (log.errorEnabled) {
                GrailsUtil.deepSanitize(e)
                log.error("caught xml parse exception for xhtml: $xhtml", e)
            }
            throw new XmlParseException(xhtml, e)
        }
    }

    protected Document createDocument(InputSource xhtml) {
        try {
            XMLResource.load(xhtml).document
        } catch (Exception e) {
            if (log.errorEnabled) {
                GrailsUtil.deepSanitize(e)
                log.error("xml parse exception for input source: $xhtml", e)
            }
            throw new XmlParseException(xhtml, e)
        }
    }

    protected String generateXhtml(Map args) {
        StringWriter xhtmlWriter = new StringWriter()

        RenderEnvironment.with(grailsApplication.mainContext, xhtmlWriter) {
            createTemplate(args).make(args.model as Map).writeTo(xhtmlWriter)
        }

        def xhtml = xhtmlWriter.toString()
        xhtmlWriter.close()

        if (log.debugEnabled) {
            log.debug("xhtml for $args -- \n ${xhtml}")
        }

        xhtml
    }

    protected Template createTemplate(Map args) {
        if (!args.template) {
            throw new IllegalArgumentException("The 'template' argument must be specified")
        }
        String templateName = args.template as String

        if (templateName.startsWith("/")) {
            if (!args.controller) {
                args.controller = ""
            }
        } else {
            if (!args.controller) {
                throw new IllegalArgumentException("template names must start with '/' if controller is not provided")
            }
        }

        String contextPath = getContextPath(args)
        String controllerName = args.controller instanceof CharSequence ? args.controller : groovyPagesUriService.getLogicalControllerName(args.controller as GroovyObject)
        String templateUri = groovyPagesUriService.getTemplateURI(controllerName, templateName)
        String[] uris = ["$contextPath/$templateUri", "$contextPath/grails-app/views/$templateUri"] as String[]
        Template template = groovyPagesTemplateEngine.createTemplateForUri(uris)

        if (!template) {
            throw new UnknownTemplateException(args.template as String, args.plugin as String)
        }

        template
    }

    protected String getContextPath(Map args) {
        String contextPath = args.contextPath?.toString() ?: ""
        String pluginName = args.plugin

        if (pluginName) {
            def plugin = Holders.pluginManager.getGrailsPlugin(pluginName)
            if (plugin && !plugin.isBasePlugin()) {
                contextPath = plugin.pluginPath
            }
        }

        contextPath
    }
}
