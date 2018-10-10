package org.cybergarage.http.netty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cybergarage.http.HTTPHeader;
import org.cybergarage.http.HTTPResponse;
import org.cybergarage.http.netty.HttpRange.RangeIndex;
import org.cybergarage.xml.XML;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.jboss.netty.util.CharsetUtil;

import cn.ipanel.dlna.Logger;

import android.webkit.MimeTypeMap;

public class NettyHTTPServer {
	public static final String SERVER_DESC = "Netty 3.6.1 HTTP Server";
	
	public interface NettyRequestListener {
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception;
	}

	private int port;

	private NettyRequestListener listener;
	
	private ExecutorService boss;
	private ExecutorService worker;
	private Channel serverChannel;

	public NettyHTTPServer(int port) {
		this.port = port;
	}
	
	public void setPort(int port){
		this.port = port;
	}

	public void setRequestListener(NettyRequestListener listener) {
		this.listener = listener;
	}

	public void start() {
		if(serverChannel != null && serverChannel.isBound())
			return;
		ServerBootstrap b = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						boss = Executors.newCachedThreadPool(),
						worker = Executors.newCachedThreadPool()));

		b.setPipelineFactory(new PipeFactory());

		serverChannel = b.bind(new InetSocketAddress(port));
	}
	
	public void stop(){
		if (serverChannel != null) {
			ChannelFuture cf = serverChannel.close();
			cf.awaitUninterruptibly();
			boss.shutdown();
			worker.shutdown();
			serverChannel = null;
			boss = null;
			worker = null;
		}
	}

	class PipeFactory implements ChannelPipelineFactory {

		@Override
		public ChannelPipeline getPipeline() throws Exception {
			ChannelPipeline pipe = Channels.pipeline();

			pipe.addLast("decoder", new HttpRequestDecoder());
			pipe.addLast("aggregator", new HttpChunkAggregator(65536));
			pipe.addLast("encoder", new HttpResponseEncoder());
			pipe.addLast("chunkedWriter", new ChunkedWriteHandler());

			pipe.addLast("handler", new RequestHandler());
			return pipe;
		}

	}

	class RequestHandler extends SimpleChannelUpstreamHandler {

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
				throws Exception {
			if (listener != null)
				listener.messageReceived(ctx, e);
			else
				sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
				throws Exception {
			Channel ch = e.getChannel();
			Throwable cause = e.getCause();
			if (cause instanceof TooLongFrameException) {
				sendError(ctx, HttpResponseStatus.BAD_REQUEST);
				return;
			}

//			cause.printStackTrace();
			if (ch.isConnected()) {
				sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			}
		}

	}
	
	public static void sendXML(ChannelHandlerContext ctx, MessageEvent e, byte[] text){
		sendText(ctx, e, XML.CONTENT_TYPE, text);
	}

	public static void sendText(ChannelHandlerContext ctx, MessageEvent e, String contentType, byte[] text){
		HttpRequest request = (HttpRequest) e.getMessage();
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
		HttpHeaders.setContentLength(response, text.length);
		response.setHeader(HttpHeaders.Names.SERVER, SERVER_DESC);
		response.setContent(ChannelBuffers.copiedBuffer(text));
		
		ChannelFuture writeFuture= ctx.getChannel().write(response);
		
		// Decide whether to close the connection or not.
		if (!HttpHeaders.isKeepAlive(request)) {
			// Close the connection when the whole content is written out.
			writeFuture.addListener(ChannelFutureListener.CLOSE);
		}

	}
	
	public static void sendResponse(ChannelHandlerContext ctx, MessageEvent e, HTTPResponse resp){
//		Logger.d(resp.toString());
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(resp.getStatusCode()));
		
		int count = resp.getNHeaders();
		for(int i=0; i<count; i++){
			HTTPHeader hh = resp.getHeader(i);
			response.setHeader(hh.getName(), hh.getValue());
		}
		response.setContent(ChannelBuffers.copiedBuffer(resp.getContent()));
		
		ChannelFuture writeFuture= ctx.getChannel().write(response);
		
		// Decide whether to close the connection or not.
		if (!HttpHeaders.isKeepAlive((HttpRequest)e.getMessage())) {
			// Close the connection when the whole content is written out.
			writeFuture.addListener(ChannelFutureListener.CLOSE);
		}

	}
	
	public static void sendError(ChannelHandlerContext ctx,
			HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				status);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE,
				"text/plain; charset=UTF-8");
		response.setHeader(HttpHeaders.Names.SERVER, SERVER_DESC);
		response.setContent(ChannelBuffers.copiedBuffer(
				"Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

		// Close the connection as soon as the error message is sent.
		ctx.getChannel().write(response)
				.addListener(ChannelFutureListener.CLOSE);
	}

	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	public static final int HTTP_CACHE_SECONDS = 60;

	public static void sendFile(ChannelHandlerContext ctx, MessageEvent e,
			final File file, String contentType) throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();
		HttpRange httpRange = HttpRange.parseRange(request.getHeader(HttpHeaders.Names.RANGE));
		Logger.d("Resource content type: "+contentType);
		// Cache Validation
//		String ifModifiedSince = request
//				.getHeader(HttpHeaders.Names.IF_MODIFIED_SINCE);
//		if (ifModifiedSince != null && !ifModifiedSince.equals("")) {
//			SimpleDateFormat dateFormatter = new SimpleDateFormat(
//					HTTP_DATE_FORMAT, Locale.US);
//			Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
//
//			// Only compare up to the second because the datetime format we send
//			// to the client does not have milliseconds
//			long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
//			long fileLastModifiedSeconds = file.lastModified() / 1000;
//			if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
//				sendNotModified(ctx);
//				return;
//			}
//		}
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException fnfe) {
			sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}

		if(httpRange == null || httpRange.getRanges().size() == 0)
			sendRange(ctx, e, raf, file, contentType, request, null);
		else {
			for(RangeIndex ri : httpRange.getRanges()){
				sendRange(ctx, e, raf, file, contentType, request, ri);
			}
		}
	}

	protected static void sendRange(ChannelHandlerContext ctx, MessageEvent e,RandomAccessFile raf,
			final File file, String contentType, HttpRequest request, RangeIndex rangeIndex)
			throws IOException {
//		RandomAccessFile raf;
//		try {
//			raf = new RandomAccessFile(file, "r");
//		} catch (FileNotFoundException fnfe) {
//			sendError(ctx, HttpResponseStatus.NOT_FOUND);
//			return;
//		}
		long fileLength = raf.length();

		long start = 0;
		long length = fileLength;
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK);
		if(rangeIndex != null){
			response.setStatus(HttpResponseStatus.PARTIAL_CONTENT);
			response.addHeader(HttpHeaders.Names.CONTENT_RANGE, rangeIndex.toContentRange(fileLength));
			start = rangeIndex.getStart(fileLength);
			length = rangeIndex.getEnd(fileLength) - start + 1;
			Logger.d(start + " "+length);
		} else {
			response.setHeader(HttpHeaders.Names.ACCEPT_RANGES, HttpRange.UNITE_BYTES);
		}
		
		HttpHeaders.setContentLength(response, length);
//		setContentTypeHeader(response, file);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
//		setDateAndCacheHeaders(response, file);
		if (HttpHeaders.isKeepAlive(request)) {
			response.setHeader(HttpHeaders.Names.CONNECTION,
					HttpHeaders.Values.KEEP_ALIVE);
		}

		Channel ch = e.getChannel();

		// Write the initial line and the header.
		ch.write(response);

		// Write the content.
		ChannelFuture writeFuture;
		if (ch.getPipeline().get(SslHandler.class) != null) {
			// Cannot use zero-copy with HTTPS.
			writeFuture = ch.write(new ChunkedFile(raf, start, length, 8192));
		} else {
			// No encryption - use zero-copy.
			final FileRegion region = new DefaultFileRegion(raf.getChannel(),
					start, length);
			writeFuture = ch.write(region);
			writeFuture.addListener(new ChannelFutureProgressListener() {
				@Override
				public void operationComplete(ChannelFuture future) {
					region.releaseExternalResources();
				}

				@Override
				public void operationProgressed(ChannelFuture future,
						long amount, long current, long total) {
					 Logger.d(String.format("%s: %d / %d (+%d)%n", file.getPath(),
					 current, total, amount));
				}
			});
		}

		// Decide whether to close the connection or not.
		if (!HttpHeaders.isKeepAlive(request)) {
			// Close the connection when the whole content is written out.
			writeFuture.addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
	 * When file timestamp is the same as what the browser is sending up, send a
	 * "304 Not Modified"
	 * 
	 * @param ctx
	 *            Context
	 */
	private static void sendNotModified(ChannelHandlerContext ctx) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				HttpResponseStatus.NOT_MODIFIED);
		setDateHeader(response);

		// Close the connection as soon as the error message is sent.
		ctx.getChannel().write(response)
				.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * Sets the Date header for the HTTP response
	 * 
	 * @param response
	 *            HTTP response
	 */
	private static void setDateHeader(HttpResponse response) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT,
				Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		Calendar time = new GregorianCalendar();
		response.setHeader(HttpHeaders.Names.DATE,
				dateFormatter.format(time.getTime()));
	}

	/**
	 * Sets the Date and Cache headers for the HTTP Response
	 * 
	 * @param response
	 *            HTTP response
	 * @param fileToCache
	 *            file to extract content type
	 */
	private static void setDateAndCacheHeaders(HttpResponse response,
			File fileToCache) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT,
				Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		response.setHeader(HttpHeaders.Names.DATE,
				dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
		response.setHeader(HttpHeaders.Names.EXPIRES,
				dateFormatter.format(time.getTime()));
		response.setHeader(HttpHeaders.Names.CACHE_CONTROL, "private, max-age="
				+ HTTP_CACHE_SECONDS);
		response.setHeader(HttpHeaders.Names.LAST_MODIFIED,
				dateFormatter.format(new Date(fileToCache.lastModified())));
	}

	/**
	 * Sets the content type header for the HTTP Response
	 * 
	 * @param response
	 *            HTTP response
	 * @param file
	 *            file to extract content type
	 */
	private static void setContentTypeHeader(HttpResponse response, File file) {
		MimeTypeMap mimeTypesMap = MimeTypeMap.getSingleton();
		String ext = MimeTypeMap
				.getFileExtensionFromUrl(file.getAbsolutePath());
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE,
				mimeTypesMap.getMimeTypeFromExtension(ext));
	}

}
