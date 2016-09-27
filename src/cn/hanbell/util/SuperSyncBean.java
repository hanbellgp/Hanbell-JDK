/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hanbell.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;

/**
 *
 * @author C0160
 */
public abstract class SuperSyncBean implements Serializable {

    public SuperSyncBean() {

    }

    public abstract EntityManager getEntityManager();

    //新增资料,一个表头多个明细
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void persist(Object entity, HashMap<SuperEJB, List<?>> detailAdded) {
        try {
            getEntityManager().persist(entity);
            if (detailAdded != null && !detailAdded.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailAdded.entrySet()) {
                    for (Object o : entry.getValue()) {
                        getEntityManager().persist(o);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //更新资料,一个表头多个明细
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void update(Object entity, HashMap<SuperEJB, List<?>> detailEdited) {
        try {
            getEntityManager().merge(entity);
            if (detailEdited != null && !detailEdited.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailEdited.entrySet()) {
                    for (Object o : entry.getValue()) {
                        getEntityManager().merge(o);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //更新资料,一个表头多个明细
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void update(Object entity, HashMap<SuperEJB, List<?>> detailAdded, HashMap<SuperEJB, List<?>> detailEdited, HashMap<SuperEJB, List<?>> detailDeleted) {
        try {
            getEntityManager().persist(entity);
            if (detailEdited != null && !detailEdited.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailEdited.entrySet()) {
                    for (Object o : entry.getValue()) {
                        getEntityManager().merge(o);
                    }
                }
            }
            if (detailDeleted != null && !detailDeleted.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailDeleted.entrySet()) {
                    for (Object o : entry.getValue()) {
                        if (getEntityManager().contains(o)) {
                            getEntityManager().remove(o);
                        } else {
                            getEntityManager().remove(getEntityManager().merge(o));
                        }
                    }
                }
            }
            if (detailAdded != null && !detailAdded.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailAdded.entrySet()) {
                    for (Object o : entry.getValue()) {
                        getEntityManager().persist(o);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //删除资料,一个表头多个明细
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(Object entity, HashMap<SuperEJB, List<?>> detailDeleted) {
        try {
            if (getEntityManager().contains(entity)) {
                getEntityManager().remove(entity);
            } else {
                getEntityManager().remove(getEntityManager().merge(entity));
            }
            if (detailDeleted != null && !detailDeleted.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailDeleted.entrySet()) {
                    for (Object o : entry.getValue()) {
                        if (getEntityManager().contains(o)) {
                            getEntityManager().remove(o);
                        } else {
                            getEntityManager().remove(getEntityManager().merge(o));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //新事务新增资料,一个表头多个明细
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void syncPersist(Object entity, HashMap<SuperEJB, List<?>> detailAdded) {
        try {
            getEntityManager().persist(entity);
            if (detailAdded != null && !detailAdded.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailAdded.entrySet()) {
                    for (Object o : entry.getValue()) {
                        getEntityManager().persist(o);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //新事务更新资料,一个表头多个明细
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void syncUpdate(Object entity, HashMap<SuperEJB, List<?>> detailEdited) {
        try {
            getEntityManager().merge(entity);
            if (detailEdited != null && !detailEdited.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailEdited.entrySet()) {
                    for (Object o : entry.getValue()) {
                        getEntityManager().merge(o);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //新事务更新资料,一个表头多个明细
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void syncUpdate(Object entity, HashMap<SuperEJB, List<?>> detailAdded, HashMap<SuperEJB, List<?>> detailEdited, HashMap<SuperEJB, List<?>> detailDeleted) {
        try {
            getEntityManager().persist(entity);
            if (detailEdited != null && !detailEdited.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailEdited.entrySet()) {
                    for (Object o : entry.getValue()) {
                        getEntityManager().merge(o);
                    }
                }
            }
            if (detailDeleted != null && !detailDeleted.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailDeleted.entrySet()) {
                    for (Object o : entry.getValue()) {
                        getEntityManager().remove(o);
                    }
                }
            }
            if (detailAdded != null && !detailAdded.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailAdded.entrySet()) {
                    for (Object o : entry.getValue()) {
                        getEntityManager().persist(o);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //新事务删除资料,一个表头多个明细
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void syncDelete(Object entity, HashMap<SuperEJB, List<?>> detailDeleted) {
        try {
            if (getEntityManager().contains(entity)) {
                getEntityManager().remove(entity);
            } else {
                getEntityManager().remove(getEntityManager().merge(entity));
            }
            if (detailDeleted != null && !detailDeleted.isEmpty()) {
                for (Entry<SuperEJB, List<?>> entry : detailDeleted.entrySet()) {
                    for (Object o : entry.getValue()) {
                        if (getEntityManager().contains(o)) {
                            getEntityManager().remove(o);
                        } else {
                            getEntityManager().remove(getEntityManager().merge(o));
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
