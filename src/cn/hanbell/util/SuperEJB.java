/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hanbell.util;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author KevinDong
 * @param <T>
 */
public abstract class SuperEJB<T> implements Serializable {

    protected String className;

    protected Class<T> entityClass;

    public SuperEJB(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.className = this.entityClass.getSimpleName();
    }

    public abstract EntityManager getEntityManager();

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

    public JsonArrayBuilder createJsonArrayBuilder(List<T> entityList) {
        if (entityList != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            for (T entity : entityList) {
                jab.add(createJsonObjectBuilder(entity));
            }
            return jab;
        } else {
            return null;
        }
    }

    public JsonObjectBuilder createJsonObjectBuilder(T entity) {
        return null;
    }

    //新增
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void persist(T entity) {
        try {
            getEntityManager().persist(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //修改
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public T update(T entity) {
        try {
            T e = getEntityManager().merge(entity);
            return e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //修改
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void update(List<T> entityList) {
        try {
            for (T t : entityList) {
                update(t);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //删除
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(T entity) {
        try {
            if (getEntityManager().contains(entity)) {
                getEntityManager().remove(entity);
            } else {
                getEntityManager().remove(getEntityManager().merge(entity));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //删除
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(List<T> entityList) {
        try {
            for (T t : entityList) {
                delete(t);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //审核
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public T verify(T entity) {
        try {
            T e = getEntityManager().merge(entity);
            return e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //取消
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public T unverify(T entity) {
        try {
            T e = getEntityManager().merge(entity);
            return e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getFormId(Date day, String code, String format, int len) {
        return getFormId(day, code, format, len, this.className, "formid");
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

    public int getRowCount() {
        Query query = getEntityManager().createNamedQuery(getClassName() + ".getRowCount");
        if (query.getSingleResult() == null) {
            return 0;
        } else {
            return Integer.parseInt(query.getSingleResult().toString());
        }
    }

    public int getRowCount(Map<String, Object> filters) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(e) FROM ");
        sb.append(this.className);
        sb.append(" e WHERE 1=1 ");
        if (filters != null) {
            this.setQueryFilter(sb, filters);
        }
        //生成SQL
        Query query = getEntityManager().createQuery(sb.toString());
        //参数赋值
        if (filters != null) {
            this.setQueryParam(query, filters);
        }
        return Integer.parseInt(query.getSingleResult().toString());
    }

    public List<T> findAll() {
        Query query = getEntityManager().createNamedQuery(getClassName() + ".findAll");
        return query.getResultList();
    }

    public List<T> findAll(int first, int pageSize) {
        Query query = getEntityManager().createNamedQuery(getClassName() + ".findAll").setFirstResult(first).setMaxResults(first + pageSize);
        return query.getResultList();
    }

    public List<T> findByFilters(Map<String, Object> filters) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT e FROM ");
        sb.append(this.className);
        sb.append(" e WHERE 1=1 ");
        if (filters != null) {
            this.setQueryFilter(sb, filters);
        }
        //生成SQL
        Query query = getEntityManager().createQuery(sb.toString());
        //参数赋值
        if (filters != null) {
            this.setQueryParam(query, filters);
        }
        return query.getResultList();
    }

    public List<T> findByFilters(Map<String, Object> filters, Map<String, String> orderBy) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT e FROM ");
        sb.append(this.className);
        sb.append(" e WHERE 1=1 ");
        if (filters != null) {
            this.setQueryFilter(sb, filters);
        }
        if ((orderBy != null) && (orderBy.size() > 0)) {
            sb.append(" ORDER BY ");
            for (Map.Entry<String, String> o : orderBy.entrySet()) {
                sb.append(" e.").append(o.getKey()).append(" ").append(o.getValue()).append(",");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
        }
        //生成SQL
        Query query = getEntityManager().createQuery(sb.toString());
        //参数赋值
        if (filters != null) {
            this.setQueryParam(query, filters);
        }
        return query.getResultList();
    }

    public List<T> findByFilters(Map<String, Object> filters, int first, int pageSize, Map<String, String> orderBy) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT e FROM ");
        sb.append(this.className);
        sb.append(" e WHERE 1=1 ");
        if (filters != null) {
            this.setQueryFilter(sb, filters);
        }
        if ((orderBy != null) && (orderBy.size() > 0)) {
            sb.append(" ORDER BY ");
            for (Map.Entry<String, String> o : orderBy.entrySet()) {
                sb.append(" e.").append(o.getKey()).append(" ").append(o.getValue()).append(",");
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
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

    public List<T> findByPId(Object value) {
        Query query = getEntityManager().createNamedQuery(getClassName() + ".findByPId");
        query.setParameter("pid", value);
        return query.getResultList();
    }

    public List<T> findByStatus(String status) {
        Query query = getEntityManager().createNamedQuery(getClassName() + ".findByStatus");
        query.setParameter("status", status);
        return query.getResultList();
    }

    public List<T> findByStatus(String status, int first, int pageSize) {
        Query query = getEntityManager().createNamedQuery(getClassName() + ".findByStatus").setFirstResult(first).setMaxResults(pageSize);
        query.setParameter("status", status);
        return query.getResultList();
    }

    public T getNextById(int value) {
        Query query = getEntityManager().createNamedQuery(className + ".findNextById").setFirstResult(0).setMaxResults(1);
        query.setParameter("id", value);
        List<T> list = query.getResultList();
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public T getPrevById(int value) {
        Query query = getEntityManager().createNamedQuery(className + ".findPrevById").setFirstResult(0).setMaxResults(1);
        query.setParameter("id", value);
        List<T> list = query.getResultList();
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return this.className;
    }

    public void setDetail(Object value) {

    }

    protected void setQueryFilter(StringBuilder sb, Map<String, Object> filters) {
        for (Map.Entry<String, Object> e : filters.entrySet()) {
            if (e.getKey().contains(" IN ")) {
                if (e.getKey().contains(".")) {
                    sb.append(" AND e.").append(e.getKey().substring(0, e.getKey().indexOf(" IN") + 3)).append(" :").append(e.getKey().substring(0, e.getKey().indexOf(" IN")).replace(".", ""));
                } else {
                    sb.append(" AND e.").append(e.getKey().substring(0, e.getKey().indexOf(" IN") + 3)).append(" :").append(e.getKey().substring(0, e.getKey().indexOf(" IN")));
                }
            } else if (e.getKey().contains(".") && e.getValue().getClass() == Date.class && e.getKey().endsWith("Begin")) {
                sb.append(" AND e.").append(e.getKey().substring(0, e.getKey().indexOf("Begin"))).append(" >= :").append(e.getKey().substring(e.getKey().indexOf(".") + 1));
            } else if (e.getKey().contains(".") && e.getValue().getClass() == Date.class && e.getKey().endsWith("End")) {
                sb.append(" AND e.").append(e.getKey().substring(0, e.getKey().indexOf("End"))).append(" <= :").append(e.getKey().substring(e.getKey().indexOf(".") + 1));
            } else if (e.getKey().contains(".") && (e.getValue().getClass() == String.class)) {
                sb.append(" AND e.").append(e.getKey()).append(" LIKE :").append(e.getKey().replace(".", ""));
            } else if (e.getKey().contains(".") && (e.getValue().getClass() != String.class)) {
                sb.append(" AND e.").append(e.getKey()).append(" = :").append(e.getKey().replace(".", ""));
            } else if (!e.getKey().contains(".") && e.getValue().getClass() == Date.class && e.getKey().endsWith("Begin")) {
                sb.append(" AND e.").append(e.getKey().substring(0, e.getKey().indexOf("Begin"))).append(" >= :").append(e.getKey());
            } else if (!e.getKey().contains(".") && e.getValue().getClass() == Date.class && e.getKey().endsWith("End")) {
                sb.append(" AND e.").append(e.getKey().substring(0, e.getKey().indexOf("End"))).append(" <= :").append(e.getKey());
            } else if (!e.getKey().contains(".") && (e.getValue().getClass() == String.class)) {
                sb.append(" AND e.").append(e.getKey()).append(" LIKE :").append(e.getKey());
            } else {
                sb.append(" AND e.").append(e.getKey()).append(" = :").append(e.getKey());
            }
        }
    }

    protected void setQueryParam(Query query, Map<String, Object> filters) {
        for (Map.Entry<String, Object> e : filters.entrySet()) {
            if (e.getKey().contains(" IN")) {
                if (e.getKey().contains(".")) {
                    query.setParameter(e.getKey().substring(0, e.getKey().indexOf(" IN")).replace(".", ""), e.getValue());
                } else {
                    query.setParameter(e.getKey().substring(0, e.getKey().indexOf(" IN")), e.getValue());
                }
            } else if ((e.getKey().contains(".")) && (e.getValue().getClass() == String.class)) {
                query.setParameter(e.getKey().replace(".", ""), "%" + e.getValue() + "%");
            } else if ((!e.getKey().contains(".")) && (e.getValue().getClass() == String.class)) {
                query.setParameter(e.getKey(), "%" + e.getValue() + "%");
            } else if ((e.getKey().contains(".")) && (e.getValue().getClass() != String.class)) {
                if (e.getValue().getClass() == Date.class && (e.getKey().endsWith("Begin") || e.getKey().endsWith("End"))) {
                    query.setParameter(e.getKey().substring(e.getKey().indexOf(".") + 1), e.getValue());
                } else {
                    query.setParameter(e.getKey().replace(".", ""), e.getValue());
                }
            } else {
                query.setParameter(e.getKey(), e.getValue());
            }
        }
    }

}
