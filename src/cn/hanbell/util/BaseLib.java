/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hanbell.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.xml.rpc.ServiceException;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 *
 * @author KevinDong
 */
public class BaseLib {

    public static boolean ADAuth(String url, String account, String password) throws Exception {
        Hashtable<String, String> property = new Hashtable<>();
        property.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory"); // LDAP工厂类
        property.put(Context.PROVIDER_URL, "ldap://" + url);// 默认端口389
        property.put(Context.SECURITY_AUTHENTICATION, "simple"); // LDAP访问安全级别(none,simple,strong)
        property.put(Context.SECURITY_PRINCIPAL, account); //AD账户
        property.put(Context.SECURITY_CREDENTIALS, password); //AD密码
        property.put("com.sun.jndi.ldap.connect.timeout", "6000");//连接超时设置
        LdapContext ctx = null;
        try {
            ctx = new InitialLdapContext(property, null);// 初始化上下文
            return true;
        } catch (AuthenticationException ex) {
            throw new Exception("身份验证失败!");
        } catch (javax.naming.CommunicationException ex) {
            throw new Exception("AD服务器连接失败!");
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (null != ctx) {
                try {
                    ctx.close();
                    ctx = null;
                } catch (Exception ex) {
                    throw ex;
                }
            }
        }
    }

    public static boolean ADAuth(String host, String port, String account, String password) throws Exception {
        String url = host + ":" + port;
        return ADAuth(url, account, password);
    }

    public static String formatDate(String format, Date date) {
        if (format != null && date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.format(date);
        } else {
            return "";
        }
    }

    public static String formatString(String format, String value) {
        if (value.length() >= format.length()) {
            return value;
        }
        return format.substring(0, format.length() - value.length()) + value;
    }

    public static Call getAXISCall() throws ServiceException {
        Service service = new Service();
        Call call = (Call) service.createCall();
        return call;
    }

    public static Call getAXISCall(String host, String port, String api) throws ServiceException {
        Call call = getAXISCall();
        call.setTargetEndpointAddress(host + ":" + port + api);
        return call;
    }

    public static Date getDate() {
        return Calendar.getInstance().getTime();
    }

    public static Date getDate(String format, String date) throws ParseException {
        if (format != null && date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            return dateFormat.parse(date);
        } else {
            return null;
        }
    }

    public static String getLocalOperateMessage(String value) {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundle = context.getApplication().getResourceBundle(context, "i18n");
        return bundle.getString(value);
    }

    public static Tax getTaxes(String taxtype, String taxkind, BigDecimal taxrate, BigDecimal amts, int scale) {
        Tax t = new Tax();
        if (taxtype == null || taxrate == null || amts == null) {
            return t;
        }
        if (amts.compareTo(BigDecimal.ZERO) == 0) {
            t.extax = BigDecimal.ZERO;
            t.taxes = BigDecimal.ZERO;
            return t;
        }
        if (taxrate.compareTo(BigDecimal.ZERO) == 0) {
            t.extax = amts;
            t.taxes = BigDecimal.ZERO;
            return t;
        }
        switch (taxtype) {
            case "0":
                t.extax = amts.divide(taxrate.divide(BigDecimal.valueOf(100)).add(BigDecimal.ONE), scale, RoundingMode.HALF_UP);
                t.taxes = amts.subtract(t.extax);
                break;
            case "1":
                t.extax = amts;
                t.taxes = amts.multiply(taxrate.divide(BigDecimal.valueOf(100)));
                break;
            case "2":
            case "3":
                t.extax = amts;
                t.taxes = BigDecimal.ZERO;
        }
        return t;
    }

    public static String securityMD5(String str) {

        try {
            return encrypt("MD5", str);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BaseLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public static String sha(String str) {

        try {
            return encrypt("SHA", str);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BaseLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public static String sha1(String str) {

        try {
            return encrypt("SHA-1", str);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(BaseLib.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public static String encrypt(String method, String str) throws UnsupportedEncodingException {

        if (str == null) {
            return "";
        }
        byte[] byteArray = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(method);
            digest.reset();
            digest.update(str.getBytes("UTF-8"));
            byteArray = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            Logger.getLogger(BaseLib.class.getName()).log(Level.SEVERE, null, e);
        }
        StringBuilder encryptionBuff = new StringBuilder();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                encryptionBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            } else {
                encryptionBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }
        return encryptionBuff.toString();

    }

}
