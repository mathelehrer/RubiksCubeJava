package com.numbercruncher.rubikscube.xml;


import com.numbercruncher.rubikscube.logger.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.PrintStream;

public class ErrorHandler implements org.xml.sax.ErrorHandler {

	private PrintStream out;
	
	public ErrorHandler(PrintStream out) {
		this.out=out;
	}

	private String getParseExceptionInfo(SAXParseException spe) {
        String systemId = spe.getSystemId();

        if (systemId == null) {
            systemId = "null";
        }

        String info = "URI=" + systemId + " Line=" 
            + spe.getLineNumber() + ": " + spe.getMessage();

        return info;
    }
	
	@Override
	public void error(SAXParseException spe) throws SAXException {
		String message = "Error: " + getParseExceptionInfo(spe);
		Logger.logging(Logger.Level.error, message);
        throw new SAXException(message);
	}

	@Override
	public void fatalError(SAXParseException spe) throws SAXException {
		String message = "Fatal Error: " + getParseExceptionInfo(spe);
		Logger.logging(Logger.Level.error, message);
        throw new SAXException(message);
	}

	@Override
	public void warning(SAXParseException spe) throws SAXException {
		Logger.logging(Logger.Level.warning, "Warning: " + getParseExceptionInfo(spe));

	}

}
