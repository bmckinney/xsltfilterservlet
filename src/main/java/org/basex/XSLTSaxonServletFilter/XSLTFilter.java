package org.basex.XSLTSaxonServletFilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XSLTFilter implements Filter {

	/** The transformer Factory. */
	private static final TransformerFactory tfc = SAXTransformerFactory
			.newInstance();
	/** Filter configuration */
	private FilterConfig filterConfig;
	/** Cache of transformer Objects */
	private final Map<String, Transformer> transformers = new HashMap<String, Transformer>();
	/** Flag enables transformer cache. */
	private boolean cache = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 * javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest rq, ServletResponse rp,
			FilterChain chain) throws IOException, ServletException {

		final CachingHttpServletResponseWrapper wrapper = new CachingHttpServletResponseWrapper(
				(HttpServletResponse) rp);
		chain.doFilter(rq, wrapper);
		final byte[] data = wrapper.getData();

		final boolean transform = (wrapper.getContentType() != null
				&& wrapper.getContentType().startsWith("application/xml") && needsFilter(data));
		// we need to wrap (i.e. cache) this so we can transform the content
		if (transform) {
			try {
				transform(rp, data);
			} catch (Exception e) {
				throw new ServletException(e.getCause());
			}

		} else {
			if (data.length > 0) {
				rp.getOutputStream().write(data);
				rp.setContentLength(data.length);
			}
		}
		rp.getOutputStream().flush();
	}

	/**
	 * Transforms an OutputStream and writes the transformed stream to the
	 * {@link ServletResponse}
	 * 
	 * @param rp
	 *            target for the transformed output stream
	 * @param data
	 *            the cached output stream, ready to be transformed
	 * @throws IOException
	 *             if the {@link ServletResponse} stream has already been
	 *             commited
	 * @throws TransformerException
	 *             if the stylesheet contains errors
	 */
	private void transform(final ServletResponse rp, byte[] data)
			throws IOException, TransformerException {
		final Source xmlSource = new StreamSource(
				new ByteArrayInputStream(data));
		final Source stylesheet = tfc.getAssociatedStylesheet(new StreamSource(
				new ByteArrayInputStream(data)), null, null, null);

		// this only works for styles in `file://`
		stylesheet.setSystemId(this.filterConfig.getServletContext()
				.getRealPath(stylesheet.getSystemId().replace("file:", "")));

		final Transformer trans = transformer(tfc, stylesheet);

		ByteArrayOutputStream resultBuf = new ByteArrayOutputStream();
		trans.transform(xmlSource, new StreamResult(resultBuf));
		rp.resetBuffer();
		rp.setContentLength(resultBuf.size());
		rp.setContentType("text/html"); // Important, otherwise XSLTForms
										// Javascript won't work...
		rp.getOutputStream().write(resultBuf.toByteArray());
		rp.flushBuffer();
	}

	/**
	 * Returns a Transformer Object for a given Stylesheet’s SystemID. Tries to
	 * cache the transformers to avoid reparsing a stylesheet.
	 * 
	 * @param tfc
	 *            {@link TransformerFactory} that creates a new
	 *            {@link Transformer} if needed
	 * @param stylesheet
	 *            the stylesheet’s SystemID.
	 * @return cached Transformer
	 * @throws TransformerConfigurationException
	 */
	private Transformer transformer(final TransformerFactory tfc,
			final Source stylesheet) throws TransformerConfigurationException {
		if (!cache)
			return tfc.newTransformer(stylesheet);

		if (transformers.get(stylesheet.getSystemId()) != null) {
			return transformers.get(stylesheet.getSystemId());

		} else {
			System.err.println("New Transformer added");
			Transformer t = tfc.newTransformer(stylesheet);
			transformers.put(stylesheet.getSystemId(), t);
			return t;
		}

	}

	/**
	 * Checks if the output stream starts with &lt;?xml-stylesheet
	 * 
	 * @param data
	 *            output stream
	 * @return true if the stream starts with the stylesheet pi
	 * @throws IOException
	 */
	private boolean needsFilter(byte[] data) throws IOException {
		if (data.length == 0)
			return false;
		try {
			StreamSource s = new StreamSource(new ByteArrayInputStream(data));
			return null != tfc.getAssociatedStylesheet(s, null, null, null);
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		cache = "true".equals(filterConfig.getInitParameter("cache"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		this.filterConfig = null;
	}

}
