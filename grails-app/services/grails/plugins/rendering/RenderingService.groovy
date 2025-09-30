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

import grails.core.GrailsApplication
import grails.plugins.rendering.document.XhtmlDocumentService
import grails.util.GrailsUtil
import groovy.transform.CompileStatic
import jakarta.servlet.http.HttpServletResponse

import org.w3c.dom.Document

@CompileStatic
abstract class RenderingService {

	static transactional = false

    XhtmlDocumentService xhtmlDocumentService
    GrailsApplication grailsApplication

	protected abstract doRender(Map args, Document document, OutputStream outputStream)

	protected abstract String getDefaultContentType()

	OutputStream render(Map args, OutputStream outputStream = new ByteArrayOutputStream()) {
		Document document = args.document as Document ?: xhtmlDocumentService.createDocument(args)
		render(args, document, outputStream)
	}

	OutputStream render(Map args, Document document, OutputStream outputStream = new ByteArrayOutputStream()) {
		try {
			processArgs(args)
			doRender(args, document, outputStream)
			outputStream
		} catch (Exception e) {
			if (log.errorEnabled) {
				GrailsUtil.deepSanitize(e)
				log.error("Rendering exception", e)
			}
			throw new RenderingException(e)
		}
	}

	boolean render(Map args, HttpServletResponse response) {
		processArgs(args)
		if (args.bytes) {
			writeToResponse(args, response, args.bytes as byte[])
		} else if (args.input) {
			writeToResponse(args, response, args.input as InputStream)
		} else {
			if (args.stream) {
				configureResponse(args, response)
				render(args, response.outputStream)
			} else {
				writeToResponse(args, response, (render(args) as ByteArrayOutputStream).toByteArray())
			}
		}
		false
	}

	protected writeToResponse(Map args, HttpServletResponse response, InputStream input) {
		configureResponse(args, response)
		if ((args.contentLength as int) > 0) {
			response.setContentLength(args.contentLength as int)
		}
		response.outputStream << input
	}

	protected writeToResponse(Map args, HttpServletResponse response, byte[] bytes) {
		configureResponse(args, response)
		response.setContentLength(bytes.size())
		response.outputStream << bytes
	}

	protected configureResponse(Map args, HttpServletResponse response) {
		setContentType(args, response)
		setResponseHeaders(args, response)
	}

	protected setResponseHeaders(Map args, HttpServletResponse response) {
		setContentDisposition(args, response)
	}

	protected setContentType(Map args, HttpServletResponse response) {
		response.setContentType(args.contentType as String ?: getDefaultContentType())
	}

	protected setContentDisposition(Map args, HttpServletResponse response) {
		if (args.filename) {
			response.setHeader("Content-Disposition", "attachment; filename=\"$args.filename\";")
		}
	}

	protected processArgs(Map args) {
		if (!args.base) {
			args.base = grailsApplication.config.get('grails.serverURL') ?: null
		}
	}
}
