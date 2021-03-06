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
package ch.takoyaki.email.html.client;

import ch.takoyaki.email.html.client.service.FileService;
import ch.takoyaki.email.html.client.utils.Html;
import ch.takoyaki.email.html.client.utils.Postprocessing;
import ch.takoyaki.email.html.client.utils.Xsl;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;

public class HtmlPreview extends Composite implements ContentRenderer {

	private static final String preview_id = "preview_id";

	private static HtmlPreviewUiBinder uiBinder = GWT
			.create(HtmlPreviewUiBinder.class);

	private final FileService fservice;

	private int scrollTopValue = 0;

	private String previewedName;

	interface HtmlPreviewUiBinder extends UiBinder<Widget, HtmlPreview> {
	}

	public HtmlPreview(FileService fservice) {
		initWidget(uiBinder.createAndBindUi(this));
		this.fservice = fservice;
		getFrame().getElement().setId(preview_id);

		getFrame().addDomHandler(new LoadHandler() {
			public void onLoad(LoadEvent event) {
				setScrollTop(getScrollTopValue());
			}
		}, LoadEvent.getType());
	}

	public int getScrollTopValue() {
		return scrollTopValue;
	}

	private Frame getFrame() {
		return (Frame) getWidget();
	}

	private boolean isXmlContent(String content) {
		return RegExp.compile("^<\\?xml", "gmi").exec(content) != null;
	}

	private native int scrollTop() /*-{
		return $wnd.getIframeScrollTop("preview_id");
	}-*/;

	private native void setScrollTop(int top) /*-{
		$wnd.setIframeScrollTop("preview_id", top);
	}-*/;

	public native String getRenderedContent() /*-{
		return $wnd.getIframeContent("preview_id");
	}-*/;

	private native void setPreviewContent(String content) /*-{
		$wnd.setIframeContent("preview_id", content);
	}-*/;

	private void setPreviewContent(String content, String contentType) {
		getFrame().setUrl(Html.createDataUri(content, contentType, "UTF-8"));
	}

	public void preview(String text) {
		if (text == null) {
			return;
		}

		scrollTopValue = scrollTop();

		String content = fservice.retrieve(text);
		if (content != null) {
			this.previewedName = text;
			if (isXmlContent(content)) {
				String xsl = Xsl.loadStyleSheetContent(content, fservice);
				content = Xsl.removeStyleSheet(content);
				if (xsl != null) {
					content = Xsl.xslttransform(xsl, content);
				}
			}
			content = Html.inlinecss(content, fservice);

			content = Postprocessing.beforePreview(content, fservice);

			setPreviewContent(content);
		}

	}

	@Override
	public String getRenderedFileName() {
		if (this.previewedName != null) {
			return this.previewedName;
		}
		return "empty";
	}

}
