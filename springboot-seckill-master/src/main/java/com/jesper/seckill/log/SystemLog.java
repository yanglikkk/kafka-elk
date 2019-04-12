/**
 * 
 */
package com.jesper.seckill.log;
/**
* @author Li Yang
* @version vb1.0
* @email 1246457819@qq.com
* @date 2019年4月11日 下午8:07:05
*/


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SystemLog {
}
