/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.remote;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.internal.webapp.data.UrlUtil;

public class MockContentServlet extends HttpServlet {

	private static final long serialVersionUID = 2360013070409217702L;

	/**
	 * Return a create page based on the path and locale unless the path
	 * starts with "/invalid" in which case return an I/O error
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String locale = UrlUtil.getLocale(req, resp);
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
		String path = req.getPathInfo();
		int slash = path.indexOf('/', 1);
		String plugin = path.substring(1, slash);
		String file = path.substring(slash);
		if (file.startsWith("/invalid")) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
		    String response = RemoteTestUtils.createMockContent(plugin, file, locale);
		    resp.getWriter().write(response);
		}
	}

}