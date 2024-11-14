package org.jeecg.modules.cas.db.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;

import org.jeecg.modules.cas.db.exceptions.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 加签、验签工具.
 *
 * @author zhoujf
 */
public abstract class SignatureUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SignatureUtil.class);
    
    /**
     * 加签,MD5.
     * @param paramMap 参数Map,不包含商户秘钥且顺序确定
     * @param key  商户秘钥
     * @return  签名串
     * @throws BusinessException 
     */
    public static String sign(Map<String, String> paramMap, String key) throws BusinessException {
        if(key == null){
            throw new BusinessException("key不能为空");
        }
        String sign = createSign(paramMap,key);
        return sign;
    }
    
    /**
	 * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 */
	private static String createSign(Map<String, String> paramMap, String key) {
		StringBuffer sb = new StringBuffer();
		SortedMap<String,String> sort=new TreeMap<String,String>(paramMap);  
		Set<Entry<String, String>> es = sort.entrySet();
		Iterator<Entry<String, String>> it = es.iterator();
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v)&& !"null".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + key);
		LOG.info("HMAC source:{}", new Object[] { sb.toString() } );
		String sign = MD5Util.MD5Encode(sb.toString(), "UTF-8").toUpperCase();
		LOG.info("HMAC:{}", new Object[] { sign } );
		return sign;
	}

    /**
     * 验签, 仅支持MD5.
     * @param paramMap 参数Map,不包含商户秘钥且顺序确定
     * @param key  商户秘钥
     * @param sign     签名串
     * @return         验签结果
     * @throws BusinessException 
     */
    public static boolean checkSign(Map<String, String> paramMap, String key, String sign) throws BusinessException {
        if(key == null){
            throw new BusinessException("key不能为空");
        }
        if(sign == null){
            throw new BusinessException("需要验签的字符为空");
        }

        return sign.equals(sign(paramMap,key));
    }
    
	public static Map<String, String> getParameterMap(HttpServletRequest request) {
		// 参数Map
		Map<?, ?> properties = request.getParameterMap();
		// 返回值Map 
		Map<String, String> returnMap = new HashMap<String, String>();
		Iterator<?> entries = properties.entrySet().iterator();
		
		Map.Entry<String, Object> entry;
		String name = "";
		String value = "";
		Object valueObj =null;
		while (entries.hasNext()) {
			entry = (Map.Entry<String, Object>) entries.next();
			name = (String) entry.getKey();
			valueObj = entry.getValue();
			if ("_t".equals(name) || null == valueObj) {
				value = "";
			} else if (valueObj instanceof String[]) {
				String[] values = (String[]) valueObj;
				for (int i = 0; i < values.length; i++) {
					value = values[i] + ",";
				}
				value = value.substring(0, value.length() - 1);
			} else {
				value = valueObj.toString();
			}
			returnMap.put(name, value);
		} 
		return returnMap;
	}
    
    public static void main(String[] args) throws BusinessException {
    	String id = "4";
    	Map<String,String> paramMap = new HashMap<String, String>();
    	paramMap.put("sysCode", "jeecgbpm");
    	paramMap.put("dataId", id);
    	paramMap.put("applySysCode", "qyweixin");
		paramMap.put("applyUserId", "test");
		paramMap.put("bizTitile", "第三方测试订单【"+id+"】");
		paramMap.put("formUrl", "http://www.baidu.com");
		paramMap.put("mobileFormUrl", "");
		paramMap.put("data", "{id:'"+id+"',name:'zhangsan'}");
		paramMap.put("processKey", "process1489455729094");
		paramMap.put("callBackUrl", "http://www.baidu.com");
		String sign = sign(paramMap,"ED6F7278AED61912B6C470372E839C3FC839C56CAB1912B53FC47BF72780372E");
		System.out.println(sign);
		paramMap.put("sign", sign);
		String url = "http://127.0.0.1:8888/jeecg-bpm/flowApi/startProcess.do";
		/*String result = HttpUtils.post(url, paramMap);
		System.out.println("--------------"+result);*/
		
	}
}
