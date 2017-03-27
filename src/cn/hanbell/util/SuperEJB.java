/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hanbell.util;

import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Query;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author KevinDong
 * @param <T>
 */
public abstract class SuperEJB<T extends Object> extends com.lightshell.comm.SuperEJB<T> {

    public SuperEJB(Class<T> entityClass) {
        super(entityClass);
    }

    public T createInstance() {
        try {
            return entityClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(SuperEJB.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    //透过xml格式创建对象实例
    public T createInstance(String xmlObject) {
        try {
            JAXBContext context = JAXBContext.newInstance(entityClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            //Unmarshal the XML in the stringWriter back into an object
            T entity = (T) unmarshaller.unmarshal(new StringReader(xmlObject));
            return entity;
        } catch (JAXBException ex) {
            Logger.getLogger(SuperEJB.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public String getFormId(Date day, String code, String format, int len, String tableName, String columnName) {
        String maxid, newid;
        int id;
        if (day != null && code != null && len > 0) {
            String d = "";
            if (format != null && !format.equals("")) {
                d = BaseLib.formatDate(format, day);
            }
            int c = code.length();
            int f = d.length();
            Query query = getEntityManager().createNativeQuery("select max(" + columnName + ") from  " + tableName
                    + " where substring(" + columnName + "," + 1 + "," + (c + f) + ")='" + (code + d) + "'");
            if (query.getSingleResult() != null) {
                maxid = query.getSingleResult().toString();
                int m = maxid.length();
                id = Integer.parseInt(maxid.substring(m - len, m)) + 1;
                newid = code + d + String.format("%0" + len + "d", id);
            } else {
                newid = code + d + String.format("%0" + len + "d", 1);
            }
            return newid;
        } else {
            return "";
        }
    }

    public T findById(Object value) {
        Query query = getEntityManager().createNamedQuery(getClassName() + ".findById");
        query.setParameter("id", value);
        try {
            Object entity = query.getSingleResult();
            if (entity != null) {
                return (T) entity;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public List<T> findByFilters(Map<String, Object> filters, int first, int pageSize) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT e FROM ");
        sb.append(this.className);
        sb.append(" e WHERE 1=1 ");
        if (filters != null) {
            this.setQueryFilter(sb, filters);
        }
        //生成SQL
        Query query = getEntityManager().createQuery(sb.toString()).setFirstResult(first).setMaxResults(pageSize);
        //参数赋值
        if (filters != null) {
            this.setQueryParam(query, filters);
        }
        List<T> results = query.getResultList();
        return results;
    }

}
