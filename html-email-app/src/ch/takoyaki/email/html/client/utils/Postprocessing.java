/*******************************************************************************
 * Copyright (c) 2013 takoyaki.ch.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     takoyaki.ch - Initial version
 ******************************************************************************/
package ch.takoyaki.email.html.client.utils;

import ch.takoyaki.email.html.client.service.FileService;

public class Postprocessing {
	private static native String postProcessNative(String functionHook,
			String content, String functions) /*-{
		try {
			$wnd.functionHook = null;
			$wnd.eval(functions);
			return $wnd[functionHook](content);
		} catch (e) {
			console.log("Error: processing failed in function:" + functionHook
					+ " with message:" + e.message);
			return content;
		}
	}-*/;

	private static String postProcess(String functionHook, String content,
			FileService fservice) {
		String postprocessing = fservice.retrieve("__postprocessing.js");
		if (postprocessing != null) {
			content = postProcessNative(functionHook, content, postprocessing);
		}
		return content;
	}

	public static String beforePreview(String content, FileService fservice) {
		return postProcess("before_preview", content, fservice);
	}

	public static String beforeSaveEml(String content, FileService fservice) {
		return postProcess("before_save_eml", content, fservice);
	}

	public static String beforeSaveHtml(String content, FileService fservice) {
		return postProcess("before_save_html", content, fservice);
	}

}
