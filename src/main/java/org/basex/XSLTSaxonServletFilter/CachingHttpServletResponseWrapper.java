package org.basex.XSLTSaxonServletFilter;

import java.io.ByteArrayOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CachingHttpServletResponseWrapper extends
		HttpServletResponseWrapper {
	private ByteArrayOutputStream out;

	public CachingHttpServletResponseWrapper(HttpServletResponse response) {
		super(response);
		out = new ByteArrayOutputStream();
		// TODO Auto-generated constructor stub
	}

	public ServletOutputStream getOutputStream() {
		return new CachingServletOutputStream(out);
	}

	public byte[] getData() {
		return out.toByteArray();
	}

}
