package org.baeldung;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.io.CharStreams;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@Component
public class ModifyResponseBodyFilter extends ZuulFilter {

	@Autowired
	private FilterUtil filterUtil;

	@Override
	public boolean shouldFilter() {
		return FilterUtil.SHOULD_FILTER;
	}

	@Override
	public String filterType() {
		return FilterUtil.FILTER_TYPE_POST;
	}

	@Override
	public int filterOrder() {
		return FilterUtil.FILTER_ORDER;
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext context = RequestContext.getCurrentContext();
		try (final InputStream responseDataStream = context.getResponseDataStream()) {

			if (responseDataStream == null) {
				//logger.info("BODY: {}", "");
				return null;
			}

			String responseData = CharStreams.toString(new InputStreamReader(responseDataStream, "UTF-8"));
			//logger.info("BODY: {}", responseData);
			JSONObject json = new JSONObject(responseData);
			String bearerToken = json.getString("access_token");
			json.remove("access_token");
			json.append("access_token", "curl -X POST Authorization Bearer" + bearerToken);

			context.setResponseBody(json.toString());
		} catch (Exception e) {
			//throw new ZuulException(e, INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return null;
	}

}