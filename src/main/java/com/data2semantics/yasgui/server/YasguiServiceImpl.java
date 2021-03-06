package com.data2semantics.yasgui.server;

/*
 * #%L
 * YASGUI
 * %%
 * Copyright (C) 2013 Laurens Rietveld
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import org.json.JSONObject;
import com.data2semantics.yasgui.client.YasguiService;
import com.data2semantics.yasgui.server.fetchers.ConfigFetcher;
import com.data2semantics.yasgui.server.fetchers.PrefixesFetcher;
import com.data2semantics.yasgui.server.fetchers.endpoints.EndpointsFetcher;
import com.data2semantics.yasgui.shared.SettingKeys;
import com.data2semantics.yasgui.shared.exceptions.FetchException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.rosaloves.bitlyj.*;
import static com.rosaloves.bitlyj.Bitly.*;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class YasguiServiceImpl extends RemoteServiceServlet implements YasguiService {
	public static String CACHE_DIR = "/cache";
	private final static Logger LOGGER = Logger.getLogger(YasguiServiceImpl.class.getName()); 
	public String fetchPrefixes(boolean forceUpdate) throws IllegalArgumentException, FetchException {
		String prefixes = "";
		try {
			prefixes = PrefixesFetcher.fetch(forceUpdate, new File(getServletContext().getRealPath(CACHE_DIR))); 
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "exception", e);
			throw new FetchException("Unable to fetch prefixes", e);
		}
		return prefixes;
	}
	
	public String getShortUrl(String longUrlString) throws IllegalArgumentException, FetchException {
		String shortUrl;
		try {
			ServletContext servletContext = this.getServletContext();
			JSONObject config = ConfigFetcher.getJsonObject(servletContext.getRealPath("/"));
			String bitlyApiKey = config.getString(SettingKeys.BITLY_API_KEY);
			String bitlyUsername = config.getString(SettingKeys.BITLY_USERNAME);
			LOGGER.info("issuing bitly call for url " + longUrlString);
			Url url = as(bitlyUsername, bitlyApiKey).call(shorten(longUrlString));
			LOGGER.info("bitly call done");
			shortUrl = url.getShortUrl();
			LOGGER.info("retrieved short url: " + shortUrl);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "exception", e);
			throw new FetchException("Unable to create short url", e);
		}
		return shortUrl;
	}
	
	
	public String fetchEndpoints(boolean forceUpdate) throws IllegalArgumentException, FetchException {
		String endpoints = "";
		try {
			endpoints = EndpointsFetcher.fetch(forceUpdate, new File(getServletContext().getRealPath(CACHE_DIR))); 
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "exception", e);
			throw new FetchException("Unable to fetch endpoints: " + e.getMessage() + "\n", e);
		}
		return endpoints;
	}
}
