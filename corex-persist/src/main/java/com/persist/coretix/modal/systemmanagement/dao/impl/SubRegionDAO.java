/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.persist.coretix.modal.systemmanagement.dao.impl;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Subregions;
import com.persist.coretix.modal.systemmanagement.dao.ISubRegionDAO;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

/**
 *
 * @author Pragadeesh
 */
@Named
public class SubRegionDAO implements ISubRegionDAO {

    private static final Logger logger = LoggerFactory.getLogger(SubRegionDAO.class);
    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public GeneralConstants addSubRegion(Subregions subRegion) {
        logger.debug("inside SubRegionDAO addSubRegion");


        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from SubRegions o where o.name = :name")
                    .setParameter("name", subRegion.getName())
                    .uniqueResult();
            logger.debug("count : " + count);

            if (count != null && count > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            session.save(subRegion);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public GeneralConstants deleteSubRegion(Subregions subRegion)  {
        logger.debug("inside dao deleteOrganization !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Organizations o where o.id = :id")
                    .setParameter("id", subRegion.getId())
                    .uniqueResult();
            logger.debug("delete count : " + count);

            if (count == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            session.delete(subRegion);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception e) {
            logger.debug("Exception : " + e);
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }


    public GeneralConstants updateSubRegion(Subregions subRegion) {
        logger.debug("inside dao updateOrganization !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long countById = (Long) session.createQuery(
                            "select count(o) from SubRegions o where o.id = :id")
                    .setParameter("id", subRegion.getId())
                    .uniqueResult();
            logger.debug("update count by ID: " + countById);
            if (countById == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long countByName = (Long) session.createQuery(
                            "select count(o) from SubRegions o where o.name = :name and o.id != :id")
                    .setParameter("name", subRegion.getName())
                    .setParameter("id", subRegion.getId())
                    .uniqueResult();
            logger.debug("update count by name: " + countByName);
            if (countByName > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            logger.debug("crossed and before update");
            session.update(subRegion);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }


    public Subregions getSubRegion(int id) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from SubRegions where id=?1").setParameter(1, id)
                .list();

        trans.commit();
        return (Subregions) list.get(0);
    }
    
    public Subregions getSubregionBySubregionName(String subregionName) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from SubRegions where name=?1").setParameter(1, subregionName)
                .list();

        trans.commit();
        return (Subregions) list.get(0);
    }

    public List<Subregions> getSubRegionsList() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        @SuppressWarnings("unchecked")
        List<Subregions> list = (List<Subregions>) session.createQuery("from Subregions").list();

        trans.commit();
        return list;
    }
}



