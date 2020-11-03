package com.virjar.thanos.system;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.virjar.thanos.api.util.URLEncodedUtils;
import com.virjar.thanos.entity.ThanosUser;
import com.virjar.thanos.entity.vo.CommonRes;
import com.virjar.thanos.service.impl.ThanosOpLogService;
import com.virjar.thanos.service.impl.ThanosUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

@Component
@Slf4j

public class LoginInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<ThanosUser> threadLocal = new ThreadLocal<>();

    @Resource
    private ThanosUserService grabUserService;

    @Resource
    private ThanosOpLogService grabOpLogService;

    private static final byte[] needLoginResponse = JSONObject.toJSONString(CommonRes.failed(CommonRes.statusNeedLogin, "need login")).getBytes(Charsets.UTF_8);
    private static final byte[] loginExpire = JSONObject.toJSONString(CommonRes.failed(CommonRes.statusLoginExpire, "login expire")).getBytes(Charsets.UTF_8);
    private static final byte[] onlyForAdminResponse = JSONObject.toJSONString(CommonRes.failed(CommonRes.statusLoginExpire, "only available for administrator")).getBytes(Charsets.UTF_8);

    @Override

    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        Method method = ((HandlerMethod) handler).getMethod();
        LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
        if (loginRequired == null) {
            return true;
        }

        String operatorToken = request.getParameter("operatorToken");
        if (StringUtils.isBlank(operatorToken)) {
            response.addHeader("content-type", "application/json; charset=utf-8");
            response.getOutputStream().write(needLoginResponse);
            return false;
        }

        ThanosUser thanosUser = grabUserService.checkLogin(operatorToken);
        if (thanosUser == null) {
            response.addHeader("content-type", "application/json; charset=utf-8");
            response.getOutputStream().write(loginExpire);
            return false;
        }

        if (loginRequired.forAdmin()) {
            if (!BooleanUtils.isTrue(thanosUser.getIsAdmin())) {
                response.addHeader("content-type", "application/json; charset=utf-8");
                response.getOutputStream().write(onlyForAdminResponse);
                return false;
            }
        }

        if (thanosUser.getBlocked()) {
            response.addHeader("content-type", "application/json; charset=utf-8");
            response.getOutputStream().write(loginExpire);
            return false;
        }

        threadLocal.set(thanosUser);

        if (loginRequired.monitor()) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            StringBuilder stringBuilder = new StringBuilder();
            for (String key : parameterMap.keySet()) {
                String[] values = parameterMap.get(key);
                for (String value : values) {
                    stringBuilder.append(URLEncodedUtils.urlEncode(key))
                            .append("=");
                    if (value != null) {
                        stringBuilder.append(URLEncodedUtils.urlEncode(value));
                    }
                    stringBuilder.append("&");
                }
            }
            if (stringBuilder.length() > 1) {
                stringBuilder.setLength(stringBuilder.length() - 1);
            }
            //只能打印GET的参数，post参数需要依靠业务层独立打印日志
            grabOpLogService.log("access uri: " + request.getRequestURI()
                    + " method:" + request.getMethod() + " param: " + stringBuilder.toString());
        }
        return true;
    }

    @Override

    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) {
        threadLocal.remove();

    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest httpServletRequest, @NonNull HttpServletResponse httpServletResponse, @NonNull Object o, Exception e) {
        if (e != null) {
            log.error("error", e);
        }
    }

    public static ThanosUser getSessionUser() {
        return threadLocal.get();
    }
}
