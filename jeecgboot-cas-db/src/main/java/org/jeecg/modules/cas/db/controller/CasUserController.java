package org.jeecg.modules.cas.db.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jeecg.modules.cas.db.entity.CasSysUser;
import org.jeecg.modules.cas.db.entity.SysUser;
import org.jeecg.modules.cas.db.mapper.SysUserMapper;
import org.jeecg.modules.cas.db.util.CommonConstant;
import org.jeecg.modules.cas.db.util.PasswordUtil;
import org.jeecg.modules.cas.db.util.SignatureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * CAS单点登录服务端REST权限认证
 * </p>
 *
 * @Author zhoujf
 * @since 2018-12-20
 */
@Slf4j
@RestController
@RequestMapping("/cas/user")
public class CasUserController {

	@Autowired
    private SysUserMapper sysUserMapper;
	
	@Value(value = "${syncUser.key}")
	private String syncUserKey;
	
	@SuppressWarnings("rawtypes")
	@PostMapping("/login")
	public Object login(@RequestHeader HttpHeaders httpHeaders) throws Exception {
		
		log.info("Rest api login.");
		log.debug("request headers: " + httpHeaders);
		CasSysUser user = null;
        try {
            UserLoginInfo userTemp = obtainUserFormHeader(httpHeaders);
            //当没有 传递 参数的情况
            if(userTemp == null){
                return new ResponseEntity<SysUser>(HttpStatus.NOT_FOUND);
            }
            SysUser sysUser = sysUserMapper.getSysUser(userTemp.username);
            //尝试查找用户库是否存在
            user  = checkUser(sysUser);
            if (user != null) {
            	//2. 校验用户名或密码是否正确
        		String userpassword = PasswordUtil.encrypt(userTemp.username, userTemp.password, sysUser.getSalt());
        		String syspassword = sysUser.getPassword();
        		if (!syspassword.equals(userpassword)) {
        			//密码不匹配
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
        		}
                if (user.isDisabled()) {
                    //禁用 403
                    return new ResponseEntity(HttpStatus.FORBIDDEN);
                }
                if (user.isLocked()) {
                    //锁定 423
                    return new ResponseEntity(HttpStatus.LOCKED);
                }
                if (user.isExpired()) {
                    //过期 428
                    return new ResponseEntity(HttpStatus.PRECONDITION_REQUIRED);
                }
            } else {
                //不存在 404
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
        } catch (UnsupportedEncodingException e) {
        	log.error("", e);
            new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        log.info("[{" + user.getUsername() + "}] login is ok");
        return user;
	}
	
	
    /**
	  * 查询用户列表
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/queryUserList")
	public Object queryUserList(HttpServletRequest req) throws Exception {
		log.info("查询用户接口");
		try {
			Map<String, String> paramMap = SignatureUtil.getParameterMap(req);
			boolean flag = SignatureUtil.checkSign(paramMap, syncUserKey, paramMap.get("sign"));
			if(!flag) {
				 return new ResponseEntity<String>("签名无效", HttpStatus.FORBIDDEN);
			}
			List<SysUser> ls = sysUserMapper.queryUserList();
			if(ls==null || ls.size()==0) {
	            return new ResponseEntity<String>("查无数据", HttpStatus.BAD_REQUEST);
			}
			return new ResponseEntity<List<SysUser>>(ls, HttpStatus.OK);
		} catch (Exception e) {
			log.error("", e);
			return new ResponseEntity<String>("系统异常:"+e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
	
	/**
	   * 校验用户是否有效
	 * @param sysUser
	 * @return
	 */
	public CasSysUser checkUser(SysUser sysUser) {
		//情况1：根据用户信息查询，该用户不存在
		if (sysUser == null) {
			return null;
		}
		CasSysUser  casSysUser = new CasSysUser();
		casSysUser.setId(sysUser.getUsername());
		casSysUser.setUsername(sysUser.getUsername());
		casSysUser.setPassword(sysUser.getPassword());
		//情况2：根据用户信息查询，该用户已注销
		if (CommonConstant.DEL_FLAG_1.toString().equals(sysUser.getDelFlag())) {
			casSysUser.setDisabled(true);
		}
		//情况3：根据用户信息查询，该用户已冻结
		if (CommonConstant.USER_FREEZE.equals(sysUser.getStatus())) {
			casSysUser.setLocked(true);
		}
		return casSysUser;
	}
	
	/**
     * 根据请求头获取用户名及密码
     *
     * @param httpHeaders
     * @return
     * @throws UnsupportedEncodingException
     */
    private UserLoginInfo obtainUserFormHeader(HttpHeaders httpHeaders) throws UnsupportedEncodingException {
        /**
         *
         * This allows the CAS server to reach to a remote REST endpoint via a POST for verification of credentials.
         * Credentials are passed via an Authorization header whose value is Basic XYZ where XYZ is a Base64 encoded version of the credentials.
         */
        //根据官方文档，当请求过来时，会通过把用户信息放在请求头authorization中，并且通过Basic认证方式加密
        String authorization = httpHeaders.getFirst("authorization");//将得到 Basic Base64(用户名:密码)
        if(StringUtils.isEmpty(authorization)){
            return null;
        }
        String baseCredentials = authorization.split(" ")[1];
        String usernamePassword = new String(Base64Utils.decodeFromString(baseCredentials), "UTF-8");//用户名:密码
        log.debug("login user: " + usernamePassword);
        String credentials[] = usernamePassword.split(":");
        return new UserLoginInfo(credentials[0], credentials[1]);
    }


    /**
     * 解析请求过来的用户
     */
    private class UserLoginInfo {
        private String username;
        private String password;

        public UserLoginInfo(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

	
}
