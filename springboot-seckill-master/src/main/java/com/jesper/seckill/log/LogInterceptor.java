/**
 * 
 */
package com.jesper.seckill.log;
/**
* @author Li Yang
* @version vb1.0
* @email 1246457819@qq.com
* @date 2019年4月11日 下午8:06:31
*/


import java.util.Enumeration;
 
import javax.servlet.http.HttpServletRequest;
 
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
 
import com.alibaba.fastjson.JSONObject;
 
@Aspect  
@Component 
public class LogInterceptor implements Ordered{
	  @Autowired
	  private KafkaTemplate kafkaTemplate;
	  
	  
	  @Around("@annotation(systemLog)")
	  public Object Log(ProceedingJoinPoint joinPoint,SystemLog systemLog){
	    	Object retVal = null;
	        try {
	            if (joinPoint == null) { 
	            	return null;
	            }
	            JSONObject message = new JSONObject();
	            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	            String ip = getIpAddr(request);
	
	            //获取方法参数
	            Enumeration<String> eParams = request.getParameterNames();
	            while (eParams.hasMoreElements()) {
	                String key = eParams.nextElement();
	                String value = request.getParameter(key);
	                message.put(key, value);
	            }
	
	            //获取header参数
	            Enumeration<?> headerNames = request.getHeaderNames();
	            while (headerNames.hasMoreElements()) {
	                String key = (String) headerNames.nextElement();
	                String value = request.getHeader(key);
	                //时间戳单位统一
	                if ("timestamp".equals(key) && StringUtils.isNotBlank(value) && value.length() > 10) {
	                    value = value.substring(0, 10);
	                }
	                message.put(key, value);
	            }
	            String requestURL=request.getRequestURL().toString();
	            message.put("requestURL", requestURL);message.put("class", joinPoint.getTarget().getClass().getName());
	            message.put("request_method", joinPoint.getSignature().getName());
	            message.put("ip", ip);
	            message.put("systemName", "all");
	            kafkaTemplate.send("systemlog-yangli_seckill",message.toJSONString());
	            retVal =joinPoint.proceed();
	        } catch (Throwable throwable) {
	        	throwable.getMessage();
	        }
	        return retVal;
		}
	  	
	    
	    public String getIpAddr(HttpServletRequest request) {
	        String ipAddress = null;
	        ipAddress = request.getHeader("x-forwarded-for");
	        if (ipAddress == null || ipAddress.length() == 0
	                || "unknown".equalsIgnoreCase(ipAddress)) {
	            ipAddress = request.getHeader("Proxy-Client-IP");
	        }
	        if (ipAddress == null || ipAddress.length() == 0
	                || "unknown".equalsIgnoreCase(ipAddress)) {
	            ipAddress = request.getHeader("WL-Proxy-Client-IP");
	        }
	        if (ipAddress == null || ipAddress.length() == 0
	                || "unknown".equalsIgnoreCase(ipAddress)) {
	            ipAddress = request.getRemoteAddr();
	        }
	
	// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
	        if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
	            // = 15
	            if (ipAddress.indexOf(",") > 0) {
	                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
	            }
	        }
	        //或者这样也行,对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
	//return ipAddress!=null&&!"".equals(ipAddress)?ipAddress.split(",")[0]:null;
	        return ipAddress;
	    }
	    
 
		@Override
		public int getOrder() {
			return 0;
		}
}
