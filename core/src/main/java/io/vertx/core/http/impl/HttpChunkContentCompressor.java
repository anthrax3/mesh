/*
 * Copied from https://github.com/eclipse/vert.x/blob/1ea29f558425713cfd9ee77a3c000be86c91efde/src/main/java/io/vertx/core/http/impl/HttpChunkContentCompressor.java
 * to patch https://github.com/eclipse/vert.x/issues/2184 which caused http requests to fail
 * when "Connection: close" header is set and compression is enabled.
 *
 * TODO: remove this patch when upgrading to Vert.x 3.5.1
 * 
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.core.http.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContentCompressor;

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
final class HttpChunkContentCompressor extends HttpContentCompressor {

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof ByteBuf) {
			// convert ByteBuf to HttpContent to make it work with compression. This is needed as we use the
			// ChunkedWriteHandler to send files when compression is enabled.
			ByteBuf buff = (ByteBuf) msg;
			if (buff.isReadable()) {
				// We only encode non empty buffers, as empty buffers can be used for determining when
				// the content has been flushed and it confuses the HttpContentCompressor
				// if we let it go
				msg = new DefaultHttpContent(buff);
			}
		}
		super.write(ctx, msg, promise);
	}

	HttpChunkContentCompressor(int compressionLevel) {
		super(compressionLevel);
	}

}