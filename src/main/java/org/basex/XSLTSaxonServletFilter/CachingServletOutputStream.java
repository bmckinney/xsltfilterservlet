package org.basex.XSLTSaxonServletFilter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

/**
 * This class caches the output stream such that even a commited stream can be 
 * written to again. 
 * @author michael
 *
 */
public class CachingServletOutputStream extends ServletOutputStream {

	private final DataOutputStream stream;
	public CachingServletOutputStream(OutputStream out){
		super();
		stream = new DataOutputStream(out);
	}
	@Override
	public void write(int b) throws IOException {
		stream.write(b);
	}
}
